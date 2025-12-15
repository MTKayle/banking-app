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
import com.example.mobilebanking.activities.BiometricAuthActivity;
import com.example.mobilebanking.activities.CccdBackScannerActivity;
import com.example.mobilebanking.activities.CccdCaptureActivity;
import com.example.mobilebanking.models.CccdCaptureData;

/**
 * Fragment for capturing back side of CCCD
 */
public class CccdBackCaptureFragment extends Fragment {
    private static final String TAG = "CccdBackCapture";
    private static final int REQUEST_AUTO_SCAN_BACK = 501;
    private static final int REQUEST_FACE_CAPTURE = 502;
    
    private CccdCaptureData captureData;
    
    private ImageView ivPreview;
    private TextView tvStatus;
    private Button btnCapture;
    private Button btnContinue;
    private Button btnRetake;
    private ProgressBar progressBar;
    
    public static CccdBackCaptureFragment newInstance(CccdCaptureData data) {
        CccdBackCaptureFragment fragment = new CccdBackCaptureFragment();
        fragment.captureData = data;
        return fragment;
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cccd_capture, container, false);
        
        initializeViews(view);
        setupListeners();
        loadData();
        
        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Auto-start camera when fragment becomes visible if not captured yet
        if (captureData.getBackImage() == null && btnCapture.getVisibility() == View.VISIBLE) {
            // Small delay to ensure UI is ready
            btnCapture.postDelayed(() -> {
                if (captureData.getBackImage() == null) {
                    startAutoScan();
                }
            }, 300);
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
        btnCapture.setText("Quét CCCD Mặt Sau");
        tvStatus.setText("Nhấn nút để bắt đầu quét mặt sau CCCD");
    }
    
    private void setupListeners() {
        btnCapture.setOnClickListener(v -> startAutoScan());
        btnContinue.setOnClickListener(v -> continueToNextStep());
        btnRetake.setOnClickListener(v -> retakeImage());
    }
    
    private void loadData() {
        if (captureData.getBackImage() != null) {
            // Already captured
            showPreview(captureData.getBackImage());
        } else {
            // Not captured yet
            showCaptureState();
        }
    }
    
    private void startAutoScan() {
        Intent intent = new Intent(getActivity(), CccdBackScannerActivity.class);
        startActivityForResult(intent, REQUEST_AUTO_SCAN_BACK);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_AUTO_SCAN_BACK && resultCode == android.app.Activity.RESULT_OK && data != null) {
            String imagePath = data.getStringExtra("back_image_path");
            
            if (imagePath != null) {
                Bitmap backImage = BitmapFactory.decodeFile(imagePath);
                if (backImage != null) {
                    captureData.setBackImage(backImage);
                    captureData.setBackImagePath(imagePath);
                    
                    Log.d(TAG, "Back image captured successfully. Auto-starting face recognition...");
                    
                    // Auto-start face recognition after back scan
                    startFaceCapture();
                } else {
                    Toast.makeText(getActivity(), "Không thể tải ảnh đã chụp", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "Không nhận được ảnh từ scanner", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_FACE_CAPTURE && resultCode == android.app.Activity.RESULT_OK && data != null) {
            // Handle face capture result
            String faceImagePath = data.getStringExtra("face_image_path");
            
            if (faceImagePath != null) {
                Bitmap faceImage = BitmapFactory.decodeFile(faceImagePath);
                if (faceImage != null) {
                    captureData.setFaceImage(faceImage);
                    captureData.setFaceImagePath(faceImagePath);
                    
                    Log.d(TAG, "Face image captured successfully from path: " + faceImagePath);
                    Toast.makeText(getActivity(), "Đã chụp ảnh khuôn mặt thành công!", Toast.LENGTH_LONG).show();
                    
                    // Show preview of back image and continue to next step
                    showPreview(captureData.getBackImage());
                } else {
                    Log.e(TAG, "Failed to load face image from path: " + faceImagePath);
                    Toast.makeText(getActivity(), "Không thể tải ảnh khuôn mặt", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "No face_image_path in result");
                Toast.makeText(getActivity(), "Không nhận được ảnh khuôn mặt", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void startFaceCapture() {
        Log.d(TAG, "Starting face capture...");
        Intent intent = new Intent(getActivity(), BiometricAuthActivity.class);
        intent.putExtra("mode", "capture");
        startActivityForResult(intent, REQUEST_FACE_CAPTURE);
    }
    
    private void showPreview(Bitmap image) {
        ivPreview.setImageBitmap(image);
        ivPreview.setVisibility(View.VISIBLE);
        btnCapture.setVisibility(View.GONE);
        btnContinue.setVisibility(View.VISIBLE);
        btnRetake.setVisibility(View.VISIBLE);
        tvStatus.setText("Ảnh mặt sau đã được chụp. Nhấn 'Tiếp tục' để chuyển sang bước tiếp theo.");
    }
    
    private void showCaptureState() {
        ivPreview.setVisibility(View.GONE);
        btnCapture.setVisibility(View.VISIBLE);
        btnContinue.setVisibility(View.GONE);
        btnRetake.setVisibility(View.GONE);
        tvStatus.setText("Nhấn nút để bắt đầu quét mặt sau CCCD");
    }
    
    private void retakeImage() {
        captureData.setBackImage(null);
        captureData.setBackImagePath(null);
        showCaptureState();
    }
    
    private void continueToNextStep() {
        if (captureData.getBackImage() == null) {
            Toast.makeText(getActivity(), "Vui lòng chụp ảnh mặt sau CCCD trước", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (captureData.getFaceImage() == null) {
            Toast.makeText(getActivity(), "Vui lòng chụp ảnh khuôn mặt trước", Toast.LENGTH_SHORT).show();
            // Auto-start face capture if not done yet
            startFaceCapture();
            return;
        }
        
        // All steps completed - navigate to next step
        if (getActivity() instanceof CccdCaptureActivity) {
            ((CccdCaptureActivity) getActivity()).goToNextStep();
        }
    }
}

