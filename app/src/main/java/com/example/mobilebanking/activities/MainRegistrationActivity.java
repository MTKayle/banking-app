package com.example.mobilebanking.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mobilebanking.R;
import com.example.mobilebanking.adapters.RegistrationPagerAdapter;
import com.example.mobilebanking.models.RegistrationData;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * Main Registration Activity with 4-step flow using ViewPager2
 */
public class MainRegistrationActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ProgressBar progressBar;
    private TextView tvProgress;
    private RegistrationPagerAdapter adapter;
    private RegistrationData registrationData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_registration);
        
        // Always initialize registration data (Bitmap cannot be serialized, so we don't save/restore)
        // Fragments will get the instance from activity
        if (registrationData == null) {
            registrationData = new RegistrationData();
        }
        
        initializeViews();
        setupViewPager();
    }
    
    private void initializeViews() {
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);
        progressBar = findViewById(R.id.progress_bar);
        tvProgress = findViewById(R.id.tv_progress);
        
        // Disable swipe between pages (only allow programmatic navigation)
        viewPager.setUserInputEnabled(false);
    }
    
    private void setupViewPager() {
        adapter = new RegistrationPagerAdapter(this, registrationData);
        viewPager.setAdapter(adapter);
        
        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            // Tab icons or labels can be set here if needed
        }).attach();
        
        // Update progress indicator
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateProgress(position + 1);
            }
        });
        
        updateProgress(1);
    }
    
    private void updateProgress(int currentStep) {
        int totalSteps = 5; // Updated to 5 steps
        int progress = (currentStep * 100) / totalSteps;
        progressBar.setProgress(progress);
        tvProgress.setText(String.format("%d/%d", currentStep, totalSteps));
    }
    
    /**
     * Navigate to next step
     */
    public void goToNextStep() {
        int currentItem = viewPager.getCurrentItem();
        if (currentItem < adapter.getItemCount() - 1) {
            viewPager.setCurrentItem(currentItem + 1, true);
        }
    }
    
    /**
     * Navigate to previous step
     */
    public void goToPreviousStep() {
        int currentItem = viewPager.getCurrentItem();
        if (currentItem > 0) {
            viewPager.setCurrentItem(currentItem - 1, true);
        }
    }
    
    /**
     * Get registration data
     */
    public RegistrationData getRegistrationData() {
        return registrationData;
    }
    
    /**
     * Complete registration - This will be called after face verification succeeds
     */
    public void completeRegistration() {
        // Registration is completed in Step5FaceVerificationFragment after API call succeeds
        // This method is kept for backward compatibility but may not be used
    }
}

