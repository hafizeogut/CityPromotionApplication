package com.hafizeogut.b210109058;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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

public class HistoricalPlacesActivity extends AppCompatActivity {

    ImageView uploadImage;
    EditText uploadHistoricalName;
    Uri imageUri;
    ProgressBar progressBar;
    RecyclerView recyclerView;

    View adminPanel, historicalAdminLayout, historicalUpload, btnCreate, btnDelete, btnUpdate;

    List<HistoricalPlaces> historicalPlacesList;
    HistoricalPlacesAdapter historicalPlacesAdapter;

    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    private String selectedKey = null;
    final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("historicalPlaces");
    final StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    final DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users");

    ActivityResultLauncher<Intent> activityResultLauncher;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_historical_places);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        uploadImage = findViewById(R.id.imageHistorical);
        uploadHistoricalName = findViewById(R.id.historicalInput);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar.setVisibility(View.INVISIBLE);

        adminPanel = findViewById(R.id.adminPanel);
        historicalAdminLayout = findViewById(R.id.historicalAdminLayout);
        historicalUpload = findViewById(R.id.historicalUpload);
        btnCreate = findViewById(R.id.btn_create);
        btnDelete = findViewById(R.id.btn_delete);
        btnUpdate = findViewById(R.id.btn_update);


        if (currentUser == null) {
            Log.d("FirebaseAuth", "Session closed, redirecting to LoginActivity...");
            startActivity(new Intent(HistoricalPlacesActivity.this, LoginActivity.class));
            finish();
        } else {
            Log.d("FirebaseAuth", "Session active, user UID: " + currentUser.getUid());
            checkUserRole(currentUser.getUid());
        }


        historicalPlacesList = new ArrayList<>();
        historicalPlacesAdapter = new HistoricalPlacesAdapter(this, historicalPlacesList, place -> {
            selectedKey = place.getId();
            uploadHistoricalName.setText(place.getHistoricalPlace());
            imageUri = Uri.parse(place.getImageURL());
            Glide.with(this).load(imageUri).into(uploadImage);
            Toast.makeText(this, "Selected: " + place.getHistoricalPlace(), Toast.LENGTH_SHORT).show();
        });

        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerView.setAdapter(historicalPlacesAdapter);

        loadData();

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            imageUri = data.getData();
                            uploadImage.setImageURI(imageUri);
                        }
                    } else {
                        Toast.makeText(this, "No Image Selected", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void checkUserRole(String uid) {
        userReference.child(uid).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String role = snapshot.getValue(String.class);
                    Log.d("FirebaseDebug", "The role retrieved from Firebase: " + role);

                    if ("admin".equalsIgnoreCase(role)) {
                        Log.d("FirebaseDebug", "Admin components are being displayed.");
                    } else {
                        Log.d("FirebaseDebug", "User is not an admin, hiding components.");
                        hideAdminComponents();
                    }
                } else {
                    Log.d("FirebaseDebug", "Role field was not found.");
                    hideAdminComponents();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseDebug", "Database error: " + error.getMessage());
                hideAdminComponents();
            }
        });
    }

    private void hideAdminComponents() {
        adminPanel.setVisibility(View.GONE);
        historicalAdminLayout.setVisibility(View.GONE);
        historicalUpload.setVisibility(View.GONE);
        btnCreate.setVisibility(View.GONE);
        btnDelete.setVisibility(View.GONE);
        btnUpdate.setVisibility(View.GONE);
        uploadImage.setVisibility(View.GONE);
    }

    private void loadData() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                historicalPlacesList.clear();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    HistoricalPlaces place = itemSnapshot.getValue(HistoricalPlaces.class);
                    if (place != null) {
                        place.setId(itemSnapshot.getKey());
                        historicalPlacesList.add(place);
                    }
                }
                historicalPlacesAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialog.dismiss();
                Toast.makeText(HistoricalPlacesActivity.this, "Failed to load data!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void save(View view) {
        String historicalName = uploadHistoricalName.getText().toString().trim();
        if (historicalName.isEmpty()) {
            Toast.makeText(this, "Please enter a name for Historical Places!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri != null) {
            uploadToFirebase(imageUri);
        } else {
            Toast.makeText(this, "Please select an image!", Toast.LENGTH_SHORT).show();
        }
        if (imageUri != null) {
            uploadToFirebase(imageUri);
        } else {
            Toast.makeText(this, "Please select an image!", Toast.LENGTH_SHORT).show();
        }
    }

    public void uploadToFirebase(Uri uri) {
        String uploadHistName = uploadHistoricalName.getText().toString();
        final StorageReference imageReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(uri));

        imageReference.putFile(uri).addOnSuccessListener(taskSnapshot -> imageReference.getDownloadUrl().addOnSuccessListener(uri1 -> {
            String key = databaseReference.push().getKey();
            if (key != null) {
                HistoricalPlaces place = new HistoricalPlaces(key, uri1.toString(), uploadHistName);
                databaseReference.child(key).setValue(place)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Uploaded Successfully!", Toast.LENGTH_SHORT).show();
                            clearInputs();
                        });
            }
        })).addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public void delete(View view) {
        if (selectedKey != null) {
            databaseReference.child(selectedKey).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Data deleted successfully!", Toast.LENGTH_SHORT).show();
                        clearInputs();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error deleting data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Please select a data to delete!", Toast.LENGTH_SHORT).show();
        }
    }

    public void update(View view) {
        Log.d("UPDATE", "Update button clicked");
        if (selectedKey == null) {
            Toast.makeText(this, "Please select a data to update!", Toast.LENGTH_SHORT).show();
            return;
        }

        String updatedName = uploadHistoricalName.getText().toString().trim();
        boolean isImageSelected = imageUri != null;

        if (updatedName.isEmpty() && !isImageSelected) {
            Toast.makeText(this, "Please enter a name or select an image to update!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        if (isImageSelected) {
            // Yeni resim seçilmişse yükle ve güncelle
            final StorageReference imageReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
            imageReference.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                    imageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        Log.d("UPDATE", "New image URL: " + uri.toString());
                        updateFirebaseRecord(uri.toString(), updatedName); // Yeni URL ile güncelle
                    })
            ).addOnFailureListener(e -> {
                Log.e("UPDATE", "Image upload failed: " + e.getMessage());
                Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
            });
        } else {
            // Yeni resim yüklenmemişse sadece adı güncelle
            updateFirebaseRecord(null, updatedName);
        }
    }


    private void updateFirebaseRecord(String imageUrl, String updatedName) {
        Log.d("UPDATE", "Updating Firebase Record");
        DatabaseReference recordRef = databaseReference.child(selectedKey);

        // Yalnızca URL varsa güncelle
        if (imageUrl != null) {
            recordRef.child("imageURL").setValue(imageUrl)
                    .addOnFailureListener(e -> Log.e("UPDATE", "Failed to update imageURL: " + e.getMessage()));
        }

        // Adı güncelle
        if (updatedName != null && !updatedName.isEmpty()) {
            recordRef.child("historicalPlace").setValue(updatedName)
                    .addOnFailureListener(e -> Log.e("UPDATE", "Failed to update historicalPlace: " + e.getMessage()));
        }

        Toast.makeText(this, "Data updated successfully!", Toast.LENGTH_SHORT).show();
        progressBar.setVisibility(View.INVISIBLE);
        loadData(); // RecyclerView'i güncelle
        clearInputs();
    }



    private void clearInputs() {
        uploadHistoricalName.setText("");
        uploadImage.setImageResource(R.drawable.baseline_add_photo_alternate_24);
        imageUri = null;
        selectedKey = null;
    }

    public void uploadImage(View view) {
        Intent photoPicker = new Intent(Intent.ACTION_GET_CONTENT);
        photoPicker.setType("image/*");
        activityResultLauncher.launch(photoPicker);
    }

    public String getFileExtension(Uri fileUri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(fileUri));
    }

    public void home(View view) {
        startActivity(new Intent(this, MainActivity2.class));
    }
    public void showDetails(View view) {
        int position = recyclerView.getChildAdapterPosition((View) view.getParent());
        HistoricalPlaces selectedPlace = historicalPlacesList.get(position);

        // Detay sayfasına veri gönder
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("placeID", selectedPlace.getId());
        intent.putExtra("imageURL", selectedPlace.getImageURL());
        intent.putExtra("historicalPlace", selectedPlace.getHistoricalPlace());
        startActivity(intent);
    }

    public void logOut(View view) {
        FirebaseAuth.getInstance().signOut(); // Oturumu kapat
        Toast.makeText(this, "Log out.", Toast.LENGTH_SHORT).show();

        // LoginActivity'ye yönlendir
        Intent intent = new Intent(HistoricalPlacesActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
