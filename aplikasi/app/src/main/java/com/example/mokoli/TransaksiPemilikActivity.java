package com.example.mokoli;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class TransaksiPemilikActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaksi_pemilik);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fetchDataFromFirestore();
    }

    private void fetchDataFromFirestore() {
        firestore.collection("request_token").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<RequestToken> requestTokens = new ArrayList<>();
                for (DocumentSnapshot document : task.getResult()) {
                    RequestToken requestToken = document.toObject(RequestToken.class);
                    if (requestToken != null) {
                        requestTokens.add(requestToken);
                    }
                }
                // Set adapter RecyclerView dengan data yang diambil dari Firestore
                RequestTokenAdapter adapter = new RequestTokenAdapter(requestTokens);
                recyclerView.setAdapter(adapter);
            } else {
                Log.e("Firestore", "Error getting documents.", task.getException());
            }
        });
    }
}

