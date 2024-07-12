package com.example.mokoli;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class HistoryPenyewaActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TransaksiAdapter adapter;
    private ArrayList<Transaksi> transaksiList;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_penyewa);

        recyclerView = findViewById(R.id.recyclerViewhistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        transaksiList = new ArrayList<>();
        adapter = new TransaksiAdapter(transaksiList);
        recyclerView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            CollectionReference transaksiRef = firestore.collection("users").document(userId).collection("transaksi");
            transaksiRef.addSnapshotListener((queryDocumentSnapshots, e) -> {
                if (e != null) {
                    // Handle error
                    return;
                }

                if (queryDocumentSnapshots != null) {
                    for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                        switch (dc.getType()) {
                            case ADDED:
                                Transaksi transaksi = dc.getDocument().toObject(Transaksi.class);
                                transaksiList.add(transaksi);
                                adapter.notifyDataSetChanged();
                                break;
                            // Handle other cases if needed (MODIFIED, REMOVED)
                        }
                    }
                }
            });
        }
    }
}

