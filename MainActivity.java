package com.example.helloworld;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager2;
    private TabLayout tabLayout;
    private TablayoutAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Khởi tạo ViewPager2 và TabLayout
        viewPager2 = findViewById(R.id.viewpaper2);
        tabLayout = findViewById(R.id.tablayout);

        // Gắn adapter
        adapter = new TablayoutAdapter(this);
        viewPager2.setAdapter(adapter);

        // Gắn TabLayoutMediator để đồng bộ tab với ViewPager2
        new TabLayoutMediator(tabLayout, viewPager2,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        switch (position) {
                            case 0:
                                tab.setText("Home");
                                tab.setIcon(R.drawable.home_1);

                                break;
                            case 1:
                                tab.setText("Wishlist");
                                tab.setIcon(R.drawable.heart_1);

                                break;
                            case 2:
                                tab.setText("Cart");
                                tab.setIcon(R.drawable.cart_shop);

                                break;
                            case 3:
                                tab.setText("Search");
                                tab.setIcon(R.drawable.search_1);
                                break;
                            case 4:
                                tab.setText("Setting");
                                tab.setIcon(R.drawable.settings);

                                break;
                        }
                    }
                }).attach();
    }public void goToProfile(View view) {
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

}