package com.hafizeogut.b210109058;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class PopulationActivity extends AppCompatActivity {
    ListView listView;
    EditText yearText, populationText;
    DatabaseReference databaseReference;

    ArrayList<PopulationData> yearPopulationList;
    PopulationAdapter adapter;

    String selectedKey = null; // Seçili öğenin anahtarı

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_population_distribution);

        // XML bileşenlerini bağlama
        listView = findViewById(R.id.listView);
        yearText = findViewById(R.id.yearText);
        populationText = findViewById(R.id.population);

        databaseReference = FirebaseDatabase.getInstance().getReference("populationDistribution");

        // Liste ve Adapter başlatma
        yearPopulationList = new ArrayList<>();
        adapter = new PopulationAdapter(this, R.layout.population_items, yearPopulationList);
        listView.setAdapter(adapter);

        // Rol kontrolü
        checkUserRole();

        // Firebase'den veriyi yükleme
        loadData();

        // ListView öğe tıklama işlemi
        listView.setOnItemClickListener((parent, view, position, id) -> {
            PopulationData selectedItem = yearPopulationList.get(position);
            yearText.setText(String.valueOf(selectedItem.getYear()));
            populationText.setText(String.valueOf(selectedItem.getPopulation()));
            selectedKey = selectedItem.getId(); // Anahtarı kaydet
        });
    }

    private void checkUserRole() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users").child(userId);

            userReference.child("role").get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    String role = task.getResult().getValue(String.class);
                    View adminTablePanel = findViewById(R.id.adminTablePanel);
                    if ("admin".equals(role)) {
                        adminTablePanel.setVisibility(View.VISIBLE);
                    } else {
                        adminTablePanel.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    public void save(View view) {
        String yearInput = yearText.getText().toString();
        String populationInput = populationText.getText().toString();

        if (validateInputs(yearInput, populationInput)) {
            int year = Integer.parseInt(yearInput);
            int population = Integer.parseInt(populationInput);
            String id = databaseReference.push().getKey();

            if (id != null) {
                PopulationData populationData = new PopulationData(id, year, population);
                databaseReference.child(id).setValue(populationData)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Data saved successfully!", Toast.LENGTH_SHORT).show();
                            clearInputs();
                            loadData();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Error saving data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }
    }

    public void delete(View view) {
        if (selectedKey != null) {
            databaseReference.child(selectedKey).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Data deleted successfully!", Toast.LENGTH_SHORT).show();
                        clearInputs();
                        loadData();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error deleting data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Please select a data to delete!", Toast.LENGTH_SHORT).show();
        }
    }

    public void update(View view) {
        String populationInput = populationText.getText().toString();

        if (selectedKey != null && populationInput.matches("\\d+")) {
            int population = Integer.parseInt(populationInput);
            databaseReference.child(selectedKey).child("population").setValue(population)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Data updated successfully!", Toast.LENGTH_SHORT).show();
                        clearInputs();
                        loadData();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error updating data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Please select a data and enter valid population!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadData() {
        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                yearPopulationList.clear();
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    PopulationData data = snapshot.getValue(PopulationData.class);
                    if (data != null) {
                        yearPopulationList.add(data);
                    }
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInputs(String yearInput, String populationInput) {
        if (TextUtils.isEmpty(yearInput) || yearInput.length() < 4) {
            Toast.makeText(this, "Please enter a valid year (4 digits)!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(populationInput) || !populationInput.matches("\\d+")) {
            Toast.makeText(this, "Population must be a valid number!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void clearInputs() {
        yearText.setText("");
        populationText.setText("");
        selectedKey = null;
    }

    public void home(View view) {
        startActivity(new Intent(this, MainActivity2.class));
    }
}
