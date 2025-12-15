package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mobilebanking.R;
import com.example.mobilebanking.adapters.CccdCapturePagerAdapter;
import com.example.mobilebanking.models.CccdCaptureData;

/**
 * CCCD Capture Activity with 3-step flow:
 * Step 1: Capture Front Side
 * Step 2: Capture Back Side
 * Step 3: Face Recognition
 */
public class CccdCaptureActivity extends AppCompatActivity {
    public enum CaptureStep {
        FRONT_SIDE(0),
        BACK_SIDE(1),
        FACE(2);
        
        private final int index;
        
        CaptureStep(int index) {
            this.index = index;
        }
        
        public int getIndex() {
            return index;
        }
        
        public static CaptureStep fromIndex(int index) {
            for (CaptureStep step : values()) {
                if (step.index == index) {
                    return step;
                }
            }
            return FRONT_SIDE;
        }
    }
    
    private ViewPager2 viewPager;
    private ProgressBar progressBar;
    private TextView tvProgress;
    private TextView tvStepTitle;
    private TextView tvStepInstruction;
    private Button btnBack;
    private CccdCapturePagerAdapter adapter;
    private CccdCaptureData captureData;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cccd_capture);
        
        // Initialize capture data
        captureData = new CccdCaptureData();
        
        initializeViews();
        setupViewPager();
        updateStepUI(CaptureStep.FRONT_SIDE);
    }
    
    private void initializeViews() {
        viewPager = findViewById(R.id.view_pager);
        progressBar = findViewById(R.id.progress_bar);
        tvProgress = findViewById(R.id.tv_progress);
        tvStepTitle = findViewById(R.id.tv_step_title);
        tvStepInstruction = findViewById(R.id.tv_step_instruction);
        btnBack = findViewById(R.id.btn_back);
        
        // Disable swipe between pages (only allow programmatic navigation)
        viewPager.setUserInputEnabled(false);
        
        btnBack.setOnClickListener(v -> goToPreviousStep());
    }
    
    private void setupViewPager() {
        adapter = new CccdCapturePagerAdapter(this, captureData);
        viewPager.setAdapter(adapter);
        
        // Update progress indicator
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                CaptureStep step = CaptureStep.fromIndex(position);
                updateStepUI(step);
            }
        });
    }
    
    private void updateStepUI(CaptureStep step) {
        int currentStep = step.getIndex() + 1;
        int totalSteps = 3;
        int progress = (currentStep * 100) / totalSteps;
        
        progressBar.setProgress(progress);
        tvProgress.setText(String.format("Bước %d/%d", currentStep, totalSteps));
        
        switch (step) {
            case FRONT_SIDE:
                tvStepTitle.setText("Chụp mặt trước CCCD");
                tvStepInstruction.setText("Đặt mặt trước CCCD vào khung và giữ yên. Ảnh sẽ được chụp tự động khi phát hiện CCCD.");
                btnBack.setVisibility(View.GONE);
                break;
            case BACK_SIDE:
                tvStepTitle.setText("Chụp mặt sau CCCD");
                tvStepInstruction.setText("Đặt mặt sau CCCD vào khung và giữ yên. Ảnh sẽ được chụp tự động khi phát hiện CCCD.");
                btnBack.setVisibility(View.VISIBLE);
                break;
            case FACE:
                tvStepTitle.setText("Nhận diện khuôn mặt");
                tvStepInstruction.setText("Nhìn thẳng vào camera và giữ yên. Ảnh sẽ được chụp tự động khi phát hiện khuôn mặt rõ nét.");
                btnBack.setVisibility(View.VISIBLE);
                break;
        }
    }
    
    /**
     * Navigate to next step
     */
    public void goToNextStep() {
        android.util.Log.d("CccdCaptureActivity", "goToNextStep() called");
        int currentItem = viewPager.getCurrentItem();
        android.util.Log.d("CccdCaptureActivity", "Current item: " + currentItem + ", Total items: " + adapter.getItemCount());
        
        if (currentItem < adapter.getItemCount() - 1) {
            android.util.Log.d("CccdCaptureActivity", "Moving to next step: " + (currentItem + 1));
            viewPager.setCurrentItem(currentItem + 1, true);
        } else {
            android.util.Log.d("CccdCaptureActivity", "All steps completed, calling completeCapture()");
            // All steps completed
            completeCapture();
        }
    }
    
    /**
     * Navigate to previous step
     */
    public void goToPreviousStep() {
        int currentItem = viewPager.getCurrentItem();
        if (currentItem > 0) {
            viewPager.setCurrentItem(currentItem - 1, true);
        } else {
            finish();
        }
    }
    
    /**
     * Get capture data
     */
    public CccdCaptureData getCaptureData() {
        return captureData;
    }
    
    /**
     * Complete capture and return result
     */
    public void completeCapture() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("front_image_path", captureData.getFrontImagePath());
        resultIntent.putExtra("back_image_path", captureData.getBackImagePath());
        resultIntent.putExtra("face_image_path", captureData.getFaceImagePath());
        setResult(RESULT_OK, resultIntent);
        finish();
    }
    
    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() > 0) {
            goToPreviousStep();
        } else {
            super.onBackPressed();
        }
    }
}

