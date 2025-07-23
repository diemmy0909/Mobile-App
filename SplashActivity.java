package com.example.helloworld;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 1500; // 1.5 giây

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        // Tự động chuyển sau thời gian chờ
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                goToNext();
            }
        }, SPLASH_DELAY);
    }

    private void goToNext() {
        Intent intent = new Intent(SplashActivity.this, SplashActivity1.class);
        startActivity(intent);
        finish(); // Không cho quay lại SplashActivity
    }
}
