package com.example.mokoli;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyXAxisValueFormatter extends ValueFormatter {

    private final List<String> timestamps;
    private final List<Float> energyValues;

    public MyXAxisValueFormatter(List<String> timestamps, List<Float> energyValues) {
        this.timestamps = timestamps;
        this.energyValues = energyValues;
    }

    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        int index = (int) value;
        if (index >= 0 && index < energyValues.size()) {
            String originalTimestamp = timestamps.get(index);

            // Periksa apakah ada nilai energi dan timestamp yang valid
            if (energyValues.get(index) != null && !originalTimestamp.isEmpty()) {
                SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat desiredFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

                try {
                    Date date = originalFormat.parse(originalTimestamp);
                    return desiredFormat.format(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return originalTimestamp;
                }
            } else {
                // Kembalikan string kosong untuk titik tanpa nilai energi atau timestamp
                return "";
            }
        } else {
            // Kembalikan string kosong untuk index di luar batas
            return "";
        }
    }
}
