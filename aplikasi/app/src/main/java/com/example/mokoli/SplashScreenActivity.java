package com.example.mokoli;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 3000;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        // Inisialisasi ImageView
        imageView = findViewById(R.id.splashview);

        // Atur gambar di ImageView
        imageView.setImageResource(R.drawable.splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Intent untuk berpindah ke RegisterActivity
                Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
                startActivity(intent);

                // Tutup activity saat ini agar tidak dapat dikembalikan lagi
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}

