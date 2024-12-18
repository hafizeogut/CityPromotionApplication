package com.hafizeogut.b210109058;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class PopulationAdapter extends ArrayAdapter<PopulationData> {

    private Context mContext;
    private int mResource;
    public PopulationAdapter(@NonNull Context context, int resource, @NonNull ArrayList<PopulationData> objects) {
        super(context, resource, objects);
        this.mResource=resource;
        this.mContext=context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
        view= LayoutInflater.from(mContext).inflate(mResource,parent,false);

        TextView yil= view.findViewById(R.id.idYear);
        TextView nufus_degeri= view.findViewById(R.id.idPopulation);

        yil.setText("Year: " +getItem(position).getYear());
        nufus_degeri.setText("Population: " +getItem(position).getPopulation());

        return view;
    }
}
