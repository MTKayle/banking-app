package com.example.mobilebanking.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mobilebanking.R;
import com.example.mobilebanking.activities.BiometricAuthActivity;
import com.example.mobilebanking.activities.CccdBackScannerActivity;
import com.example.mobilebanking.activities.DataPreviewActivity;
import com.example.mobilebanking.activities.MainRegistrationActivity;
import com.example.mobilebanking.models.RegistrationData;

/**
 * Step 4: Capture Back Side of CCCD and Confirm
 */
public class Step4BackCardFragment extends Fragment {
    private static final String TAG = "Step4BackCard";
    private static final int REQUEST_IMAGE_CAPTURE = 400;
    private static final int REQUEST_AUTO_SCAN_BACK = 401;
    private static final int REQUEST_FACE_CAPTURE = 402;
    
    private RegistrationData registrationData;
    
    private ImageView ivFrontCard, ivBackCard;
    private TextView tvInstruction;
    private Button btnCaptureBack, btnRetakeFront, btnRetakeBack, btnComplete;
    
    public static Step4BackCardFragment newInstance(RegistrationData data) {
        Step4BackCardFragment fragment = new Step4BackCardFragment();
        fragment.registrationData = data;
        return fragment;
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_step4_back_card, container, false);
        
        // Ensure registrationData is not null
        ensureRegistrationData();
        
        initializeViews(view);
        setupListeners();
        loadData();
        
        return view;
    }
    
    private void ensureRegistrationData() {
        if (registrationData == null) {
            if (getActivity() instanceof MainRegistrationActivity) {
                registrationData = ((MainRegistrationActivity) getActivity()).getRegistrationData();
            }
            if (registrationData == null) {
                registrationData = new RegistrationData();
            }
        }
    }
    
    private void initializeViews(View view) {
        ivFrontCard = view.findViewById(R.id.iv_front_card);
        ivBackCard = view.findViewById(R.id.iv_back_card);
        tvInstruction = view.findViewById(R.id.tv_instruction);
        btnCaptureBack = view.findViewById(R.id.btn_capture_back);
        btnRetakeFront = view.findViewById(R.id.btn_retake_front);
        btnRetakeBack = view.findViewById(R.id.btn_retake_back);
        btnComplete = view.findViewById(R.id.btn_complete);
    }
    
    private void setupListeners() {
        btnCaptureBack.setOnClickListener(v -> startAutoScanBack());
        btnRetakeFront.setOnClickListener(v -> retakeFrontImage());
        btnRetakeBack.setOnClickListener(v -> startAutoScanBack());
        btnComplete.setOnClickListener(v -> completeRegistration());
    }
    
    private void startAutoScanBack() {
        Intent intent = new Intent(getActivity(), CccdBackScannerActivity.class);
        startActivityForResult(intent, REQUEST_AUTO_SCAN_BACK);
    }
    
    private void startFaceCapture() {
        Log.d(TAG, "Starting face capture...");
        Intent intent = new Intent(getActivity(), BiometricAuthActivity.class);
        intent.putExtra("mode", "capture");
        startActivityForResult(intent, REQUEST_FACE_CAPTURE);
    }
    
    private void loadData() {
        ensureRegistrationData();
        
        // Display front card image
        if (registrationData != null && registrationData.getFrontCardImage() != null) {
            ivFrontCard.setImageBitmap(registrationData.getFrontCardImage());
        }
        
        // Display back card image if exists
        if (registrationData != null && registrationData.getBackCardImage() != null) {
            ivBackCard.setImageBitmap(registrationData.getBackCardImage());
            btnCaptureBack.setText("Chụp lại mặt sau");
            btnComplete.setVisibility(View.VISIBLE);
        } else {
            btnComplete.setVisibility(View.GONE);
        }
    }
    
    private void captureBackImage() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(getActivity(), "Không thể mở camera", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void retakeFrontImage() {
        // Navigate back to step 3
        if (getActivity() instanceof MainRegistrationActivity) {
            ((MainRegistrationActivity) getActivity()).goToPreviousStep();
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == android.app.Activity.RESULT_OK) {
            // Handle auto scanner result
            if (requestCode == REQUEST_AUTO_SCAN_BACK && data != null) {
                String backImagePath = data.getStringExtra("back_image_path");
                if (backImagePath != null) {
                    Bitmap backImage = BitmapFactory.decodeFile(backImagePath);
                    if (backImage != null) {
                        // Save back card image
                        registrationData.setBackCardImage(backImage);
                        
                        // Display image
                        ivBackCard.setImageBitmap(backImage);
                        btnCaptureBack.setText("Chụp lại mặt sau");
                        btnRetakeBack.setVisibility(View.VISIBLE);
                        
                        Log.d(TAG, "Back image captured successfully. Auto-starting face recognition...");
                        Toast.makeText(getActivity(), "Đã chụp ảnh mặt sau CCCD thành công!", 
                                Toast.LENGTH_SHORT).show();
                        
                        // Auto-start face recognition after back scan with delay
                        // This ensures CccdBackScannerActivity is fully finished and camera is released
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                            if (getActivity() != null && !getActivity().isFinishing()) {
                                startFaceCapture();
                            }
                        }, 500); // 500ms delay to ensure smooth transition
                        return;
                    }
                }
            } else if (requestCode == REQUEST_FACE_CAPTURE && resultCode == android.app.Activity.RESULT_OK && data != null) {
                // Handle face capture result
                String faceImagePath = data.getStringExtra("face_image_path");
                
                if (faceImagePath != null) {
                    Bitmap faceImage = BitmapFactory.decodeFile(faceImagePath);
                    if (faceImage != null) {
                        registrationData.setPortraitImage(faceImage);
                        Log.d(TAG, "Face image captured successfully from path: " + faceImagePath);
                        Toast.makeText(getActivity(), "Đã chụp ảnh khuôn mặt thành công!", Toast.LENGTH_LONG).show();
                        
                        // Show complete button now that both images are captured
                        btnComplete.setVisibility(View.VISIBLE);
                        
                        // Open data preview activity to show all captured data
                        openDataPreviewActivity(faceImagePath);
                        return;
                    } else {
                        Log.e(TAG, "Failed to load face image from path: " + faceImagePath);
                        Toast.makeText(getActivity(), "Không thể tải ảnh khuôn mặt", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "No face_image_path in result");
                    Toast.makeText(getActivity(), "Không nhận được ảnh khuôn mặt", Toast.LENGTH_SHORT).show();
                }
            }
            
            // Fallback: handle manual capture
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                
                if (imageBitmap != null) {
                    // Save back card image
                    registrationData.setBackCardImage(imageBitmap);
                    
                    // Display image
                    ivBackCard.setImageBitmap(imageBitmap);
                    btnCaptureBack.setText("Chụp lại mặt sau");
                    btnRetakeBack.setVisibility(View.VISIBLE);
                    btnComplete.setVisibility(View.VISIBLE);
                    
                    Toast.makeText(getActivity(), "Đã chụp ảnh mặt sau CCCD", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    
    private void completeRegistration() {
        ensureRegistrationData();
        
        if (registrationData == null) {
            Toast.makeText(getActivity(), "Lỗi: Không thể tải dữ liệu đăng ký", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (registrationData.getFrontCardImage() == null) {
            Toast.makeText(getActivity(), "Vui lòng chụp ảnh mặt trước CCCD", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (registrationData.getBackCardImage() == null) {
            Toast.makeText(getActivity(), "Vui lòng chụp ảnh mặt sau CCCD", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (registrationData.getPortraitImage() == null) {
            Toast.makeText(getActivity(), "Vui lòng chụp ảnh khuôn mặt", Toast.LENGTH_SHORT).show();
            // Auto-start face capture if not done yet
            startFaceCapture();
            return;
        }
        
        // All steps completed - complete registration
        if (getActivity() instanceof MainRegistrationActivity) {
            ((MainRegistrationActivity) getActivity()).completeRegistration();
        }
    }
    
    private void openDataPreviewActivity(String selfieImagePath) {
        Intent intent = new Intent(getActivity(), DataPreviewActivity.class);
        
        // Pass image paths
        if (selfieImagePath != null) {
            intent.putExtra("selfie_image_path", selfieImagePath);
        }
        
        // Get front and back card image paths from registration data
        if (registrationData != null) {
            // Try to get paths if available, otherwise pass null and let activity load from bitmap
            // Note: We don't have paths stored, so we'll pass null and let DataPreviewActivity
            // load from RegistrationData bitmaps
        }
        
        startActivity(intent);
    }
}

