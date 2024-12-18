package com.hafizeogut.b210109058;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class HistoricalPlacesAdapter extends RecyclerView.Adapter<HistoricalPlacesAdapter.MyViewHolder> {

    private final Context context;
    private final List<HistoricalPlaces> historicalList;
    private final OnItemClickListener listener;

    // Constructor
    public HistoricalPlacesAdapter(Context context, List<HistoricalPlaces> historicalList, OnItemClickListener listener) {
        this.context = context;
        this.historicalList = historicalList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_recycle, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        HistoricalPlaces place = historicalList.get(position);


        Glide.with(context).load(place.getImageURL()).into(holder.recImage);
        holder.recTitle.setText(place.getHistoricalPlace());

        holder.recTitle.setOnClickListener(view -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("placeID", place.getId());
            intent.putExtra("imageURL", place.getImageURL());
            intent.putExtra("historicalPlace", place.getHistoricalPlace());
            context.startActivity(intent);
        });


        holder.recCard.setOnClickListener(view -> listener.onItemClick(place));



    }

    @Override
    public int getItemCount() {
        return historicalList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView recImage;
        TextView recTitle;
        CardView recCard;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            recImage = itemView.findViewById(R.id.recImage);
            recTitle = itemView.findViewById(R.id.recyclerHistorical);
            recCard = itemView.findViewById(R.id.recCard);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(HistoricalPlaces place);
    }



}
