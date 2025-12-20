package com.example.mobilebanking.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.mobilebanking.fragments.CheckingAccountFragment;
import com.example.mobilebanking.fragments.MortgageAccountFragment;
import com.example.mobilebanking.fragments.SavingAccountFragment;

/**
 * Account Pager Adapter
 * Quản lý 3 tabs trong AccountActivity
 */
public class AccountPagerAdapter extends FragmentStateAdapter {
    
    public AccountPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }
    
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new CheckingAccountFragment();
            case 1:
                return new SavingAccountFragment();
            case 2:
                return new MortgageAccountFragment();
            default:
                return new CheckingAccountFragment();
        }
    }
    
    @Override
    public int getItemCount() {
        return 3;
    }
}

