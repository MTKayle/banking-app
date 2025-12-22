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
import com.example.mobilebanking.api.dto.AuthResponse;
import com.example.mobilebanking.models.User;
import com.example.mobilebanking.utils.DataManager;
import com.example.mobilebanking.utils.SessionManager;
import com.example.mobilebanking.views.FaceDetectionOverlay;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
 * Face Login Activity
 * Xác thực khuôn mặt khi đăng nhập bằng tài khoản khác (không phải tài khoản cuối cùng)
 */
public class FaceLoginActivity extends AppCompatActivity {
    private static final String TAG = "FaceLoginActivity";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 500;
    private static final int MIN_FACE_SIZE_PERCENTAGE = 60;
    private static final long CAPTURE_DELAY_MS = 3000;
    
    public static final String EXTRA_PHONE = "phone";
    public static final String EXTRA_PASSWORD = "password";
    
    private String phone;
    private String password;
    
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
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_step5_face_verification);
        
        // Get phone and password from intent
        phone = getIntent().getStringExtra(EXTRA_PHONE);
        password = getIntent().getStringExtra(EXTRA_PASSWORD);
        
        if (phone == null || password == null) {
            Toast.makeText(this, "Thiếu thông tin đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        dataManager = DataManager.getInstance(this);
        sessionManager = SessionManager.getInstance(this);
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
            tvTitle.setText("Xác Thực Khuôn Mặt");
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
            Log.e(TAG, "Error binding camera", e);
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
        
        InputImage image = InputImage.fromMediaImage(mediaImage,
                imageProxy.getImageInfo().getRotationDegrees());
        
        faceDetector.process(image)
                .addOnSuccessListener(faces -> processFaceDetectionResult(faces, imageProxy))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Face detection failed", e);
                    imageProxy.close();
                })
                .addOnCompleteListener(task -> {
                    // Image will be closed in processFaceDetectionResult or above
                });
    }
    
    private void processFaceDetectionResult(List<Face> faces, ImageProxy imageProxy) {
        if (faces.isEmpty()) {
            mainHandler.post(() -> {
                overlayView.updateFaceRect(null, 0f, false);
                if (tvInstruction != null) {
                    tvInstruction.setText("Không phát hiện khuôn mặt");
                }
            });
            isReadyToCapture = false;
        } else {
            Face face = faces.get(0);
            Rect faceRect = face.getBoundingBox();
            
            int frameWidth = imageProxy.getWidth();
            int frameHeight = imageProxy.getHeight();
            float faceSizePercentage = (float) (faceRect.width() * faceRect.height()) /
                    (frameWidth * frameHeight) * 100;
            
            mainHandler.post(() -> {
                boolean ready = faceSizePercentage >= MIN_FACE_SIZE_PERCENTAGE;
                overlayView.updateFaceRect(faceRect, faceSizePercentage, ready);
                
                if (faceSizePercentage >= MIN_FACE_SIZE_PERCENTAGE) {
                    if (!isReadyToCapture) {
                        isReadyToCapture = true;
                        readyToCaptureTime = System.currentTimeMillis();
                        if (tvInstruction != null) {
                            tvInstruction.setText("Giữ nguyên tư thế...");
                        }
                    } else {
                        long elapsed = System.currentTimeMillis() - readyToCaptureTime;
                        if (elapsed >= CAPTURE_DELAY_MS && !isCapturing) {
                            captureFace();
                        }
                    }
                } else {
                    isReadyToCapture = false;
                    if (tvInstruction != null) {
                        tvInstruction.setText("Di chuyển gần hơn");
                    }
                }
            });
        }
        
        imageProxy.close();
    }
    
    private void captureFace() {
        if (isCapturing || isVerifying || imageCapture == null) return;
        
        isCapturing = true;
        mainHandler.post(() -> {
            if (tvInstruction != null) {
                tvInstruction.setText("Đang chụp...");
            }
            if (progressBar != null) {
                progressBar.setVisibility(android.view.View.VISIBLE);
            }
        });
        
        File photoFile = new File(getCacheDir(), "face_login_" + System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();
        
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Log.d(TAG, "Face captured: " + photoFile.getAbsolutePath());
                        verifyFaceWithApi(photoFile);
                    }
                    
                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Error capturing face", exception);
                        isCapturing = false;
                        mainHandler.post(() -> {
                            if (tvInstruction != null) {
                                tvInstruction.setText("Lỗi chụp ảnh. Thử lại...");
                            }
                            if (progressBar != null) {
                                progressBar.setVisibility(android.view.View.GONE);
                            }
                        });
                    }
                });
    }
    
    private void verifyFaceWithApi(File facePhoto) {
        isVerifying = true;
        
        mainHandler.post(() -> {
            if (tvInstruction != null) {
                tvInstruction.setText("Đang xác thực...");
            }
        });
        
        // Prepare multipart request
        RequestBody phoneBody = RequestBody.create(MediaType.parse("text/plain"), phone);
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), facePhoto);
        MultipartBody.Part facePhotoPart = MultipartBody.Part.createFormData(
                "facePhoto", facePhoto.getName(), requestFile);
        
        // Call API
        AuthApiService authApiService = ApiClient.getAuthApiService();
        Call<AuthResponse> call = authApiService.loginWithFace(phoneBody, facePhotoPart);
        
        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    handleLoginSuccess(authResponse);
                } else {
                    handleLoginFailure("Xác thực khuôn mặt thất bại");
                }
            }
            
            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                handleLoginFailure("Lỗi kết nối: " + t.getMessage());
            }
        });
    }
    
    private void handleLoginSuccess(AuthResponse authResponse) {
        mainHandler.post(() -> {
            // Save session
            User.UserRole role = "CUSTOMER".equalsIgnoreCase(authResponse.getRole())
                    ? User.UserRole.CUSTOMER
                    : User.UserRole.OFFICER;
            dataManager.saveLoggedInUser(phone, role);
            
            // Save user info
            if (authResponse.getUserId() != null) {
                dataManager.saveUserId(authResponse.getUserId());
            }
            if (authResponse.getPhone() != null) {
                dataManager.saveUserPhone(authResponse.getPhone());
            }
            if (authResponse.getFullName() != null) {
                dataManager.saveUserFullName(authResponse.getFullName());
                dataManager.saveLastFullName(authResponse.getFullName());
            }
            if (authResponse.getEmail() != null) {
                dataManager.saveUserEmail(authResponse.getEmail());
            }
            
            // Save tokens
            if (authResponse.getToken() != null && authResponse.getRefreshToken() != null) {
                dataManager.saveTokens(authResponse.getToken(), authResponse.getRefreshToken());
            }
            
            // Reset session
            sessionManager.onLoginSuccess();
            
            Toast.makeText(FaceLoginActivity.this, "Xác thực thành công!", Toast.LENGTH_SHORT).show();
            
            // Navigate to dashboard based on role (role đã được định nghĩa ở trên)
            Intent intent;
            if (role == User.UserRole.OFFICER) {
                intent = new Intent(FaceLoginActivity.this, OfficerDashboardActivity.class);
            } else {
                intent = new Intent(FaceLoginActivity.this,
                        com.example.mobilebanking.ui_home.UiHomeActivity.class);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
    
    private void handleLoginFailure(String errorMessage) {
        mainHandler.post(() -> {
            isCapturing = false;
            isVerifying = false;
            isReadyToCapture = false;
            
            if (progressBar != null) {
                progressBar.setVisibility(android.view.View.GONE);
            }
            
            new AlertDialog.Builder(this)
                    .setTitle("Xác Thực Thất Bại")
                    .setMessage(errorMessage + "\n\nVui lòng thử lại.")
                    .setPositiveButton("Thử Lại", (dialog, which) -> {
                        if (tvInstruction != null) {
                            tvInstruction.setText("Đặt khuôn mặt vào khung hình");
                        }
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();
        });
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
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }
}
