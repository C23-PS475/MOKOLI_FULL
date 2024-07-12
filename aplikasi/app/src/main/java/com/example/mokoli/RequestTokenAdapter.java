package com.example.mokoli;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Map;

public class RequestTokenAdapter extends RecyclerView.Adapter<RequestTokenAdapter.RequestTokenViewHolder> {
    private ArrayList<RequestToken> requestTokens;
    private FirebaseFirestore firestore;

    public RequestTokenAdapter(ArrayList<RequestToken> requestTokens) {
        this.requestTokens = requestTokens;
        firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public RequestTokenViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaksi, parent, false);
        return new RequestTokenViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestTokenViewHolder holder, int position) {
        RequestToken requestToken = requestTokens.get(position);
        holder.bind(requestToken);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tampilkan dialog alert
                showDialog(v.getContext(), requestToken);
            }
        });
    }

    private void showDialog(Context context, RequestToken requestToken) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.activity_approve, null);

        TextView namaTextView = dialogView.findViewById(R.id.nama);
        TextView pembelianTextView = dialogView.findViewById(R.id.trpembelian);
        TextView tanggalTextView = dialogView.findViewById(R.id.trtgl);
        TextView jumlahTextView = dialogView.findViewById(R.id.trjumlah);
        TextView metodeTextView = dialogView.findViewById(R.id.trmetode);
        TextView buktiTextView = dialogView.findViewById(R.id.ttbukti);
        ImageView buktiImageView = dialogView.findViewById(R.id.buktiImageView);

        // Set data dari requestToken ke TextViews
        namaTextView.setText(requestToken.getFullname());
        pembelianTextView.setText(requestToken.getPembelianToken());
        tanggalTextView.setText(requestToken.getCurrentDate());
        jumlahTextView.setText(requestToken.getJumlahToken());
        metodeTextView.setText(requestToken.getMetodePembayaran());
        // Tampilkan gambar dengan URL menggunakan Glide atau Picasso
        Glide.with(context).load(requestToken.getBuktiPembayaran()).into(buktiImageView);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        // Jika tombol "Setuju" diklik
        dialogView.findViewById(R.id.btnSetuju).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Simpan data transaksi ke Firestore
                saveToUserTransactions(requestToken);

                // Salin data transaksi ke koleksi "riwayat"
                copyTransactionsToHistory(requestToken);

                // Hapus data dari request_token
                deleteRequestToken(requestToken);

                dialog.dismiss();
            }
        });


        // Jika tombol "Tolak" diklik
        dialogView.findViewById(R.id.btnTolak).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tolak permintaan token
                rejectRequestToken(requestToken);
                dialog.dismiss();
            }
        });

    }

    private void rejectRequestToken(RequestToken requestToken) {
        String fullname = requestToken.getFullname();
        // Dapatkan referensi ke Firestore
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        // Buat query untuk mencari dokumen dengan fullname yang sesuai
        firestore.collection("request_token")
                .whereEqualTo("fullname", fullname)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        // Hapus dokumen yang sesuai
                        documentSnapshot.getReference().delete()
                                .addOnSuccessListener(aVoid -> {
                                    // Dokumen berhasil dihapus
                                    // Hapus item dari ArrayList
                                    requestTokens.remove(requestToken);
                                    // Memberi tahu RecyclerView bahwa dataset telah berubah
                                    notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> {
                                    // Gagal menghapus dokumen
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Gagal mengambil dokumen
                });
    }

    private void saveToUserTransactions(RequestToken requestToken) {
        String userId = requestToken.getUserId(); // Dapatkan userId dari requestToken

        // Dapatkan referensi ke Firestore
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Dapatkan collection "users" -> "userId" -> "transaksi"
        firestore.collection("users").document(userId).collection("transaksi")
                .add(requestToken.toMap())
                .addOnSuccessListener(documentReference -> {
                    // Data transaksi berhasil disimpan di koleksi "transaksi

                    // Set data jumlahToken ke Realtime Database
                    setJumlahTokenToSensorData(requestToken);
                })
                .addOnFailureListener(e -> {
                    // Gagal menyimpan data transaksi di koleksi "transaksi"
                });
    }

    private void setJumlahTokenToSensorData(RequestToken requestToken) {
        String jumlahTokenString = requestToken.getJumlahToken();
        // Ubah koma menjadi titik
        jumlahTokenString = jumlahTokenString.replace(",", ".");

        try {
            // Konversi jumlahTokenString ke Double
            Double jumlahTokenDouble = Double.parseDouble(jumlahTokenString);

            // Dapatkan referensi ke Firebase Realtime Database
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("SensorData/Token");

            // Dapatkan nilai Batasan sebelumnya dari Firebase Realtime Database
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        try {
                            // Ambil nilai Batasan sebelumnya dari Firebase Realtime Database
                            Double previousValue = dataSnapshot.getValue(Double.class);

                            // Tambahkan nilai jumlahTokenDouble ke nilai sebelumnya
                            Double newValue = previousValue + jumlahTokenDouble;

                            // Set nilai baru ke Firebase Realtime Database
                            myRef.setValue(newValue)
                                    .addOnSuccessListener(aVoid -> {
                                        // Data berhasil disimpan di Firebase Realtime Database
                                    })
                                    .addOnFailureListener(e -> {
                                        // Gagal menyimpan data di Firebase Realtime Database
                                    });
                        } catch (DatabaseException e) {
                            e.printStackTrace();
                            // Penanganan jika terjadi kesalahan saat mengambil nilai dari Firebase Realtime Database
                        }
                    } else {
                        // Kasus di mana nilai Batasan sebelumnya tidak ada di Firebase Realtime Database
                        // Tindakan yang sesuai dapat diambil, misalnya, menambahkan nilai awal
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Penanganan jika ada pembatalan permintaan
                }
            });
        } catch (NumberFormatException e) {
            e.printStackTrace();
            // Penanganan jika ada kesalahan dalam konversi string ke double
            // Misalnya, tampilkan pesan kesalahan atau lakukan tindakan yang sesuai
        }
    }


    private void copyTransactionsToHistory(RequestToken requestToken) {
        // Dapatkan referensi ke Firestore
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Salin data dari requestToken ke koleksi "riwayat" dengan menyertakan userId
        Map<String, Object> transactionData = requestToken.toMap();
        transactionData.put("userId", requestToken.getUserId());

        // Simpan data ke koleksi "riwayat"
        firestore.collection("riwayat")
                .add(transactionData)
                .addOnSuccessListener(documentReference -> {
                    // Data berhasil disalin ke koleksi "riwayat"
                })
                .addOnFailureListener(e -> {
                    // Gagal menyalin data ke koleksi "riwayat"
                });
    }


    private void deleteRequestToken(RequestToken requestToken) {
        // Dapatkan fullname dari requestToken
        String fullname = requestToken.getFullname();
        // Dapatkan referensi ke Firestore
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        // Buat query untuk mencari dokumen dengan fullname yang sesuai
        firestore.collection("request_token")
                .whereEqualTo("fullname", fullname)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        // Hapus dokumen yang sesuai
                        documentSnapshot.getReference().delete()
                                .addOnSuccessListener(aVoid -> {
                                    // Dokumen berhasil dihapus
                                    // Hapus item dari ArrayList
                                    requestTokens.remove(requestToken);
                                    // Memberi tahu RecyclerView bahwa dataset telah berubah
                                    notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> {
                                    // Gagal menghapus dokumen
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Gagal mengambil dokumen
                });
    }

    @Override
    public int getItemCount() {
        return requestTokens.size();
    }

    public static class RequestTokenViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTanggal, textViewPembelianToken, textViewJumlahToken, textViewMetodePembayaran, textViewNama;

        public RequestTokenViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTanggal = itemView.findViewById(R.id.textTanggal);
            textViewPembelianToken = itemView.findViewById(R.id.textPembelian);
            textViewJumlahToken = itemView.findViewById(R.id.textJumlah);
            textViewMetodePembayaran = itemView.findViewById(R.id.textmetode);
            textViewNama = itemView.findViewById(R.id.textnama);
        }

        public void bind(RequestToken requestToken) {
            textViewTanggal.setText(requestToken.getCurrentDate());
            textViewPembelianToken.setText(requestToken.getPembelianToken());
            textViewJumlahToken.setText(requestToken.getJumlahToken());
            textViewMetodePembayaran.setText(requestToken.getMetodePembayaran());
            textViewNama.setText(requestToken.getFullname());
        }
    }
}
