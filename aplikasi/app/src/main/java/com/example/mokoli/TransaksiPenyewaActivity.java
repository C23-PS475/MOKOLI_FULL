package com.example.mokoli;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TransaksiPenyewaActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    TextView title, token, jumlah, total, tgl, pembelianTokenTextView, jumlahTokenTextView, namaFile, tglRequestTextView, pembelianTokenCardTextView, jumlahTokenCardTextView, nama;
    Button req;
    CardView cardview;
    Spinner spinnerToken, spinnerPembayaran;
    ImageButton camera;
    ImageView imageView;
    int width = 150;
    private Uri selectedImageUri;
    private ArrayList<CustomItem> customList;
    private ArrayList<CustomItem> customList1;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private String currentDate;
    private String selectedSpinnerItem;
    private String totalText;
    private CustomItem selectItem;
    private String imageURL;

    private double ppjPercentage;
    private double electricityPrice;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaksi_penyewa);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Initialize Firebase Storage
        storage = FirebaseStorage.getInstance();

        // Initialize Views
        title = findViewById(R.id.tittle);
        token = findViewById(R.id.tokentransaksi);
        jumlah = findViewById(R.id.jmltoken);
        total = findViewById(R.id.total);
        tgl = findViewById(R.id.tgl);
        cardview = findViewById(R.id.cardViewRincian);
        pembelianTokenTextView = findViewById(R.id.pembelianTokenTextView);
        jumlahTokenTextView = findViewById(R.id.jumlahTokenTextView);
        req = findViewById(R.id.btnreq);
        spinnerToken = findViewById(R.id.Spinnertransaksi);
        spinnerPembayaran = findViewById(R.id.Spinnerpembayaran);
        namaFile = findViewById(R.id.namafile);
        camera = findViewById(R.id.camera);
        nama = findViewById(R.id.user);

        tglRequestTextView = findViewById(R.id.tgl);
        pembelianTokenCardTextView = findViewById(R.id.pembelianTokenTextView);
        jumlahTokenCardTextView = findViewById(R.id.jumlahTokenTextView);

        customList = getCustomList();
        customList1 = getCustomList1();

        CustomAdapter adapterToken = new CustomAdapter(this, customList);
        spinnerToken.setAdapter(adapterToken);
        spinnerToken.setOnItemSelectedListener(this);

        CustomAdapter adapterMetode = new CustomAdapter(this, customList1);
        spinnerPembayaran.setAdapter(adapterMetode);
        spinnerPembayaran.setOnItemSelectedListener(this);

        // Set "Metode Pembayaran" and "Pembelian Token" unselectable
        spinnerToken.setSelection(0, false);
        spinnerPembayaran.setSelection(0, false);

        req.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentDate = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH).format(new Date());
                CustomItem selectedItem = (CustomItem) spinnerToken.getSelectedItem();
                selectItem = (CustomItem) spinnerPembayaran.getSelectedItem();
                selectedSpinnerItem = selectedItem.getSpinnerItemName();
                totalText = total.getText().toString();

                tglRequestTextView.setText(currentDate);
                pembelianTokenCardTextView.setText(selectedSpinnerItem);
                jumlahTokenCardTextView.setText(totalText);

                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    if (isDataValid()) {
                        // Panggil metode saveRequestToFirestore dengan URL gambar yang telah diperoleh sebelumnya
                        saveRequestToFirestore(currentUser.getUid());
                        cardview.setVisibility(View.VISIBLE);
                        clearInputFields();
                    }
                } else {
                    Log.d("TransaksiPenyewaActivity", "No user logged in.");
                }
            }
        });


        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 100);
            }
        });

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            getUserDataFromFirestore(currentUser.getUid());
        } else {
            Log.d("TransaksiPenyewaActivity", "No user logged in.");
        }
    }

    private void saveRequestToFirestore(String userId) {
        String currentDate = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH).format(new Date());
        CustomItem selectedItem = (CustomItem) spinnerToken.getSelectedItem();
        selectItem = (CustomItem) spinnerPembayaran.getSelectedItem();
        selectedSpinnerItem = selectedItem.getSpinnerItemName();
        totalText = total.getText().toString();

        tglRequestTextView.setText(currentDate);
        pembelianTokenCardTextView.setText(selectedSpinnerItem);
        jumlahTokenCardTextView.setText(totalText);

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("currentDate", currentDate);
        requestData.put("pembelianToken", selectedSpinnerItem);
        requestData.put("jumlahToken", totalText);
        requestData.put("metodePembayaran", selectItem.getSpinnerItemName());
        requestData.put("fullname", nama.getText().toString());
        requestData.put("userId", userId); // Menambahkan userID ke data transaksi
        requestData.put("buktiPembayaran", imageURL); // Menambahkan URL gambar ke data transaksi

        firestore.collection("request_token")
                .add(requestData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Firestore", "DocumentSnapshot added with ID: " + documentReference.getId());
                    Toast.makeText(TransaksiPenyewaActivity.this, "Data successfully saved to Firestore", Toast.LENGTH_SHORT).show();
                    // Clear input fields setelah data berhasil disimpan
                    clearInputFields();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error adding document", e);
                    Toast.makeText(TransaksiPenyewaActivity.this, "Failed to save data to Firestore", Toast.LENGTH_SHORT).show();
                });
    }

    private boolean isDataValid() {
        if (selectedImageUri == null) {
            Toast.makeText(TransaksiPenyewaActivity.this, "Please select the proof of payment image", Toast.LENGTH_SHORT).show();
            return false;
        } else if (spinnerToken.getSelectedItemPosition() == 0) {
            Toast.makeText(TransaksiPenyewaActivity.this, "Please select token purchase", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    private void clearInputFields() {
        total.setText("");
        namaFile.setText("");
        spinnerToken.setSelection(0);
        spinnerPembayaran.setSelection(0);
    }

    private ArrayList<CustomItem> getCustomList1() {
        ArrayList<CustomItem> customList1 = new ArrayList<>();
        customList1.add(new CustomItem("Payment Method", android.R.color.darker_gray));
        customList1.add(new CustomItem("Cash", android.R.color.darker_gray));
        customList1.add(new CustomItem("Transfer", android.R.color.darker_gray));
        return customList1;
    }

    private ArrayList<CustomItem> getCustomList() {
        ArrayList<CustomItem> customList = new ArrayList<>();
        customList.add(new CustomItem("Token Purchase", android.R.color.darker_gray));
        customList.add(new CustomItem("IDR 20.000", android.R.color.darker_gray));
        customList.add(new CustomItem("IDR 50.000", android.R.color.darker_gray));
        customList.add(new CustomItem("IDR 100.000", android.R.color.darker_gray));
        customList.add(new CustomItem("IDR 200.000", android.R.color.darker_gray));
        customList.add(new CustomItem("IDR 500.000", android.R.color.darker_gray));
        return customList;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        try {
            width = findViewById(R.id.customSpinnerItemLayout).getWidth();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Spinner spinner = (Spinner) adapterView;
        spinner.setDropDownWidth(width);

        switch (spinner.getId()) {
            case R.id.Spinnertransaksi:
                CustomItem item = (CustomItem) adapterView.getSelectedItem();
                if (item.getSpinnerItemName().equals("Token Purchase")) {
                    total.setText("");
                } else {
                    total.setText(item.getSpinnerItemName());
                    total.setTextColor(getResources().getColor(R.color.black));
                    calculate(item);
                }
                break;
            case R.id.Spinnerpembayaran:
                selectItem = customList1.get(position);
                break;
        }
    }

    private void calculate(final CustomItem selectedItem) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Setting");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    double ppjPercent = dataSnapshot.child("PPJ").getValue(Double.class);
                    double electricityPrice = dataSnapshot.child("kWhPrice").getValue(Double.class);

                    // Tampilkan nilai PPJ dan harga listrik di logcat
                    Log.d("TransaksiPenyewaActivity", "PPJ Percentage: " + ppjPercent);
                    Log.d("TransaksiPenyewaActivity", "Electricity Price: " + electricityPrice);

                    // Ambil nilai dari spinner atau komponen lainnya sesuai kebutuhan
                    double itemValue = Double.parseDouble(selectedItem.getSpinnerItemName().replaceAll("[^\\d]+", ""));

                    // Hitung nilai total berdasarkan PPJ dan harga listrik yang telah dikonversi
                    double calculatedValue = (itemValue - (itemValue * ppjPercent)) / electricityPrice;

                    // Format hasil perhitungan menjadi dua desimal
                    String formattedResult = String.format(Locale.getDefault(), "%.2f", calculatedValue);

                    // Tampilkan hasil perhitungan pada TextView 'total' atau komponen UI lainnya
                    total.setText(formattedResult);

                    // Tampilkan nilai yang diinputkan dan yang dikonversikan di logcat
                    Log.d("TransaksiPenyewaActivity", "Nilai yang diinput: " + itemValue);
                    Log.d("TransaksiPenyewaActivity", "Nilai yang dikonversikan: " + formattedResult);
                } else {
                    Log.d("FirebaseData", "No data found in Setting node.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseData", "Failed to read data from Setting node.", databaseError.toException());
            }
        });
    }


    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            String fileName = getFileName(selectedImageUri);
            namaFile.setText(fileName);
            // Langsung upload gambar ke Firebase Storage saat dipilih dari galeri
            uploadBuktiPembayaranToFirestore(selectedImageUri);
        }
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void uploadBuktiPembayaranToFirestore(Uri imageUri) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        if (imageUri != null) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                StorageReference storageRef = storage.getReference();
                StorageReference imageRef = storageRef.child("bukti_pembayaran").child(imageUri.getLastPathSegment());
                UploadTask uploadTask = imageRef.putFile(imageUri);
                uploadTask.addOnSuccessListener(taskSnapshot -> {
                    // Ketika gambar berhasil diunggah, ambil URL gambar
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        imageURL = uri.toString(); // Simpan URL gambar sebagai variabel global
                        // Tampilkan toast ketika gambar berhasil diunggah
                        progressDialog.dismiss();
                        //Toast.makeText(TransaksiPenyewaActivity.this, "Gambar berhasil diunggah ke Firebase Storage", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(exception -> {
                        Log.e("FirebaseStorage", "Failed to get image URL", exception);
                        Toast.makeText(TransaksiPenyewaActivity.this, "Failed to get image URL", Toast.LENGTH_SHORT).show();
                    });
                }).addOnFailureListener(exception -> {
                    Log.e("FirebaseStorage", "Failed to upload image", exception);
                    Toast.makeText(TransaksiPenyewaActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                });
            } else {
                progressDialog.dismiss();
                Toast.makeText(TransaksiPenyewaActivity.this, "No user logged in", Toast.LENGTH_SHORT).show();
            }
        } else {
            progressDialog.dismiss();
            Toast.makeText(TransaksiPenyewaActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void getUserDataFromFirestore(String userId) {
        DocumentReference userRef = firestore.collection("users").document(userId);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String fullname = document.getString("fullName");
                    nama.setText(fullname);
                } else {
                    Log.d("Firestore", "Document not found");
                }
            } else {
                Log.e("FirestoreError", "Failed to get user data", task.getException());
            }
        });
    }
}
