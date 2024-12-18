package com.hafizeogut.b210109058;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CityDistrictsActivity extends AppCompatActivity {

    private TableLayout districtsTable;
    private EditText cityDescription;
    private TextView selectedTextView;

    private DatabaseReference databaseReference, userReference;
    private String selectedKey;

    private RelativeLayout inputPanel;
    private View adminTablePanel, cityAdd, cityDelete, cityUpdate;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_districts);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        // Kullanıcı oturum kontrolü
        if (currentUser == null) {
            Log.d("FirebaseAuth", "Session closed, redirecting to LoginActivity...");
            startActivity(new Intent(CityDistrictsActivity.this, LoginActivity.class));
            finish();
            return;
        } else {
            Log.d("FirebaseAuth", "Session active, user UID: " + currentUser.getUid());
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("cityDistricts");
        userReference = FirebaseDatabase.getInstance().getReference("users");

        districtsTable = findViewById(R.id.districtsTable);
        cityDescription = findViewById(R.id.cityDescription);
        inputPanel = findViewById(R.id.inputPanel);
        adminTablePanel = findViewById(R.id.adminTablePanel);
        cityAdd = findViewById(R.id.cityAdd);
        cityDelete = findViewById(R.id.cityDelete);
        cityUpdate = findViewById(R.id.cityUpdate);

        checkUserRole(currentUser.getUid());
        fetchDistricts();
    }

    private void checkUserRole(String uid) {
        Log.d("DebugRole", "Checking role for UID: " + uid);

        userReference.child(uid).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String role = snapshot.getValue(String.class);
                    Log.d("FirebaseDebug", "The role retrieved from Firebase " + role);

                    if ("admin".equalsIgnoreCase(role)) {
                        showAdminComponents();
                        Log.d("FirebaseDebug", "Admin components are being displayed..");
                    } else {
                        hideAdminComponents();
                        Log.d("FirebaseDebug", "The user is not an admin, components are being hidden.");
                    }
                } else {
                    Log.d("FirebaseDebug", "The role field was not found.");
                    hideAdminComponents();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("FirebaseDebug", "Veri çekme hatası: " + error.getMessage());
                hideAdminComponents();
            }
        });
    }

    private void showAdminComponents() {
        inputPanel.setVisibility(View.VISIBLE);
        adminTablePanel.setVisibility(View.VISIBLE);
        cityAdd.setVisibility(View.VISIBLE);
        cityDelete.setVisibility(View.VISIBLE);
        cityUpdate.setVisibility(View.VISIBLE);
    }

    private void hideAdminComponents() {
        inputPanel.setVisibility(View.GONE);
        adminTablePanel.setVisibility(View.GONE);
        cityAdd.setVisibility(View.GONE);
        cityDelete.setVisibility(View.GONE);
        cityUpdate.setVisibility(View.GONE);
    }

    private void fetchDistricts() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                districtsTable.removeViews(1, Math.max(0, districtsTable.getChildCount() - 1));

                for (DataSnapshot data : snapshot.getChildren()) {
                    String district = data.getValue(String.class);
                    addTableRow(data.getKey(), district);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CityDistrictsActivity.this, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addTableRow(String key, String district) {
        TableRow tableRow = new TableRow(this);
        TextView textView = new TextView(this);
        textView.setText(district);
        textView.setTextSize(20);
        textView.setPadding(10, 10, 10, 10);
        tableRow.addView(textView);

        tableRow.setOnClickListener(v -> {
            if (selectedTextView != null) {
                selectedTextView.setBackgroundColor(0x00000000);
            }
            selectedTextView = textView;
            selectedKey = key;
            cityDescription.setText(district);
            selectedTextView.setBackgroundColor(0xFFB0E0E6);
        });

        districtsTable.addView(tableRow);
    }

    public void citySave(View view) {
        String newDistrict = cityDescription.getText().toString().trim();
        if (!newDistrict.isEmpty()) {
            String key = databaseReference.push().getKey();
            if (key != null) {
                databaseReference.child(key).setValue(newDistrict).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "New district added.", Toast.LENGTH_SHORT).show();
                        cityDescription.setText("");
                    }
                });
            }
        } else {
            Toast.makeText(this, "Please enter a county name.", Toast.LENGTH_SHORT).show();
        }
    }

    public void cityDelete(View view) {
        if (selectedKey != null) {
            databaseReference.child(selectedKey).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "The district has been deleted.", Toast.LENGTH_SHORT).show();
                    selectedKey = null;
                    selectedTextView = null;
                    cityDescription.setText("");
                }
            });
        } else {
            Toast.makeText(this, "Select a county to delete.", Toast.LENGTH_SHORT).show();
        }
    }

    public void cityUpdate(View view) {
        if (selectedKey != null) {
            String updatedDistrict = cityDescription.getText().toString().trim();
            if (!updatedDistrict.isEmpty()) {
                databaseReference.child(selectedKey).setValue(updatedDistrict).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "District updated.", Toast.LENGTH_SHORT).show();
                        selectedTextView.setText(updatedDistrict);
                        cityDescription.setText("");
                    }
                });
            } else {
                Toast.makeText(this, "Please enter a new district name.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Select a county to update.", Toast.LENGTH_SHORT).show();
        }
    }

    public void home(View view) {
        Intent intent = new Intent(CityDistrictsActivity.this, MainActivity2.class);
        startActivity(intent);
    }

    public void logOut(View view) {
        FirebaseAuth.getInstance().signOut(); // Oturumu kapat
        Toast.makeText(this, "Log out.", Toast.LENGTH_SHORT).show();

        // LoginActivity'ye yönlendir
        Intent intent = new Intent(CityDistrictsActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
