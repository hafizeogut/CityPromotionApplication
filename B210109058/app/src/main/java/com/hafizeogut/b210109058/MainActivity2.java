package com.hafizeogut.b210109058;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity2 extends AppCompatActivity {
    ImageSlider imageSlider;
    TextView cityName, cityDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imageSlider = findViewById(R.id.imageSlider);
        cityName = findViewById(R.id.cityName);
        cityDescription = findViewById(R.id.cityDescription);

        final List<SlideModel> imageList = new ArrayList<>();


        FirebaseDatabase.getInstance().getReference().child("cities").addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Get the image URL list
                for (DataSnapshot data : snapshot.child("images").getChildren()) {
                    String imageUrl = Objects.requireNonNull(data.child("url").getValue()).toString();
                    Log.d("Firebase", "Image URL: " + imageUrl);
                    imageList.add(new SlideModel(imageUrl, ScaleTypes.FIT));
                }

                // Set the ImageSlider
                imageSlider.setImageList(imageList, ScaleTypes.FIT);

                // Get name and description
                String name = snapshot.child("name").getValue(String.class);  // Get name from the database
                String description = snapshot.child("description").getValue(String.class);  // Get description from the database

                // Set the City Name and Description
                if (name != null && !name.isEmpty()) {
                    cityName.setText(name);  // Set CITY NAME
                } else {
                    cityName.setText("Name is missing");
                }

                if (description != null && !description.isEmpty()) {
                    cityDescription.setText(description);  // Set CITY DESCRIPTION
                } else {
                    cityDescription.setText("Description is missing");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error: " + error.getMessage());
            }
        });
    }

    public void home2(View view) {
        Intent intent = new Intent(MainActivity2.this, MainActivity.class);
        startActivity(intent);
    }

    public void populationDistribution(View view) {
        Intent intent = new Intent(MainActivity2.this,PopulationDistributionActivity.class);
        startActivity(intent);
    }

    public void cityDistricts(View view) {
        Intent intent = new Intent(MainActivity2.this, CityDistrictsActivity.class);
        startActivity(intent);
    }

    public void historicalPlaces(View view) {
        Intent intent = new Intent(MainActivity2.this,HistoricalPlacesActivity.class);
        startActivity(intent);
    }

    public void sliderOperations(View view) {
        Intent intent = new Intent(MainActivity2.this,SliderOperations.class);
        startActivity(intent);

    }
}