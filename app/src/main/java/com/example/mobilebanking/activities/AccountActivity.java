package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mobilebanking.R;
import com.example.mobilebanking.adapters.AccountPagerAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * Account Activity - BIDV Style
 * Hiển thị 3 tabs: Thanh toán, Tiết kiệm, Tiền vay
 */
public class AccountActivity extends BaseActivity {
    
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private AccountPagerAdapter adapter;
    private MaterialToolbar toolbar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        
        initViews();
        setupToolbar();
        setupViewPager();
        setupTabs();
        
        // Check if we need to navigate to a specific tab
        handleTabNavigation();
    }
    
    /**
     * Handle navigation to specific tab from Intent
     */
    private void handleTabNavigation() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("TAB_INDEX")) {
            int tabIndex = intent.getIntExtra("TAB_INDEX", 0);
            // Validate tab index
            if (tabIndex >= 0 && tabIndex < 3) {
                viewPager.setCurrentItem(tabIndex, false);
            }
        }
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    private void setupViewPager() {
        adapter = new AccountPagerAdapter(this);
        viewPager.setAdapter(adapter);
    }
    
    private void setupTabs() {
        new TabLayoutMediator(tabLayout, viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        switch (position) {
                            case 0:
                                tab.setText("Thanh toán");
                                break;
                            case 1:
                                tab.setText("Tiết kiệm");
                                break;
                            case 2:
                                tab.setText("Tiền vay");
                                break;
                        }
                    }
                }
        ).attach();
    }
}

