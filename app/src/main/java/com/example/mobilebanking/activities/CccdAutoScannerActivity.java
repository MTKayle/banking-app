package com.example.mobilebanking.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.Surface;
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
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Auto CCCD Scanner Activity
 * Automatically detects and captures CCCD, then extracts portrait with high quality
 */
public class CccdAutoScannerActivity extends AppCompatActivity {
    private static final String TAG = "CccdAutoScanner";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;
    private static final int SCAN_TIMEOUT = 10000; // 10 seconds
    private static final int MIN_PORTRAIT_WIDTH = 600;
    private static final int MIN_PORTRAIT_HEIGHT = 800;
    private static final int TARGET_PORTRAIT_WIDTH = 800;
    private static final int TARGET_PORTRAIT_HEIGHT = 1000;
    private static final float MIN_BRIGHTNESS_THRESHOLD = 0.3f;
    private static final int JPEG_QUALITY = 95; // High quality
    
    // CCCD detection keywords (Vietnamese)
    private static final String[] CCCD_KEYWORDS = {
        "CĂN CƯỚC CÔNG DÂN",
        "CĂN CƯỚC",
        "CÔNG DÂN",
        "CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM",
        "SOCIALIST REPUBLIC OF VIETNAM"
    };
    
    private PreviewView previewView;
    private TextView tvStatus;
    private TextView tvInstructions;
    private Button btnCancel;
    private Button btnRetry;
    private ProgressBar progressBar;
    private ImageView ivPreview;
    private ImageView ivPortraitPreview;
    
    private ProcessCameraProvider cameraProvider;
    private Camera camera;
    private ImageCapture imageCapture;
    private FaceDetector faceDetector;
    private TextRecognizer textRecognizer;
    private ExecutorService imageAnalysisExecutor;
    
    private boolean isScanning = false;
    private boolean isCapturing = false;
    private boolean isTimeoutDialogShown = false; // Flag to prevent multiple dialogs
    private long scanStartTime;
    private Handler mainHandler;
    private Runnable timeoutRunnable;
    
    // Orientation handling
    private OrientationEventListener orientationEventListener;
    private int currentOrientation = 0; // 0, 90, 180, 270 degrees
    private int imageCaptureRotation = 0;
    
    // Detection state
    private boolean cccdDetected = false;
    private long cccdDetectedTime = 0;
    private static final long CAPTURE_DELAY_MS = 1500; // Wait 1.5s after detection before capture
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cccd_auto_scanner);
        
        mainHandler = new Handler(Looper.getMainLooper());
        imageAnalysisExecutor = Executors.newSingleThreadExecutor();
        
        initializeViews();
        setupMLKit();
        setupListeners();
        setupOrientationListener();
        
        if (checkCameraPermission()) {
            startCamera();
        } else {
            requestCameraPermission();
        }
    }
    
    private void setupOrientationListener() {
        orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (orientation == ORIENTATION_UNKNOWN) {
                    return;
                }
                
                // Update rotation based on current display rotation
                // This is more reliable than calculating from orientation angle
                runOnUiThread(() -> {
                    Display display = getWindowManager().getDefaultDisplay();
                    int rotation = display.getRotation();
                    
                    // Convert to degrees for logging
                    int rotationDegrees = 0;
                    switch (rotation) {
                        case Surface.ROTATION_0:
                            rotationDegrees = 0;
                            break;
                        case Surface.ROTATION_90:
                            rotationDegrees = 90;
                            break;
                        case Surface.ROTATION_180:
                            rotationDegrees = 180;
                            break;
                        case Surface.ROTATION_270:
                            rotationDegrees = 270;
                            break;
                    }
                    
                    // Update ImageCapture rotation if it changed
                    if (imageCapture != null) {
                        imageCapture.setTargetRotation(rotation);
                        Log.d(TAG, "ImageCapture rotation updated to: " + rotation + " (" + rotationDegrees + "°)");
                    }
                    
                    // Update Preview rotation if camera is bound
                    if (cameraProvider != null && previewView != null) {
                        // Preview rotation is handled automatically by PreviewView
                        // But we can update it explicitly if needed
                    }
                });
            }
        };
        
        if (orientationEventListener.canDetectOrientation()) {
            orientationEventListener.enable();
            Log.d(TAG, "Orientation listener enabled");
        } else {
            Log.w(TAG, "Orientation detection not available, using display rotation");
            // Fallback: use display rotation
            updateOrientationFromDisplay();
        }
    }
    
    private void updateOrientationFromDisplay() {
        Display display = getWindowManager().getDefaultDisplay();
        int rotation = display.getRotation();
        
        switch (rotation) {
            case Surface.ROTATION_0:
                currentOrientation = 0;
                break;
            case Surface.ROTATION_90:
                currentOrientation = 90;
                break;
            case Surface.ROTATION_180:
                currentOrientation = 180;
                break;
            case Surface.ROTATION_270:
                currentOrientation = 270;
                break;
        }
        
        updateImageCaptureRotation();
    }
    
    private void updateImageCaptureRotation() {
        if (imageCapture == null) return;
        
        // Get display rotation
        Display display = getWindowManager().getDefaultDisplay();
        int rotation = display.getRotation();
        
        // Convert Surface.ROTATION to degrees
        int rotationDegrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                rotationDegrees = 0;
                break;
            case Surface.ROTATION_90:
                rotationDegrees = 90;
                break;
            case Surface.ROTATION_180:
                rotationDegrees = 180;
                break;
            case Surface.ROTATION_270:
                rotationDegrees = 270;
                break;
        }
        
        // Set target rotation for ImageCapture
        // CameraX will automatically handle the rotation based on sensor orientation
        imageCapture.setTargetRotation(rotation);
        
        Log.d(TAG, "ImageCapture target rotation set to: " + rotation + " (degrees: " + rotationDegrees + ")");
    }
    
    private void initializeViews() {
        previewView = findViewById(R.id.preview_view);
        tvStatus = findViewById(R.id.tv_status);
        tvInstructions = findViewById(R.id.tv_instructions);
        btnCancel = findViewById(R.id.btn_cancel);
        btnRetry = findViewById(R.id.btn_retry);
        progressBar = findViewById(R.id.progress_bar);
        ivPreview = findViewById(R.id.iv_preview);
        ivPortraitPreview = findViewById(R.id.iv_portrait_preview);
        
        btnRetry.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        ivPreview.setVisibility(View.GONE);
        ivPortraitPreview.setVisibility(View.GONE);
    }
    
    private void setupMLKit() {
        // Face Detector
        FaceDetectorOptions faceOptions = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                .setMinFaceSize(0.1f)
                .enableTracking()
                .build();
        faceDetector = FaceDetection.getClient(faceOptions);
        
        // Text Recognizer
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    }
    
    private void setupListeners() {
        btnCancel.setOnClickListener(v -> finish());
        btnRetry.setOnClickListener(v -> {
            resetScan();
            startCamera();
        });
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
                startCamera();
            } else {
                showPermissionDeniedDialog();
            }
        }
    }
    
    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Quyền Camera")
                .setMessage("Ứng dụng cần quyền camera để quét CCCD. Vui lòng cấp quyền trong Cài đặt.")
                .setPositiveButton("Đồng ý", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
    
    private void startCamera() {
        isScanning = true;
        scanStartTime = System.currentTimeMillis();
        updateStatus("Đang khởi động camera...");
        
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);
        
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
                startTimeoutTimer();
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera", e);
                showError("Không thể khởi động camera. Vui lòng thử lại.");
            }
        }, ContextCompat.getMainExecutor(this));
    }
    
    private void bindCameraUseCases() {
        if (cameraProvider == null) return;
        
        cameraProvider.unbindAll();
        
        // Preview with orientation support
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        
        // Set preview target rotation to match device orientation
        Display display = getWindowManager().getDefaultDisplay();
        int rotation = display.getRotation();
        preview.setTargetRotation(rotation);
        
        // Image Analysis for CCCD and face detection
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build();
        
        imageAnalysis.setAnalyzer(imageAnalysisExecutor, this::analyzeImage);
        
        // Image Capture with orientation support
        imageCapture = new ImageCapture.Builder()
                .setTargetResolution(new android.util.Size(1920, 2560)) // High resolution
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();
        
        // Set initial rotation
        updateImageCaptureRotation();
        
        // Camera selector - use back camera for CCCD scanning
        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
        
        try {
            camera = cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalysis,
                    imageCapture
            );
            
            updateStatus("Đặt CCCD vào khung hình");
            tvInstructions.setText("Đảm bảo CCCD rõ ràng, đủ ánh sáng và không bị phản chiếu");
        } catch (Exception e) {
            Log.e(TAG, "Error binding camera use cases", e);
            showError("Không thể khởi động camera. Vui lòng thử lại.");
        }
    }
    
    private void analyzeImage(ImageProxy image) {
        if (!isScanning || isCapturing) {
            image.close();
            return;
        }
        
        // Check timeout
        if (System.currentTimeMillis() - scanStartTime > SCAN_TIMEOUT) {
            image.close();
            if (!isTimeoutDialogShown) {
                mainHandler.post(() -> handleTimeout());
            }
            return;
        }
        
        InputImage inputImage = InputImage.fromMediaImage(
                image.getImage(),
                image.getImageInfo().getRotationDegrees()
        );
        
        // Detect CCCD text first
        textRecognizer.process(inputImage)
                .addOnSuccessListener(text -> {
                    if (text != null && !text.getText().isEmpty()) {
                        checkCccdDetection(text, image);
                    } else {
                        image.close();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Text recognition failed", e);
                    image.close();
                });
    }
    
    private void checkCccdDetection(Text text, ImageProxy image) {
        String fullText = text.getText().toUpperCase();
        boolean detected = false;
        
        // Check for CCCD keywords
        for (String keyword : CCCD_KEYWORDS) {
            if (fullText.contains(keyword)) {
                detected = true;
                break;
            }
        }
        
        if (detected && !cccdDetected) {
            cccdDetected = true;
            cccdDetectedTime = System.currentTimeMillis();
            mainHandler.post(() -> {
                updateStatus("Đã phát hiện CCCD. Đang xử lý...");
                // Also check for face
                detectFaceAndCapture(image);
            });
        } else if (detected) {
            // Already detected, check if ready to capture
            long timeSinceDetection = System.currentTimeMillis() - cccdDetectedTime;
            if (timeSinceDetection >= CAPTURE_DELAY_MS && !isCapturing) {
                detectFaceAndCapture(image);
            } else {
                image.close();
            }
        } else {
            cccdDetected = false;
            cccdDetectedTime = 0;
            image.close();
        }
    }
    
    private void detectFaceAndCapture(ImageProxy image) {
        InputImage inputImage = InputImage.fromMediaImage(
                image.getImage(),
                image.getImageInfo().getRotationDegrees()
        );
        
        faceDetector.process(inputImage)
                .addOnSuccessListener(faces -> {
                    if (faces != null && !faces.isEmpty()) {
                        // Face detected, capture image
                        mainHandler.post(() -> captureCccdImage());
                    } else {
                        mainHandler.post(() -> {
                            updateStatus("Đã phát hiện CCCD nhưng chưa thấy khuôn mặt. Vui lòng đặt CCCD thẳng hơn.");
                        });
                    }
                    image.close();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Face detection failed", e);
                    // Still capture even if face detection fails
                    mainHandler.post(() -> captureCccdImage());
                    image.close();
                });
    }
    
    private void captureCccdImage() {
        if (isCapturing || imageCapture == null) return;
        
        isCapturing = true;
        updateStatus("Đang chụp ảnh CCCD...");
        progressBar.setVisibility(View.VISIBLE);
        
        File outputFile = new File(getCacheDir(), "cccd_capture_" + System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(outputFile).build();
        
        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                        Bitmap bitmap = BitmapFactory.decodeFile(outputFile.getAbsolutePath());
                        
                        if (bitmap == null) {
                            showError("Không thể chụp ảnh. Vui lòng thử lại.");
                            resetScan();
                            return;
                        }
                        
                        // Apply rotation correction if needed
                        // ImageCapture should handle rotation, but we verify and correct if necessary
                        bitmap = correctImageOrientation(bitmap);
                        
                        // Check brightness
                        float brightness = calculateBrightness(bitmap);
                        if (brightness < MIN_BRIGHTNESS_THRESHOLD) {
                            showError("Ánh sáng quá yếu. Vui lòng tăng độ sáng hoặc di chuyển đến nơi sáng hơn.");
                            resetScan();
                            return;
                        }
                        
                        // Save front image to permanent storage
                        String frontImagePath = saveFrontImage(bitmap);
                        
                        if (frontImagePath == null) {
                            showError("Không thể lưu ảnh CCCD. Vui lòng thử lại.");
                            resetScan();
                            return;
                        }
                        
                        Log.d(TAG, "Front image saved successfully: " + frontImagePath);
                        
                        // Extract portrait (will save and return result, then finish)
                        extractPortrait(bitmap, frontImagePath);
                        
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
    
    /**
     * Correct image orientation based on device orientation
     * CameraX should handle rotation automatically, but we verify and correct if needed
     */
    private Bitmap correctImageOrientation(Bitmap bitmap) {
        if (bitmap == null) return null;
        
        // Get current device orientation
        Display display = getWindowManager().getDefaultDisplay();
        int rotation = display.getRotation();
        
        // Check if image orientation matches device orientation
        boolean isDevicePortrait = (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180);
        boolean isImagePortrait = bitmap.getHeight() > bitmap.getWidth();
        
        // If orientations don't match, we need to rotate
        if (isDevicePortrait != isImagePortrait) {
            int rotationAngle = 90; // Rotate 90 degrees
            
            // Determine rotation direction based on device orientation
            if (rotation == Surface.ROTATION_0) {
                // Device in portrait, image is landscape - rotate 90° clockwise
                rotationAngle = 90;
            } else if (rotation == Surface.ROTATION_90) {
                // Device in landscape, image is portrait - rotate 90° counter-clockwise
                rotationAngle = -90;
            } else if (rotation == Surface.ROTATION_180) {
                // Device in reverse portrait, image is landscape - rotate 90° counter-clockwise
                rotationAngle = -90;
            } else if (rotation == Surface.ROTATION_270) {
                // Device in reverse landscape, image is portrait - rotate 90° clockwise
                rotationAngle = 90;
            }
            
            // Normalize rotation angle to 0-360 range
            if (rotationAngle < 0) {
                rotationAngle += 360;
            }
            
            Log.d(TAG, "Orientation mismatch detected. Device: " + rotation + 
                  ", Image: " + (isImagePortrait ? "portrait" : "landscape") + 
                  ", Applying " + rotationAngle + "° rotation");
            
            // Apply rotation
            Matrix matrix = new Matrix();
            matrix.postRotate(rotationAngle);
            
            try {
                Bitmap rotatedBitmap = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true
                );
                
                // Recycle original bitmap
                if (rotatedBitmap != bitmap && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
                
                Log.d(TAG, "Image rotated. Original: " + bitmap.getWidth() + "x" + bitmap.getHeight() + 
                      ", Rotated: " + rotatedBitmap.getWidth() + "x" + rotatedBitmap.getHeight());
                
                return rotatedBitmap;
            } catch (Exception e) {
                Log.e(TAG, "Error rotating image", e);
                return bitmap; // Return original if rotation fails
            }
        }
        
        // Orientation already matches, no rotation needed
        Log.d(TAG, "Image orientation matches device orientation. No rotation needed.");
        return bitmap;
    }
    
    private float calculateBrightness(Bitmap bitmap) {
        long sum = 0;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int sampleSize = 10;
        
        for (int y = 0; y < height; y += sampleSize) {
            for (int x = 0; x < width; x += sampleSize) {
                int pixel = bitmap.getPixel(x, y);
                int r = (pixel >> 16) & 0xFF;
                int g = (pixel >> 8) & 0xFF;
                int b = pixel & 0xFF;
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
    }
    
    private String saveFrontImage(Bitmap bitmap) {
        try {
            File internalDir = new File(getFilesDir(), "cccd_images");
            if (!internalDir.exists()) {
                internalDir.mkdirs();
            }
            
            String fileName = "front_" + System.currentTimeMillis() + ".jpg";
            File frontImageFile = new File(internalDir, fileName);
            
            FileOutputStream fos = new FileOutputStream(frontImageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, fos);
            fos.flush();
            fos.close();
            
            return frontImageFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error saving front image", e);
            return null;
        }
    }
    
    private void extractPortrait(Bitmap fullImage, String frontImagePath) {
        updateStatus("Đang trích xuất ảnh chân dung...");
        
        InputImage image = InputImage.fromBitmap(fullImage, 0);
        
        // First, use text recognition to find CCCD position
        textRecognizer.process(image)
                .addOnSuccessListener(text -> {
                    // Find portrait position using text recognition
                    Rect cccdTextBounds = findCccdTextBounds(text, fullImage);
                    
                    // Then detect face
                    faceDetector.process(image)
                            .addOnSuccessListener(faces -> {
                                Bitmap portrait = null;
                                
                                if (faces != null && !faces.isEmpty()) {
                                    // Find best face (largest and in expected position)
                                    Face bestFace = findBestFace(faces, fullImage, cccdTextBounds);
                                    
                                    if (bestFace != null) {
                                        portrait = cropPortraitFromFace(fullImage, bestFace, cccdTextBounds);
                                    }
                                }
                                
                                // Fallback: use smart crop with text position
                                if (portrait == null) {
                                    portrait = cropPortraitSmart(fullImage, cccdTextBounds);
                                }
                                
                                // Validate and ensure quality
                                portrait = validateAndEnsureQuality(portrait, fullImage);
                                
                                // Save portrait (silently, no preview) - will finish activity
                                savePortrait(portrait, frontImagePath);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Face detection failed during extraction", e);
                                // Use smart crop with text position
                                Bitmap portrait = cropPortraitSmart(fullImage, cccdTextBounds);
                                portrait = validateAndEnsureQuality(portrait, fullImage);
                                // Save portrait (silently, no preview) - will finish activity
                                savePortrait(portrait, frontImagePath);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Text recognition failed, using face detection only", e);
                    // Fallback to face detection only
                    extractPortraitWithFaceOnly(fullImage, frontImagePath);
                });
    }
    
    private void extractPortraitWithFaceOnly(Bitmap fullImage) {
        extractPortraitWithFaceOnly(fullImage, null);
    }
    
    private void extractPortraitWithFaceOnly(Bitmap fullImage, String frontImagePath) {
        InputImage image = InputImage.fromBitmap(fullImage, 0);
        
        faceDetector.process(image)
                .addOnSuccessListener(faces -> {
                    Bitmap portrait = null;
                    
                    if (faces != null && !faces.isEmpty()) {
                        Face bestFace = findBestFace(faces, fullImage, null);
                        if (bestFace != null) {
                            portrait = cropPortraitFromFace(fullImage, bestFace, null);
                        }
                    }
                    
                    if (portrait == null) {
                        portrait = cropPortraitSmart(fullImage, null);
                    }
                    
                    portrait = validateAndEnsureQuality(portrait, fullImage);
                    // Save portrait (silently, no preview) - will finish activity
                    savePortrait(portrait, frontImagePath);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Face detection failed", e);
                    Bitmap portrait = cropPortraitSmart(fullImage, null);
                    portrait = validateAndEnsureQuality(portrait, fullImage);
                    // Save portrait (silently, no preview) - will finish activity
                    savePortrait(portrait, frontImagePath);
                });
    }
    
    private Rect findCccdTextBounds(Text text, Bitmap image) {
        if (text == null || text.getTextBlocks().isEmpty()) {
            return null;
        }
        
        // Find text blocks containing CCCD keywords
        for (Text.TextBlock block : text.getTextBlocks()) {
            String blockText = block.getText().toUpperCase();
            for (String keyword : CCCD_KEYWORDS) {
                if (blockText.contains(keyword)) {
                    Rect bounds = block.getBoundingBox();
                    if (bounds != null) {
                        Log.d(TAG, "Found CCCD text at: " + bounds.left + "," + bounds.top);
                        return bounds;
                    }
                }
            }
        }
        
        return null;
    }
    
    private Face findBestFace(List<Face> faces, Bitmap image, Rect cccdTextBounds) {
        Face bestFace = null;
        float bestScore = 0;
        
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        
        // Expected portrait position: top-left area (0-35% width, 0-45% height)
        float expectedLeftMax = imageWidth * 0.35f;
        float expectedTopMax = imageHeight * 0.45f;
        
        for (Face face : faces) {
            Rect bounds = face.getBoundingBox();
            if (bounds == null) continue;
            
            float area = bounds.width() * bounds.height();
            float score = area;
            
            // Bonus if face is in expected position (top-left)
            if (bounds.left < expectedLeftMax && bounds.top < expectedTopMax) {
                score *= 1.5f; // 50% bonus
            }
            
            // Bonus if CCCD text is found and face is near it
            if (cccdTextBounds != null) {
                // Face should be to the left of or above the text
                if (bounds.right < cccdTextBounds.left || bounds.bottom < cccdTextBounds.top) {
                    score *= 1.3f; // 30% bonus
                }
            }
            
            // Penalty if face is too small (< 5% of image)
            float faceAreaPercent = (area / (imageWidth * imageHeight)) * 100f;
            if (faceAreaPercent < 5f) {
                score *= 0.5f; // 50% penalty
            }
            
            // Penalty if face is in wrong position (bottom-right)
            if (bounds.left > imageWidth * 0.5f && bounds.top > imageHeight * 0.5f) {
                score *= 0.3f; // 70% penalty
            }
            
            if (score > bestScore) {
                bestScore = score;
                bestFace = face;
            }
        }
        
        Log.d(TAG, "Best face score: " + bestScore);
        return bestFace;
    }
    
    private Bitmap cropPortraitFromFace(Bitmap fullImage, Face face, Rect cccdTextBounds) {
        Rect bounds = face.getBoundingBox();
        if (bounds == null) return null;
        
        int imageWidth = fullImage.getWidth();
        int imageHeight = fullImage.getHeight();
        
        // Calculate padding: more padding on top and bottom for portrait
        int paddingX = (int)(bounds.width() * 0.4f); // 40% horizontal padding
        int paddingY = (int)(bounds.height() * 0.6f); // 60% vertical padding (more for portrait)
        
        // Ensure minimum padding
        paddingX = Math.max(paddingX, 20);
        paddingY = Math.max(paddingY, 30);
        
        // Calculate crop area
        int left = Math.max(0, bounds.left - paddingX);
        int top = Math.max(0, bounds.top - paddingY);
        int right = Math.min(imageWidth, bounds.right + paddingX);
        int bottom = Math.min(imageHeight, bounds.bottom + paddingY);
        
        // Adjust if CCCD text bounds are known (portrait should be above/left of text)
        if (cccdTextBounds != null) {
            // Ensure portrait doesn't overlap with text area
            if (right > cccdTextBounds.left - 10) {
                right = Math.max(bounds.right, cccdTextBounds.left - 10);
            }
            if (bottom > cccdTextBounds.top - 10) {
                bottom = Math.max(bounds.bottom, cccdTextBounds.top - 10);
            }
        }
        
        int width = right - left;
        int height = bottom - top;
        
        // Make it portrait-oriented (height should be 1.2-1.5x width)
        float aspectRatio = (float) height / width;
        float targetAspectRatio = 1.3f; // Portrait aspect ratio
        
        if (aspectRatio < targetAspectRatio) {
            // Need to increase height
            int targetHeight = (int)(width * targetAspectRatio);
            int heightDiff = targetHeight - height;
            top = Math.max(0, top - heightDiff / 2);
            bottom = Math.min(imageHeight, bottom + heightDiff / 2);
            height = bottom - top;
        } else if (aspectRatio > 1.6f) {
            // Too tall, reduce height slightly
            int targetHeight = (int)(width * 1.5f);
            int heightDiff = height - targetHeight;
            top += heightDiff / 2;
            bottom -= heightDiff / 2;
            height = bottom - top;
        }
        
        // Ensure valid bounds
        if (width <= 0 || height <= 0 || left < 0 || top < 0 || 
            right > imageWidth || bottom > imageHeight) {
            Log.w(TAG, "Invalid crop bounds, using fallback");
            return null;
        }
        
        try {
            Bitmap cropped = Bitmap.createBitmap(fullImage, left, top, width, height);
            Log.d(TAG, "Cropped portrait from face: " + width + "x" + height + " at " + left + "," + top);
            return cropped;
        } catch (Exception e) {
            Log.e(TAG, "Error cropping portrait from face", e);
            return null;
        }
    }
    
    private Bitmap cropPortraitSmart(Bitmap fullImage, Rect cccdTextBounds) {
        int width = fullImage.getWidth();
        int height = fullImage.getHeight();
        
        // If we know where the CCCD text is, portrait is definitely above/left of it
        int maxRight = width;
        int maxBottom = height;
        if (cccdTextBounds != null) {
            maxRight = Math.min(maxRight, cccdTextBounds.left - 10);
            maxBottom = Math.min(maxBottom, cccdTextBounds.top - 10);
            Log.d(TAG, "Using CCCD text bounds to limit crop area");
        }
        
        // Vietnamese CCCD portrait is typically in top-left area
        // Try multiple crop strategies with portrait aspect ratio (1.2-1.5:1)
        float[] aspectRatios = {1.2f, 1.3f, 1.4f, 1.5f};
        int[][] baseStrategies = {
            // left, top, width%, height%
            {(int)(width * 0.02f), (int)(height * 0.05f), 25, 35},
            {(int)(width * 0.01f), (int)(height * 0.03f), 28, 38},
            {(int)(width * 0.03f), (int)(height * 0.06f), 24, 34},
            {(int)(width * 0.04f), (int)(height * 0.08f), 22, 32},
            {(int)(width * 0.015f), (int)(height * 0.04f), 26, 36}
        };
        
        for (int[] baseStrategy : baseStrategies) {
            for (float aspectRatio : aspectRatios) {
                int left = baseStrategy[0];
                int top = baseStrategy[1];
                int cropWidth = (int)(width * baseStrategy[2] / 100f);
                int cropHeight = (int)(cropWidth * aspectRatio); // Portrait: taller than wide
                
                // Adjust to fit within bounds
                if (left + cropWidth > maxRight) {
                    cropWidth = maxRight - left;
                    cropHeight = (int)(cropWidth * aspectRatio);
                }
                if (top + cropHeight > maxBottom) {
                    cropHeight = maxBottom - top;
                    cropWidth = (int)(cropHeight / aspectRatio);
                }
                
                // Ensure minimum size
                if (cropWidth < 100 || cropHeight < 120) continue;
                
                // Ensure valid bounds
                if (left >= 0 && top >= 0 && left + cropWidth <= width && 
                    top + cropHeight <= height && cropWidth > 0 && cropHeight > 0) {
                    try {
                        Bitmap cropped = Bitmap.createBitmap(fullImage, left, top, cropWidth, cropHeight);
                        Log.d(TAG, "Smart crop: " + cropWidth + "x" + cropHeight + " at " + left + "," + top);
                        return cropped;
                    } catch (Exception e) {
                        Log.w(TAG, "Crop strategy failed", e);
                        continue;
                    }
                }
            }
        }
        
        // Fallback: top-left square crop
        int size = Math.min((int)(width * 0.3f), (int)(height * 0.4f));
        size = Math.min(size, maxRight);
        size = Math.min(size, maxBottom);
        if (size > 100) {
            try {
                return Bitmap.createBitmap(fullImage, 0, 0, size, size);
            } catch (Exception e) {
                Log.e(TAG, "Fallback crop failed", e);
            }
        }
        
        // Last resort: center crop
        size = Math.min(width, height) / 2;
        int left = (width - size) / 2;
        int top = (height - size) / 2;
        return Bitmap.createBitmap(fullImage, left, top, size, size);
    }
    
    private Bitmap validateAndEnsureQuality(Bitmap portrait, Bitmap originalImage) {
        if (portrait == null) {
            Log.w(TAG, "Portrait is null, using fallback");
            return cropPortraitSmart(originalImage, null);
        }
        
        // Validate aspect ratio (should be portrait: height > width)
        float aspectRatio = (float) portrait.getHeight() / portrait.getWidth();
        if (aspectRatio < 1.0f) {
            Log.w(TAG, "Invalid aspect ratio: " + aspectRatio + ", recropping");
            // Not portrait-oriented, try to fix
            int newHeight = (int)(portrait.getWidth() * 1.3f);
            if (newHeight <= originalImage.getHeight()) {
                // Try to recrop with better aspect ratio
                Bitmap recropped = cropPortraitSmart(originalImage, null);
                if (recropped != null && (float)recropped.getHeight() / recropped.getWidth() >= 1.0f) {
                    portrait = recropped;
                }
            }
        }
        
        // Ensure quality
        return ensurePortraitQuality(portrait);
    }
    
    private Bitmap ensurePortraitQuality(Bitmap portrait) {
        if (portrait == null) return null;
        
        int currentWidth = portrait.getWidth();
        int currentHeight = portrait.getHeight();
        
        // Scale up if too small
        if (currentWidth < MIN_PORTRAIT_WIDTH || currentHeight < MIN_PORTRAIT_HEIGHT) {
            float scaleX = (float) TARGET_PORTRAIT_WIDTH / currentWidth;
            float scaleY = (float) TARGET_PORTRAIT_HEIGHT / currentHeight;
            float scale = Math.max(scaleX, scaleY);
            
            int newWidth = (int) (currentWidth * scale);
            int newHeight = (int) (currentHeight * scale);
            
            return Bitmap.createScaledBitmap(portrait, newWidth, newHeight, true);
        }
        
        // Scale to target if larger
        if (currentWidth > TARGET_PORTRAIT_WIDTH || currentHeight > TARGET_PORTRAIT_HEIGHT) {
            float scaleX = (float) TARGET_PORTRAIT_WIDTH / currentWidth;
            float scaleY = (float) TARGET_PORTRAIT_HEIGHT / currentHeight;
            float scale = Math.min(scaleX, scaleY);
            
            int newWidth = (int) (currentWidth * scale);
            int newHeight = (int) (currentHeight * scale);
            
            return Bitmap.createScaledBitmap(portrait, newWidth, newHeight, true);
        }
        
        return portrait;
    }
    
    private void showPortraitPreview(Bitmap portrait) {
        ivPortraitPreview.setVisibility(View.VISIBLE);
        ivPortraitPreview.setImageBitmap(portrait);
        progressBar.setVisibility(View.GONE);
        updateStatus("Đã trích xuất ảnh chân dung thành công!");
    }
    
    private void savePortrait(Bitmap portrait) {
        savePortrait(portrait, null);
    }
    
    private void savePortrait(Bitmap portrait, String frontImagePath) {
        try {
            // Save to internal storage
            File internalDir = new File(getFilesDir(), "cccd_portraits");
            if (!internalDir.exists()) {
                internalDir.mkdirs();
            }
            
            String fileName = "portrait_" + System.currentTimeMillis() + ".jpg";
            File portraitFile = new File(internalDir, fileName);
            
            FileOutputStream fos = new FileOutputStream(portraitFile);
            portrait.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, fos);
            fos.flush();
            fos.close();
            
            // Encode to base64 for backend
            String base64Image = encodeToBase64(portrait);
            
            // Return result - MUST include front_image_path
            Intent resultIntent = new Intent();
            if (frontImagePath != null) {
                resultIntent.putExtra("front_image_path", frontImagePath);
                Log.d(TAG, "Front image path: " + frontImagePath);
            } else {
                Log.e(TAG, "WARNING: frontImagePath is null!");
            }
            resultIntent.putExtra("portrait_path", portraitFile.getAbsolutePath());
            resultIntent.putExtra("portrait_base64", base64Image);
            resultIntent.putExtra("portrait_width", portrait.getWidth());
            resultIntent.putExtra("portrait_height", portrait.getHeight());
            setResult(RESULT_OK, resultIntent);
            
            Log.d(TAG, "Portrait saved: " + portraitFile.getAbsolutePath());
            Log.d(TAG, "Portrait size: " + portrait.getWidth() + "x" + portrait.getHeight());
            Log.d(TAG, "Result set with front_image_path: " + (frontImagePath != null));
            
            // Show success message
            Toast.makeText(this, "Đã chụp ảnh mặt trước CCCD thành công!", Toast.LENGTH_SHORT).show();
            
            // Finish immediately to return result
            finish();
            
        } catch (IOException e) {
            Log.e(TAG, "Error saving portrait", e);
            showError("Lỗi khi lưu ảnh chân dung. Vui lòng thử lại.");
        }
    }
    
    private String encodeToBase64(Bitmap bitmap) {
        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
    }
    
    private void handleTimeout() {
        // Prevent multiple dialogs
        if (isTimeoutDialogShown || !isScanning) {
            return;
        }
        
        // Stop scanning immediately
        isScanning = false;
        isTimeoutDialogShown = true;
        
        // Remove timeout callback to prevent it from running again
        if (timeoutRunnable != null) {
            mainHandler.removeCallbacks(timeoutRunnable);
            timeoutRunnable = null;
        }
        
        // Stop image analysis
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        
        // Show dialog on main thread
        runOnUiThread(() -> {
            new AlertDialog.Builder(this)
                    .setTitle("Hết thời gian")
                    .setMessage("Không phát hiện CCCD sau 10 giây. Vui lòng:\n" +
                            "• Đảm bảo ánh sáng đủ sáng\n" +
                            "• Đặt CCCD thẳng, không bị nghiêng\n" +
                            "• Giữ điện thoại ổn định\n" +
                            "• Tránh phản chiếu ánh sáng")
                    .setPositiveButton("Thử lại", (dialog, which) -> {
                        isTimeoutDialogShown = false;
                        resetScan();
                        startCamera();
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> {
                        finish();
                    })
                    .setCancelable(false)
                    .setOnDismissListener(dialog -> {
                        isTimeoutDialogShown = false;
                    })
                    .show();
        });
    }
    
    private void resetScan() {
        isScanning = false;
        isCapturing = false;
        isTimeoutDialogShown = false;
        cccdDetected = false;
        cccdDetectedTime = 0;
        
        progressBar.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);
        ivPreview.setVisibility(View.GONE);
        ivPortraitPreview.setVisibility(View.GONE);
        previewView.setVisibility(View.VISIBLE);
        
        // Remove timeout callback
        if (timeoutRunnable != null) {
            mainHandler.removeCallbacks(timeoutRunnable);
            timeoutRunnable = null;
        }
    }
    
    private void startTimeoutTimer() {
        // Remove any existing timeout callback first
        if (timeoutRunnable != null) {
            mainHandler.removeCallbacks(timeoutRunnable);
        }
        
        timeoutRunnable = () -> {
            // Only handle timeout if still scanning and not capturing
            if (isScanning && !isCapturing && !isTimeoutDialogShown) {
                handleTimeout();
            }
        };
        mainHandler.postDelayed(timeoutRunnable, SCAN_TIMEOUT);
    }
    
    private void updateStatus(String status) {
        tvStatus.setText(status);
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        updateStatus(message);
        btnRetry.setVisibility(View.VISIBLE);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        isScanning = false;
        isCapturing = false;
        
        if (timeoutRunnable != null) {
            mainHandler.removeCallbacks(timeoutRunnable);
        }
        
        if (orientationEventListener != null) {
            orientationEventListener.disable();
        }
        
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        
        if (faceDetector != null) {
            faceDetector.close();
        }
        
        if (textRecognizer != null) {
            textRecognizer.close();
        }
        
        if (imageAnalysisExecutor != null) {
            imageAnalysisExecutor.shutdown();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Update orientation when activity resumes
        if (orientationEventListener != null && orientationEventListener.canDetectOrientation()) {
            // Orientation listener will handle updates
        } else {
            updateOrientationFromDisplay();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Keep orientation listener active to detect changes
    }
}

