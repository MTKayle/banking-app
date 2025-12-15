package com.example.mobilebanking.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mobilebanking.R;
import com.example.mobilebanking.activities.CccdAutoScannerActivity;
import com.example.mobilebanking.activities.CccdCaptureActivity;
import com.example.mobilebanking.models.CccdCaptureData;

/**
 * Fragment for capturing front side of CCCD
 */
public class CccdFrontCaptureFragment extends Fragment {
    private static final String TAG = "CccdFrontCapture";
    private static final int REQUEST_AUTO_SCAN_FRONT = 500;
    
    private CccdCaptureData captureData;
    
    private ImageView ivPreview;
    private TextView tvStatus;
    private Button btnCapture;
    private Button btnContinue;
    private Button btnRetake;
    private ProgressBar progressBar;
    
    public static CccdFrontCaptureFragment newInstance(CccdCaptureData data) {
        CccdFrontCaptureFragment fragment = new CccdFrontCaptureFragment();
        fragment.captureData = data;
        return fragment;
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cccd_capture, container, false);
        
        // Get captureData from activity if not set
        if (captureData == null && getActivity() instanceof CccdCaptureActivity) {
            captureData = ((CccdCaptureActivity) getActivity()).getCaptureData();
            Log.d(TAG, "Retrieved captureData from activity");
        }
        
        initializeViews(view);
        setupListeners();
        loadData();
        
        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment resumes
        if (captureData != null) {
            Log.d(TAG, "onResume - Front image: " + (captureData.getFrontImage() != null ? "exists" : "null"));
            Log.d(TAG, "onResume - Front image path: " + captureData.getFrontImagePath());
            loadData();
        }
    }
    
    private void initializeViews(View view) {
        ivPreview = view.findViewById(R.id.iv_preview);
        tvStatus = view.findViewById(R.id.tv_status);
        btnCapture = view.findViewById(R.id.btn_capture);
        btnContinue = view.findViewById(R.id.btn_continue);
        btnRetake = view.findViewById(R.id.btn_retake);
        progressBar = view.findViewById(R.id.progress_bar);
        
        // Update UI text
        btnCapture.setText("Quét CCCD Mặt Trước");
        tvStatus.setText("Nhấn nút để bắt đầu quét mặt trước CCCD");
    }
    
    private void setupListeners() {
        btnCapture.setOnClickListener(v -> startAutoScan());
        btnContinue.setOnClickListener(v -> continueToNextStep());
        btnRetake.setOnClickListener(v -> retakeImage());
    }
    
    private void loadData() {
        if (captureData.getFrontImage() != null) {
            // Already captured
            showPreview(captureData.getFrontImage());
        } else {
            // Not captured yet
            showCaptureState();
        }
    }
    
    private void startAutoScan() {
        Intent intent = new Intent(getActivity(), CccdAutoScannerActivity.class);
        startActivityForResult(intent, REQUEST_AUTO_SCAN_FRONT);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_AUTO_SCAN_FRONT && resultCode == android.app.Activity.RESULT_OK && data != null) {
            String imagePath = data.getStringExtra("front_image_path");
            String portraitPath = data.getStringExtra("portrait_path");
            
            Log.d(TAG, "Received result - front_image_path: " + imagePath);
            Log.d(TAG, "Received result - portrait_path: " + portraitPath);
            
            if (imagePath != null) {
                Bitmap frontImage = BitmapFactory.decodeFile(imagePath);
                if (frontImage != null) {
                    captureData.setFrontImage(frontImage);
                    captureData.setFrontImagePath(imagePath);
                    
                    Log.d(TAG, "Front image loaded and set to captureData. Size: " + frontImage.getWidth() + "x" + frontImage.getHeight());
                    
                    // Store portrait silently (for backend comparison)
                    if (portraitPath != null) {
                        captureData.setPortraitPath(portraitPath);
                        Bitmap portrait = BitmapFactory.decodeFile(portraitPath);
                        if (portrait != null) {
                            captureData.setPortraitImage(portrait);
                            Log.d(TAG, "Portrait extracted and stored: " + portraitPath);
                        }
                    }
                    
                    showPreview(frontImage);
                    Toast.makeText(getActivity(), "Đã chụp ảnh mặt trước CCCD thành công!", Toast.LENGTH_LONG).show();
                } else {
                    Log.e(TAG, "Failed to decode front image from path: " + imagePath);
                    Toast.makeText(getActivity(), "Không thể tải ảnh đã chụp", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "front_image_path is null in result");
                Toast.makeText(getActivity(), "Không nhận được ảnh từ scanner", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "Result not OK or data is null. resultCode: " + resultCode);
        }
    }
    
    private void showPreview(Bitmap image) {
        Log.d(TAG, "showPreview() called with image: " + (image != null ? image.getWidth() + "x" + image.getHeight() : "null"));
        ivPreview.setImageBitmap(image);
        ivPreview.setVisibility(View.VISIBLE);
        btnCapture.setVisibility(View.GONE);
        btnContinue.setVisibility(View.VISIBLE);
        btnRetake.setVisibility(View.VISIBLE);
        tvStatus.setText("Ảnh mặt trước đã được chụp. Nhấn 'Tiếp tục' để chuyển sang bước tiếp theo.");
        
        // Verify data is set
        Log.d(TAG, "After showPreview - Front image in captureData: " + (captureData.getFrontImage() != null ? "exists" : "null"));
        Log.d(TAG, "After showPreview - Front image path: " + captureData.getFrontImagePath());
    }
    
    private void showCaptureState() {
        ivPreview.setVisibility(View.GONE);
        btnCapture.setVisibility(View.VISIBLE);
        btnContinue.setVisibility(View.GONE);
        btnRetake.setVisibility(View.GONE);
        tvStatus.setText("Nhấn nút để bắt đầu quét mặt trước CCCD");
    }
    
    private void retakeImage() {
        captureData.setFrontImage(null);
        captureData.setFrontImagePath(null);
        showCaptureState();
    }
    
    private void continueToNextStep() {
        Log.d(TAG, "continueToNextStep() called");
        Log.d(TAG, "Front image: " + (captureData.getFrontImage() != null ? "exists" : "null"));
        Log.d(TAG, "Front image path: " + captureData.getFrontImagePath());
        
        // Check both image and path to ensure data is set
        if (captureData.getFrontImage() == null && captureData.getFrontImagePath() == null) {
            Log.e(TAG, "Both front image and path are null!");
            Toast.makeText(getActivity(), "Vui lòng chụp ảnh mặt trước CCCD trước", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // If image is null but path exists, try to load it
        if (captureData.getFrontImage() == null && captureData.getFrontImagePath() != null) {
            Log.d(TAG, "Loading front image from path: " + captureData.getFrontImagePath());
            Bitmap frontImage = BitmapFactory.decodeFile(captureData.getFrontImagePath());
            if (frontImage != null) {
                captureData.setFrontImage(frontImage);
                Log.d(TAG, "Front image loaded successfully");
            } else {
                Log.e(TAG, "Failed to load front image from path");
                Toast.makeText(getActivity(), "Không thể tải ảnh đã chụp. Vui lòng chụp lại.", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        
        // Navigate to next step
        if (getActivity() instanceof CccdCaptureActivity) {
            Log.d(TAG, "Calling goToNextStep() on CccdCaptureActivity");
            ((CccdCaptureActivity) getActivity()).goToNextStep();
        } else {
            Log.e(TAG, "Activity is not instance of CccdCaptureActivity: " + (getActivity() != null ? getActivity().getClass().getName() : "null"));
        }
    }
}

