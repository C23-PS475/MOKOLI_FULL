package com.example.mokoli;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RekapActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private RecyclerView recyclerView;
    private RekapAdapter adapter;
    private List<RekapItem> rekapList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rekap);

        // Inisialisasi Firestore
        firestore = FirebaseFirestore.getInstance();

        // Inisialisasi RecyclerView dan adapter
        recyclerView = findViewById(R.id.recyclerViewrekap);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        rekapList = new ArrayList<>();
        adapter = new RekapAdapter(this, rekapList);
        recyclerView.setAdapter(adapter);

        // Ambil data dari Firestore
        fetchDataFromFirestore();
    }

    private void fetchDataFromFirestore() {
        // Ambil semua dokumen dari koleksi riwayat
        firestore.collection("riwayat")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Reset rekapList
                    rekapList.clear();

                    // Inisialisasi map untuk menyimpan total pembelian token dan jumlah token untuk setiap bulan
                    Map<String, Double> totalPembelianTokenMap = new HashMap<>();
                    Map<String, Double> totalJumlahTokenMap = new HashMap<>();

                    // Proses setiap dokumen dalam hasil query
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        // Dapatkan data dari dokumen
                        String currentDate = documentSnapshot.getString("currentDate");
                        String pembelianToken = documentSnapshot.getString("pembelianToken");
                        String jumlahToken = documentSnapshot.getString("jumlahToken");

                        // Ekstrak bulan dari tanggal
                        String[] parts = currentDate.split(" ");
                        String bulan = parts[1]; // Ambil bagian bulan dari tanggal

                        // Hitung total pembelian token dan jumlah token untuk bulan ini
                        double pembelianTokenValue = Double.parseDouble(pembelianToken.replaceAll("[^\\d]+", ""));
                        double jumlahTokenValue = Double.parseDouble(jumlahToken.replace(",", "."));

                        totalPembelianTokenMap.put(bulan, totalPembelianTokenMap.getOrDefault(bulan, 0.0) + pembelianTokenValue);
                        totalJumlahTokenMap.put(bulan, totalJumlahTokenMap.getOrDefault(bulan, 0.0) + jumlahTokenValue);
                    }

                    // Simpan total pembelian token dan jumlah token ke Firestore
                    saveRekapDataToFirestore(totalPembelianTokenMap, totalJumlahTokenMap);

                    // Tampilkan data dari Firestore di RecyclerView
                    fetchRekapDataFromFirestore();
                })
                .addOnFailureListener(e -> {
                    // Tampilkan pesan error jika gagal mengambil data
                    Toast.makeText(RekapActivity.this, "Gagal mengambil data dari Firestore", Toast.LENGTH_SHORT).show();
                    Log.e("RekapActivity", "Error fetching data", e);
                });
    }

    private void saveRekapDataToFirestore(Map<String, Double> totalPembelianTokenMap, Map<String, Double> totalJumlahTokenMap) {
        for (String bulan : totalPembelianTokenMap.keySet()) {
            double totalPembelianToken = totalPembelianTokenMap.get(bulan);
            double totalJumlahToken = totalJumlahTokenMap.get(bulan);

            // Buat objek Map untuk menyimpan data rekap
            Map<String, Object> rekapData = new HashMap<>();
            rekapData.put("bulan", bulan);
            rekapData.put("totalPembelianToken", totalPembelianToken);
            rekapData.put("totalJumlahToken", totalJumlahToken);

            // Simpan data rekap ke dalam Firestore
            firestore.collection("rekap").document(bulan)
                    .set(rekapData)
                    .addOnSuccessListener(aVoid -> Log.d("RekapActivity", "Data rekap berhasil disimpan"))
                    .addOnFailureListener(e -> Log.e("RekapActivity", "Gagal menyimpan data rekap", e));
        }
    }

    private void fetchRekapDataFromFirestore() {
        // Ambil semua dokumen dari koleksi rekap
        firestore.collection("rekap")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Proses setiap dokumen dalam hasil query
                    List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                    Collections.sort(documents, (doc1, doc2) -> {
                        String bulan1 = doc1.getString("bulan");
                        String bulan2 = doc2.getString("bulan");
                        return Integer.compare(getMonthNumber(bulan1), getMonthNumber(bulan2));
                    });

                    // Bersihkan list rekap sebelum menambahkan data baru
                    rekapList.clear();

                    // Tambahkan data ke dalam list rekap
                    for (DocumentSnapshot documentSnapshot : documents) {
                        String bulan = documentSnapshot.getString("bulan");
                        double totalPembelianToken = documentSnapshot.getDouble("totalPembelianToken");
                        double totalJumlahToken = documentSnapshot.getDouble("totalJumlahToken");
                        rekapList.add(new RekapItem(bulan, totalPembelianToken, totalJumlahToken));
                    }

                    // Setelah selesai, refresh RecyclerView
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Tampilkan pesan error jika gagal mengambil data
                    Toast.makeText(RekapActivity.this, "Gagal mengambil data rekap dari Firestore", Toast.LENGTH_SHORT).show();
                    Log.e("RekapActivity", "Error fetching rekap data", e);
                });
    }

    private int getMonthNumber(String month) {
        switch (month.toLowerCase()) {
            case "januari":
                return 1;
            case "februari":
                return 2;
            case "maret":
                return 3;
            case "april":
                return 4;
            case "mei":
                return 5;
            case "juni":
                return 6;
            case "juli":
                return 7;
            case "agustus":
                return 8;
            case "september":
                return 9;
            case "oktober":
                return 10;
            case "november":
                return 11;
            case "desember":
                return 12;
            default:
                return 0; // Jika nama bulan tidak valid
        }
    }
}

