package com.example.helloworld;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.helloworld.model.Product;
import com.example.helloworld.model.SessionManager;

public class DetailActivity extends AppCompatActivity {

    private int quantity = 1;
    private int maxStock = 1;
    private int productId = 0;

    private TextView txtQuantity;
    private Button btnIncrease, btnDecrease;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail);

        ApiCaller apiCaller = new ApiCaller(this);

        // Nhận dữ liệu sản phẩm
        Product product = (Product) getIntent().getSerializableExtra("product");

        if (product != null) {
            TextView name = findViewById(R.id.detail_name);
            TextView desc = findViewById(R.id.detail_description);
            TextView price = findViewById(R.id.detail_price);
            TextView qty = findViewById(R.id.detail_qty);
            ImageView img = findViewById(R.id.detail_image);

            name.setText(product.getName());
            desc.setText(product.getDescription());
            price.setText(product.getPrice() + " đ");
            qty.setText("Kho: " + product.getQuantity());

            maxStock = product.getQuantity();
            productId = product.getId();

            Bitmap bitmap = decodeBase64(product.getImageBase64());
            if (bitmap != null) img.setImageBitmap(bitmap);
        }

        // Xử lý chọn số lượng
        txtQuantity = findViewById(R.id.txt_quantity);
        btnIncrease = findViewById(R.id.btn_increase);
        btnDecrease = findViewById(R.id.btn_decrease);

        txtQuantity.setText(String.valueOf(quantity));

        btnIncrease.setOnClickListener(v -> {
            if (quantity < maxStock) {
                quantity++;
                txtQuantity.setText(String.valueOf(quantity));
            } else {
                Toast.makeText(this, "Vượt quá số lượng trong kho", Toast.LENGTH_SHORT).show();
            }
        });

        btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                txtQuantity.setText(String.valueOf(quantity));
            }
        });

        // Xử lý thêm vào giỏ hàng
        Button btnAddToCart = findViewById(R.id.btn_add_to_cart);
        btnAddToCart.setOnClickListener(v -> {
            int uid = SessionManager.userId;
            int pid = productId;
            int qty = quantity;

            apiCaller.addToCart(uid, pid, qty, new ApiCaller.OnRawResponse() {
                @Override
                public void onSuccess(String response) {
                    Log.d("AddToCart", "Response: " + response);
                    Toast.makeText(DetailActivity.this, "Đã thêm " + qty + " sản phẩm vào giỏ hàng", Toast.LENGTH_SHORT).show();

                    // 👉 Chuyển sang CartActivity sau khi thêm thành công
                    Intent intent = new Intent(DetailActivity.this, CartActivity.class);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onError(String error) {
                    Log.e("AddToCart", "Lỗi: " + error);
                    Toast.makeText(DetailActivity.this, "Không thể thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private Bitmap decodeBase64(String base64String) {
        try {
            byte[] bytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) {
            Log.e("DetailActivity", "Lỗi giải mã ảnh: " + e.getMessage());
            return null;
        }
    }
}
