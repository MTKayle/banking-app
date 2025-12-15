package com.example.mobilebanking.activities;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.example.mobilebanking.api.BiometricApiService;
import com.example.mobilebanking.api.BiometricRequest;
import com.example.mobilebanking.api.BiometricResponse;
import com.example.mobilebanking.models.User;
import com.example.mobilebanking.utils.DataManager;
import com.example.mobilebanking.utils.ImageEncryptionUtil;
import com.example.mobilebanking.views.FaceDetectionOverlay;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Biometric Authentication Activity with real-time face detection and auto capture
 */
public class BiometricAuthActivity extends AppCompatActivity {
    private static final String TAG = "BiometricAuth";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 101;
    private static final int MIN_FACE_SIZE_PERCENTAGE = 60; // 60% of frame
    private static final int MIN_IMAGE_WIDTH = 640;
    private static final int MIN_IMAGE_HEIGHT = 480;
    private static final int JPEG_QUALITY = 85;
    private static final int FACE_DETECTION_TIMEOUT = 30000; // 30 seconds (increased to give user more time)
    private static final float MIN_BRIGHTNESS_THRESHOLD = 0.3f; // Minimum brightness level
    private static final int MIN_FACE_CROP_SIZE = 400; // Minimum 400x400 pixels for cropped face
    
    private PreviewView previewView;
    private FaceDetectionOverlay overlayView;
    private TextView tvStatus;
    private TextView tvInstructions;
    private Button btnCancel;
    private Button btnRetry;
    private Button btnRetake;
    private Button btnUseThis;
    private Button btnSaveImage;
    private ProgressBar progressBar;
    private ImageView ivPreview;
    
    // Captured image data
    private Bitmap capturedFaceBitmap;
    private String capturedFaceImagePath;
    private String capturedOriginalImagePath;
    private Rect capturedFaceRect;
    private long capturedTimestamp;
    private int capturedImageQuality;
    
    private ProcessCameraProvider cameraProvider;
    private Camera camera;
    private ImageCapture imageCapture;
    private FaceDetector faceDetector;
    private ExecutorService imageAnalysisExecutor;
    
    private DataManager dataManager;
    private boolean isScanning = false;
    private boolean isCapturing = false;
    private long scanStartTime;
    private Handler mainHandler;
    private Runnable timeoutRunnable;
    private String mode = "login"; // "login" or "capture"
    private boolean isTimeoutDialogShown = false; // Prevent multiple timeout dialogs
    
    // Face detection state
    private Rect currentFaceRect;
    private float currentFaceSizePercentage = 0f;
    private int faceCount = 0;
    private boolean isReadyToCapture = false;
    private long readyToCaptureTime = 0;
    private static final long CAPTURE_DELAY_MS = 3000; // Wait 3 seconds when ready before capturing
    
    // Image analysis resolution for coordinate conversion
    private int analysisImageWidth = 0;
    private int analysisImageHeight = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biometric_auth);
        
        // Check mode from intent
        if (getIntent() != null && getIntent().hasExtra("mode")) {
            mode = getIntent().getStringExtra("mode");
        }

        dataManager = DataManager.getInstance(this);
        mainHandler = new Handler(Looper.getMainLooper());
        imageAnalysisExecutor = Executors.newSingleThreadExecutor();

        initializeViews();
        setupFaceDetector();
        setupListeners();
        
        // Delay camera start to ensure activity is fully displayed and previous camera is released
        // This prevents black screen when transitioning from CccdBackScannerActivity
        previewView.postDelayed(() -> {
        if (checkCameraPermission()) {
            startCamera();
        } else {
            requestCameraPermission();
        }
        }, 300); // 300ms delay to ensure smooth transition
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Only restart camera if it was stopped and we have permission
        // Don't restart if we're already scanning or if activity is finishing
        if (!isFinishing() && !isDestroyed() && checkCameraPermission() && !isScanning && cameraProvider == null) {
            previewView.postDelayed(() -> {
                if (!isFinishing() && !isDestroyed() && checkCameraPermission()) {
                    startCamera();
                }
            }, 200);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Stop camera when activity is paused to release resources
        if (cameraProvider != null) {
            try {
                cameraProvider.unbindAll();
                Log.d(TAG, "Camera unbound in onPause");
            } catch (Exception e) {
                Log.e(TAG, "Error unbinding camera in onPause", e);
            }
        }
        isScanning = false;
    }

    private void initializeViews() {
        previewView = findViewById(R.id.preview_view);
        overlayView = findViewById(R.id.overlay_view);
        tvStatus = findViewById(R.id.tv_status);
        tvInstructions = findViewById(R.id.tv_instructions);
        btnCancel = findViewById(R.id.btn_cancel);
        btnRetry = findViewById(R.id.btn_retry);
        btnRetake = findViewById(R.id.btn_retake);
        btnUseThis = findViewById(R.id.btn_use_this);
        btnSaveImage = findViewById(R.id.btn_save_image);
        progressBar = findViewById(R.id.progress_bar);
        ivPreview = findViewById(R.id.iv_preview);
        
        btnRetry.setVisibility(View.GONE);
        btnRetake.setVisibility(View.GONE);
        btnUseThis.setVisibility(View.GONE);
        btnSaveImage.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        ivPreview.setVisibility(View.GONE);
    }
    
    private void setupFaceDetector() {
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE) // More accurate for registration
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL) // Enable eye/mouth detection
                .setMinFaceSize(0.1f)
                .enableTracking()
                .build();
        
        faceDetector = FaceDetection.getClient(options);
    }

    private void setupListeners() {
        btnCancel.setOnClickListener(v -> finish());
        btnRetry.setOnClickListener(v -> {
            resetScan();
            startCamera();
        });
        if (btnRetake != null) {
            btnRetake.setOnClickListener(v -> {
                // Retake photo - go back to camera
            resetScan();
            startCamera();
        });
        }
        if (btnUseThis != null) {
            btnUseThis.setOnClickListener(v -> {
                // Use this photo - save and return result
                useCapturedImage();
            });
        }
        if (btnSaveImage != null) {
            btnSaveImage.setOnClickListener(v -> {
                // Save image to gallery/downloads
                saveImageToGallery();
            });
        }
    }
    
    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED;
    }
    
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_REQUEST_CODE);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start camera
                if (!isFinishing() && !isDestroyed()) {
                    previewView.postDelayed(() -> {
                        if (!isFinishing() && !isDestroyed()) {
                            startCamera();
                        }
                    }, 100);
                }
            } else {
                // Permission denied, show dialog only if activity is still active
                if (!isFinishing() && !isDestroyed()) {
                    showPermissionDeniedDialog();
                }
            }
        } else if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Storage permission granted, retry saving image
                saveImageToGallery();
            } else {
                Toast.makeText(this, "Cần quyền lưu trữ để lưu ảnh. Vui lòng cấp quyền trong Cài đặt.", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private void showPermissionDeniedDialog() {
        // Check if activity is still valid before showing dialog
        if (isFinishing() || isDestroyed()) {
            return;
        }
        
        // Check if user has permanently denied permission
        boolean shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA);
        
        new AlertDialog.Builder(this)
                .setTitle("Quyền Camera")
                .setMessage(shouldShowRationale 
                    ? "Ứng dụng cần quyền camera để xác thực khuôn mặt. Vui lòng cấp quyền để tiếp tục."
                    : "Ứng dụng cần quyền camera để xác thực khuôn mặt. Vui lòng cấp quyền trong Cài đặt.")
                .setPositiveButton(shouldShowRationale ? "Đồng ý" : "Cài đặt", (dialog, which) -> {
                    if (shouldShowRationale) {
                        // Request permission again
                        requestCameraPermission();
                    } else {
                        // Open app settings
                        try {
                            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        } catch (Exception e) {
                            Log.e(TAG, "Error opening settings", e);
                            finish();
                        }
                    }
                })
                .setNegativeButton("Hủy", (dialog, which) -> finish())
                .setCancelable(false)
                .setOnDismissListener(dialog -> {
                    // Don't finish if user dismissed without action
                })
                .show();
    }
    
    private void startCamera() {
        isScanning = true;
        scanStartTime = System.currentTimeMillis();
        updateStatus("Đang khởi động camera...");
        
        Log.d(TAG, "=== Starting camera initialization ===");
        Log.d(TAG, "PreviewView state: " + (previewView != null ? "exists" : "null"));
        if (previewView != null) {
            Log.d(TAG, "PreviewView visibility: " + (previewView.getVisibility() == View.VISIBLE ? "VISIBLE" : "GONE"));
            Log.d(TAG, "PreviewView width: " + previewView.getWidth() + ", height: " + previewView.getHeight());
        }
        
        // Ensure preview view is visible
        if (previewView != null) {
            previewView.setVisibility(View.VISIBLE);
            previewView.post(() -> {
                Log.d(TAG, "PreviewView posted - width: " + previewView.getWidth() + ", height: " + previewView.getHeight());
            });
        }
        
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);
        
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                Log.d(TAG, "Camera provider obtained, binding use cases...");
                
                // Wait for preview view to be ready
                if (previewView != null) {
                    previewView.post(() -> {
                        Log.d(TAG, "PreviewView ready, binding camera...");
                bindCameraUseCases();
                        // Start timeout timer only after camera is successfully bound
                        // This gives user time to position their face
                        previewView.postDelayed(() -> {
                            if (isScanning && !isTimeoutDialogShown) {
                startTimeoutTimer();
                            }
                        }, 1000); // Wait 1 second after camera is bound before starting timeout
                    });
                } else {
                    Log.e(TAG, "PreviewView is null, cannot bind camera");
                    showError("Lỗi khởi động camera. Vui lòng thử lại.");
                }
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera", e);
                showError("Không thể khởi động camera. Vui lòng thử lại.");
            }
        }, ContextCompat.getMainExecutor(this));
    }
    
    private void bindCameraUseCases() {
        if (cameraProvider == null) {
            Log.e(TAG, "Camera provider is null, cannot bind");
            return;
        }
        
        if (previewView == null) {
            Log.e(TAG, "PreviewView is null, cannot bind");
            return;
        }
        
        Log.d(TAG, "=== Binding camera use cases ===");
        
        // Unbind all use cases before rebinding
        try {
        cameraProvider.unbindAll();
            Log.d(TAG, "Unbound all previous camera use cases");
        } catch (Exception e) {
            Log.w(TAG, "Error unbinding camera use cases", e);
        }
        
        // Ensure preview view is visible and ready
        previewView.setVisibility(View.VISIBLE);
        overlayView.setVisibility(View.VISIBLE);
        ivPreview.setVisibility(View.GONE);
        
        // Preview with proper initialization
        Preview preview = new Preview.Builder()
                .setTargetRotation(previewView.getDisplay().getRotation())
                .build();
        
        // Set surface provider - this is critical for preview to work
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        Log.d(TAG, "Preview surface provider set");
        
        // Image Analysis for face detection
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .setTargetRotation(previewView.getDisplay().getRotation())
                .build();
        
        imageAnalysis.setAnalyzer(imageAnalysisExecutor, this::analyzeImage);
        Log.d(TAG, "Image analysis configured");
        
        // Image Capture for taking photos
        imageCapture = new ImageCapture.Builder()
                .setTargetResolution(new android.util.Size(MIN_IMAGE_WIDTH * 2, MIN_IMAGE_HEIGHT * 2))
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetRotation(previewView.getDisplay().getRotation())
                .build();
        Log.d(TAG, "Image capture configured");
        
        // Camera selector - use front camera for selfie
        CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
        Log.d(TAG, "Using front camera for selfie capture");
        
        try {
            camera = cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalysis,
                    imageCapture
            );
            
            Log.d(TAG, "Camera bound successfully");
            Log.d(TAG, "Camera info: " + (camera != null ? camera.getCameraInfo().toString() : "null"));
            
            // Update UI after camera is ready
            updateStatus("Vui lòng đặt mặt vào khung");
            if ("capture".equals(mode)) {
                tvInstructions.setText("Vui lòng đưa khuôn mặt của bạn vào trong khung hình oval. Giữ yên 3 giây để tự động chụp ảnh. Vui lòng không đội mũ, không đeo khẩu trang hoặc kính.");
            } else {
            tvInstructions.setText("Bắt đầu làm thử, hãy đưa khuôn mặt của bạn vào trong khung hình. Vui lòng không đội mũ, không đeo khẩu trang hoặc đeo kính.");
            }
            
            // Force preview view to request layout
            previewView.post(() -> {
                previewView.requestLayout();
                Log.d(TAG, "PreviewView layout requested");
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error binding camera use cases", e);
            e.printStackTrace();
            showError("Không thể khởi động camera. Vui lòng thử lại.");
        }
    }
    
    private void analyzeImage(ImageProxy image) {
        if (!isScanning || isCapturing) {
            image.close();
            return;
        }
        
        // Check timeout (only if scanStartTime is set and timeout dialog not shown)
        if (scanStartTime > 0 && !isTimeoutDialogShown) {
            long elapsedTime = System.currentTimeMillis() - scanStartTime;
            if (elapsedTime > FACE_DETECTION_TIMEOUT) {
            image.close();
                mainHandler.post(() -> {
                    if (!isTimeoutDialogShown) {
                        handleTimeout();
                    }
                });
            return;
            }
        }
        
        // Store analysis image dimensions for coordinate conversion
        analysisImageWidth = image.getWidth();
        analysisImageHeight = image.getHeight();
        
        // Convert ImageProxy to InputImage
        InputImage inputImage = InputImage.fromMediaImage(
                image.getImage(),
                image.getImageInfo().getRotationDegrees()
        );
        
        // Detect faces
        faceDetector.process(inputImage)
                .addOnSuccessListener(faces -> {
                    processFaceDetectionResult(faces, image);
                    image.close();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Face detection failed", e);
                    image.close();
                });
    }
    
    private void processFaceDetectionResult(List<Face> faces, ImageProxy image) {
        if (!isScanning || isCapturing) return;
        
        faceCount = faces != null ? faces.size() : 0;
        
        if (faceCount == 0) {
            // No face detected
            currentFaceRect = null;
            currentFaceSizePercentage = 0f;
            isReadyToCapture = false;
            mainHandler.post(() -> {
                overlayView.updateFaceRect(null, 0f, false);
                updateStatus("Không phát hiện khuôn mặt. Vui lòng đặt mặt vào khung hình.");
            });
            return;
        }
        
        if (faceCount > 1) {
            // Multiple faces detected
            mainHandler.post(() -> {
                overlayView.clearFaceRect();
                updateStatus("Phát hiện nhiều khuôn mặt. Vui lòng chỉ để một người trong khung hình.");
            });
            return;
        }
        
        // Single face detected
        Face face = faces.get(0);
        Rect bounds = face.getBoundingBox();
        
        if (bounds == null || bounds.width() <= 0 || bounds.height() <= 0) {
            return;
        }
        
        // Calculate face size percentage relative to image
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        float faceArea = bounds.width() * bounds.height();
        float imageArea = imageWidth * imageHeight;
        float faceSizePercentage = (faceArea / imageArea) * 100f;
        
        // Check if face is in center area (within 20% of center)
        int centerX = imageWidth / 2;
        int centerY = imageHeight / 2;
        int faceCenterX = bounds.centerX();
        int faceCenterY = bounds.centerY();
        
        boolean isCentered = Math.abs(faceCenterX - centerX) < imageWidth * 0.2f &&
                            Math.abs(faceCenterY - centerY) < imageHeight * 0.2f;
        
        // Check face angle (allow ±15 degrees tilt)
        // getHeadEulerAngleY() returns float directly, not Float
        float faceAngleY = Math.abs(face.getHeadEulerAngleY());
        boolean isStraight = faceAngleY <= 15f; // Allow ±15 degrees
        
        // Check for face occlusion (eyes closed or covered)
        // getLeftEyeOpenProbability() and getRightEyeOpenProbability() return float directly
        // Value is -1 if not available, otherwise 0.0-1.0
        boolean hasOcclusion = false;
        float leftEyeProb = face.getLeftEyeOpenProbability();
        float rightEyeProb = face.getRightEyeOpenProbability();
        
        // Check if probabilities are available (not -1) and eyes are closed
        if (leftEyeProb >= 0f && leftEyeProb < 0.3f) {
            hasOcclusion = true;
        }
        if (rightEyeProb >= 0f && rightEyeProb < 0.3f) {
            hasOcclusion = true;
        }
        
        // Check if ready to capture (face >= 60%, centered, straight, and no occlusion)
        boolean ready = faceSizePercentage >= MIN_FACE_SIZE_PERCENTAGE && 
                       isCentered && 
                       isStraight && 
                       !hasOcclusion;
        
        // Update state
        currentFaceRect = bounds;
        currentFaceSizePercentage = faceSizePercentage;
        
        if (hasOcclusion) {
            mainHandler.post(() -> {
                updateStatus("Khuôn mặt bị che một phần. Vui lòng bỏ mũ, khẩu trang, kính.");
            });
        }
        
        if (ready && !isReadyToCapture) {
            // Just became ready
            isReadyToCapture = true;
            readyToCaptureTime = System.currentTimeMillis();
        } else if (!ready) {
            isReadyToCapture = false;
            readyToCaptureTime = 0;
            overlayView.setCountdown(0); // Clear countdown
        }
        
        // Update UI on main thread
        mainHandler.post(() -> {
            overlayView.updateFaceRect(bounds, faceSizePercentage, ready);
            
            if (ready) {
                long timeSinceReady = System.currentTimeMillis() - readyToCaptureTime;
                long remainingTime = CAPTURE_DELAY_MS - timeSinceReady;
                
                if (timeSinceReady >= CAPTURE_DELAY_MS && !isCapturing) {
                    // Auto capture using ImageCapture
                    captureImageWithImageCapture();
                } else if (remainingTime > 0) {
                    // Show countdown: 3, 2, 1
                    int secondsRemaining = (int) Math.ceil(remainingTime / 1000.0);
                    updateStatus(String.format("Giữ nguyên vị trí... %d giây (%.0f%%)", secondsRemaining, faceSizePercentage));
                    // Update overlay with countdown
                    overlayView.setCountdown(secondsRemaining);
                } else {
                    updateStatus(String.format("Giữ nguyên vị trí... (%.0f%%)", faceSizePercentage));
                }
            } else if (faceSizePercentage < MIN_FACE_SIZE_PERCENTAGE) {
                updateStatus(String.format("Di chuyển gần hơn (%.0f%% / %d%%)", 
                        faceSizePercentage, MIN_FACE_SIZE_PERCENTAGE));
            } else if (!isCentered) {
                updateStatus("Di chuyển khuôn mặt vào giữa khung hình");
            }
        });
    }
    
    private void captureImageWithImageCapture() {
        if (isCapturing || imageCapture == null) return;
        
        isCapturing = true;
        updateStatus("Đang chụp ảnh...");
        progressBar.setVisibility(View.VISIBLE);
        
        // Create output file options
        java.io.File outputFile = new java.io.File(getCacheDir(), "biometric_capture_" + System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(outputFile).build();
        
        // Capture image
        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                        // Load captured image
                        Bitmap bitmap = BitmapFactory.decodeFile(outputFile.getAbsolutePath());
                        
                        if (bitmap == null) {
                            showError("Không thể chụp ảnh. Vui lòng thử lại.");
                            resetScan();
                            return;
                        }
                        
                        // Get rotation from display and calculate needed rotation for front camera
                        // Front camera images are usually rotated 270° (or -90°) from device orientation
                        int displayRotation = previewView.getDisplay().getRotation();
                        int rotationAngle = calculateFrontCameraRotation(displayRotation);
                        Log.d(TAG, "Display rotation: " + displayRotation + ", Calculated rotation angle: " + rotationAngle);
                        
                        // Rotate bitmap if needed (front camera usually needs 270 or 90 degree rotation)
                        if (rotationAngle != 0) {
                            bitmap = rotateBitmap(bitmap, rotationAngle);
                            Log.d(TAG, "Rotated bitmap by " + rotationAngle + " degrees");
                        }
                        
                        // Validate image size
                        if (bitmap.getWidth() < MIN_IMAGE_WIDTH || bitmap.getHeight() < MIN_IMAGE_HEIGHT) {
                            // Scale up if too small
                            float scaleX = (float) MIN_IMAGE_WIDTH / bitmap.getWidth();
                            float scaleY = (float) MIN_IMAGE_HEIGHT / bitmap.getHeight();
                            float scale = Math.max(scaleX, scaleY);
                            int newWidth = (int) (bitmap.getWidth() * scale);
                            int newHeight = (int) (bitmap.getHeight() * scale);
                            bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
                        }
                        
                        // Check brightness
                        float brightness = calculateBrightness(bitmap);
                        if (brightness < MIN_BRIGHTNESS_THRESHOLD) {
                            showError("Ánh sáng quá yếu. Vui lòng tăng độ sáng hoặc di chuyển đến nơi sáng hơn.");
                            resetScan();
                            return;
                        }
                        
                        // Save face image if in capture mode
                        if ("capture".equals(mode)) {
                            // Detect face again on captured image for accurate bounding box
                            Bitmap croppedFace = detectAndCropFaceFromCapturedImage(bitmap);
                            if (croppedFace == null) {
                                // Fallback: try to convert coordinates from analysis to capture
                                Log.w(TAG, "Face detection on captured image failed, trying coordinate conversion");
                                Rect convertedRect = convertFaceRectToCaptureCoordinates(
                                        currentFaceRect, 
                                        analysisImageWidth, 
                                        analysisImageHeight,
                                        bitmap.getWidth(),
                                        bitmap.getHeight()
                                );
                                if (convertedRect != null) {
                                    croppedFace = cropFaceFromImage(bitmap, convertedRect);
                                }
                                if (croppedFace == null) {
                                    croppedFace = bitmap; // Final fallback to full image
                                    Log.w(TAG, "Using full image as fallback");
                                }
                            }
                            
                            // Store captured data
                            capturedFaceBitmap = croppedFace;
                            capturedFaceRect = currentFaceRect;
                            capturedTimestamp = System.currentTimeMillis();
                            capturedImageQuality = calculateImageQuality(croppedFace);
                            
                            // Save both original and cropped face (unencrypted for now, will encrypt when user confirms)
                            String originalPath = saveOriginalImage(bitmap);
                            String faceImagePath = saveFaceImage(croppedFace, bitmap, currentFaceRect);
                            
                            if (faceImagePath != null && originalPath != null) {
                                capturedFaceImagePath = faceImagePath;
                                capturedOriginalImagePath = originalPath;
                                
                                // Show preview with buttons
                                showImagePreviewWithButtons(croppedFace);
                                updateStatus("Chụp thành công! Vui lòng xem lại ảnh.");
                            } else {
                                showError("Không thể lưu ảnh khuôn mặt. Vui lòng thử lại.");
                                resetScan();
                            }
                        } else {
                            // Show preview and process for login mode
                            showImagePreview(bitmap);
                            processAndSendImage(bitmap);
                        }
                        
                        // Clean up temp file
                        outputFile.delete();
                    }
                    
                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Image capture failed", exception);
                        showError("Không thể chụp ảnh. Vui lòng thử lại.");
                        resetScan();
                    }
                }
        );
    }
    
    private float calculateBrightness(Bitmap bitmap) {
        long sum = 0;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int sampleSize = 10; // Sample every 10th pixel for performance
        
        for (int y = 0; y < height; y += sampleSize) {
            for (int x = 0; x < width; x += sampleSize) {
                int pixel = bitmap.getPixel(x, y);
                int r = (pixel >> 16) & 0xFF;
                int g = (pixel >> 8) & 0xFF;
                int b = pixel & 0xFF;
                // Calculate luminance
                sum += (r * 0.299 + g * 0.587 + b * 0.114);
            }
        }
        
        int sampleCount = (width / sampleSize) * (height / sampleSize);
        return (sum / (float) sampleCount) / 255f;
    }
    
    private void showImagePreview(Bitmap bitmap) {
        ivPreview.setVisibility(View.VISIBLE);
        ivPreview.setImageBitmap(bitmap);
        previewView.setVisibility(View.GONE);
        overlayView.setVisibility(View.GONE);
    }
    
    private void showImagePreviewWithButtons(Bitmap bitmap) {
        showImagePreview(bitmap);
        // Show retake, use, and save buttons
        if (btnRetake != null) {
            btnRetake.setVisibility(View.VISIBLE);
        }
        if (btnUseThis != null) {
            btnUseThis.setVisibility(View.VISIBLE);
        }
        if (btnSaveImage != null) {
            btnSaveImage.setVisibility(View.VISIBLE);
        }
        // Hide cancel button when showing preview buttons (to avoid overlap)
        btnCancel.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);
    }
    
    private void useCapturedImage() {
        if (capturedFaceImagePath == null || capturedFaceBitmap == null) {
            showError("Không có ảnh để sử dụng. Vui lòng chụp lại.");
            return;
        }
        
        updateStatus("Đang lưu ảnh...");
        progressBar.setVisibility(View.VISIBLE);
        
        // Save with encryption and metadata in background
        new Thread(() -> {
            try {
                // Encrypt images
                java.io.File originalFile = new java.io.File(capturedOriginalImagePath);
                java.io.File faceFile = new java.io.File(capturedFaceImagePath);
                
                java.io.File encryptedDir = new java.io.File(getFilesDir(), "face_images_encrypted");
                if (!encryptedDir.exists()) {
                    encryptedDir.mkdirs();
                }
                
                java.io.File encryptedOriginalFile = new java.io.File(encryptedDir, "encrypted_original_" + capturedTimestamp + ".dat");
                java.io.File encryptedFaceFile = new java.io.File(encryptedDir, "encrypted_face_" + capturedTimestamp + ".dat");
                
                boolean encrypted = ImageEncryptionUtil.encryptImageFile(originalFile, encryptedOriginalFile) &&
                                   ImageEncryptionUtil.encryptImageFile(faceFile, encryptedFaceFile);
                
                if (!encrypted) {
                    Log.w(TAG, "Encryption failed, but continuing with unencrypted files");
                }
                
                // Prepare base64 encoded image for backend
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                capturedFaceBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream);
                byte[] imageBytes = outputStream.toByteArray();
                String base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
                
                // Also prepare encrypted base64
                String encryptedBase64 = ImageEncryptionUtil.encryptAndEncodeBase64(imageBytes);
                
                // Save metadata
                saveFaceMetadata(capturedFaceImagePath, capturedOriginalImagePath, 
                               encryptedOriginalFile.getAbsolutePath(), encryptedFaceFile.getAbsolutePath(),
                               base64Image, encryptedBase64);
                
                // Return result on main thread
                // DO NOT pass base64 via Intent - it causes TransactionTooLargeException
                // Base64 can be generated from file path later if needed
                mainHandler.post(() -> {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("face_image_path", capturedFaceImagePath);
                    resultIntent.putExtra("face_image_original_path", capturedOriginalImagePath);
                    resultIntent.putExtra("face_image_encrypted_path", encryptedFaceFile.getAbsolutePath());
                    resultIntent.putExtra("face_image_original_encrypted_path", encryptedOriginalFile.getAbsolutePath());
                    resultIntent.putExtra("face_bounding_box_left", capturedFaceRect != null ? capturedFaceRect.left : 0);
                    resultIntent.putExtra("face_bounding_box_top", capturedFaceRect != null ? capturedFaceRect.top : 0);
                    resultIntent.putExtra("face_bounding_box_right", capturedFaceRect != null ? capturedFaceRect.right : 0);
                    resultIntent.putExtra("face_bounding_box_bottom", capturedFaceRect != null ? capturedFaceRect.bottom : 0);
                    resultIntent.putExtra("capture_timestamp", capturedTimestamp);
                    resultIntent.putExtra("image_quality", capturedImageQuality);
                    // Removed base64 to avoid TransactionTooLargeException
                    // Base64 is saved in metadata file, can be loaded from there if needed
                    // resultIntent.putExtra("face_image_base64", base64Image);
                    // resultIntent.putExtra("face_image_encrypted_base64", encryptedBase64);
                    setResult(RESULT_OK, resultIntent);
                    
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(BiometricAuthActivity.this, "Đã lưu ảnh khuôn mặt thành công!", Toast.LENGTH_SHORT).show();
                    
                    // Finish after a short delay to ensure result is set
                    mainHandler.postDelayed(() -> {
                        if (!isFinishing() && !isDestroyed()) {
                            finish();
                        }
                    }, 300);
                });
            } catch (Exception e) {
                Log.e(TAG, "Error saving encrypted image", e);
                mainHandler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    showError("Lỗi khi lưu ảnh. Vui lòng thử lại.");
                });
            }
        }).start();
    }
    
    private void saveFaceMetadata(String facePath, String originalPath, String encryptedOriginalPath, 
                                  String encryptedFacePath, String base64Image, String encryptedBase64) {
        try {
            java.io.File metadataDir = new java.io.File(getFilesDir(), "face_metadata");
            if (!metadataDir.exists()) {
                metadataDir.mkdirs();
            }
            
            java.io.File metadataFile = new java.io.File(metadataDir, "metadata_" + capturedTimestamp + ".json");
            java.io.FileWriter writer = new java.io.FileWriter(metadataFile);
            
            // Create JSON metadata
            writer.write("{\n");
            writer.write("  \"timestamp\": " + capturedTimestamp + ",\n");
            writer.write("  \"face_image_path\": \"" + facePath + "\",\n");
            writer.write("  \"original_image_path\": \"" + originalPath + "\",\n");
            writer.write("  \"encrypted_face_path\": \"" + encryptedFacePath + "\",\n");
            writer.write("  \"encrypted_original_path\": \"" + encryptedOriginalPath + "\",\n");
            writer.write("  \"bounding_box\": {\n");
            writer.write("    \"left\": " + (capturedFaceRect != null ? capturedFaceRect.left : 0) + ",\n");
            writer.write("    \"top\": " + (capturedFaceRect != null ? capturedFaceRect.top : 0) + ",\n");
            writer.write("    \"right\": " + (capturedFaceRect != null ? capturedFaceRect.right : 0) + ",\n");
            writer.write("    \"bottom\": " + (capturedFaceRect != null ? capturedFaceRect.bottom : 0) + "\n");
            writer.write("  },\n");
            writer.write("  \"image_quality\": " + capturedImageQuality + ",\n");
            writer.write("  \"image_width\": " + capturedFaceBitmap.getWidth() + ",\n");
            writer.write("  \"image_height\": " + capturedFaceBitmap.getHeight() + ",\n");
            writer.write("  \"has_base64\": " + (base64Image != null) + ",\n");
            writer.write("  \"has_encrypted_base64\": " + (encryptedBase64 != null) + "\n");
            writer.write("}\n");
            
            writer.flush();
            writer.close();
            
            Log.d(TAG, "Metadata saved: " + metadataFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "Error saving metadata", e);
        }
    }
    
    private void processAndSendImage(Bitmap bitmap) {
        updateStatus("Đang xử lý ảnh...");
        
        // Encode to base64
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        String base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
        
        // Create request
        BiometricRequest request = new BiometricRequest();
        request.setImageBase64(base64Image);
        request.setTimestamp(String.valueOf(System.currentTimeMillis()));
        request.setDeviceInfo(Build.MANUFACTURER + " " + Build.MODEL + " Android " + Build.VERSION.RELEASE);
        request.setImageWidth(bitmap.getWidth());
        request.setImageHeight(bitmap.getHeight());
        
        // Get current user if logged in
        String username = dataManager.getLoggedInUser();
        if (username != null) {
            request.setUserId(username);
        }
        
        // Send to backend
        sendToBackend(request);
    }
    
    private void sendToBackend(BiometricRequest request) {
        updateStatus("Đang gửi đến server...");
        
        BiometricApiService apiService = ApiClient.getBiometricApiService();
        retrofit2.Call<BiometricResponse> call = apiService.registerBiometric(request);
        
        call.enqueue(new retrofit2.Callback<BiometricResponse>() {
            @Override
            public void onResponse(retrofit2.Call<BiometricResponse> call,
                                 retrofit2.Response<BiometricResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BiometricResponse biometricResponse = response.body();
                    if (biometricResponse.isSuccess()) {
                        handleSuccess();
                    } else {
                        handleBackendError(biometricResponse.getMessage());
                    }
                } else {
                    handleBackendError("Lỗi kết nối server. Vui lòng thử lại.");
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<BiometricResponse> call, Throwable t) {
                Log.e(TAG, "Network error", t);
                // Save locally and retry option
                handleNetworkError();
            }
        });
    }
    
    /**
     * Detect face on captured image and crop it accurately
     */
    private Bitmap detectAndCropFaceFromCapturedImage(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        
        try {
            // Create InputImage from bitmap
            InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
            
            // Detect face on captured image synchronously (on background thread)
            final Bitmap[] result = {null};
            final Object lock = new Object();
            final boolean[] completed = {false};
            
            faceDetector.process(inputImage)
                    .addOnSuccessListener(faces -> {
                        synchronized (lock) {
                            if (faces != null && !faces.isEmpty()) {
                                Face face = faces.get(0);
                                Rect bounds = face.getBoundingBox();
                                
                                if (bounds != null && bounds.width() > 0 && bounds.height() > 0) {
                                    // Crop with padding
                                    Bitmap cropped = cropFaceFromImage(bitmap, bounds);
                                    result[0] = cropped;
                                    Log.d(TAG, "Face detected on captured image: " + bounds.toString());
                                    Log.d(TAG, "Cropped face size: " + 
                                          (cropped != null ? cropped.getWidth() + "x" + cropped.getHeight() : "null"));
                                }
                            }
                            completed[0] = true;
                            lock.notify();
                        }
                    })
                    .addOnFailureListener(e -> {
                        synchronized (lock) {
                            Log.e(TAG, "Face detection on captured image failed", e);
                            completed[0] = true;
                            lock.notify();
                        }
                    });
            
            // Wait for detection to complete (with timeout)
            synchronized (lock) {
                if (!completed[0]) {
                    try {
                        lock.wait(2000); // 2 second timeout
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Interrupted while waiting for face detection", e);
                    }
                }
            }
            
            return result[0];
        } catch (Exception e) {
            Log.e(TAG, "Error detecting face on captured image", e);
            return null;
        }
    }
    
    /**
     * Convert face rect from analysis image coordinates to capture image coordinates
     */
    private Rect convertFaceRectToCaptureCoordinates(Rect analysisRect, 
                                                     int analysisWidth, 
                                                     int analysisHeight,
                                                     int captureWidth,
                                                     int captureHeight) {
        if (analysisRect == null || analysisWidth <= 0 || analysisHeight <= 0 || 
            captureWidth <= 0 || captureHeight <= 0) {
            return null;
        }
        
        try {
            // Calculate scale factors
            float scaleX = (float) captureWidth / analysisWidth;
            float scaleY = (float) captureHeight / analysisHeight;
            
            // Convert coordinates
            int left = (int) (analysisRect.left * scaleX);
            int top = (int) (analysisRect.top * scaleY);
            int right = (int) (analysisRect.right * scaleX);
            int bottom = (int) (analysisRect.bottom * scaleY);
            
            // Ensure within bounds
            left = Math.max(0, Math.min(left, captureWidth));
            top = Math.max(0, Math.min(top, captureHeight));
            right = Math.max(left, Math.min(right, captureWidth));
            bottom = Math.max(top, Math.min(bottom, captureHeight));
            
            Rect convertedRect = new Rect(left, top, right, bottom);
            Log.d(TAG, "Converted face rect from analysis (" + analysisWidth + "x" + analysisHeight + 
                  ") to capture (" + captureWidth + "x" + captureHeight + "): " + convertedRect.toString());
            
            return convertedRect;
        } catch (Exception e) {
            Log.e(TAG, "Error converting face rect coordinates", e);
            return null;
        }
    }
    
    /**
     * Crop face from image based on bounding box
     */
    private Bitmap cropFaceFromImage(Bitmap bitmap, Rect faceRect) {
        if (bitmap == null || faceRect == null) {
            return null;
        }
        
        try {
            // Add padding around face (30% on each side for better crop)
            int paddingX = (int) (faceRect.width() * 0.3f);
            int paddingY = (int) (faceRect.height() * 0.3f);
            
            int left = Math.max(0, faceRect.left - paddingX);
            int top = Math.max(0, faceRect.top - paddingY);
            int right = Math.min(bitmap.getWidth(), faceRect.right + paddingX);
            int bottom = Math.min(bitmap.getHeight(), faceRect.bottom + paddingY);
            
            int width = right - left;
            int height = bottom - top;
            
            if (width <= 0 || height <= 0) {
                Log.w(TAG, "Invalid crop dimensions: " + width + "x" + height);
                return null;
            }
            
            // Ensure minimum size
            if (width < 100 || height < 100) {
                Log.w(TAG, "Crop too small: " + width + "x" + height);
                return null;
            }
            
            // Crop the face
            Bitmap cropped = Bitmap.createBitmap(bitmap, left, top, width, height);
            
            // Scale to minimum size if needed
            if (cropped.getWidth() < MIN_FACE_CROP_SIZE || cropped.getHeight() < MIN_FACE_CROP_SIZE) {
                float scale = Math.max(
                    (float) MIN_FACE_CROP_SIZE / cropped.getWidth(),
                    (float) MIN_FACE_CROP_SIZE / cropped.getHeight()
                );
                int newWidth = (int) (cropped.getWidth() * scale);
                int newHeight = (int) (cropped.getHeight() * scale);
                Bitmap scaled = Bitmap.createScaledBitmap(cropped, newWidth, newHeight, true);
                if (scaled != cropped) {
                    cropped.recycle();
                }
                cropped = scaled;
            }
            
            Log.d(TAG, "Cropped face from " + bitmap.getWidth() + "x" + bitmap.getHeight() + 
                  " to " + cropped.getWidth() + "x" + cropped.getHeight() + 
                  " (rect: " + faceRect.toString() + ")");
            return cropped;
        } catch (Exception e) {
            Log.e(TAG, "Error cropping face", e);
            return null;
        }
    }
    
    /**
     * Calculate image quality score (0-100)
     */
    private int calculateImageQuality(Bitmap bitmap) {
        if (bitmap == null) return 0;
        
        float brightness = calculateBrightness(bitmap);
        int brightnessScore = (int) (brightness * 50); // Max 50 points
        
        // Check sharpness (simple edge detection)
        int sharpnessScore = 0;
        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int edgeCount = 0;
            int sampleCount = 0;
            
            for (int y = 1; y < height - 1; y += 5) {
                for (int x = 1; x < width - 1; x += 5) {
                    int pixel1 = bitmap.getPixel(x, y);
                    int pixel2 = bitmap.getPixel(x + 1, y);
                    int diff = Math.abs(getLuminance(pixel1) - getLuminance(pixel2));
                    if (diff > 30) {
                        edgeCount++;
                    }
                    sampleCount++;
                }
            }
            
            if (sampleCount > 0) {
                float edgeRatio = (float) edgeCount / sampleCount;
                sharpnessScore = (int) (edgeRatio * 50); // Max 50 points
            }
        } catch (Exception e) {
            Log.e(TAG, "Error calculating sharpness", e);
        }
        
        int totalScore = Math.min(100, brightnessScore + sharpnessScore);
        Log.d(TAG, "Image quality: " + totalScore + " (brightness: " + brightnessScore + ", sharpness: " + sharpnessScore + ")");
        return totalScore;
    }
    
    private int getLuminance(int pixel) {
        int r = (pixel >> 16) & 0xFF;
        int g = (pixel >> 8) & 0xFF;
        int b = pixel & 0xFF;
        return (int) (r * 0.299 + g * 0.587 + b * 0.114);
    }
    
    /**
     * Calculate rotation angle needed for front camera based on display rotation
     * Front camera sensor is typically rotated 270° relative to device orientation
     */
    private int calculateFrontCameraRotation(int displayRotation) {
        // Front camera images are typically rotated 270° (or -90°) from device orientation
        // We need to rotate them back to match the preview orientation
        int rotationAngle = 0;
        
        switch (displayRotation) {
            case android.view.Surface.ROTATION_0:
                // Device is in portrait, front camera image is rotated 270° clockwise
                // Need to rotate 90° counter-clockwise (or 270° clockwise)
                rotationAngle = 270;
                break;
            case android.view.Surface.ROTATION_90:
                // Device is in landscape (rotated left), front camera image is rotated 180°
                rotationAngle = 180;
                break;
            case android.view.Surface.ROTATION_180:
                // Device is in reverse portrait, front camera image is rotated 90° clockwise
                rotationAngle = 90;
                break;
            case android.view.Surface.ROTATION_270:
                // Device is in landscape (rotated right), front camera image is already correct
                rotationAngle = 0;
                break;
        }
        
        Log.d(TAG, "Display rotation: " + displayRotation + " -> Front camera rotation: " + rotationAngle);
        return rotationAngle;
    }
    
    /**
     * Rotate bitmap by specified degrees
     */
    private Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        if (bitmap == null || degrees == 0) {
            return bitmap;
        }
        
        try {
            android.graphics.Matrix matrix = new android.graphics.Matrix();
            matrix.postRotate(degrees);
            Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            if (rotated != bitmap) {
                bitmap.recycle();
            }
            Log.d(TAG, "Rotated bitmap from " + bitmap.getWidth() + "x" + bitmap.getHeight() + 
                  " to " + rotated.getWidth() + "x" + rotated.getHeight() + " by " + degrees + " degrees");
            return rotated;
        } catch (Exception e) {
            Log.e(TAG, "Error rotating bitmap", e);
            return bitmap;
        }
    }
    
    /**
     * Save original full image
     */
    private String saveOriginalImage(Bitmap bitmap) {
        try {
            java.io.File internalDir = new java.io.File(getFilesDir(), "face_images");
            if (!internalDir.exists()) {
                internalDir.mkdirs();
            }
            
            String fileName = "face_original_" + System.currentTimeMillis() + ".jpg";
            java.io.File imageFile = new java.io.File(internalDir, fileName);
            
            java.io.FileOutputStream fos = new java.io.FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, fos);
            fos.flush();
            fos.close();
            
            Log.d(TAG, "Original image saved: " + imageFile.getAbsolutePath());
            return imageFile.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "Error saving original image", e);
            return null;
        }
    }
    
    /**
     * Save cropped face image with metadata
     */
    private String saveFaceImage(Bitmap croppedFace, Bitmap originalImage, Rect faceRect) {
        try {
            java.io.File internalDir = new java.io.File(getFilesDir(), "face_images");
            if (!internalDir.exists()) {
                internalDir.mkdirs();
            }
            
            // Check available storage space
            long availableSpace = internalDir.getFreeSpace();
            long requiredSpace = croppedFace.getByteCount() * 2; // Estimate
            if (availableSpace < requiredSpace) {
                Log.e(TAG, "Insufficient storage space. Available: " + availableSpace + ", Required: " + requiredSpace);
                return null;
            }
            
            String fileName = "face_" + System.currentTimeMillis() + ".jpg";
            java.io.File faceImageFile = new java.io.File(internalDir, fileName);
            
            java.io.FileOutputStream fos = new java.io.FileOutputStream(faceImageFile);
            croppedFace.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, fos);
            fos.flush();
            fos.close();
            
            Log.d(TAG, "Face image saved: " + faceImageFile.getAbsolutePath() + 
                  " (Size: " + croppedFace.getWidth() + "x" + croppedFace.getHeight() + ")");
            return faceImageFile.getAbsolutePath();
        } catch (java.io.IOException e) {
            Log.e(TAG, "Error saving face image", e);
            return null;
        }
    }
    
    private void handleSuccess() {
        updateStatus("Xác thực thành công!");
        progressBar.setVisibility(View.GONE);
        
        // Only login if not in capture mode
        if (!"capture".equals(mode)) {
            // Mock login for demo (in real app, backend would handle this)
                User mockUser = dataManager.getMockUsers().get(0);
                dataManager.saveLoggedInUser(mockUser.getUsername(), mockUser.getRole());
                
            Toast.makeText(this, "Xác thực khuôn mặt thành công!", Toast.LENGTH_SHORT).show();
                
            // Navigate to dashboard after delay
            mainHandler.postDelayed(() -> {
                    Intent intent = new Intent(BiometricAuthActivity.this, CustomerDashboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
            }, 1500);
        }
    }
    
    private void handleBackendError(String message) {
        progressBar.setVisibility(View.GONE);
        showError(message);
        btnRetry.setVisibility(View.VISIBLE);
    }
    
    private void handleNetworkError() {
        progressBar.setVisibility(View.GONE);
        new AlertDialog.Builder(this)
                .setTitle("Lỗi kết nối")
                .setMessage("Không thể kết nối đến server. Ảnh đã được lưu tạm. Bạn có muốn thử lại?")
                .setPositiveButton("Thử lại", (dialog, which) -> {
                    // TODO: Retry with saved image
                    resetScan();
                    startCamera();
                })
                .setNegativeButton("Hủy", (dialog, which) -> finish())
                .show();
    }
    
    private void handleTimeout() {
        if (!isScanning || isTimeoutDialogShown) {
            Log.d(TAG, "Timeout ignored - isScanning: " + isScanning + ", isTimeoutDialogShown: " + isTimeoutDialogShown);
            return;
        }
        
        isTimeoutDialogShown = true;
        isScanning = false; // Stop scanning to prevent multiple dialogs
        
        // Cancel timeout timer
        if (timeoutRunnable != null) {
            mainHandler.removeCallbacks(timeoutRunnable);
            timeoutRunnable = null;
        }
        
        new AlertDialog.Builder(this)
                .setTitle("Hết thời gian")
                .setMessage("Không phát hiện khuôn mặt sau 30 giây. Vui lòng:\n" +
                        "• Đảm bảo ánh sáng đủ sáng\n" +
                        "• Đặt khuôn mặt vào khung hình oval\n" +
                        "• Giữ điện thoại ổn định\n" +
                        "• Không đội mũ, đeo khẩu trang hoặc kính")
                .setPositiveButton("Thử lại", (dialog, which) -> {
                    isTimeoutDialogShown = false;
                    resetScan();
                    // Delay camera start to ensure clean state
                    previewView.postDelayed(() -> {
                        if (!isFinishing() && !isDestroyed()) {
                    startCamera();
                        }
                    }, 300);
                })
                .setNegativeButton("Hủy", (dialog, which) -> finish())
                .setCancelable(false)
                .setOnDismissListener(dialog -> {
                    isTimeoutDialogShown = false;
                })
                .show();
    }
    
    private void resetScan() {
        isScanning = false;
        isCapturing = false;
        isReadyToCapture = false;
        isTimeoutDialogShown = false; // Reset timeout dialog flag
        currentFaceRect = null;
        currentFaceSizePercentage = 0f;
        faceCount = 0;
        readyToCaptureTime = 0;
        scanStartTime = 0; // Reset scan start time
        
        // Clear captured data
        capturedFaceBitmap = null;
        capturedFaceImagePath = null;
        capturedOriginalImagePath = null;
        capturedFaceRect = null;
        capturedTimestamp = 0;
        capturedImageQuality = 0;
        
        progressBar.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);
        if (btnRetake != null) {
            btnRetake.setVisibility(View.GONE);
        }
        if (btnUseThis != null) {
            btnUseThis.setVisibility(View.GONE);
        }
        if (btnSaveImage != null) {
            btnSaveImage.setVisibility(View.GONE);
        }
        // Show cancel button when resetting (camera mode)
        btnCancel.setVisibility(View.VISIBLE);
        ivPreview.setVisibility(View.GONE);
        previewView.setVisibility(View.VISIBLE);
        overlayView.setVisibility(View.VISIBLE);
        overlayView.clearFaceRect();
        
        // Cancel any pending timeout
        if (timeoutRunnable != null) {
            mainHandler.removeCallbacks(timeoutRunnable);
            timeoutRunnable = null;
        }
    }
    
    private void startTimeoutTimer() {
        // Cancel any existing timeout timer first
        if (timeoutRunnable != null) {
            mainHandler.removeCallbacks(timeoutRunnable);
            timeoutRunnable = null;
        }
        
        // Reset scan start time when starting timer
        scanStartTime = System.currentTimeMillis();
        
        // Start new timeout timer
        timeoutRunnable = () -> {
            if (isScanning && !isCapturing && !isTimeoutDialogShown) {
                Log.d(TAG, "Timeout triggered after " + FACE_DETECTION_TIMEOUT + "ms");
                handleTimeout();
            } else {
                Log.d(TAG, "Timeout ignored - isScanning: " + isScanning + ", isCapturing: " + isCapturing + ", isTimeoutDialogShown: " + isTimeoutDialogShown);
            }
        };
        mainHandler.postDelayed(timeoutRunnable, FACE_DETECTION_TIMEOUT);
        Log.d(TAG, "Timeout timer started, will trigger after " + FACE_DETECTION_TIMEOUT + "ms");
    }
    
    private void updateStatus(String status) {
        tvStatus.setText(status);
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        updateStatus(message);
    }
    
    /**
     * Save captured image to gallery/downloads
     */
    private void saveImageToGallery() {
        if (capturedFaceBitmap == null) {
            Toast.makeText(this, "Không có ảnh để lưu", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Android 10+ (API 29+) uses scoped storage with MediaStore - no permission needed
        // Android 13+ (API 33+) needs READ_MEDIA_IMAGES for reading, but writing to MediaStore doesn't need permission
        // Only Android < 10 needs WRITE_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            // Android 9 and below - need WRITE_EXTERNAL_STORAGE permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != 
                PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 
                    STORAGE_PERMISSION_REQUEST_CODE);
                return;
            }
        }
        // For Android 10+, MediaStore API doesn't require permission, proceed directly
        
        new Thread(() -> {
            try {
                String fileName = "face_capture_" + System.currentTimeMillis() + ".jpg";
                Uri imageUri = null;
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Use MediaStore for Android 10+
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                    contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MobileBanking");
                    
                    imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                    
                    if (imageUri != null) {
                        java.io.OutputStream outputStream = getContentResolver().openOutputStream(imageUri);
                        if (outputStream != null) {
                            capturedFaceBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream);
                            outputStream.close();
                        }
                    }
                } else {
                    // Use file system for older Android versions
                    java.io.File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                    java.io.File appDir = new java.io.File(picturesDir, "MobileBanking");
                    if (!appDir.exists()) {
                        appDir.mkdirs();
                    }
                    
                    java.io.File imageFile = new java.io.File(appDir, fileName);
                    java.io.FileOutputStream fos = new java.io.FileOutputStream(imageFile);
                    capturedFaceBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, fos);
                    fos.flush();
                    fos.close();
                    
                    // Notify media scanner
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    imageUri = Uri.fromFile(imageFile);
                    mediaScanIntent.setData(imageUri);
                    sendBroadcast(mediaScanIntent);
                }
                
                // Create final variables for use in lambda
                final Uri finalImageUri = imageUri;
                final String finalFileName = fileName;
                
                if (finalImageUri != null) {
                    mainHandler.post(() -> {
                        Toast.makeText(this, "Đã lưu ảnh vào thư viện ảnh: " + finalFileName, Toast.LENGTH_LONG).show();
                        Log.d(TAG, "Image saved to gallery: " + finalImageUri.toString());
                    });
                } else {
                    mainHandler.post(() -> {
                        Toast.makeText(this, "Không thể lưu ảnh. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error saving image to gallery", e);
                final String errorMessage = e.getMessage();
                mainHandler.post(() -> {
                    Toast.makeText(this, "Lỗi khi lưu ảnh: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isScanning = false;
        isCapturing = false;
        
        if (timeoutRunnable != null) {
            mainHandler.removeCallbacks(timeoutRunnable);
        }
        
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        
        if (faceDetector != null) {
            faceDetector.close();
        }
        
        if (imageAnalysisExecutor != null) {
            imageAnalysisExecutor.shutdown();
        }
    }
}
