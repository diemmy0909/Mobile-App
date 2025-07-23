package com.example.helloworld;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

// ✅ Thêm import từ ảnh:
import android.util.Log;

import com.example.helloworld.model.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

public class SignInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);

        // ✅ Đồng bộ id
        TextView signUp = findViewById(R.id.sign_up);
        TextView btn_login = findViewById(R.id.btn_login);
        EditText email_id = findViewById(R.id.emailId);
        EditText password = findViewById(R.id.password);
        TextView forgotpw = findViewById(R.id.forgotpw);

        ApiCaller apiCaller = new ApiCaller(this);

        forgotpw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, ForGetActivity.class);
                startActivity(intent);
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = email_id.getText().toString().trim();
                String pass = password.getText().toString().trim();

                if (email.isEmpty()) {
                    Toast.makeText(SignInActivity.this, "Bạn chưa nhập Email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (pass.isEmpty()) {
                    Toast.makeText(SignInActivity.this, "Bạn chưa nhập Password", Toast.LENGTH_SHORT).show();
                    return;
                }

                apiCaller.Login(email, pass, new ApiCaller.OnRawResponse() {
                    @Override
                    public void onSuccess(String response) {
                        JSONObject json = null;
                        try {
                            json = new JSONObject(response);
                            SessionManager.userId = json.getInt("id");
                            SessionManager.email = json.getString("emailId");
                            SessionManager.password = json.getString("password");
                            Toast.makeText(SignInActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } catch (JSONException e) {
                            Toast.makeText(SignInActivity.this, "Lỗi hệ thống!", Toast.LENGTH_SHORT).show();
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(SignInActivity.this, "Tài khoản không tồn tại hoặc sai mật khẩu!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }
}
