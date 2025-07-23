package com.example.helloworld;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.helloworld.fragments.CartFragment;
import com.example.helloworld.fragments.HomeFragment;
import com.example.helloworld.fragments.SearchFragment;
import com.example.helloworld.fragments.SettingFragment;
import com.example.helloworld.fragments.WishlistFragment;

public class TablayoutAdapter extends FragmentStateAdapter {
    public TablayoutAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new HomeFragment();
            case 1:
                return new WishlistFragment();
            case 2:
                return new CartFragment();
            case 3:
                return new SearchFragment()    ;
            case 4:
                return new SettingFragment();
            default:
                return new HomeFragment();
        }
    }
    @Override
    public int getItemCount() {
        return 5;
    }
}