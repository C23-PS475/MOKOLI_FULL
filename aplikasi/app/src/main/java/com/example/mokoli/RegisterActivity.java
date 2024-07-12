package com.example.mokoli;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    EditText nama, email, password;
    Spinner tipeSpinner, kamarSpinner;

    LinearLayout kamar;
    Button register;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseApp.initializeApp(this);
        firestore = FirebaseFirestore.getInstance();

        nama = findViewById(R.id.namalengkap);
        email = findViewById(R.id.email);
        password = findViewById(R.id.pass);
        register = findViewById(R.id.btnregister);
        tipeSpinner = findViewById(R.id.tipe);
        kamar =findViewById(R.id.kamarlayout);
        kamarSpinner = findViewById(R.id.kamar);

        CustomAdapter adapter = new CustomAdapter(this, getCustomList());
        tipeSpinner.setAdapter(adapter);
        tipeSpinner.setOnItemSelectedListener(this);
        CustomAdapter kamarAdapter = new CustomAdapter(this, getKamarList());
        kamarSpinner.setAdapter(kamarAdapter);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullName = nama.getText().toString();
                String userEmail = email.getText().toString();
                String userPassword = password.getText().toString();
                String selectedTipe = tipeSpinner.getSelectedItem().toString();
                String selectedKamar = kamarSpinner.getSelectedItem().toString();

                if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(userEmail) || TextUtils.isEmpty(userPassword) || selectedTipe.equals("Account Type")) {
                    Toast.makeText(RegisterActivity.this, "Please complete the personal data", Toast.LENGTH_SHORT).show();
                } else if (tipeSpinner.getSelectedItemPosition() == 0) {
                    // Jika tipeSpinner masih dalam keadaan default
                    Toast.makeText(RegisterActivity.this, "Please select an account type", Toast.LENGTH_SHORT).show();
                } else {
                    if (isValidPassword(userPassword)) {
                        // Lanjutkan proses registrasi jika password valid dan tipe akun dipilih dengan benar
                        registerUser(fullName, userEmail, userPassword, selectedTipe, selectedKamar);
                    } else {
                        Toast.makeText(RegisterActivity.this, "Passwords must consist of at least 8 characters, including uppercase letters, lowercase letters, numbers, and symbols.", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private ArrayList<CustomItem> getCustomList() {
        ArrayList<CustomItem> customList = new ArrayList<>();
        customList.add(new CustomItem("Account Type", android.R.color.darker_gray));
        customList.add(new CustomItem("Owner", android.R.color.black));
        customList.add(new CustomItem("Tenants", android.R.color.black));
        return customList;
    }

    private ArrayList<CustomItem> getKamarList() {
        ArrayList<CustomItem> kamarList = new ArrayList<>();
        kamarList.add(new CustomItem("Room Numbers", android.R.color.darker_gray));
        for (int i = 1; i <= 10; i++) {
            kamarList.add(new CustomItem(String.format("%02d", i), android.R.color.black));
        }
        return kamarList;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        CustomItem selectedItem = (CustomItem) adapterView.getItemAtPosition(position);
        if (adapterView.getId() == R.id.tipe) {
            if (!selectedItem.getSpinnerItemName().equals("Account Type")) {
                if(selectedItem.getSpinnerItemName().equals("Owner")) {
                    showDialogForAdminCode();
                    kamar.setVisibility(View.GONE);
                } else if(selectedItem.getSpinnerItemName().equals("Tenants")) {
                    kamar.setVisibility(View.VISIBLE);
                }
            } else {
                // Jika pengguna memilih "Tipe Akun", atur kembali ke posisi default
                tipeSpinner.setSelection(0);
            }
        } else if (adapterView.getId() == R.id.kamar) {
            if (!selectedItem.getSpinnerItemName().equals("Room Numbers")) {
                // Jika pengguna memilih nomor kamar, lakukan pengecekan kamar
                checkKamarAvailability(selectedItem.getSpinnerItemName());
            } else {
                // Jika pengguna memilih "No Kamar", atur kembali ke posisi default
                kamarSpinner.setSelection(0);
            }
        }
    }


    private void showDialogForAdminCode() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
        View view = getLayoutInflater().inflate(R.layout.activity_kode, null);
        builder.setView(view);

        final EditText kodeAdminEditText = view.findViewById(R.id.kodeBox);
        Button submitButton = view.findViewById(R.id.kodeReset);
        Button cancelButton = view.findViewById(R.id.kodeCancel);

        final AlertDialog dialog = builder.create();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String kodeAdmin = kodeAdminEditText.getText().toString();
                if (kodeAdmin.equals("admin123")) {
                    registerUser(nama.getText().toString(), email.getText().toString(), password.getText().toString(), "Owner", null);
                    dialog.dismiss();
                } else {
                    Toast.makeText(RegisterActivity.this, "Invalid admin code", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                tipeSpinner.setSelection(0);
            }
        });

        kamarSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CustomItem selectedItem = (CustomItem) parent.getItemAtPosition(position);
                if (!selectedItem.getSpinnerItemName().equals("Room Numbers")) {
                    // Lakukan pengecekan nomor kamar
                    String selectedKamar = selectedItem.getSpinnerItemName();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        dialog.show();
    }

    private void checkKamarAvailability(final String selectedKamar) {
        // Lakukan pengecekan terpisah untuk setiap nomor kamar
        switch (selectedKamar) {
            case "01":
            case "02":
            case "03":
            case "04":
            case "05":
            case "06":
            case "07":
            case "08":
            case "09":
            case "10":
                // Jika nomor kamar yang dipilih berada dalam rentang yang ditentukan,
                // lakukan kueri Firestore untuk memeriksa ketersediaannya
                firestore.collection("users")
                        .whereEqualTo("noKamar", selectedKamar)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    if (!task.getResult().isEmpty()) {
                                        // Kamar sudah ditempati
                                        Toast.makeText(RegisterActivity.this, "Room number already used", Toast.LENGTH_SHORT).show();
                                        kamarSpinner.setSelection(0); // Reset spinner
                                    } else {
                                        // Kamar tersedia, lanjutkan registrasi
                                        registerUser(nama.getText().toString(), email.getText().toString(), password.getText().toString(), "Tenants", selectedKamar);
                                    }
                                } else {
                                    // Penanganan kesalahan untuk kueri Firestore
                                    Toast.makeText(RegisterActivity.this, "Error checking room availability: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                break;
            default:
                // Tangani nomor kamar yang tidak valid di sini
                Toast.makeText(RegisterActivity.this, "Invalid room numbers", Toast.LENGTH_SHORT).show();
                kamarSpinner.setSelection(0); // Reset spinner
                break;
        }
    }



    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    private void registerUser(final String fullName, final String userEmail, final String userPassword, final String selectedTipe, final String selectedKamar ) {
        if (selectedTipe.equals("Account Type")) {
            Toast.makeText(RegisterActivity.this, "Select a valid account type", Toast.LENGTH_SHORT).show();
        } else if (selectedTipe.equals("Tenants") && TextUtils.isEmpty(selectedKamar)) {
            // Jika tipe akun adalah "Penyewa" tetapi nomor kamar tidak dipilih
            Toast.makeText(RegisterActivity.this, "Select room number", Toast.LENGTH_SHORT).show();
        } else {
            // Jika tipe akun adalah "Pemilik" atau nomor kamar sudah dipilih untuk "Penyewa"
            firebaseAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(RegisterActivity.this, "Registered successfully", Toast.LENGTH_LONG).show();

                                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                                if (currentUser != null) {
                                    String userId = currentUser.getUid();
                                    // Ambil nilai aktual dari objek CustomItem yang dipilih
                                    String tipe = ((CustomItem) tipeSpinner.getSelectedItem()).getSpinnerItemName();
                                    String kamar = ((CustomItem) kamarSpinner.getSelectedItem()).getSpinnerItemName();

                                    // Simpan data ke Firestore tergantung pada tipe akun yang dipilih
                                    if (selectedTipe.equals("Owner")) {
                                        saveUserDataToFirestore(userId, fullName, userEmail, tipe, null);
                                    } else {
                                        saveUserDataToFirestore(userId, fullName, userEmail, tipe, kamar);
                                    }
                                }

                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }




    private void saveUserDataToFirestore(String userId, String fullName, String email, String tipeAkun, String noKamar) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("userId", userId);
        userMap.put("fullName", fullName);
        userMap.put("email", email);
        userMap.put("tipeAkun", tipeAkun);
        if (noKamar != null) {
            userMap.put("noKamar", noKamar);
        }

        firestore.collection("users").document(userId)
                .set(userMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Data pengguna berhasil disimpan di Firestore
                        } else {
                            // Gagal menyimpan data pengguna di Firestore
                            Toast.makeText(RegisterActivity.this, "Failed to save user data: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            Log.e("FirestoreError", "Failed to save user data", task.getException());
                        }
                    }
                });
    }

    private boolean isValidPassword(String password) {
        if (password.length() < 8) {
            return false;
        }

        if (!Pattern.compile("[A-Z]").matcher(password).find()) {
            return false;
        }

        if (!Pattern.compile("[a-z]").matcher(password).find()) {
            return false;
        }

        if (!Pattern.compile("[0-9]").matcher(password).find()) {
            return false;
        }

        if (!Pattern.compile("[^A-Za-z0-9]").matcher(password).find()) {
            return false;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        tipeSpinner.setSelection(0);
    }

}
