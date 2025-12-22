package com.example.mobilebanking.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.mobilebanking.R;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.AuthApiService;
import com.example.mobilebanking.api.dto.FaceCompareResponse;
import com.example.mobilebanking.utils.DataManager;
import com.example.mobilebanking.views.FaceDetectionOverlay;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Face Verification for Transaction
 * Xác thực khuôn mặt cho giao dịch >= 10 triệu
 */
public class FaceVerificationTransactionActivity extends AppCompatActivity {
    private static final String TAG = "FaceVerifyTransaction";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 500;
    private static final int MIN_FACE_SIZE_PERCENTAGE = 60;
    private static final long CAPTURE_DELAY_MS = 3000;
    
    private PreviewView previewView;
    private FaceDetectionOverlay overlayView;
    private TextView tvInstruction;
    private ProgressBar progressBar;
    
    private ProcessCameraProvider cameraProvider;
    private ImageCapture imageCapture;
    private FaceDetector faceDetector;
    private ExecutorService imageAnalysisExecutor;
    private Handler mainHandler;
    
    private boolean isCapturing = false;
    private boolean isVerifying = false;
    private boolean isReadyToCapture = false;
    private long readyToCaptureTime = 0;
    
    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_step5_face_verification);
        
        dataManager = DataManager.getInstance(this);
        mainHandler = new Handler(Looper.getMainLooper());
        imageAnalysisExecutor = Executors.newSingleThreadExecutor();
        
        initializeViews();
        setupFaceDetector();
        checkCameraPermission();
    }
    
    private void initializeViews() {
        previewView = findViewById(R.id.preview_view);
        overlayView = findViewById(R.id.overlay_view);
        tvInstruction = findViewById(R.id.tv_instruction);
        progressBar = findViewById(R.id.progress_bar);
        
        TextView tvTitle = findViewById(R.id.tv_title);
        if (tvTitle != null) {
            tvTitle.setText("Xác Thực Giao Dịch");
        }
        
        if (tvInstruction != null) {
            tvInstruction.setText("Đặt khuôn mặt vào khung hình");
        }
    }
    
    private void setupFaceDetector() {
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                .setMinFaceSize(0.15f)
                .build();
        
        faceDetector = FaceDetection.getClient(options);
    }
    
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Cần quyền camera để xác thực khuôn mặt", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
    
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);
        
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera", e);
                Toast.makeText(this, "Không thể khởi động camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    
    private void bindCameraUseCases() {
        if (cameraProvider == null) return;
        
        cameraProvider.unbindAll();
        
        // Preview
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        
        // ImageCapture
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();
        
        // ImageAnalysis for face detection
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        
        imageAnalysis.setAnalyzer(imageAnalysisExecutor, this::analyzeFace);
        
        // Bind to lifecycle
        CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
        
        try {
            Camera camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalysis);
        } catch (Exception e) {
            Log.e(TAG, "Use case binding failed", e);
        }
    }
    
    private void analyzeFace(@NonNull ImageProxy imageProxy) {
        if (isCapturing || isVerifying) {
            imageProxy.close();
            return;
        }
        
        @SuppressWarnings("UnsafeOptInUsageError")
        android.media.Image mediaImage = imageProxy.getImage();
        if (mediaImage == null) {
            imageProxy.close();
            return;
        }
        
        InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
        
        faceDetector.process(image)
                .addOnSuccessListener(faces -> {
                    processFaceDetectionResult(faces, imageProxy.getWidth(), imageProxy.getHeight());
                    imageProxy.close();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Face detection failed", e);
                    imageProxy.close();
                });
    }
    
    private void processFaceDetectionResult(List<Face> faces, int imageWidth, int imageHeight) {
        if (faces.isEmpty()) {
            mainHandler.post(() -> {
                overlayView.clearFaceRect();
                tvInstruction.setText("Không phát hiện khuôn mặt");
                isReadyToCapture = false;
            });
            return;
        }
        
        Face face = faces.get(0);
        Rect faceRect = face.getBoundingBox();
        
        // Calculate face size percentage
        float faceArea = faceRect.width() * faceRect.height();
        float imageArea = imageWidth * imageHeight;
        float faceSizePercentage = (faceArea / imageArea) * 100;
        
        boolean isFaceLargeEnough = faceSizePercentage >= MIN_FACE_SIZE_PERCENTAGE;
        
        mainHandler.post(() -> {
            overlayView.updateFaceRect(faceRect, faceSizePercentage, isFaceLargeEnough);
            
            if (isFaceLargeEnough) {
                if (!isReadyToCapture) {
                    isReadyToCapture = true;
                    readyToCaptureTime = System.currentTimeMillis();
                    tvInstruction.setText("Giữ nguyên...");
                } else {
                    long elapsedTime = System.currentTimeMillis() - readyToCaptureTime;
                    if (elapsedTime >= CAPTURE_DELAY_MS && !isCapturing) {
                        tvInstruction.setText("Đang chụp...");
                        captureImage();
                    }
                }
            } else {
                isReadyToCapture = false;
                tvInstruction.setText("Di chuyển gần hơn");
            }
        });
    }
    
    private void captureImage() {
        if (isCapturing || imageCapture == null) return;
        
        isCapturing = true;
        
        File photoFile = new File(getCacheDir(), "face_verify_" + System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();
        
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                        Log.d(TAG, "Image captured: " + photoFile.getAbsolutePath());
                        verifyFace(photoFile);
                    }
                    
                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Image capture failed", exception);
                        isCapturing = false;
                        Toast.makeText(FaceVerificationTransactionActivity.this,
                                "Chụp ảnh thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private void verifyFace(File imageFile) {
        if (isVerifying) return;
        
        isVerifying = true;
        progressBar.setVisibility(android.view.View.VISIBLE);
        tvInstruction.setText("Đang xác thực...");
        
        Log.d(TAG, "Verifying face with image: " + imageFile.getAbsolutePath());
        
        // Prepare multipart request with key "faceImage"
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("faceImage", imageFile.getName(), requestFile);
        
        AuthApiService authApiService = ApiClient.getAuthApiService();
        Call<FaceCompareResponse> call = authApiService.compareFace(body);
        
        call.enqueue(new Callback<FaceCompareResponse>() {
            @Override
            public void onResponse(Call<FaceCompareResponse> call, Response<FaceCompareResponse> response) {
                progressBar.setVisibility(android.view.View.GONE);
                
                Log.d(TAG, "Face verification response code: " + response.code());
                
                if (response.isSuccessful() && response.body() != null) {
                    FaceCompareResponse faceResponse = response.body();
                    
                    Log.d(TAG, "Face verification success: " + faceResponse.isSuccess() + 
                          ", matched: " + faceResponse.isMatched() + 
                          ", message: " + faceResponse.getMessage());
                    
                    if (faceResponse.isSuccess() && faceResponse.isMatched()) {
                        // Face verification successful
                        showSuccessDialog();
                    } else {
                        // Face verification failed
                        String message = faceResponse.getMessage();
                        if (message == null || message.isEmpty()) {
                            message = "Khuôn mặt không khớp";
                        }
                        showFailureDialog(message);
                    }
                } else {
                    Log.e(TAG, "Face verification failed with code: " + response.code());
                    try {
                        String errorBody = response.errorBody() != null ? 
                            response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Error body: " + errorBody);
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    showFailureDialog("Xác thực thất bại (Mã lỗi: " + response.code() + ")");
                }
            }
            
            @Override
            public void onFailure(Call<FaceCompareResponse> call, Throwable t) {
                Log.e(TAG, "Face verification failed", t);
                progressBar.setVisibility(android.view.View.GONE);
                showFailureDialog("Lỗi kết nối: " + t.getMessage());
            }
        });
    }
    
    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("XÁC THỰC THÀNH CÔNG")
                .setMessage("Khuôn mặt của bạn đã được xác thực thành công")
                .setPositiveButton("Tiếp tục", (dialog, which) -> {
                    // Navigate to OTP verification after face verification success
                    navigateToOtpVerification();
                })
                .setCancelable(false)
                .show();
    }
    
    /**
     * Navigate to OTP verification after face verification success
     */
    private void navigateToOtpVerification() {
        Intent currentIntent = getIntent();
        String from = currentIntent.getStringExtra("from");
        
        Intent otpIntent = new Intent(this, OtpVerificationActivity.class);
        
        if ("movie_booking".equals(from)) {
            // Movie booking flow
            String userPhone = currentIntent.getStringExtra("user_phone");
            otpIntent.putExtra("phone", userPhone);
            otpIntent.putExtra("from", "movie_booking");
            
            // Pass all booking data
            otpIntent.putExtra("customer_name", currentIntent.getStringExtra("customer_name"));
            otpIntent.putExtra("customer_phone", currentIntent.getStringExtra("customer_phone"));
            otpIntent.putExtra("customer_email", currentIntent.getStringExtra("customer_email"));
            otpIntent.putExtra("screening_id", currentIntent.getLongExtra("screening_id", -1));
            otpIntent.putExtra("seat_ids", currentIntent.getLongArrayExtra("seat_ids"));
            
            // Display info
            otpIntent.putExtra("movie_title", currentIntent.getStringExtra("movie_title"));
            otpIntent.putExtra("cinema_name", currentIntent.getStringExtra("cinema_name"));
            otpIntent.putExtra("showtime", currentIntent.getStringExtra("showtime"));
            otpIntent.putExtra("seats", currentIntent.getStringExtra("seats"));
            otpIntent.putExtra("total_amount", currentIntent.getDoubleExtra("total_amount", 0));
            
        } else if ("BILL_PAYMENT".equals(from)) {
            // Bill payment flow
            String userPhone = currentIntent.getStringExtra("phone");
            otpIntent.putExtra("phone", userPhone);
            otpIntent.putExtra("from", "BILL_PAYMENT");
            
            // Pass all bill payment data
            otpIntent.putExtra("BILL_CODE", currentIntent.getStringExtra("BILL_CODE"));
            otpIntent.putExtra("BILL_TYPE", currentIntent.getStringExtra("BILL_TYPE"));
            otpIntent.putExtra("PROVIDER_NAME", currentIntent.getStringExtra("PROVIDER_NAME"));
            otpIntent.putExtra("AMOUNT", currentIntent.getStringExtra("AMOUNT"));
            otpIntent.putExtra("ACCOUNT_NUMBER", currentIntent.getStringExtra("ACCOUNT_NUMBER"));
            otpIntent.putExtra("BILLING_PERIOD", currentIntent.getStringExtra("BILLING_PERIOD"));
            
        } else if ("MORTGAGE_PAYMENT".equals(from)) {
            // Mortgage payment flow
            String userPhone = dataManager.getUserPhone();
            if (userPhone == null || userPhone.isEmpty()) {
                userPhone = dataManager.getLastUsername();
            }
            
            Log.d(TAG, "MORTGAGE_PAYMENT - Phone for OTP: " + userPhone);
            
            otpIntent.putExtra("PHONE_NUMBER", userPhone);
            otpIntent.putExtra("FROM_ACTIVITY", "MORTGAGE_PAYMENT");
            
            // Pass all mortgage payment data
            otpIntent.putExtra("MORTGAGE_ID", currentIntent.getLongExtra("MORTGAGE_ID", 0));
            otpIntent.putExtra("PAYMENT_AMOUNT", currentIntent.getDoubleExtra("PAYMENT_AMOUNT", 0));
            otpIntent.putExtra("PAYMENT_ACCOUNT", currentIntent.getStringExtra("PAYMENT_ACCOUNT"));
            otpIntent.putExtra("MORTGAGE_ACCOUNT", currentIntent.getStringExtra("MORTGAGE_ACCOUNT"));
            otpIntent.putExtra("PERIOD_NUMBER", currentIntent.getIntExtra("PERIOD_NUMBER", 0));
            
        } else if ("MORTGAGE_SETTLEMENT".equals(from)) {
            // Mortgage settlement flow
            String userPhone = dataManager.getUserPhone();
            if (userPhone == null || userPhone.isEmpty()) {
                userPhone = dataManager.getLastUsername();
            }
            
            Log.d(TAG, "MORTGAGE_SETTLEMENT - Phone for OTP: " + userPhone);
            
            otpIntent.putExtra("PHONE_NUMBER", userPhone);
            otpIntent.putExtra("FROM_ACTIVITY", "MORTGAGE_SETTLEMENT");
            
            // Pass all mortgage settlement data
            otpIntent.putExtra("MORTGAGE_ID", currentIntent.getLongExtra("MORTGAGE_ID", 0));
            otpIntent.putExtra("SETTLEMENT_AMOUNT", currentIntent.getDoubleExtra("SETTLEMENT_AMOUNT", 0));
            otpIntent.putExtra("PAYMENT_ACCOUNT", currentIntent.getStringExtra("PAYMENT_ACCOUNT"));
            otpIntent.putExtra("MORTGAGE_ACCOUNT", currentIntent.getStringExtra("MORTGAGE_ACCOUNT"));
            
        } else if ("transaction".equals(from)) {
            // Transfer transaction flow
            String transactionCode = currentIntent.getStringExtra("transaction_code");
            String toAccount = currentIntent.getStringExtra("to_account");
            String toName = currentIntent.getStringExtra("to_name");
            String note = currentIntent.getStringExtra("note");
            String bank = currentIntent.getStringExtra("bank");
            double amount = currentIntent.getDoubleExtra("amount", 0);
            String userPhone = currentIntent.getStringExtra("userPhone");
            
            // Get phone from DataManager if not provided
            if (userPhone == null || userPhone.isEmpty()) {
                userPhone = dataManager.getUserPhone();
                if (userPhone == null || userPhone.isEmpty()) {
                    userPhone = dataManager.getLastUsername();
                }
            }
            
            Log.d(TAG, "Transfer - Phone for OTP: " + userPhone);
            Log.d(TAG, "Transfer - Transaction Code: " + transactionCode);
            Log.d(TAG, "Transfer - Bank: " + bank);
            
            otpIntent.putExtra("from", "transaction");
            otpIntent.putExtra("phone", userPhone);
            
            // Pass all transaction data
            otpIntent.putExtra("transaction_code", transactionCode);
            otpIntent.putExtra("amount", amount);
            otpIntent.putExtra("to_account", toAccount);
            otpIntent.putExtra("to_name", toName);
            otpIntent.putExtra("note", note);
            otpIntent.putExtra("bank", bank);
            
        } else {
            // Legacy or unknown flow - try to get data from old field names
            String recipientAccount = currentIntent.getStringExtra("recipientAccount");
            String recipientName = currentIntent.getStringExtra("recipientName");
            String recipientBank = currentIntent.getStringExtra("recipientBank");
            double amount = currentIntent.getDoubleExtra("amount", 0);
            String description = currentIntent.getStringExtra("description");
            String userPhone = currentIntent.getStringExtra("userPhone");
            String transactionCode = currentIntent.getStringExtra("transaction_code");
            String toAccount = currentIntent.getStringExtra("to_account");
            String toName = currentIntent.getStringExtra("to_name");
            String note = currentIntent.getStringExtra("note");
            String bank = currentIntent.getStringExtra("bank");
            
            // Get phone from DataManager if not provided
            if (userPhone == null || userPhone.isEmpty()) {
                userPhone = dataManager.getUserPhone();
                if (userPhone == null || userPhone.isEmpty()) {
                    userPhone = dataManager.getLastUsername();
                }
            }
            
            Log.d(TAG, "Transfer - Phone for OTP: " + userPhone);
            Log.d(TAG, "Transfer - Transaction Code: " + transactionCode);
            Log.d(TAG, "Transfer - Bank: " + bank);
            
            otpIntent.putExtra("from", "transaction");
            otpIntent.putExtra("phone", userPhone);
            
            // Pass all transaction data
            otpIntent.putExtra("transaction_code", transactionCode);
            otpIntent.putExtra("amount", amount);
            otpIntent.putExtra("to_account", toAccount != null ? toAccount : recipientAccount);
            otpIntent.putExtra("to_name", toName != null ? toName : recipientName);
            otpIntent.putExtra("note", note != null ? note : description);
            otpIntent.putExtra("bank", bank != null ? bank : recipientBank);
            
            // Legacy fields for backward compatibility
            otpIntent.putExtra("recipientAccount", recipientAccount);
            otpIntent.putExtra("recipientName", recipientName);
            otpIntent.putExtra("recipientBank", recipientBank);
            otpIntent.putExtra("description", description);
        }
        
        startActivity(otpIntent);
        finish();
    }
    
    private void showFailureDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("XÁC THỰC THẤT BẠI")
                .setMessage(message != null ? message : "Khuôn mặt không khớp. Vui lòng thử lại.")
                .setPositiveButton("Thử lại", (dialog, which) -> {
                    // Reset and try again
                    isCapturing = false;
                    isVerifying = false;
                    isReadyToCapture = false;
                    tvInstruction.setText("Đặt khuôn mặt vào khung hình");
                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    setResult(RESULT_CANCELED);
                    finish();
                })
                .setCancelable(false)
                .show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (imageAnalysisExecutor != null && !imageAnalysisExecutor.isShutdown()) {
            imageAnalysisExecutor.shutdown();
        }
        if (faceDetector != null) {
            faceDetector.close();
        }
    }
}
