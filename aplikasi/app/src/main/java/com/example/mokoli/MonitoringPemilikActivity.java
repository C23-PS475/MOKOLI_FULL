package com.example.mokoli;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonitoringPemilikActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> userList;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring_pemilik);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recycler_view);

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();
        // Initialize user list
        userList = new ArrayList<>();

        // Get user data from Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("tipeAkun", "Tenants") // Filter documents with tipeAkun = "Penyewa"
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Get noKamar and fullName data from each found document
                                String noKamar = document.getString("noKamar");
                                String fullName = document.getString("fullName");

                                // Determine the corresponding sensor name based on the room number
                                Map<String, String> sensorMap = new HashMap<>();
                                sensorMap.put("01", "SensorData");
                                sensorMap.put("02", "Sensor2");
                                sensorMap.put("03", "Sensor3");
                                sensorMap.put("04", "Sensor4");
                                sensorMap.put("05", "Sensor5");
                                sensorMap.put("06", "Sensor6");
                                sensorMap.put("07", "Sensor7");
                                sensorMap.put("08", "Sensor8");
                                sensorMap.put("09", "Sensor9");
                                sensorMap.put("10", "Sensor10");

                                String sensorName = sensorMap.get(noKamar);

                                // Determine KWH value based on the corresponding sensor data
                                final double[] kwh = new double[1]; // Initialize KWH value

                                // Obtain the corresponding sensor data
                                if (sensorName != null) {
                                    mRef.child(sensorName).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                double energy = dataSnapshot.child("Energy").getValue(Double.class);
                                                double token = dataSnapshot.child("Token").getValue(Double.class);

                                                // Calculate KWH
                                                kwh[0] = token - energy;

                                                // Add data to the user list
                                                userList.add(new User(noKamar, fullName, kwh[0] + " kWh"));

                                                // Sort userList based on room number
                                                Collections.sort(userList, (user1, user2) -> {
                                                    String kamar1 = user1.getNoKamar();
                                                    String kamar2 = user2.getNoKamar();
                                                    return kamar1.compareTo(kamar2);
                                                });

                                                // Set up RecyclerView with adapter and grid layout manager
                                                GridLayoutManager layoutManager = new GridLayoutManager(MonitoringPemilikActivity.this, 2);
                                                layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                                                    @Override
                                                    public int getSpanSize(int position) {
                                                        return position % 2 == 0 ? 2 : 1;
                                                    }
                                                });
                                                userAdapter = new UserAdapter(userList, MonitoringPemilikActivity.this);
                                                recyclerView.setAdapter(userAdapter);
                                            } else {
                                                Log.w(TAG, "No sensor data found for sensor: " + sensorName);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.e(TAG, "Database error: " + error.getMessage());
                                        }
                                    });
                                }
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}
