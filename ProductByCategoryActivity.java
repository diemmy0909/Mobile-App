package com.example.helloworld;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.helloworld.model.Product;
import com.example.helloworld.ApiCaller;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProductByCategoryActivity extends AppCompatActivity {

    private LinearLayout productContainer;
    private static final String TAG = "ProductByCategory";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_by_category);

        productContainer = findViewById(R.id.productByCategoryContainer);

        String category = getIntent().getStringExtra("category");
        if (category == null) {
            Toast.makeText(this, "Không nhận được danh mục!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setTitle("Danh mục: " + category);
        Log.d(TAG, "Category nhận được từ Intent: [" + category + "]");

        loadProductsByCategory(category);
    }

    private void loadProductsByCategory(String category) {
        ApiCaller caller = new ApiCaller(this);
        caller.getItems("/products/getall", null, new ApiCaller.OnRawResponse() {
            @Override
            public void onSuccess(String response) {
                List<Product> filteredList = new ArrayList<>();
                try {
                    JSONArray array = new JSONArray(response);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);

                        JSONObject catObj = obj.getJSONObject("category");
                        String catFromApi = catObj.getString("category");  // hoặc "name" tùy API

                        Log.d(TAG, "→ So sánh: từ API [" + catFromApi + "] vs Intent [" + category + "]");

                        if (!catFromApi.trim().equalsIgnoreCase(category.trim())) continue;

                        int id = obj.getInt("id");
                        String name = obj.getString("name");
                        String description = obj.getString("description");
                        String price = obj.getString("price");
                        int qty = obj.getInt("qty");

                        JSONObject image = obj.getJSONObject("image");
                        String imageName = image.getString("name");
                        String base64 = image.getString("image");

                        Product p = new Product(id, name, description, price, qty, catFromApi, imageName, base64);
                        filteredList.add(p);
                    }

                    Log.d(TAG, "Tổng sản phẩm sau khi lọc: " + filteredList.size());
                    displayProducts(filteredList);
                } catch (Exception e) {
                    Log.e(TAG, "Lỗi xử lý JSON: " + e.getMessage());
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Lỗi API: " + error);
            }
        });
    }

    private void displayProducts(List<Product> products) {
        productContainer.removeAllViews();

        if (products.isEmpty()) {
            Toast.makeText(this, "Không có sản phẩm nào trong danh mục này.", Toast.LENGTH_SHORT).show();
            return;
        }

        LinearLayout row = null;
        int count = 0;

        for (Product p : products) {
            if (count % 2 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                productContainer.addView(row);
            }

            View item = getLayoutInflater().inflate(R.layout.item_product, row, false);

            ((TextView) item.findViewById(R.id.productname)).setText(p.getName());
            ((TextView) item.findViewById(R.id.description)).setText(p.getDescription());
            ((TextView) item.findViewById(R.id.price)).setText(p.getPrice() + " đ");
            ((TextView) item.findViewById(R.id.qty)).setText("SL: " + p.getQuantity());

            ImageView img = item.findViewById(R.id.imgProduct);
            try {
                byte[] bytes = Base64.decode(p.getImageBase64(), Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                img.setImageBitmap(bmp);
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi decode ảnh: " + e.getMessage());
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
            );
            params.setMargins(8, 8, 8, 8);
            item.setLayoutParams(params);

            item.setOnClickListener(v -> {
                Intent intent = new Intent(this, DetailActivity.class);
                intent.putExtra("product", p);
                startActivity(intent);
            });

            row.addView(item);
            count++;
        }
    }
}
