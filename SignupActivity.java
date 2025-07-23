package com.example.helloworld;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.helloworld.model.User;

public class SignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        // Khai báo các view
        TextView sign_up = findViewById(R.id.sign_up);
        TextView tvLogin = findViewById(R.id.tvLogin); // 👉 Thêm dòng này
        EditText emailID = findViewById(R.id.emailId);
        EditText password = findViewById(R.id.password);
        EditText confirmpassword = findViewById(R.id.confirmpassword);

        ApiCaller apiCaller = new ApiCaller(this);

        // 👉 Xử lý khi nhấn "Login"
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, SignInActivity.class);
                startActivity(intent);
                finish(); // Đóng trang đăng ký nếu muốn
            }
        });

        // 👉 Xử lý khi nhấn nút "sign_up" để đăng ký
        sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailID.getText().toString().trim();
                String pass = password.getText().toString().trim();
                String confirm = confirmpassword.getText().toString().trim();

                if (email.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "Bạn cần nhập Email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (pass.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "Password không được rỗng", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (confirm.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "Confirm password không được rỗng", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!pass.equals(confirm)) {
                    Toast.makeText(SignupActivity.this, "Passwords không trùng khớp", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Tạo user mới
                User user = new User();
                user.setEmailId(email);
                user.setPassword(pass);
                user.setActive(true);

                // Gửi đăng ký đến server
                apiCaller.registerUser(user, new ApiCaller.OnRawResponse() {
                    @Override
                    public void onSuccess(String response) {
                        Toast.makeText(SignupActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                        finish(); // Quay lại màn hình trước
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(SignupActivity.this, "Đăng ký thất bại: " + error, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}
