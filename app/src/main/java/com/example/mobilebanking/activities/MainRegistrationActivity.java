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
    private RegistrationPagerAdapter adapter;
    private RegistrationData registrationData;
    private boolean isNavigating = false; // Prevent multiple navigation calls
    private boolean isFromOfficer = false; // Flag để biết đang mở từ Officer hay User

    // Step indicator views (4 steps now)
    private TextView stepCircle1, stepCircle2, stepCircle3, stepCircle4;
    private View line1_2, line2_3, line3_4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_registration);
        
        // Kiểm tra xem có mở từ Officer không
        isFromOfficer = getIntent().getBooleanExtra(OfficerOpenAccountActivity.EXTRA_FROM_OFFICER, false);
        android.util.Log.d("MainRegistrationActivity", "isFromOfficer: " + isFromOfficer);
        
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

        stepCircle1 = findViewById(R.id.step_circle_1);
        stepCircle2 = findViewById(R.id.step_circle_2);
        stepCircle3 = findViewById(R.id.step_circle_3);
        stepCircle4 = findViewById(R.id.step_circle_4);

        line1_2 = findViewById(R.id.step_line_1_2);
        line2_3 = findViewById(R.id.step_line_2_3);
        line3_4 = findViewById(R.id.step_line_3_4);
        
        // Disable swipe between pages (only allow programmatic navigation)
        viewPager.setUserInputEnabled(false);
    }
    
    private void setupViewPager() {
        adapter = new RegistrationPagerAdapter(this, registrationData);
        viewPager.setAdapter(adapter);
        
        // Set offscreen page limit to 1 to prevent pre-loading too many fragments
        // Minimum value is 1, cannot be 0
        viewPager.setOffscreenPageLimit(1);
        
        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            // Tab icons or labels can be set here if needed
        }).attach();
        
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                android.util.Log.d("MainRegistrationActivity", "onPageSelected called with position: " + position);
                isNavigating = false; // Reset flag when page selection completes
                updateStepIndicator(position);
            }
        });
        
        updateStepIndicator(0);
    }

    private void updateStepIndicator(int position) {
        // position: 0-based index của STEP HIỆN TẠI
        // Chỉ tô xanh cho các bước ĐÃ HOÀN THÀNH (index < position)
        // 4 steps: Basic Info -> QR Scan -> Front+Back CCCD -> Face Verification
        setStepState(stepCircle1, line1_2, position > 0); // Step 1 xanh khi đã sang Step 2
        setStepState(stepCircle2, line2_3, position > 1); // Step 2 xanh khi đã sang Step 3
        setStepState(stepCircle3, line3_4, position > 2); // Step 3 xanh khi đã sang Step 4
        // Step 4 (Face Verification) - no line after it
        setCircleState(stepCircle4, position > 2);
    }

    private void setStepState(TextView circle, View lineToNext, boolean active) {
        setCircleState(circle, active);
        if (lineToNext != null) {
            lineToNext.setBackgroundColor(active ? 0xFF0F5613 : 0x80FFFFFF);
        }
    }

    private void setCircleState(TextView circle, boolean active) {
        if (circle == null) return;
        circle.setBackgroundResource(active ? R.drawable.step_circle_active : R.drawable.step_circle_inactive);
        circle.setTextColor(0xFFFFFFFF);
    }
    
    /**
     * Navigate to next step
     */
    public void goToNextStep() {
        if (isNavigating) {
            android.util.Log.w("MainRegistrationActivity", "Navigation already in progress, ignoring duplicate call");
            return;
        }
        
        int currentItem = viewPager.getCurrentItem();
        int nextItem = currentItem + 1;
        int itemCount = adapter.getItemCount();
        
        android.util.Log.d("MainRegistrationActivity", "goToNextStep called. Current: " + currentItem + ", Next: " + nextItem + ", Total: " + itemCount);
        
        // Validate next item is within bounds
        if (nextItem >= itemCount) {
            android.util.Log.w("MainRegistrationActivity", "Next item " + nextItem + " exceeds item count " + itemCount);
            return;
        }
        
        if (currentItem < itemCount - 1) {
            isNavigating = true;
            // Use false for smooth scroll to avoid potential issues with fragment lifecycle
            viewPager.setCurrentItem(nextItem, false);
            android.util.Log.d("MainRegistrationActivity", "ViewPager setCurrentItem to: " + nextItem + " (no smooth scroll)");
            
            // Verify the change took effect
            viewPager.post(() -> {
                int actualItem = viewPager.getCurrentItem();
                android.util.Log.d("MainRegistrationActivity", "After setCurrentItem, actual position is: " + actualItem);
                if (actualItem != nextItem) {
                    android.util.Log.e("MainRegistrationActivity", "ERROR: ViewPager position mismatch! Expected: " + nextItem + ", Actual: " + actualItem);
                    // Force set again if mismatch
                    viewPager.setCurrentItem(nextItem, false);
                }
                isNavigating = false;
            });
        } else {
            android.util.Log.d("MainRegistrationActivity", "Already at last step, cannot go next");
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
     * Check if registration is opened from Officer
     */
    public boolean isFromOfficer() {
        return isFromOfficer;
    }
    
    /**
     * Complete registration - This will be called after face verification succeeds
     */
    public void completeRegistration() {
        // Registration is completed in Step5FaceVerificationFragment after API call succeeds
        // This method is kept for backward compatibility but may not be used
    }
}

