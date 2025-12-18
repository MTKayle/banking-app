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
import com.example.mobilebanking.utils.CccdBackParser;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
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
import java.util.regex.Pattern;

/**
 * Auto CCCD Back Scanner Activity
 * Automatically detects and captures CCCD back side
 */
public class CccdBackScannerActivity extends AppCompatActivity {
    private static final String TAG = "CccdBackScanner";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 300;
    private static final int SCAN_TIMEOUT = 30000; // 30 seconds (increased for better detection)
    private static final float MIN_BRIGHTNESS_THRESHOLD = 0.3f;
    private static final int JPEG_QUALITY = 95; // High quality
    
    // CCCD back side detection keywords (Vietnamese)
    // More flexible keywords to detect back side
    private static final String[] CCCD_BACK_KEYWORDS = {
        "CĂN CƯỚC CÔNG DÂN",
        "CĂN CƯỚC",
        "CÔNG DÂN",
        "CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM",
        "SOCIALIST REPUBLIC OF VIETNAM",
        "NƠI THƯỜNG TRÚ",
        "THƯỜNG TRÚ",
        "ĐẶC ĐIỂM NHẬN DẠNG",
        "ĐẶC ĐIỂM",
        "NHẬN DẠNG",
        "NGÀY CẤP",
        "NƠI CẤP",
        "CẤP NGÀY",
        "CẤP TẠI",
        "QUỐC TỊCH",
        "DÂN TỘC",
        "TÔN GIÁO"
    };
    
    // Minimum number of keywords to detect (more flexible)
    // Reduced to 1 to make detection easier
    private static final int MIN_KEYWORDS_TO_DETECT = 1;
    
    // Alternative: detect if text contains CCCD number pattern (12 digits)
    private static final String CCCD_NUMBER_PATTERN = "\\d{12}";
    
    private PreviewView previewView;
    private TextView tvStatus;
    private TextView tvInstructions;
    private Button btnCancel;
    private Button btnRetry;
    private ProgressBar progressBar;
    private ImageView ivPreview;
    
    private ProcessCameraProvider cameraProvider;
    private Camera camera;
    private ImageCapture imageCapture;
    private TextRecognizer textRecognizer;
    private ExecutorService imageAnalysisExecutor;
    
    private boolean isScanning = false;
    private boolean isCapturing = false;
    private boolean isTimeoutDialogShown = false;
    private long scanStartTime;
    private Handler mainHandler;
    private Runnable timeoutRunnable;
    
    // Detection state
    private boolean cccdDetected = false;
    private long cccdDetectedTime = 0;
    private static final long CAPTURE_DELAY_MS = 2000; // Wait 2s after detection before capture
    private String lastRecognizedText = ""; // Store recognized text for parsing
    private CccdBackParser.CccdBackData extractedData = null;
    
    // Orientation handling
    private OrientationEventListener orientationEventListener;
    
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
        
        // Update UI for back side
        updateUIForBackSide();
        
        if (checkCameraPermission()) {
            startCamera();
        } else {
            requestCameraPermission();
        }
    }
    
    private void updateUIForBackSide() {
        TextView tvTitle = findViewById(R.id.tv_title);
        if (tvTitle != null) {
            tvTitle.setText("Quét Mặt Sau CCCD Tự Động");
        }
        tvInstructions.setText("Đặt mặt sau CCCD vào khung hình. Đảm bảo rõ ràng, đủ ánh sáng và không bị phản chiếu");
    }
    
    private void initializeViews() {
        previewView = findViewById(R.id.preview_view);
        tvStatus = findViewById(R.id.tv_status);
        tvInstructions = findViewById(R.id.tv_instructions);
        btnCancel = findViewById(R.id.btn_cancel);
        btnRetry = findViewById(R.id.btn_retry);
        progressBar = findViewById(R.id.progress_bar);
        ivPreview = findViewById(R.id.iv_preview);
        
        btnRetry.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        ivPreview.setVisibility(View.GONE);
    }
    
    private void setupMLKit() {
        // Text Recognizer for CCCD back side detection
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    }
    
    private void setupListeners() {
        btnCancel.setOnClickListener(v -> finish());
        btnRetry.setOnClickListener(v -> {
            resetScan();
            // startCamera() is called inside resetScan() with delay
        });
    }
    
    private void setupOrientationListener() {
        orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (orientation == ORIENTATION_UNKNOWN) {
                    return;
                }
                
                runOnUiThread(() -> {
                    Display display = getWindowManager().getDefaultDisplay();
                    int rotation = display.getRotation();
                    
                    if (imageCapture != null) {
                        imageCapture.setTargetRotation(rotation);
                    }
                });
            }
        };
        
        if (orientationEventListener.canDetectOrientation()) {
            orientationEventListener.enable();
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
        
        Display display = getWindowManager().getDefaultDisplay();
        int rotation = display.getRotation();
        preview.setTargetRotation(rotation);
        
        // Image Analysis for CCCD detection
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build();
        
        imageAnalysis.setAnalyzer(imageAnalysisExecutor, this::analyzeImage);
        
        // Image Capture
        imageCapture = new ImageCapture.Builder()
                .setTargetResolution(new android.util.Size(1920, 2560))
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();
        
        imageCapture.setTargetRotation(rotation);
        
        // Camera selector - use back camera
        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
        
        try {
            camera = cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalysis,
                    imageCapture
            );
            
            updateStatus("Đặt mặt sau CCCD vào khung hình");
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
        
        // Detect CCCD back side text
        textRecognizer.process(inputImage)
                .addOnSuccessListener(text -> {
                    if (text != null && !text.getText().isEmpty()) {
                        // Store recognized text for parsing
                        lastRecognizedText = text.getText();
                        
                        // Log text recognition result for debugging
                        if (lastRecognizedText.length() > 0) {
                            Log.v(TAG, "Text recognized: " + lastRecognizedText.substring(0, Math.min(100, lastRecognizedText.length())) + "...");
                        }
                        checkCccdBackDetection(text, image);
                    } else {
                        Log.v(TAG, "No text recognized in this frame");
                        image.close();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Text recognition failed", e);
                    image.close();
                });
    }
    
    private void checkCccdBackDetection(Text text, ImageProxy image) {
        String fullText = text.getText().toUpperCase();
        int keywordCount = 0;
        String foundKeywords = "";
        
        // Check for CCCD back side keywords - count matches
        for (String keyword : CCCD_BACK_KEYWORDS) {
            if (fullText.contains(keyword)) {
                keywordCount++;
                if (foundKeywords.isEmpty()) {
                    foundKeywords = keyword;
                } else {
                    foundKeywords += ", " + keyword;
                }
            }
        }
        
        // Also check for CCCD number pattern (12 digits) as additional detection method
        boolean hasCccdNumber = Pattern.compile(CCCD_NUMBER_PATTERN).matcher(text.getText()).find();
        
        // Detect if we have keywords OR CCCD number pattern
        boolean detected = keywordCount >= MIN_KEYWORDS_TO_DETECT || hasCccdNumber;
        
        // Log detection status for debugging
        if (keywordCount > 0 || hasCccdNumber) {
            Log.d(TAG, "Found " + keywordCount + " keyword(s): " + foundKeywords);
            if (hasCccdNumber) {
                Log.d(TAG, "Also found CCCD number pattern");
            }
            Log.d(TAG, "Text sample (first 200 chars): " + fullText.substring(0, Math.min(200, fullText.length())));
        }
        
        if (detected && !cccdDetected) {
            cccdDetected = true;
            cccdDetectedTime = System.currentTimeMillis();
            Log.d(TAG, "CCCD back side detected! Keywords found: " + foundKeywords);
            
            // Try to parse data immediately for feedback
            if (!lastRecognizedText.isEmpty()) {
                extractedData = CccdBackParser.parseBackText(lastRecognizedText);
                if (extractedData != null) {
                    Log.d(TAG, "Extracted data: " + extractedData.toString());
                    mainHandler.post(() -> {
                        updateStatusWithExtractedData(extractedData);
                    });
                }
            }
            
            mainHandler.post(() -> {
                updateStatus("Đã phát hiện mặt sau CCCD. Đang xử lý...");
            });
        } else if (detected) {
            // Update extracted data while waiting
            if (!lastRecognizedText.isEmpty()) {
                extractedData = CccdBackParser.parseBackText(lastRecognizedText);
            }
            
            long timeSinceDetection = System.currentTimeMillis() - cccdDetectedTime;
            if (timeSinceDetection >= CAPTURE_DELAY_MS && !isCapturing) {
                Log.d(TAG, "Triggering capture after " + timeSinceDetection + "ms");
                mainHandler.post(() -> captureCccdImage());
            }
        } else {
            // Reset detection if keywords are lost
            if (cccdDetected) {
                Log.d(TAG, "CCCD detection lost. Resetting...");
            }
            cccdDetected = false;
            cccdDetectedTime = 0;
            extractedData = null;
        }
        
        image.close();
    }
    
    private void captureCccdImage() {
        if (isCapturing || imageCapture == null) return;
        
        isCapturing = true;
        updateStatus("Đang chụp ảnh mặt sau CCCD...");
        progressBar.setVisibility(View.VISIBLE);
        
        File outputFile = new File(getCacheDir(), "cccd_back_" + System.currentTimeMillis() + ".jpg");
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
                        
                        // Apply rotation correction
                        bitmap = correctImageOrientation(bitmap);
                        
                        // Check brightness
                        float brightness = calculateBrightness(bitmap);
                        if (brightness < MIN_BRIGHTNESS_THRESHOLD) {
                            showError("Ánh sáng quá yếu. Vui lòng tăng độ sáng hoặc di chuyển đến nơi sáng hơn.");
                            resetScan();
                            return;
                        }
                        
                        // Parse text from captured image
                        parseAndShowConfirmation(bitmap);
                        
                        // Clean up
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
    
    private Bitmap correctImageOrientation(Bitmap bitmap) {
        if (bitmap == null) return null;
        
        Display display = getWindowManager().getDefaultDisplay();
        int rotation = display.getRotation();
        
        boolean isDevicePortrait = (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180);
        boolean isImagePortrait = bitmap.getHeight() > bitmap.getWidth();
        
        if (isDevicePortrait != isImagePortrait) {
            int rotationAngle = 90;
            if (rotation == Surface.ROTATION_90) {
                rotationAngle = -90;
            } else if (rotation == Surface.ROTATION_180) {
                rotationAngle = -90;
            } else if (rotation == Surface.ROTATION_270) {
                rotationAngle = 90;
            }
            
            if (rotationAngle < 0) {
                rotationAngle += 360;
            }
            
            Matrix matrix = new Matrix();
            matrix.postRotate(rotationAngle);
            
            try {
                Bitmap rotatedBitmap = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true
                );
                
                if (rotatedBitmap != bitmap && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
                
                return rotatedBitmap;
            } catch (Exception e) {
                Log.e(TAG, "Error rotating image", e);
                return bitmap;
            }
        }
        
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
        // Stop camera scanning first
        isScanning = false;
        isCapturing = false;
        
        // Unbind camera to release resources
        if (cameraProvider != null) {
            try {
                cameraProvider.unbindAll();
                Log.d(TAG, "Camera unbound before showing preview");
            } catch (Exception e) {
                Log.e(TAG, "Error unbinding camera", e);
            }
        }
        
        // Show preview image and hide camera preview
        ivPreview.setVisibility(View.VISIBLE);
        ivPreview.setImageBitmap(bitmap);
        previewView.setVisibility(View.GONE);
        
        // Cancel timeout timer
        if (timeoutRunnable != null) {
            mainHandler.removeCallbacks(timeoutRunnable);
            timeoutRunnable = null;
        }
    }
    
    /**
     * Parse text from captured image and show confirmation dialog
     */
    private void parseAndShowConfirmation(Bitmap bitmap) {
        updateStatus("Đang trích xuất thông tin...");
        progressBar.setVisibility(View.VISIBLE);
        
        // Use stored text or re-recognize from bitmap
        final String textToParse = lastRecognizedText;
        
        if (textToParse == null || textToParse.isEmpty()) {
            // Re-recognize from bitmap if needed
            InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
            textRecognizer.process(inputImage)
                    .addOnSuccessListener(text -> {
                        if (text != null && !text.getText().isEmpty()) {
                            String recognizedText = text.getText();
                            // Auto-save without showing confirmation dialog
                            progressBar.setVisibility(View.GONE);
                            CccdBackParser.CccdBackData data = CccdBackParser.parseBackText(recognizedText);
                            saveBackImage(bitmap, data);
                        } else {
                            // No text found, auto-save without confirmation
                            Log.w(TAG, "No text found in captured image, auto-saving");
                            progressBar.setVisibility(View.GONE);
                            saveBackImage(bitmap, null);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to recognize text from captured image, auto-saving", e);
                        // Auto-save even if recognition failed
                        progressBar.setVisibility(View.GONE);
                        saveBackImage(bitmap, null);
                    });
        } else {
            // Auto-save without showing confirmation dialog
            progressBar.setVisibility(View.GONE);
            CccdBackParser.CccdBackData data = CccdBackParser.parseBackText(textToParse);
            saveBackImage(bitmap, data);
        }
    }
    
    /**
     * Show confirmation dialog with extracted data
     */
    private void showConfirmationDialog(Bitmap bitmap, String recognizedText) {
        // Parse the text
        CccdBackParser.CccdBackData data = CccdBackParser.parseBackText(recognizedText);
        
        // Show preview first
        showImagePreview(bitmap);
        progressBar.setVisibility(View.GONE);
        
        if (data == null || !data.isValid()) {
            // Invalid data, but still show preview and allow user to confirm
            Log.w(TAG, "Could not parse valid data from text, but showing preview for confirmation");
            
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận ảnh")
                    .setMessage("Đã chụp ảnh mặt sau CCCD.\n\n" +
                               "Không thể trích xuất đầy đủ thông tin từ ảnh này.\n\n" +
                               "Bạn có muốn sử dụng ảnh này không?")
                    .setPositiveButton("Xác nhận", (dialog, which) -> {
                        // Save and finish to return to fragment
                        // finish() will be called inside saveBackImage after delay
                        saveBackImage(bitmap, null);
                    })
                    .setNegativeButton("Chụp lại", (dialog, which) -> {
                        resetScan();
                        // startCamera() is called inside resetScan() with delay
                    })
                    .setCancelable(false)
                    .show();
            return;
        }
        
        // Build confirmation message
        StringBuilder message = new StringBuilder();
        message.append("Thông tin đã trích xuất:\n\n");
        
        if (data.getPermanentAddress() != null && !data.getPermanentAddress().isEmpty()) {
            message.append("• Địa chỉ thường trú: ").append(data.getPermanentAddress()).append("\n");
        }
        if (data.getIssueDate() != null && !data.getIssueDate().isEmpty()) {
            message.append("• Ngày cấp: ").append(data.getIssueDate()).append("\n");
        }
        if (data.getIssuePlace() != null && !data.getIssuePlace().isEmpty()) {
            message.append("• Nơi cấp: ").append(data.getIssuePlace()).append("\n");
        }
        if (data.getRace() != null && !data.getRace().isEmpty()) {
            message.append("• Dân tộc: ").append(data.getRace()).append("\n");
        }
        if (data.getReligion() != null && !data.getReligion().isEmpty()) {
            message.append("• Tôn giáo: ").append(data.getReligion()).append("\n");
        }
        if (data.getHometown() != null && !data.getHometown().isEmpty()) {
            message.append("• Quê quán: ").append(data.getHometown()).append("\n");
        }
        message.append("\nXác nhận lưu thông tin này?");
        
        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận thông tin")
                .setMessage(message.toString())
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    // Save and finish to return to fragment
                    // finish() will be called inside saveBackImage after delay
                    saveBackImage(bitmap, data);
                })
                .setNegativeButton("Chụp lại", (dialog, which) -> {
                    resetScan();
                    // startCamera() is called inside resetScan() with delay
                })
                .setCancelable(false)
                .show();
    }
    
    /**
     * Update status with extracted data feedback
     */
    private void updateStatusWithExtractedData(CccdBackParser.CccdBackData data) {
        if (data == null) return;
        
        int foundFields = 0;
        if (data.getPermanentAddress() != null && !data.getPermanentAddress().isEmpty()) foundFields++;
        if (data.getIssueDate() != null && !data.getIssueDate().isEmpty()) foundFields++;
        if (data.getIssuePlace() != null && !data.getIssuePlace().isEmpty()) foundFields++;
        
        if (foundFields > 0) {
            updateStatus("Đã nhận diện " + foundFields + " trường thông tin. Giữ ổn định...");
        }
    }
    
    private void saveBackImage(Bitmap bitmap, CccdBackParser.CccdBackData extractedData) {
        try {
            // Ensure camera is stopped and unbound before saving
            isScanning = false;
            isCapturing = false;
            
            // Unbind camera to release resources and prevent black screen
            if (cameraProvider != null) {
                try {
                    cameraProvider.unbindAll();
                    Log.d(TAG, "Camera unbound before saving image");
                } catch (Exception e) {
                    Log.e(TAG, "Error unbinding camera before save", e);
                }
            }
            
            // Cancel any pending timeouts
            if (timeoutRunnable != null) {
                mainHandler.removeCallbacks(timeoutRunnable);
                timeoutRunnable = null;
            }
            
            // Save to internal storage
            File internalDir = new File(getFilesDir(), "cccd_images");
            if (!internalDir.exists()) {
                internalDir.mkdirs();
            }
            
            String fileName = "cccd_back_" + System.currentTimeMillis() + ".jpg";
            File imageFile = new File(internalDir, fileName);
            
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, fos);
            fos.flush();
            fos.close();
            
            // DO NOT encode to base64 and pass via Intent - it causes TransactionTooLargeException
            // Base64 can be generated from file path later if needed
            
            // Return result - only pass path and metadata, NOT base64
            Intent resultIntent = new Intent();
            resultIntent.putExtra("back_image_path", imageFile.getAbsolutePath());
            // Removed base64 to avoid TransactionTooLargeException
            // resultIntent.putExtra("back_image_base64", base64Image);
            resultIntent.putExtra("back_image_width", bitmap.getWidth());
            resultIntent.putExtra("back_image_height", bitmap.getHeight());
            
            // Add extracted data if available
            if (extractedData != null) {
                resultIntent.putExtra("permanent_address", extractedData.getPermanentAddress());
                resultIntent.putExtra("issue_date", extractedData.getIssueDate());
                resultIntent.putExtra("issue_place", extractedData.getIssuePlace());
                resultIntent.putExtra("race", extractedData.getRace());
                resultIntent.putExtra("religion", extractedData.getReligion());
                resultIntent.putExtra("hometown", extractedData.getHometown());
                resultIntent.putExtra("has_authority_seal", extractedData.hasAuthoritySeal());
            }
            
            setResult(RESULT_OK, resultIntent);
            
            Log.d(TAG, "Back image saved: " + imageFile.getAbsolutePath());
            Log.d(TAG, "Result intent prepared with back_image_path: " + imageFile.getAbsolutePath());
            
            // Show toast and finish immediately (camera already unbound)
            Toast.makeText(this, "Đã chụp ảnh mặt sau CCCD thành công!", Toast.LENGTH_SHORT).show();
            
            // Finish activity immediately since camera is already unbound
            // This prevents black screen issue
            mainHandler.postDelayed(() -> {
                if (!isFinishing() && !isDestroyed()) {
                    try {
                        Log.d(TAG, "Finishing activity after saving image");
                        finish();
                    } catch (Exception e) {
                        Log.e(TAG, "Error finishing activity", e);
                    }
                }
            }, 300);
            
        } catch (IOException e) {
            Log.e(TAG, "Error saving back image", e);
            showError("Lỗi khi lưu ảnh. Vui lòng thử lại.");
        }
    }
    
    /**
     * Encode bitmap to base64 (not used in Intent to avoid TransactionTooLargeException)
     * Can be called separately if needed from file path
     */
    private String encodeToBase64(Bitmap bitmap) {
        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        // Use lower quality for base64 to reduce size if needed
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
    }
    
    /**
     * Encode file to base64 (utility method if needed later)
     */
    public static String encodeFileToBase64(String filePath) {
        try {
            java.io.File file = new java.io.File(filePath);
            byte[] fileBytes = new byte[(int) file.length()];
            java.io.FileInputStream fis = new java.io.FileInputStream(file);
            fis.read(fileBytes);
            fis.close();
            return Base64.encodeToString(fileBytes, Base64.NO_WRAP);
        } catch (Exception e) {
            android.util.Log.e("CccdBackScanner", "Error encoding file to base64", e);
            return null;
        }
    }
    
    private void handleTimeout() {
        if (isTimeoutDialogShown || !isScanning) {
            return;
        }
        
        isScanning = false;
        isTimeoutDialogShown = true;
        
        if (timeoutRunnable != null) {
            mainHandler.removeCallbacks(timeoutRunnable);
            timeoutRunnable = null;
        }
        
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        
        runOnUiThread(() -> {
            new AlertDialog.Builder(this)
                    .setTitle("Hết thời gian")
                    .setMessage("Không phát hiện mặt sau CCCD sau 10 giây. Vui lòng:\n" +
                            "• Đảm bảo ánh sáng đủ sáng\n" +
                            "• Đặt CCCD thẳng, không bị nghiêng\n" +
                            "• Giữ điện thoại ổn định\n" +
                            "• Tránh phản chiếu ánh sáng")
                    .setPositiveButton("Thử lại", (dialog, which) -> {
                        isTimeoutDialogShown = false;
                        resetScan();
                        // startCamera() is called inside resetScan() with delay
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
        
        // Unbind camera first to ensure clean state
        if (cameraProvider != null) {
            try {
                cameraProvider.unbindAll();
                Log.d(TAG, "Camera unbound in resetScan");
            } catch (Exception e) {
                Log.e(TAG, "Error unbinding camera in resetScan", e);
            }
        }
        
        progressBar.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);
        ivPreview.setVisibility(View.GONE);
        previewView.setVisibility(View.VISIBLE);
        
        if (timeoutRunnable != null) {
            mainHandler.removeCallbacks(timeoutRunnable);
            timeoutRunnable = null;
        }
        
        // Small delay before restarting camera to ensure clean state
        mainHandler.postDelayed(() -> {
            if (!isFinishing() && !isDestroyed()) {
                startCamera();
            }
        }, 100);
    }
    
    private void startTimeoutTimer() {
        if (timeoutRunnable != null) {
            mainHandler.removeCallbacks(timeoutRunnable);
        }
        
        timeoutRunnable = () -> {
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
        
        if (textRecognizer != null) {
            textRecognizer.close();
        }
        
        if (imageAnalysisExecutor != null) {
            imageAnalysisExecutor.shutdown();
        }
    }
}

