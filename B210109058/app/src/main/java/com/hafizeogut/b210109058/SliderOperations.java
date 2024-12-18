package com.hafizeogut.b210109058;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.interfaces.ItemClickListener;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class SliderOperations extends AppCompatActivity {

    ImageSlider imageSlider;
    TextView cityName;
    ImageView imageMain;
    ProgressBar progressBar;


    Button btnCreate, btnDelete, btnUpdate;
    View adminPanel, sliderOperationLayout, sliderUpload;



    private String selectedImageKey;
    private String selectedImageUrl;
    Uri imageUri;

    private final StorageReference storageReference = FirebaseStorage.getInstance().getReference("images");
    private final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("cities");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_slider_operations);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imageSlider = findViewById(R.id.imageSlider);
        cityName = findViewById(R.id.cityName);
        imageMain = findViewById(R.id.sliderImage);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        btnCreate = findViewById(R.id.btn_create);
        btnDelete = findViewById(R.id.btn_delete);
        btnUpdate = findViewById(R.id.btn_update);

        adminPanel = findViewById(R.id.adminPanel);
        sliderOperationLayout = findViewById(R.id.sliderOperationLayout);
        sliderUpload = findViewById(R.id.sliderUpload);

        checkUserRole();
        loadFirebaseData();
    }


    private void checkUserRole() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users").child(userId);

            userReference.child("role").get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    String role = task.getResult().getValue(String.class);
                    if (!"admin".equalsIgnoreCase(role)) {
                        hideAdminComponents();
                    }
                }
            });
        }
    }

    private void hideAdminComponents() {
        adminPanel.setVisibility(View.GONE);
        sliderOperationLayout.setVisibility(View.GONE);
        sliderUpload.setVisibility(View.GONE);
        imageMain.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        btnCreate.setVisibility(View.GONE);
        btnDelete.setVisibility(View.GONE);
        btnUpdate.setVisibility(View.GONE);
    }

    private void loadFirebaseData() {
        final List<SlideModel> imageList = new ArrayList<>();

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                imageList.clear();

                // Şehir adını çek
                String name = snapshot.child("name").getValue(String.class);
                if (name != null && !name.isEmpty()) {
                    cityName.setText(name);
                } else {
                    cityName.setText("Name is missing");
                }

                // Resimleri yükle
                DataSnapshot imagesSnapshot = snapshot.child("images");
                for (DataSnapshot data : imagesSnapshot.getChildren()) {
                    String imageUrl = data.child("url").getValue(String.class);
                    if (imageUrl != null) {
                        imageList.add(new SlideModel(imageUrl, ScaleTypes.FIT));
                    }
                }

                imageSlider.setImageList(imageList, ScaleTypes.FIT);

                imageSlider.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onItemSelected(int position) {
                        selectedImageUrl = imageList.get(position).getImageUrl();

                        // Resme ait key'i bul
                        for (DataSnapshot data : imagesSnapshot.getChildren()) {
                            if (selectedImageUrl.equals(data.child("url").getValue(String.class))) {
                                selectedImageKey = data.getKey();
                                break;
                            }
                        }

                        Glide.with(SliderOperations.this).load(selectedImageUrl).into(imageMain);
                        Toast.makeText(SliderOperations.this, "Selected Image", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void doubleClick(int position) {
                        // Çift tıklama işlevi
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error: " + error.getMessage());
            }
        });
    }

    public void uploadImage(View view) {
        Intent photoPicker = new Intent(Intent.ACTION_GET_CONTENT);
        photoPicker.setType("image/*");
        startActivityForResult(photoPicker, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageMain.setImageURI(imageUri);
        }
    }

    public void save(View view) {
        if (imageUri != null) {
            progressBar.setVisibility(View.VISIBLE);
            String fileName = System.currentTimeMillis() + "." + getFileExtension(imageUri);
            StorageReference fileRef = storageReference.child(fileName);

            fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String key = databaseReference.child("images").push().getKey();
                        if (key != null) {
                            databaseReference.child("images").child(key).child("url").setValue(uri.toString())
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Uploaded Successfully", Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.INVISIBLE);
                                        loadFirebaseData();
                                    });
                        }
                    })
            ).addOnFailureListener(e -> {
                Toast.makeText(this, "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
            });
        } else {
            Toast.makeText(this, "Please select an image!", Toast.LENGTH_SHORT).show();
        }
    }

    public void update(View view) {
        if (selectedImageKey == null || selectedImageUrl == null) {
            Toast.makeText(this, "Please select an image to update!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        if (imageUri != null) {
            String fileName = System.currentTimeMillis() + "." + getFileExtension(imageUri);
            StorageReference newImageRef = storageReference.child(fileName);

            newImageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                    newImageRef.getDownloadUrl().addOnSuccessListener(newUri -> {
                        databaseReference.child("images").child(selectedImageKey).child("url")
                                .setValue(newUri.toString()).addOnSuccessListener(aVoid -> {
                                    Toast.makeText(SliderOperations.this, "Updated Successfully!", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.INVISIBLE);
                                    loadFirebaseData();
                                    clearSelection();
                                });

                        FirebaseStorage.getInstance().getReferenceFromUrl(selectedImageUrl).delete();
                    })).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to upload new image!", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
            });
        } else {
            Toast.makeText(this, "No new image selected!", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    public void delete(View view) {
        if (selectedImageKey != null) {
            StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(selectedImageUrl);
            imageRef.delete().addOnSuccessListener(aVoid ->
                    databaseReference.child("images").child(selectedImageKey).removeValue().addOnSuccessListener(aVoid1 -> {
                        Toast.makeText(this, "Deleted Successfully!", Toast.LENGTH_SHORT).show();
                        loadFirebaseData();
                        clearSelection();
                    })
            ).addOnFailureListener(e -> Toast.makeText(this, "Delete Failed!", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Please select an image to delete!", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearSelection() {
        selectedImageKey = null;
        selectedImageUrl = null;
        imageMain.setImageResource(R.drawable.baseline_add_photo_alternate_24);
        imageUri = null;
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    public void home2(View view) {
        Intent intent = new Intent(SliderOperations.this, MainActivity2.class);
        startActivity(intent);
    }

    public void logOut(View view) {
        FirebaseAuth.getInstance().signOut(); // Oturumu kapat
        Toast.makeText(this, "Log out.", Toast.LENGTH_SHORT).show();

        // LoginActivity'ye yönlendir
        Intent intent = new Intent(SliderOperations.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
