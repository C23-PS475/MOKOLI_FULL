package com.example.mokoli;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoginActivity extends AppCompatActivity {
    TextView kata, min, lupa;
    EditText email, password;
    Button login, register;
    FirebaseAuth firebaseAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();
        kata = findViewById(R.id.silahkan);
        lupa = findViewById(R.id.lupa);
        email = findViewById(R.id.emaillogin);
        password = findViewById(R.id.passlogin);
        login = findViewById(R.id.btnlogin);
        register = findViewById(R.id.btndaftar);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        lupa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.activity_lupa_password, null);
                EditText emailBox = dialogView.findViewById(R.id.emailBox);
                builder.setView(dialogView);
                AlertDialog dialog = builder.create();
                dialogView.findViewById(R.id.btnReset).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String userEmail = emailBox.getText().toString();
                        if (TextUtils.isEmpty(userEmail) && !Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()){
                            Toast.makeText(LoginActivity.this, "Enter your registered email id", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        firebaseAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(LoginActivity.this, "Check your email", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Unable to send, failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
                dialogView.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                if (dialog.getWindow() != null){
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                }
                dialog.show();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mendapatkan nilai input
                String userEmail = email.getText().toString();
                String userPassword = password.getText().toString();

                // Melakukan validasi input
                if (TextUtils.isEmpty(userEmail) || TextUtils.isEmpty(userPassword)) {
                    // Menampilkan pesan jika ada input yang kosong
                    Toast.makeText(LoginActivity.this, "Please complete email and password", Toast.LENGTH_SHORT).show();
                } else {
                    // Melakukan login
                    loginUser(userEmail, userPassword);
                }
            }
        });

    }

    private void loginUser(String userEmail, String userPassword) {
        firebaseAuth.signInWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String userId = firebaseAuth.getCurrentUser().getUid();
                            sendUserIdToServer(userId);
                            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                            firestore.collection("users").document(userId)
                                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    // Mendapatkan token FCM yang baru
                                                    FirebaseMessaging.getInstance().getToken()
                                                            .addOnCompleteListener(new OnCompleteListener<String>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<String> task) {
                                                                    if (!task.isSuccessful()) {
                                                                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                                                                        return;
                                                                    }

                                                                    // Get new FCM registration token
                                                                    String newToken = task.getResult();

                                                                    // Mendapatkan token FCM yang sudah disimpan di Firestore
                                                                    String storedToken = document.getString("tokenFCM");



                                                                    // Update token FCM yang ada di Firestore hanya jika token baru berbeda
                                                                    if (!newToken.equals(storedToken)) {
                                                                        firestore.collection("users").document(userId)
                                                                                .update("tokenFCM", newToken)
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()) {
                                                                                            Log.d(TAG, "FCM token updated successfully");

                                                                                            // Setelah token FCM diperbarui, lanjutkan ke halaman yang sesuai
                                                                                            String tipeAkun = document.getString("tipeAkun");
                                                                                            if (tipeAkun != null && tipeAkun.equals("Owner")) {
                                                                                                Intent intent = new Intent(LoginActivity.this, HomeAdminActivity.class);
                                                                                                startActivity(intent);
                                                                                            } else {
                                                                                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                                                                startActivity(intent);
                                                                                            }
                                                                                        } else {
                                                                                            Log.e(TAG, "Failed to renew FCM token", task.getException());
                                                                                        }
                                                                                    }
                                                                                });
                                                                    } else {
                                                                        // Jika token baru sama dengan token yang sudah ada di Firestore
                                                                        Log.d(TAG, "FCM tokens are pre-updated");

                                                                        // Lanjutkan ke halaman yang sesuai tanpa memperbarui Firestore
                                                                        String tipeAkun = document.getString("tipeAkun");
                                                                        if (tipeAkun != null && tipeAkun.equals("Owner")) {
                                                                            Intent intent = new Intent(LoginActivity.this, HomeAdminActivity.class);
                                                                            startActivity(intent);
                                                                        } else {
                                                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                                            startActivity(intent);
                                                                        }
                                                                    }
                                                                }
                                                            });
                                                } else {
                                                    Toast.makeText(LoginActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                Toast.makeText(LoginActivity.this, "Failed to retrieve user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    // Fungsi untuk mengirimkan userID ke server side
    private void sendUserIdToServer(String userId) {
        Retrofit retrofit = RetrofitClient.getClient("http://147.139.214.76:3000/");
        ApiService apiService = retrofit.create(ApiService.class);
        UserIDData userIDData = new UserIDData(userId);
        Call<Void> call = apiService.sendUserID(userIDData);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "UserID successfully sent to server side");
                    // Lanjutkan dengan proses selanjutnya jika diperlukan
                } else {
                    Log.e(TAG, "Failed to send UserID to server side");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Error occurred: " + t.getMessage());
            }
        });
    }


}
