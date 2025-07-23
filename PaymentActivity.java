package com.example.helloworld;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.helloworld.model.Cart;
import com.example.helloworld.model.Product;
import com.example.helloworld.model.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PaymentActivity extends AppCompatActivity {

    private ApiCaller apiCaller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment);

        // Fix layout padding với status/navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        apiCaller = new ApiCaller(this);

        // Lấy giỏ hàng và xử lý thanh toán
        getCart(new OnCartLoaded() {
            @Override
            public void onLoaded(List<Cart> cartList) {
                if (cartList.isEmpty()) {
                    Toast.makeText(PaymentActivity.this, "Không có sản phẩm trong giỏ!", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                final int total = cartList.size();
                final int[] deletedCount = {0};

                // Xoá từng item trong giỏ
                for (Cart p : cartList) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        apiCaller.deleteItemByEndpoint("/cart_items/remove/" + p.getId(), new ApiCaller.OnRawResponse() {
                            @Override
                            public void onSuccess(String response) {
                                deletedCount[0]++;
                                if (deletedCount[0] == total) {
                                    Toast.makeText(PaymentActivity.this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                        startActivity(new Intent(PaymentActivity.this, SuccessActivity.class));
                                        finish();
                                    }, 1000);
                                }
                            }

                            @Override
                            public void onError(String error) {
                                Toast.makeText(PaymentActivity.this, "Lỗi khi xóa sản phẩm!", Toast.LENGTH_SHORT).show();
                                Log.e("PAYMENT", "API error: " + error);
                            }
                        });
                    }, 500);
                }
            }
        });
    }

    // Callback load cart
    public interface OnCartLoaded {
        void onLoaded(List<Cart> cartList);
    }

    // Lấy giỏ hàng từ API
    public void getCart(OnCartLoaded callback) {
        apiCaller.getItems("/cart/get/" + SessionManager.userId, null, new ApiCaller.OnRawResponse() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject cartObj = new JSONObject(response);
                    int cartId = cartObj.getInt("id");

                    apiCaller.getItems("/cart_items/get/" + cartId, null, new ApiCaller.OnRawResponse() {
                        @Override
                        public void onSuccess(String response2) {
                            List<Cart> cartList = new ArrayList<>();
                            try {
                                JSONArray jsonArray = new JSONArray(response2);
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject obj = jsonArray.getJSONObject(i);
                                    int cartItemId = obj.getInt("id");
                                    int qty = obj.getInt("qty");

                                    JSONObject productObj = obj.getJSONObject("product");
                                    int productId = productObj.getInt("id");
                                    String name = productObj.getString("name");
                                    String description = productObj.getString("description");
                                    int price = productObj.getInt("price");
                                    int productQty = productObj.getInt("qty");

                                    JSONObject imageObj = productObj.getJSONObject("image");
                                    String imageName = imageObj.getString("name");
                                    String imageBase64 = imageObj.getString("image");

                                    // CHỈNH Ở ĐÂY: Chuyển price -> String
                                    Product product = new Product(
                                            productId,
                                            name,
                                            description,
                                            String.valueOf(price),  // Sửa từ (double) price → String
                                            productQty,
                                            "unknown",
                                            imageName,
                                            imageBase64
                                    );

                                    Cart cart = new Cart(cartItemId, qty, product);
                                    cartList.add(cart);
                                }

                                callback.onLoaded(cartList);

                            } catch (JSONException e) {
                                Log.e("CART", "JSON parse error: " + e.getMessage());
                            }
                        }

                        @Override
                        public void onError(String error) {
                            Log.e("CART_ITEM", "API error (cart_items): " + error);
                        }
                    });

                } catch (JSONException e) {
                    Log.e("CART", "JSON error (cart): " + e.getMessage());
                }
            }

            @Override
            public void onError(String error) {
                Log.e("CART", "API error (cart): " + error);
            }
        });
    }
}
