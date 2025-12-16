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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.mobilebanking.R;
import com.example.mobilebanking.activities.BiometricAuthActivity;
import com.example.mobilebanking.activities.CustomerDashboardActivity;
import com.example.mobilebanking.activities.MainRegistrationActivity;
import com.example.mobilebanking.activities.LoginActivity;
import com.example.mobilebanking.activities.OfficerDashboardActivity;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.AuthApiService;
import com.example.mobilebanking.api.dto.AuthResponse;
import com.example.mobilebanking.models.RegistrationData;
import com.example.mobilebanking.models.User;
import com.example.mobilebanking.utils.DataManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Step 5: Face Verification
 * - Capture selfie
 * - Compare with portrait from CCCD front side
 * - Show result: XÁC THỰC THÀNH CÔNG or XÁC THỰC THẤT BẠI
 * - If successful, call register-with-face API
 */
public class Step5FaceVerificationFragment extends Fragment {
    private static final String TAG = "Step5FaceVerification";
    private static final int REQUEST_FACE_CAPTURE = 500;
    
    private RegistrationData registrationData;
    
    private ImageView ivPortraitPreview, ivSelfiePreview;
    private TextView tvInstruction, tvStatus;
    private Button btnCaptureSelfie, btnRetry, btnBack;
    private ProgressBar progressBar;
    
    private boolean isVerifying = false;
    private boolean isRegistering = false;
    private boolean isDialogShowing = false; // Đảm bảo chỉ hiển thị 1 dialog tại một thời điểm
    
    public static Step5FaceVerificationFragment newInstance(RegistrationData data) {
        Step5FaceVerificationFragment fragment = new Step5FaceVerificationFragment();
        fragment.registrationData = data;
        return fragment;
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_step5_face_verification, container, false);
        
        Log.d(TAG, "onCreateView called");
        
        ensureRegistrationData();
        
        initializeViews(view);
        setupListeners();
        loadData();
        
        // Auto-start face capture if selfie not captured yet
        // Delay to ensure fragment is fully attached
        if (registrationData.getSelfieImage() == null) {
            view.postDelayed(() -> {
                if (isAdded() && getActivity() != null && !getActivity().isFinishing()) {
                    Log.d(TAG, "Auto-starting face capture...");
                    startFaceCapture();
                }
            }, 300);
        }
        
        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
        
        // Refresh UI when fragment becomes visible
        if (isAdded() && getView() != null) {
            loadData();
        }
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
        ivPortraitPreview = view.findViewById(R.id.iv_portrait_preview);
        ivSelfiePreview = view.findViewById(R.id.iv_selfie_preview);
        tvInstruction = view.findViewById(R.id.tv_instruction);
        tvStatus = view.findViewById(R.id.tv_status);
        btnCaptureSelfie = view.findViewById(R.id.btn_capture_selfie);
        btnRetry = view.findViewById(R.id.btn_retry);
        btnBack = view.findViewById(R.id.btn_back);
        progressBar = view.findViewById(R.id.progress_bar);
        
        btnRetry.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }
    
    private void setupListeners() {
        btnCaptureSelfie.setOnClickListener(v -> startFaceCapture());
        btnRetry.setOnClickListener(v -> {
            registrationData.setSelfieImage(null);
            startFaceCapture();
        });
        btnBack.setOnClickListener(v -> {
            if (getActivity() instanceof MainRegistrationActivity) {
                ((MainRegistrationActivity) getActivity()).goToPreviousStep();
            }
        });
    }
    
    private void loadData() {
        ensureRegistrationData();
        
        // Display portrait from CCCD (for reference, not editable)
        if (registrationData != null && registrationData.getPortraitImage() != null) {
            ivPortraitPreview.setImageBitmap(registrationData.getPortraitImage());
        }
        
        // Display selfie if already captured
        if (registrationData != null && registrationData.getSelfieImage() != null) {
            ivSelfiePreview.setImageBitmap(registrationData.getSelfieImage());
            btnCaptureSelfie.setText("Chụp lại");
            // Auto-verify if selfie is captured
            verifyFace();
        } else {
            tvInstruction.setText("Vui lòng chụp ảnh selfie để xác thực khuôn mặt");
            tvStatus.setText("Chưa chụp ảnh selfie");
        }
    }
    
    private void startFaceCapture() {
        Log.d(TAG, "Starting face capture...");
        Intent intent = new Intent(getActivity(), BiometricAuthActivity.class);
        intent.putExtra("mode", "capture");
        startActivityForResult(intent, REQUEST_FACE_CAPTURE);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        Log.d(TAG, "onActivityResult called: requestCode=" + requestCode + ", resultCode=" + resultCode);
        
        if (requestCode == REQUEST_FACE_CAPTURE && resultCode == android.app.Activity.RESULT_OK && data != null) {
            String faceImagePath = data.getStringExtra("face_image_path");
            Log.d(TAG, "Face image path received: " + faceImagePath);
            
            if (faceImagePath != null) {
                Bitmap selfieImage = BitmapFactory.decodeFile(faceImagePath);
                if (selfieImage != null) {
                    registrationData.setSelfieImage(selfieImage);
                    
                    // Ensure we're on the main thread and fragment is visible
                    if (getActivity() != null && !getActivity().isFinishing() && isAdded() && getView() != null) {
                        ivSelfiePreview.setImageBitmap(selfieImage);
                        btnCaptureSelfie.setText("Chụp lại");
                        
                        Log.d(TAG, "Selfie captured successfully. Starting verification...");
                        
                        // Update UI to show we're ready to verify
                        tvStatus.setText("Đã chụp ảnh selfie. Đang so sánh với ảnh CCCD...");
                        tvStatus.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                        progressBar.setVisibility(View.VISIBLE);
                        btnCaptureSelfie.setEnabled(false);
                        btnRetry.setVisibility(View.GONE);
                        
                        // Small delay to show status update, then auto-verify
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                            if (getActivity() != null && !getActivity().isFinishing() && isAdded()) {
                                Log.d(TAG, "Starting face verification...");
                                verifyFace();
                            } else {
                                Log.w(TAG, "Fragment not attached, cannot verify face");
                            }
                        }, 500);
                    } else {
                        Log.w(TAG, "Fragment not attached or activity finishing, cannot update UI");
                    }
                } else {
                    Log.e(TAG, "Failed to load selfie image from path: " + faceImagePath);
                    if (getActivity() != null && !getActivity().isFinishing() && isAdded()) {
                        Toast.makeText(getActivity(), "Không thể tải ảnh selfie", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Log.e(TAG, "No face_image_path in result");
                if (getActivity() != null && !getActivity().isFinishing() && isAdded()) {
                    Toast.makeText(getActivity(), "Không nhận được ảnh selfie", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == REQUEST_FACE_CAPTURE && resultCode == android.app.Activity.RESULT_CANCELED) {
            Log.d(TAG, "Face capture was cancelled by user");
            if (getActivity() != null && !getActivity().isFinishing() && isAdded()) {
                tvStatus.setText("Chưa chụp ảnh selfie");
                tvStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
                progressBar.setVisibility(View.GONE);
                btnCaptureSelfie.setEnabled(true);
            }
        }
    }
    
    /**
     * Verify face by calling backend API
     * Backend will compare selfie with portrait from CCCD
     */
    private void verifyFace() {
        if (isVerifying || isRegistering) {
            Log.d(TAG, "Verification already in progress, skipping...");
            return;
        }
        
        if (getActivity() == null || getActivity().isFinishing() || !isAdded()) {
            Log.w(TAG, "Cannot verify face - fragment not attached");
            return;
        }
        
        if (registrationData.getPortraitImage() == null) {
            Log.e(TAG, "Portrait image is null");
            if (getActivity() != null && !getActivity().isFinishing() && isAdded()) {
                Toast.makeText(getActivity(), "Lỗi: Không tìm thấy ảnh chân dung từ CCCD", Toast.LENGTH_LONG).show();
            }
            return;
        }
        
        if (registrationData.getSelfieImage() == null) {
            Log.e(TAG, "Selfie image is null");
            if (getActivity() != null && !getActivity().isFinishing() && isAdded()) {
                Toast.makeText(getActivity(), "Vui lòng chụp ảnh selfie", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        
        Log.d(TAG, "Starting face verification...");
        isVerifying = true;
        
        // Update UI on main thread
        if (getView() != null && isAdded()) {
            progressBar.setVisibility(View.VISIBLE);
            tvStatus.setText("Đang so sánh khuôn mặt với ảnh CCCD...");
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
            btnCaptureSelfie.setEnabled(false);
            btnRetry.setVisibility(View.GONE);
        }
        
        // Show both images side by side for comparison
        if (registrationData.getPortraitImage() != null) {
            ivPortraitPreview.setImageBitmap(registrationData.getPortraitImage());
        }
        if (registrationData.getSelfieImage() != null) {
            ivSelfiePreview.setImageBitmap(registrationData.getSelfieImage());
        }
        
        // Initialize ApiClient
        ApiClient.init(getActivity());
        
        // Prepare images as files for multipart upload
        // QUAN TRỌNG: Gửi ảnh CCCD đầy đủ (frontCardImage) thay vì ảnh chân dung đã crop
        // Face++ API có thể tự động detect khuôn mặt tốt hơn từ ảnh CCCD đầy đủ
        Bitmap cccdImageToSend = registrationData.getFrontCardImage();
        if (cccdImageToSend == null) {
            // Fallback: nếu không có ảnh CCCD đầy đủ, dùng ảnh chân dung
            cccdImageToSend = registrationData.getPortraitImage();
            Log.w(TAG, "Using portrait image instead of full CCCD image");
        } else {
            Log.d(TAG, "Using full CCCD image for face comparison");
        }
        
        if (cccdImageToSend == null) {
            isVerifying = false;
            progressBar.setVisibility(View.GONE);
            btnCaptureSelfie.setEnabled(true);
            Toast.makeText(getActivity(), "Lỗi: Không tìm thấy ảnh CCCD", Toast.LENGTH_LONG).show();
            return;
        }
        
        final File cccdPhotoFile = saveBitmapToFile(cccdImageToSend, "cccd_full");
        final File selfiePhotoFile = saveBitmapToFile(registrationData.getSelfieImage(), "selfie");
        
        if (cccdPhotoFile == null || selfiePhotoFile == null) {
            isVerifying = false;
            progressBar.setVisibility(View.GONE);
            btnCaptureSelfie.setEnabled(true);
            Toast.makeText(getActivity(), "Lỗi: Không thể chuẩn bị ảnh để gửi", Toast.LENGTH_LONG).show();
            return;
        }
        
        Log.d(TAG, "CCCD image size: " + cccdImageToSend.getWidth() + "x" + cccdImageToSend.getHeight());
        Log.d(TAG, "Selfie image size: " + registrationData.getSelfieImage().getWidth() + "x" + registrationData.getSelfieImage().getHeight());
        Log.d(TAG, "CCCD image file path: " + cccdPhotoFile.getAbsolutePath());
        Log.d(TAG, "Selfie image file path: " + selfiePhotoFile.getAbsolutePath());
        
        // Create RequestBody for text fields
        RequestBody phoneBody = RequestBody.create(MediaType.parse("text/plain"), registrationData.getPhoneNumber());
        RequestBody emailBody = RequestBody.create(MediaType.parse("text/plain"), registrationData.getEmail());
        RequestBody passwordBody = RequestBody.create(MediaType.parse("text/plain"), registrationData.getPassword());
        RequestBody fullNameBody = RequestBody.create(MediaType.parse("text/plain"), registrationData.getFullName());
        RequestBody cccdNumberBody = RequestBody.create(MediaType.parse("text/plain"), registrationData.getIdNumber());
        
        // Optional fields
        RequestBody dateOfBirthBody = registrationData.getDateOfBirth() != null && !registrationData.getDateOfBirth().isEmpty()
                ? RequestBody.create(MediaType.parse("text/plain"), convertDateFormat(registrationData.getDateOfBirth()))
                : RequestBody.create(MediaType.parse("text/plain"), "");
        RequestBody permanentAddressBody = registrationData.getPermanentAddress() != null && !registrationData.getPermanentAddress().isEmpty()
                ? RequestBody.create(MediaType.parse("text/plain"), registrationData.getPermanentAddress())
                : RequestBody.create(MediaType.parse("text/plain"), "");
        RequestBody temporaryAddressBody = RequestBody.create(MediaType.parse("text/plain"), "");
        
        // Create MultipartBody.Part for images
        RequestBody cccdPhotoRequestBody = RequestBody.create(MediaType.parse("image/jpeg"), cccdPhotoFile);
        MultipartBody.Part cccdPhotoPart = MultipartBody.Part.createFormData("cccdPhoto", cccdPhotoFile.getName(), cccdPhotoRequestBody);
        
        RequestBody selfiePhotoRequestBody = RequestBody.create(MediaType.parse("image/jpeg"), selfiePhotoFile);
        MultipartBody.Part selfiePhotoPart = MultipartBody.Part.createFormData("selfiePhoto", selfiePhotoFile.getName(), selfiePhotoRequestBody);
        
        // Call API
        AuthApiService authApiService = ApiClient.getAuthApiService();
        Call<AuthResponse> call = authApiService.registerWithFace(
                phoneBody,
                emailBody,
                passwordBody,
                fullNameBody,
                cccdNumberBody,
                dateOfBirthBody,
                permanentAddressBody,
                temporaryAddressBody,
                cccdPhotoPart,
                selfiePhotoPart
        );
        
        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                isVerifying = false;
                
                // Ensure we're on main thread and fragment is attached
                if (getActivity() == null || getActivity().isFinishing() || !isAdded()) {
                    Log.w(TAG, "Fragment not attached, cannot update UI");
                    return;
                }
                
                getActivity().runOnUiThread(() -> {
                    if (getView() != null && isAdded()) {
                        progressBar.setVisibility(View.GONE);
                        btnCaptureSelfie.setEnabled(true);
                    }
                });
                
                if (response.isSuccessful() && response.body() != null) {
                    // Face verification successful!
                    Log.d(TAG, "Face verification successful!");
                    AuthResponse authResponse = response.body();
                    
                    // Save session
                    if (getActivity() != null) {
                        DataManager dataManager = DataManager.getInstance(getActivity());
                        User.UserRole role = "CUSTOMER".equalsIgnoreCase(authResponse.getRole())
                                ? User.UserRole.CUSTOMER
                                : User.UserRole.OFFICER;
                        dataManager.saveLoggedInUser(registrationData.getPhoneNumber(), role);
                        dataManager.saveLastUsername(registrationData.getPhoneNumber());
                        
                        // Save token
                        if (authResponse.getToken() != null) {
                            dataManager.saveTokens(authResponse.getToken(), authResponse.getToken());
                        }
                    }
                    
                    // Show success dialog
                    showVerificationSuccessDialog();
                } else {
                    // Face verification failed - parse error from backend
                    String errorMessage = "Xác thực khuôn mặt thất bại";
                    int statusCode = response.code();
                    
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.d(TAG, "Error response body: " + errorBody);
                            
                            try {
                                JsonObject jsonObject = JsonParser.parseString(errorBody).getAsJsonObject();
                                if (jsonObject.has("message")) {
                                    errorMessage = jsonObject.get("message").getAsString();
                                } else if (jsonObject.has("error")) {
                                    errorMessage = jsonObject.get("error").getAsString();
                                }
                            } catch (Exception e) {
                                // If not JSON, use raw error body
                                if (errorBody != null && !errorBody.isEmpty()) {
                                    errorMessage = errorBody;
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error response", e);
                        e.printStackTrace();
                    }
                    
                    // Add status code info if available
                    if (statusCode == 400) {
                        errorMessage = "Dữ liệu không hợp lệ: " + errorMessage;
                    } else if (statusCode == 401) {
                        errorMessage = "Xác thực thất bại: " + errorMessage;
                    } else if (statusCode == 500) {
                        errorMessage = "Lỗi server: " + errorMessage;
                    }
                    
                    Log.d(TAG, "Face verification failed: " + errorMessage);
                    showVerificationFailedDialog(errorMessage);
                }
                
                // Clean up temp files
                if (cccdPhotoFile.exists()) cccdPhotoFile.delete();
                if (selfiePhotoFile.exists()) selfiePhotoFile.delete();
            }
            
            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                isVerifying = false;
                
                // Ensure we're on main thread and fragment is attached
                if (getActivity() == null || getActivity().isFinishing() || !isAdded()) {
                    Log.w(TAG, "Fragment not attached, cannot update UI");
                    // Clean up temp files
                    if (cccdPhotoFile != null && cccdPhotoFile.exists()) cccdPhotoFile.delete();
                    if (selfiePhotoFile != null && selfiePhotoFile.exists()) selfiePhotoFile.delete();
                    return;
                }
                
                getActivity().runOnUiThread(() -> {
                    if (getView() != null && isAdded()) {
                        progressBar.setVisibility(View.GONE);
                        btnCaptureSelfie.setEnabled(true);
                    }
                });
                
                String errorMessage = "Không thể kết nối đến server";
                if (t.getMessage() != null) {
                    if (t.getMessage().contains("Failed to connect") || t.getMessage().contains("Unable to resolve host")) {
                        errorMessage = "Không thể kết nối đến server.\n\n" +
                                "Vui lòng kiểm tra:\n" +
                                "• Backend đã chạy chưa? (http://localhost:8089)\n" +
                                "• Địa chỉ IP trong ApiClient đúng chưa?\n" +
                                "• Emulator đang dùng 10.0.2.2 để kết nối localhost\n" +
                                "• Kiểm tra kết nối mạng";
                    } else if (t.getMessage().contains("timeout")) {
                        errorMessage = "Kết nối quá thời gian chờ.\n\nVui lòng thử lại sau.";
                    } else {
                        errorMessage = "Lỗi kết nối: " + t.getMessage();
                    }
                }
                
                Log.e(TAG, "Network error during face verification", t);
                t.printStackTrace();
                
                showVerificationFailedDialog(errorMessage);
                
                // Clean up temp files
                if (cccdPhotoFile != null && cccdPhotoFile.exists()) cccdPhotoFile.delete();
                if (selfiePhotoFile != null && selfiePhotoFile.exists()) selfiePhotoFile.delete();
            }
        });
    }
    
    /**
     * Show success dialog: XÁC THỰC THÀNH CÔNG
     */
    private void showVerificationSuccessDialog() {
        if (getActivity() == null || getActivity().isFinishing() || !isAdded()) {
            Log.w(TAG, "Cannot show success dialog - fragment not attached");
            return;
        }
        
        // Đảm bảo chỉ hiển thị 1 dialog
        if (isDialogShowing) {
            Log.w(TAG, "Dialog already showing, skipping...");
            return;
        }
        
        Log.d(TAG, "Showing verification success dialog");
        isDialogShowing = true;
        
        // Update UI and show dialog on main thread
        getActivity().runOnUiThread(() -> {
            if (getActivity() == null || getActivity().isFinishing() || !isAdded()) {
                isDialogShowing = false;
                return;
            }
            
            // Update UI
            if (getView() != null) {
                tvStatus.setText("✓ XÁC THỰC THÀNH CÔNG");
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                progressBar.setVisibility(View.GONE);
                btnCaptureSelfie.setEnabled(false);
                btnCaptureSelfie.setText("Hoàn tất");
                btnRetry.setVisibility(View.GONE);
            }
            
            // Show success dialog - chỉ hiển thị 1 dialog
            new AlertDialog.Builder(getActivity())
                    .setTitle("✓ XÁC THỰC THÀNH CÔNG")
                    .setMessage("Khuôn mặt của bạn đã được xác thực thành công với ảnh trên CCCD.\n\n" +
                               "Bạn sẽ được chuyển về màn hình đăng nhập để đăng nhập vào hệ thống.")
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, which) -> {
                        isDialogShowing = false;
                        // Navigate back to login screen
                        navigateToLogin();
                    })
                    .setOnDismissListener(dialog -> {
                        isDialogShowing = false;
                    })
                    .show();
        });
    }
    
    /**
     * Show failed dialog: XÁC THỰC THẤT BẠI
     */
    private void showVerificationFailedDialog(String errorMessage) {
        if (getActivity() == null || getActivity().isFinishing() || !isAdded()) {
            Log.w(TAG, "Cannot show failed dialog - fragment not attached");
            return;
        }
        
        // Đảm bảo chỉ hiển thị 1 dialog
        if (isDialogShowing) {
            Log.w(TAG, "Dialog already showing, skipping...");
            return;
        }
        
        Log.d(TAG, "Showing verification failed dialog: " + errorMessage);
        isDialogShowing = true;
        
        // Parse error message to be more user-friendly
        String userFriendlyMessage = errorMessage;
        if (errorMessage != null) {
            if (errorMessage.toLowerCase().contains("confidence") || errorMessage.toLowerCase().contains("similarity")) {
                userFriendlyMessage = "Độ tương đồng khuôn mặt không đạt yêu cầu.\n\n" +
                                    "Vui lòng đảm bảo:\n" +
                                    "• Chụp ảnh selfie rõ ràng, đủ ánh sáng\n" +
                                    "• Khuôn mặt không bị che (mũ, khẩu trang, kính)\n" +
                                    "• Nhìn thẳng vào camera\n" +
                                    "• Khuôn mặt giống với ảnh trên CCCD";
            } else if (errorMessage.toLowerCase().contains("face") || errorMessage.toLowerCase().contains("detect")) {
                userFriendlyMessage = "Không thể nhận diện khuôn mặt trong ảnh.\n\n" + errorMessage;
            }
        }
        
        // Make final for use in lambda
        final String finalUserFriendlyMessage = userFriendlyMessage;
        
        // Update UI and show dialog on main thread - chỉ hiển thị 1 dialog
        getActivity().runOnUiThread(() -> {
            if (getActivity() == null || getActivity().isFinishing() || !isAdded()) {
                isDialogShowing = false;
                return;
            }
            
            // Update UI
            if (getView() != null) {
                tvStatus.setText("✗ XÁC THỰC THẤT BẠI");
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                progressBar.setVisibility(View.GONE);
                btnCaptureSelfie.setEnabled(true);
                btnCaptureSelfie.setText("Chụp lại");
                btnRetry.setVisibility(View.VISIBLE);
            }
            
            // Show failed dialog - chỉ hiển thị 1 dialog
            new AlertDialog.Builder(getActivity())
                    .setTitle("✗ XÁC THỰC THẤT BẠI")
                    .setMessage("Khuôn mặt không khớp với ảnh trên CCCD.\n\n" + 
                               finalUserFriendlyMessage + "\n\nVui lòng thử lại.")
                    .setPositiveButton("Thử lại", (dialog, which) -> {
                        isDialogShowing = false;
                        // Reset and retry
                        registrationData.setSelfieImage(null);
                        if (getView() != null && isAdded()) {
                            ivSelfiePreview.setImageBitmap(null);
                            tvStatus.setText("Chưa chụp ảnh selfie");
                            tvStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
                            btnRetry.setVisibility(View.GONE);
                        }
                        startFaceCapture();
                    })
                    .setNegativeButton("Quay lại", (dialog, which) -> {
                        isDialogShowing = false;
                        if (getActivity() instanceof MainRegistrationActivity) {
                            ((MainRegistrationActivity) getActivity()).goToPreviousStep();
                        }
                    })
                    .setCancelable(false)
                    .setOnDismissListener(dialog -> {
                        isDialogShowing = false;
                    })
                    .show();
        });
    }
    
    /**
     * Navigate back to login screen after successful verification
     */
    private void navigateToLogin() {
        if (getActivity() == null) return;

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // Finish registration activity if still running
        getActivity().finish();
    }
    
    /**
     * Save bitmap to temporary file
     * Tăng chất lượng ảnh để Face++ có thể so sánh tốt hơn
     * QUAN TRỌNG: Đảm bảo ảnh có kích thước đủ lớn (tối thiểu 200x200) cho Face++ detect
     */
    private File saveBitmapToFile(Bitmap bitmap, String prefix) {
        try {
            if (bitmap == null || bitmap.isRecycled()) {
                Log.e(TAG, "Bitmap is null or recycled, cannot save");
                return null;
            }
            
            // Đảm bảo ảnh có kích thước tối thiểu cho Face++ (200x200)
            // Face++ yêu cầu ảnh tối thiểu 48x48, nhưng 200x200 sẽ tốt hơn
            Bitmap finalBitmap = bitmap;
            int minSize = 200;
            if (bitmap.getWidth() < minSize || bitmap.getHeight() < minSize) {
                Log.w(TAG, "Image too small (" + bitmap.getWidth() + "x" + bitmap.getHeight() + 
                      "), scaling up to minimum " + minSize + "x" + minSize);
                float scale = Math.max((float)minSize / bitmap.getWidth(), (float)minSize / bitmap.getHeight());
                int newWidth = (int)(bitmap.getWidth() * scale);
                int newHeight = (int)(bitmap.getHeight() * scale);
                finalBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
            }
            
            // Giới hạn kích thước tối đa để tránh file quá lớn (max 5MB)
            // Nhưng vẫn giữ chất lượng tốt cho Face++
            int maxSize = 2000; // Max 2000px để đảm bảo chất lượng tốt
            if (finalBitmap.getWidth() > maxSize || finalBitmap.getHeight() > maxSize) {
                float scale = Math.min((float)maxSize / finalBitmap.getWidth(), (float)maxSize / finalBitmap.getHeight());
                int newWidth = (int)(finalBitmap.getWidth() * scale);
                int newHeight = (int)(finalBitmap.getHeight() * scale);
                Bitmap scaled = Bitmap.createScaledBitmap(finalBitmap, newWidth, newHeight, true);
                if (finalBitmap != bitmap) {
                    finalBitmap.recycle(); // Recycle intermediate bitmap
                }
                finalBitmap = scaled;
            }
            
            File cacheDir = getActivity().getCacheDir();
            File imageFile = new File(cacheDir, prefix + "_" + System.currentTimeMillis() + ".jpg");
            
            FileOutputStream fos = new FileOutputStream(imageFile);
            // Tăng chất lượng lên 100% (không nén) để Face++ có thể detect tốt nhất
            // Quality 100 là tốt nhất cho face recognition, nhưng file sẽ lớn hơn
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            
            // Recycle nếu đã tạo bitmap mới
            if (finalBitmap != bitmap) {
                finalBitmap.recycle();
            }
            
            Log.d(TAG, "Saved bitmap to file: " + imageFile.getAbsolutePath() + 
                  " (file size: " + imageFile.length() + " bytes, " +
                  "image size: " + bitmap.getWidth() + "x" + bitmap.getHeight() + ")");
            
            return imageFile;
        } catch (IOException e) {
            Log.e(TAG, "Error saving bitmap to file", e);
            return null;
        }
    }
    
    /**
     * Convert date format from DD/MM/YYYY to yyyy-MM-dd
     */
    private String convertDateFormat(String date) {
        try {
            if (date.contains("/")) {
                String[] parts = date.split("/");
                if (parts.length == 3) {
                    return parts[2] + "-" + parts[1] + "-" + parts[0];
                }
            }
            return date;
        } catch (Exception e) {
            return date;
        }
    }
}

