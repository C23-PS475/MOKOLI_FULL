package com.example.mokoli;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class HistoryPemilikActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RiwayatAdapter riwayatAdapter;
    private List<RiwayatItem> riwayatList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_pemilik);

        recyclerView = findViewById(R.id.recyclerViewpemilik);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        riwayatList = new ArrayList<>();
        riwayatAdapter = new RiwayatAdapter(riwayatList);
        recyclerView.setAdapter(riwayatAdapter);

        fetchDataFromFirestore();
    }

    private void fetchDataFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("riwayat")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String currentDate = document.getString("currentDate");
                            String fullname = document.getString("fullname");
                            String jumlahToken = document.getString("jumlahToken");
                            String metodePembayaran = document.getString("metodePembayaran");
                            String pembelianToken = document.getString("pembelianToken");

                            RiwayatItem riwayatItem = new RiwayatItem(currentDate, fullname, jumlahToken, metodePembayaran, pembelianToken);
                            riwayatList.add(riwayatItem);
                        }
                        riwayatAdapter.notifyDataSetChanged();
                    } else {
                        // Log error message
                    }
                });
    }
}

