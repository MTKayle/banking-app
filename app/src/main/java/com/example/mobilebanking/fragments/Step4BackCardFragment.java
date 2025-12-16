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
    private Button btnCaptureBack, btnRetakeFront, btnRetakeBack;
    
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
    }
    
    private void setupListeners() {
        btnCaptureBack.setOnClickListener(v -> startAutoScanBack());
        btnRetakeFront.setOnClickListener(v -> retakeFrontImage());
        btnRetakeBack.setOnClickListener(v -> startAutoScanBack());
        // btnComplete is no longer used - navigation happens automatically after back scan
    }
    
    private void startAutoScanBack() {
        Intent intent = new Intent(getActivity(), CccdBackScannerActivity.class);
        startActivityForResult(intent, REQUEST_AUTO_SCAN_BACK);
    }
    
    // Face capture is now handled in Step5FaceVerificationFragment
    
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
                        
                        Log.d(TAG, "Back image captured successfully. Navigating to face verification step...");
                        Toast.makeText(getActivity(), "Đã chụp ảnh mặt sau CCCD thành công! Chuyển sang xác thực khuôn mặt...", 
                                Toast.LENGTH_SHORT).show();
                        
                        // Navigate to next step (Step 5: Face Verification) after delay
                        // This ensures CccdBackScannerActivity is fully finished and camera is released
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                            if (getActivity() != null && !getActivity().isFinishing()) {
                                if (getActivity() instanceof MainRegistrationActivity) {
                                    ((MainRegistrationActivity) getActivity()).goToNextStep();
                                }
                            }
                        }, 800); // 800ms delay to ensure smooth transition
                        return;
                    } // End of if (backImage != null)
                } // End of if (backImagePath != null)
            } // End of REQUEST_AUTO_SCAN_BACK handling
            // Face capture is now handled in Step5FaceVerificationFragment
            
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
                    
                    Toast.makeText(getActivity(), "Đã chụp ảnh mặt sau CCCD. Chuyển sang xác thực khuôn mặt...", Toast.LENGTH_SHORT).show();
                    
                    // Navigate to next step
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        if (getActivity() != null && !getActivity().isFinishing()) {
                            if (getActivity() instanceof MainRegistrationActivity) {
                                ((MainRegistrationActivity) getActivity()).goToNextStep();
                            }
                        }
                    }, 800);
                }
            }
        }
    }
    
    private void completeRegistration() {
        // This method is no longer used - registration completion is handled in Step5FaceVerificationFragment
        // Navigate to next step (face verification)
        if (getActivity() instanceof MainRegistrationActivity) {
            ((MainRegistrationActivity) getActivity()).goToNextStep();
        }
    }
}

