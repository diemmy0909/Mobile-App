package com.example.helloworld;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private double totalPrice = 0;
    private LinearLayout cartContainer;
    private TextView totalPriceView;
    private TextView btnPay;
    private LayoutInflater inflater;
    private ApiCaller apiCaller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        inflater = LayoutInflater.from(this);
        cartContainer = findViewById(R.id.cartcontainer);
        totalPriceView = findViewById(R.id.totalprice);
        btnPay = findViewById(R.id.btn_pay);
        apiCaller = new ApiCaller(this);

        getCart(cartList -> {
            cartContainer.removeAllViews();
            totalPrice = 0;

            for (Cart cart : cartList) {
                View itemView = inflater.inflate(R.layout.item_cart, cartContainer, false);

                ImageView imgProduct = itemView.findViewById(R.id.imgProduct);
                TextView productName = itemView.findViewById(R.id.productname);
                TextView productPrice = itemView.findViewById(R.id.price);
                TextView productQty = itemView.findViewById(R.id.qty);
                TextView totalItemPrice = itemView.findViewById(R.id.totalprice);
                ImageView btnDelete = itemView.findViewById(R.id.delete);

                Product product = cart.getProduct();
                double price = product.getPriceAsDouble(); // Đã chuyển kiểu đúng
                int qty = cart.getQty();

                Bitmap bitmap = convertImage(product.getImageBase64());
                if (bitmap != null) {
                    imgProduct.setImageBitmap(bitmap);
                }

                NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));

                productName.setText(product.getName());
                productPrice.setText(formatter.format(price) + " đ");
                productQty.setText("Số lượng: " + qty);
                totalItemPrice.setText("Thành tiền: " + formatter.format(price * qty) + " đ");

                btnDelete.setOnClickListener(view -> {
                    new AlertDialog.Builder(view.getContext())
                            .setTitle("Xác nhận xóa")
                            .setMessage("Bạn có chắc chắn muốn xóa mục này không?")
                            .setPositiveButton("Xóa", (dialog, which) -> {
                                apiCaller.deleteItemByEndpoint("/cart_items/remove/" + cart.getId(), new ApiCaller.OnRawResponse() {
                                    @Override
                                    public void onSuccess(String response) {
                                        Toast.makeText(view.getContext(), "Đã xóa", Toast.LENGTH_SHORT).show();
                                        recreate();
                                    }

                                    @Override
                                    public void onError(String error) {
                                        Toast.makeText(view.getContext(), "Lỗi khi xóa sản phẩm!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            })
                            .setNegativeButton("Hủy", null)
                            .show();
                });

                cartContainer.addView(itemView);
                totalPrice += price * qty;
            }

            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            totalPriceView.setText(formatter.format(totalPrice) + " đ");
        });

        btnPay.setOnClickListener(view -> {
            if (totalPrice == 0) {
                Toast.makeText(this, "Giỏ hàng đang rỗng!", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(this, PaymentActivity.class));
                finish();
            }
        });
    }

    public interface OnCartLoaded {
        void onLoaded(List<Cart> cartList);
    }

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
                                    double price = productObj.getDouble("price");
                                    int productQty = productObj.getInt("qty");

                                    JSONObject imageObj = productObj.getJSONObject("image");
                                    String imageName = imageObj.getString("name");
                                    String imageBase64 = imageObj.getString("image");

                                    String priceStr = String.valueOf(price); // Chuyển đổi từ double → String

                                    Product product = new Product(productId, name, description, priceStr, productQty, "unknown", imageName, imageBase64);
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

    public Bitmap convertImage(String base64String) {
        if (base64String == null || base64String.isEmpty()) return null;
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            Log.e("ConvertImage", "Decode error: " + e.getMessage());
            return null;
        }
    }
}
