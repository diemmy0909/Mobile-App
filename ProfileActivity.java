package com.example.helloworld;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.helloworld.model.SessionManager;

public class ProfileActivity extends AppCompatActivity {

    private TextView fullNameText, usernameText, emailText, activeText;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        fullNameText = findViewById(R.id.full_name_text);
        usernameText = findViewById(R.id.username_text);
        emailText = findViewById(R.id.email_text);
        activeText = findViewById(R.id.active_text);

        SessionManager sessionManager = new SessionManager(this);

        String firstName = sessionManager.getFirstName();
        String lastName = sessionManager.getLastName();
        String fullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
        String username = sessionManager.getUsername();
        String email = sessionManager.getEmail();
        boolean isActive = sessionManager.isActive();

        fullNameText.setText("Full Name: " + fullName);
        usernameText.setText("Username: " + username);
        emailText.setText("Email: " + email);
        activeText.setText("Active: " + (isActive ? "Yes" : "No"));

        // 👉 Xử lý nút Back
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }
}
