package com.example.mokoli;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeAdminActivity extends AppCompatActivity {
    TextView Hi, welcome;
    CardView monitor, transaksi, history, rekap;
    ImageView Logout, setting;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_admin);

        Hi = findViewById(R.id.textHiadmin);
        welcome = findViewById(R.id.textWelcomeadmin);
        transaksi = findViewById(R.id.cvtransaksiadmin);
        history = findViewById(R.id.cvhistoryadmin);
        rekap = findViewById(R.id.cvrekap);
        Logout = findViewById(R.id.imageLogout2);
        setting = findViewById(R.id.setting);
        monitor = findViewById(R.id.cvmonitoringpemilik);
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        monitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Buka MonitoringActivity
                Intent intent = new Intent(HomeAdminActivity.this, MonitoringPemilikActivity.class);
                startActivity(intent);
            }
        });

        transaksi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Buka MonitoringActivity
                Intent intent = new Intent(HomeAdminActivity.this, TransaksiPemilikActivity.class);
                startActivity(intent);
            }
        });

        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Buka MonitoringActivity
                Intent intent = new Intent(HomeAdminActivity.this, HistoryPemilikActivity.class);
                startActivity(intent);
            }
        });

        rekap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Buka MonitoringActivity
                Intent intent = new Intent(HomeAdminActivity.this, RekapActivity.class);
                startActivity(intent);
            }
        });

        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(HomeAdminActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeAdminActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.activity_setting, null);
                EditText inputBox = dialogView.findViewById(R.id.inputBox);
                EditText inputBox2 = dialogView.findViewById(R.id.inputBox2);
                TextView ppjTextView = dialogView.findViewById(R.id.ppj2);
                TextView kwhTextView = dialogView.findViewById(R.id.kwh2);
                builder.setView(dialogView);
                AlertDialog dialog = builder.create();

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                // Ambil nilai PPJ dari Firebase dan tampilkan di TextView
                databaseReference.child("Setting").child("PPJ").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            double ppjPercent = dataSnapshot.getValue(Double.class);
                            ppjTextView.setText(String.valueOf(ppjPercent));
                            Log.d("FirebaseData", "PPJ Value: " + ppjPercent);
                        } else {
                            Log.d("FirebaseData", "No PPJ data found.");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("FirebaseData", "Failed to read PPJ value.", databaseError.toException());
                    }
                });

                // Ambil nilai kWhPrice dari Firebase dan tampilkan di TextView
                databaseReference.child("Setting").child("kWhPrice").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            double electricityPrice = dataSnapshot.getValue(Double.class);
                            kwhTextView.setText(String.valueOf(electricityPrice));
                            Log.d("FirebaseData", "Electricity Price: " + electricityPrice);
                        } else {
                            Log.d("FirebaseData", "No kWhPrice data found.");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("FirebaseData", "Failed to read kWhPrice value.", databaseError.toException());
                    }
                });

                dialogView.findViewById(R.id.btnsave).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String ppjPercentStr = inputBox.getText().toString().trim();
                        String electricityPriceStr = inputBox2.getText().toString().trim();

                        try {
                            // Ambil input PPJ dan bagi dengan 100 untuk mengubahnya ke desimal
                            double ppjPercent = Double.parseDouble(ppjPercentStr) / 100.0;
                            double electricityPrice = Double.parseDouble(electricityPriceStr);

                            // Simpan nilai PPJ dan harga listrik ke Firebase Realtime Database
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                            databaseReference.child("Setting").child("PPJ").setValue(ppjPercent);
                            databaseReference.child("Setting").child("kWhPrice").setValue(electricityPrice);

                            // Log untuk memeriksa nilai yang disimpan
                            Log.d("TransaksiPenyewaActivity", "PPJ Percentage: " + ppjPercent);
                            Log.d("TransaksiPenyewaActivity", "Electricity Price: " + electricityPrice);

                            // Tutup dialog setelah menyimpan nilai
                            dialog.dismiss();
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            Toast.makeText(HomeAdminActivity.this, "Invalid input. Please enter valid numbers.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                dialogView.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                }

                dialog.show();
            }
        });

        // Mendapatkan data pengguna dari Firestore
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            getUserDataFromFirestore(userId);
        }
    }

    private void getUserDataFromFirestore(String userId) {
        DocumentReference userRef = firestore.collection("users").document(userId);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Dokumen ditemukan, ambil data nama pengguna
                    String username = document.getString("fullName");

                    // Set teks Hi + nama pengguna
                    Hi.setText("Hi, " + username);
                } else {
                    // Dokumen tidak ditemukan
                    Log.d("Firestore", "Document not found");
                }
            } else {
                // Gagal mengambil data dari Firestore
                Log.e("FirestoreError", "Failed to get user data", task.getException());
            }
        });
    }
}
