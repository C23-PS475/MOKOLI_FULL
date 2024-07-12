package com.example.mokoli;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class MonitoringActivity extends AppCompatActivity {

    private LineChart lineChart;
    private ProgressBar progressBar;
    TextView total;
    BarChart barChart;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring);

        lineChart = findViewById(R.id.chart);
        progressBar = findViewById(R.id.progress_bar);
        total = findViewById(R.id.sisaKWH);
        barChart = findViewById(R.id.bar);


        //SisaKWH//
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            String uid = user.getUid();

            // Mengambil nilai noKamar dari Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("users").document(uid);

            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        String noKamar = documentSnapshot.getString("noKamar");

                        // Lanjutkan dengan menggunakan nilai noKamar untuk mengatur DatabaseReference
                        setupFirebaseListenerBasedOnNoKamar(noKamar);
                    } else {
                        Log.d("MonitoringActivity", "No such document");
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("MonitoringActivity", "Error getting document", e);
                }
            });
        }


        //LineData//

        // Ambil referensi ke koleksi SensorData untuk tanggal hari ini
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String currentDate = dateFormat.format(new Date());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("SensorData").document(currentDate)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e("TAG", "Listen failed: " + e);
                            return;
                        }

                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            progressBar.setVisibility(View.VISIBLE);  // Tampilkan progress bar saat data dimuat ulang

                            ArrayList<Float> energyList = new ArrayList<>();
                            ArrayList<String> timestampList = new ArrayList<>();

                            // Loop melalui data untuk mengambil nilai energi dan timestamp
                            Map<String, Object> data = documentSnapshot.getData();
                            if (data != null) {
                                // Sort data berdasarkan timestamp jika diperlukan
                                List<Map.Entry<String, Object>> sortedEntries = new ArrayList<>(data.entrySet());
                                Collections.sort(sortedEntries, new Comparator<Map.Entry<String, Object>>() {
                                    @Override
                                    public int compare(Map.Entry<String, Object> entry1, Map.Entry<String, Object> entry2) {
                                        // Urutkan berdasarkan key (timestamp)
                                        return entry1.getKey().compareTo(entry2.getKey());
                                    }
                                });

                                // Mengisi list energi dan timestamp dari data yang telah diurutkan
                                for (Map.Entry<String, Object> entry : sortedEntries) {
                                    Map<String, Object> sensorData = (Map<String, Object>) entry.getValue();
                                    if (sensorData != null) {
                                        Double energy = (Double) sensorData.get("Energy");
                                        String timestamp = (String) sensorData.get("Timestamp");
                                        if (energy != null && timestamp != null) {
                                            energyList.add(energy.floatValue());
                                            timestampList.add(timestamp);
                                        }
                                    }
                                }

                                // Atur data ke dalam format yang dapat digunakan oleh grafik, entri chat yang mewakili 1 titik dalam grafik
                                ArrayList<Entry> entries = new ArrayList<>();
                                for (int i = 0; i < energyList.size(); i++) {
                                    entries.add(new Entry(i, energyList.get(i)));
                                }

                                // Membuat set data untuk grafik garis
                                LineDataSet dataSet = new LineDataSet(entries, "Energy Consumption");
                                dataSet.setColor(getResources().getColor(R.color.blue));  // Ganti "dark_blue" dengan warna yang diinginkan

                                // Set line thickness
                                dataSet.setLineWidth(2f);

                                dataSet.setValueFormatter(new ValueFormatter() {
                                    @Override
                                    public String getFormattedValue(float value) {
                                        return String.format("%.2f kWh", value); // Format nilai energi dengan dua desimal dan satuan " kWh"
                                    }
                                });

                                LineData lineData = new LineData(dataSet);

                                // Atur data ke grafik
                                lineChart.setData(lineData);
                                Description description = lineChart.getDescription();
                                description.setEnabled(false);

                                // Mengatur sumbu X untuk menampilkan timestamp
                                XAxis xAxis = lineChart.getXAxis();
                                xAxis.setValueFormatter(new MyXAxisValueFormatter(timestampList, energyList));
                                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                                YAxis leftAxis = lineChart.getAxisLeft();

                                leftAxis.setValueFormatter(new ValueFormatter() {
                                    @Override
                                    public String getFormattedValue(float value) {
                                        return String.format("%.2f kWh", value); // Format nilai energi dengan dua desimal dan satuan " kWh"
                                    }
                                });
                                leftAxis.setEnabled(true);

                                // Refresh grafik
                                lineChart.invalidate();

                                progressBar.setVisibility(View.GONE);  // Sembunyikan progress bar setelah grafik dimuat
                            }
                        } else {
                            Log.d("TAG", "Current data: null");
                            progressBar.setVisibility(View.GONE);  // Sembunyikan progress bar jika dokumen tidak ada
                        }
                    }
                });
        //Barchart//
        // Ambil referensi ke koleksi SensorData untuk 5 hari terakhir
        HashMap<String, Float> dailyEnergyDifferences = new HashMap<>();
        final CountDownLatch latch = new CountDownLatch(5); // untuk sinkronisasi

        for (int i = 1; i <= 5; i++) {
            String date = getPastDate(i); // Fungsi untuk mendapatkan tanggal i hari sebelum hari ini
            FirebaseFirestore.getInstance().collection("SensorData").document(date)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                List<Map<String, Object>> dataList = new ArrayList<>();
                                Map<String, Object> data = documentSnapshot.getData();
                                if (data != null) {
                                    // Kumpulkan semua entri data dalam list
                                    for (Map.Entry<String, Object> entry : data.entrySet()) {
                                        Map<String, Object> sensorData = (Map<String, Object>) entry.getValue();
                                        if (sensorData != null) {
                                            dataList.add(sensorData);
                                        }
                                    }
                                }

                                // Urutkan dataList berdasarkan Timestamp
                                dataList.sort(new Comparator<Map<String, Object>>() {
                                    @Override
                                    public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                                        String timestamp1 = (String) o1.get("Timestamp");
                                        String timestamp2 = (String) o2.get("Timestamp");
                                        return timestamp1.compareTo(timestamp2);
                                    }
                                });

                                if (!dataList.isEmpty()) {
                                    // Ambil nilai energi pertama dan terakhir dari data yang telah diurutkan
                                    float firstEnergy = ((Number) dataList.get(0).get("Energy")).floatValue();
                                    float lastEnergy = ((Number) dataList.get(dataList.size() - 1).get("Energy")).floatValue();

                                    // Hitung selisih energi
                                    float energyDifference = lastEnergy - firstEnergy;
                                    dailyEnergyDifferences.put(date, energyDifference );

                                    // Tambahkan log untuk menampilkan selisih energi
                                    Log.d("EnergyDifference", "Date: " + date + ", Energy Difference: " + energyDifference + " kWh");
                                }
                            }
                            latch.countDown();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("TAG", "Error getting documents: " + e.getMessage());
                            progressBar.setVisibility(View.GONE);
                            latch.countDown();
                        }
                    });
        }


        // Tunggu semua data harian diambil sebelum menampilkan diagram batang
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    latch.await(); // Tunggu hingga latch menjadi 0
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showBarChart(dailyEnergyDifferences); // Tampilkan diagram batang
                    }
                });
            }
        }).start();
    }

    private void setupFirebaseListenerBasedOnNoKamar(String noKamar) {
        DatabaseReference mRef;

        switch (noKamar) {
            case "01":
                mRef = FirebaseDatabase.getInstance().getReference().child("SensorData");
                break;
            case "02":
                mRef = FirebaseDatabase.getInstance().getReference().child("Sensor2");
                break;
            case "03":
                mRef = FirebaseDatabase.getInstance().getReference().child("Sensor3");
                break;
            case "04":
                mRef = FirebaseDatabase.getInstance().getReference().child("Sensor4");
                break;
            case "05":
                mRef = FirebaseDatabase.getInstance().getReference().child("Sensor5");
                break;
            case "06":
                mRef = FirebaseDatabase.getInstance().getReference().child("Sensor6");
                break;
            case "07":
                mRef = FirebaseDatabase.getInstance().getReference().child("Sensor7");
                break;
            case "08":
                mRef = FirebaseDatabase.getInstance().getReference().child("Sensor8");
                break;
            case "09":
                mRef = FirebaseDatabase.getInstance().getReference().child("Sensor9");
                break;
            case "10":
                mRef = FirebaseDatabase.getInstance().getReference().child("Sensor10");
                break;
            default:
                mRef = FirebaseDatabase.getInstance().getReference().child("SensorData"); // Default fallback
                break;
        }

        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Ambil nilai energi dan token dari dataSnapshot
                    Double energyValue = dataSnapshot.child("Energy").getValue(Double.class);
                    Double tokenValue = dataSnapshot.child("Token").getValue(Double.class);

                    if (energyValue != null && tokenValue != null) {
                        // Hitung sisa energi (misalnya)
                        Double remainingEnergy = tokenValue - energyValue;
                        // Tampilkan nilai remainingEnergy di TextView
                        total.setText(String.valueOf(remainingEnergy));
                    } else {
                        total.setText("No Data"); // Handle jika data tidak ada atau tidak lengkap
                    }
                } else {
                    total.setText("No Data"); // Handle jika snapshot tidak ada
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("MonitoringActivity", "Failed to read value.", databaseError.toException());
            }
        });
    }


    private void showBarChart(HashMap<String, Float> dailyEnergyConsumption) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> dates = new ArrayList<>();

        // Mengurutkan HashMap berdasarkan tanggal secara terbalik
        ArrayList<Map.Entry<String, Float>> sortedEntries = new ArrayList<>(dailyEnergyConsumption.entrySet());
        Collections.sort(sortedEntries, new Comparator<Map.Entry<String, Float>>() {
            @Override
            public int compare(Map.Entry<String, Float> entry1, Map.Entry<String, Float> entry2) {
                return entry2.getKey().compareTo(entry1.getKey()); // Urutkan dari yang terbesar ke yang terkecil
            }
        });

        // Iterasi melalui sortedEntries untuk mengisi entries dan dates
        int index = 0;
        for (Map.Entry<String, Float> entry : sortedEntries) {
            String date = entry.getKey();
            Float energyDifference = entry.getValue();
            dates.add(date);
            String energyDifferenceWithUnit = String.format("%.2f kWh", energyDifference); // Format nilai energi
            entries.add(new BarEntry(index++, energyDifference, energyDifferenceWithUnit)); // Gunakan nilai energi sebagai label
        }

        BarDataSet dataSet = new BarDataSet(entries, "Energy Usage in the Last 5 Days");
        dataSet.setColor(getResources().getColor(R.color.blue));
        dataSet.setValueTextSize(10f);

        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.2f kWh", value); // Format nilai pada batang dengan satuan " kWh"
            }
        });

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        // Atur label tanggal pada sumbu X
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dates)); // Atur label sesuai dengan tanggal
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f); // Penting untuk memastikan setiap tanggal ditampilkan

        Description description = new Description();
        description.setText("");
        barChart.setDescription(description);

        barChart.invalidate(); // Refresh chart
    }
    private String getPastDate(int days) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        long currentTime = System.currentTimeMillis();
        long pastTime = currentTime - (days * 24 * 60 * 60 * 1000); // Mengurangi waktu sebanyak 'days' hari
        Date pastDate = new Date(pastTime);
        return dateFormat.format(pastDate);
    }
}