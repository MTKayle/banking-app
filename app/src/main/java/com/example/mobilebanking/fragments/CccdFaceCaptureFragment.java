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
import com.example.mobilebanking.activities.CccdCaptureActivity;
import com.example.mobilebanking.models.CccdCaptureData;

/**
 * Fragment for capturing face recognition
 */
public class CccdFaceCaptureFragment extends Fragment {
    private static final String TAG = "CccdFaceCapture";
    private static final int REQUEST_FACE_CAPTURE = 502;
    
    private CccdCaptureData captureData;
    
    private ImageView ivPreview;
    private TextView tvStatus;
    private Button btnCapture;
    private Button btnContinue;
    private Button btnRetake;
    private ProgressBar progressBar;
    
    public static CccdFaceCaptureFragment newInstance(CccdCaptureData data) {
        CccdFaceCaptureFragment fragment = new CccdFaceCaptureFragment();
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
        if (captureData.getFaceImage() == null && btnCapture.getVisibility() == View.VISIBLE) {
            // Small delay to ensure UI is ready
            btnCapture.postDelayed(() -> {
                if (captureData.getFaceImage() == null) {
                    startFaceCapture();
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
        btnCapture.setText("Nhận Diện Khuôn Mặt");
        tvStatus.setText("Nhấn nút để bắt đầu nhận diện khuôn mặt");
    }
    
    private void setupListeners() {
        btnCapture.setOnClickListener(v -> startFaceCapture());
        btnContinue.setOnClickListener(v -> continueToNextStep());
        btnRetake.setOnClickListener(v -> retakeImage());
    }
    
    private void loadData() {
        if (captureData.getFaceImage() != null) {
            // Already captured
            showPreview(captureData.getFaceImage());
        } else {
            // Not captured yet
            showCaptureState();
        }
    }
    
    private void startFaceCapture() {
        Intent intent = new Intent(getActivity(), BiometricAuthActivity.class);
        // Pass flag to indicate this is for registration, not login
        intent.putExtra("mode", "capture");
        startActivityForResult(intent, REQUEST_FACE_CAPTURE);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_FACE_CAPTURE && resultCode == android.app.Activity.RESULT_OK && data != null) {
            // BiometricAuthActivity should return face image path
            String imagePath = data.getStringExtra("face_image_path");
            
            if (imagePath != null) {
                Bitmap faceImage = BitmapFactory.decodeFile(imagePath);
                if (faceImage != null) {
                    captureData.setFaceImage(faceImage);
                    captureData.setFaceImagePath(imagePath);
                    
                    showPreview(faceImage);
                    Toast.makeText(getActivity(), "Đã chụp ảnh khuôn mặt thành công!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), "Không thể tải ảnh đã chụp", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Fallback: try to get bitmap directly
                if (data.hasExtra("face_image")) {
                    Bitmap faceImage = (Bitmap) data.getParcelableExtra("face_image");
                    if (faceImage != null) {
                        captureData.setFaceImage(faceImage);
                        showPreview(faceImage);
                        Toast.makeText(getActivity(), "Đã chụp ảnh khuôn mặt thành công!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Không nhận được ảnh khuôn mặt", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    
    private void showPreview(Bitmap image) {
        ivPreview.setImageBitmap(image);
        ivPreview.setVisibility(View.VISIBLE);
        btnCapture.setVisibility(View.GONE);
        btnContinue.setVisibility(View.VISIBLE);
        btnRetake.setVisibility(View.VISIBLE);
        tvStatus.setText("Ảnh khuôn mặt đã được chụp. Nhấn 'Hoàn tất' để kết thúc.");
    }
    
    private void showCaptureState() {
        ivPreview.setVisibility(View.GONE);
        btnCapture.setVisibility(View.VISIBLE);
        btnContinue.setVisibility(View.GONE);
        btnRetake.setVisibility(View.GONE);
        tvStatus.setText("Nhấn nút để bắt đầu nhận diện khuôn mặt");
    }
    
    private void retakeImage() {
        captureData.setFaceImage(null);
        captureData.setFaceImagePath(null);
        showCaptureState();
    }
    
    private void continueToNextStep() {
        if (captureData.getFaceImage() == null) {
            Toast.makeText(getActivity(), "Vui lòng chụp ảnh khuôn mặt trước", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // All steps completed - finish activity and return result
        if (getActivity() instanceof CccdCaptureActivity) {
            CccdCaptureActivity activity = (CccdCaptureActivity) getActivity();
            activity.completeCapture();
        }
    }
}

