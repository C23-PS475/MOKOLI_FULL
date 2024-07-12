package com.example.mokoli;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SpinnerAdapter extends ArrayAdapter<String> {

    private Context context;
    private String[] items;

    public SpinnerAdapter(@NonNull Context context, int resource, String[] items) {
        super(context, resource, items);
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.spinner_item, parent, false);
        TextView textView = view.findViewById(R.id.spinnerText);

        textView.setText(items[position]);

        return view;
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.spinner_dropdown_item, parent, false);
        TextView textView = view.findViewById(R.id.spinnerDropdownText);

        textView.setText(items[position]);

        return view;
    }
}
