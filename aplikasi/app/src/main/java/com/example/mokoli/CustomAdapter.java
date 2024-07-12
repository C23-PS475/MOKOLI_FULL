package com.example.mokoli;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<CustomItem> {
    private Context context;
    private ArrayList<CustomItem> customList;

    public CustomAdapter(@NonNull Context context, ArrayList<CustomItem> customList) {
        super(context, 0, customList);
        this.context = context;
        this.customList = customList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.custom_spinner_layout, parent, false);
        }
        CustomItem item = getItem(position);
        TextView spinnerTV = convertView.findViewById(R.id.tvSpinnerLayout);
        if (item != null) {
            spinnerTV.setText(item.getSpinnerItemName());
            if (position == 0) {
                // Jika ini adalah item pertama, atur warna teks menjadi abu-abu dan tidak dapat dipilih
                spinnerTV.setTextColor(ContextCompat.getColor(context, R.color.grey));
                spinnerTV.setEnabled(false);
            } else {
                // Jika ini bukan item pertama, atur warna teks menjadi putih
                spinnerTV.setTextColor(ContextCompat.getColor(context, android.R.color.black));
                spinnerTV.setEnabled(true);
            }
        }
        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.custom_dropdown_layout, parent, false);
        }
        CustomItem item = getItem(position);
        TextView dropDownTV = convertView.findViewById(R.id.tvDropDownLayout);
        if (item != null) {
            dropDownTV.setText(item.getSpinnerItemName());
        }
        return convertView;
    }
}


