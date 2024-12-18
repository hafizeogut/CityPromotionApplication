package com.hafizeogut.b210109058;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class DetailActivity extends AppCompatActivity {
    ImageView detailImage;
    TextView detailTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        detailImage = findViewById(R.id.detailImage);
        detailTitle = findViewById(R.id.detailTittle);


        String imageURL = getIntent().getStringExtra("imageURL");
        String historicalPlace = getIntent().getStringExtra("historicalPlace");


        detailTitle.setText(historicalPlace);
        Glide.with(this).load(imageURL).into(detailImage);



    }


    public void feedbackPage(View view) {
        Intent intent = new Intent(DetailActivity.this,HistoricalPlacesActivity.class);
        startActivity(intent);
    }
}
