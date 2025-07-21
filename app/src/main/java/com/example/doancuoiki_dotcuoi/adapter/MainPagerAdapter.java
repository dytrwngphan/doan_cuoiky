package com.example.doancuoiki_dotcuoi.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.doancuoiki_dotcuoi.fragment.CartFragment;
import com.example.doancuoiki_dotcuoi.fragment.HomeFragment;
import com.example.doancuoiki_dotcuoi.fragment.ProfileFragment;
import com.example.doancuoiki_dotcuoi.fragment.ChatListFragment;
import com.example.doancuoiki_dotcuoi.fragment.AddEditProductFragment; // Nhớ import fragment mới

public class MainPagerAdapter extends FragmentStateAdapter {
    public MainPagerAdapter(@NonNull FragmentActivity fragmentActivity) { super(fragmentActivity); }
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new HomeFragment();
            case 1: return new CartFragment();
            case 2: return new AddEditProductFragment();
            case 3: return new ChatListFragment();// Thêm mới
            case 4: return new ProfileFragment();
            default: return new HomeFragment();
        }
    }
    @Override
    public int getItemCount() { return 5; }
}
