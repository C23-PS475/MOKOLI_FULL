package com.example.mokoli;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class MainActivity extends AppCompatActivity {

    TextView Hi, welcome;
    CardView monitoring, transaksi, history;
    ImageView Logout;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Hi = findViewById(R.id.textHi);
        welcome = findViewById(R.id.textWelcome);
        monitoring = findViewById(R.id.cvmonitoring);
        transaksi = findViewById(R.id.cvtransaksi);
        history = findViewById(R.id.cvhistory);
        Logout = findViewById(R.id.imageLogout);
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        monitoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Buka MonitoringActivity
                Intent intent = new Intent(MainActivity.this, MonitoringActivity.class);
                startActivity(intent);
            }
        });

        transaksi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Buka MonitoringActivity
                Intent intent = new Intent(MainActivity.this, TransaksiPenyewaActivity.class);
                startActivity(intent);
            }
        });

        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Buka History
                Intent intent = new Intent(MainActivity.this, HistoryPenyewaActivity.class);
                startActivity(intent);
            }
        });

        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                startActivity(intent);
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
