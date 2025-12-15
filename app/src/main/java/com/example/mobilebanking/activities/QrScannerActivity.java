package com.example.mobilebanking.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.mobilebanking.R;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * QR Code Scanner Activity for scanning Vietnamese CCCD QR codes
 * Enhanced with flash control, status indicators, and better error handling
 */
public class QrScannerActivity extends AppCompatActivity {
    private static final String TAG = "QrScannerActivity";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final long SCAN_TIMEOUT_MS = 30000; // 30 seconds timeout
    
    private PreviewView previewView;
    private ExecutorService cameraExecutor;
    private ProcessCameraProvider cameraProvider;
    private Camera camera;
    private CameraControl cameraControl;
    
    private TextView tvStatus;
    private TextView tvInstructions;
    private ProgressBar progressBar;
    private ImageButton btnFlash;
    private Button btnManualInput;
    
    private com.google.mlkit.vision.barcode.BarcodeScanner barcodeScanner;
    private AtomicBoolean isProcessing = new AtomicBoolean(false);
    private AtomicBoolean isFlashOn = new AtomicBoolean(false);
    private Handler mainHandler;
    private long scanStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        mainHandler = new Handler(Looper.getMainLooper());
        
        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quét mã QR CCCD");
        }

        initializeViews();
        setupBarcodeScanner();
        
        if (checkCameraPermission()) {
            startCamera();
        } else {
            requestCameraPermission();
        }
    }
    
    private void initializeViews() {
        previewView = findViewById(R.id.preview_view);
        tvStatus = findViewById(R.id.tv_status);
        tvInstructions = findViewById(R.id.tv_instructions);
        progressBar = findViewById(R.id.progress_bar);
        btnFlash = findViewById(R.id.btn_flash);
        btnManualInput = findViewById(R.id.btn_manual_input);
        
        // Set PreviewView implementation mode for better performance
        previewView.setImplementationMode(PreviewView.ImplementationMode.PERFORMANCE);
        
        cameraExecutor = Executors.newSingleThreadExecutor();
        
        btnFlash.setOnClickListener(v -> toggleFlash());
        btnManualInput.setOnClickListener(v -> showManualInputDialog());
        
        updateStatus("Đang khởi động camera...", false);
        
        Log.d(TAG, "Views initialized");
    }
    
    private void setupBarcodeScanner() {
        Log.d(TAG, "Setting up barcode scanner...");
        
        // Configure barcode scanner to support all QR code formats
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_QR_CODE,
                    Barcode.FORMAT_DATA_MATRIX,
                    Barcode.FORMAT_AZTEC,
                    Barcode.FORMAT_PDF417,
                    Barcode.FORMAT_CODE_128,
                    Barcode.FORMAT_CODE_39
                )
                .enableAllPotentialBarcodes()
                .build();
        
        barcodeScanner = BarcodeScanning.getClient(options);
        Log.d(TAG, "Barcode scanner initialized successfully");
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED;
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
                .setTitle("Quyền truy cập camera")
                .setMessage("Ứng dụng cần quyền truy cập camera để quét mã QR. Vui lòng cấp quyền trong Cài đặt.")
                .setPositiveButton("Cài đặt", (dialog, which) -> {
                    // Open app settings
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Hủy", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void startCamera() {
        Log.d(TAG, "Starting camera...");
        updateStatus("Đang khởi động camera...", true);
        
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = 
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                Log.d(TAG, "Getting camera provider...");
                cameraProvider = cameraProviderFuture.get();
                
                if (cameraProvider == null) {
                    Log.e(TAG, "Camera provider is null");
                    updateStatus("Lỗi: Camera provider không khả dụng", false);
                    showErrorDialog("Không thể khởi động camera. Vui lòng thử lại.", true);
                    return;
                }
                
                Log.d(TAG, "Camera provider obtained. Binding use cases...");
                bindCameraUseCases();
                scanStartTime = System.currentTimeMillis();
                startScanTimeout();
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera", e);
                updateStatus("Lỗi khởi động camera", false);
                showErrorDialog("Không thể khởi động camera. Vui lòng thử lại hoặc nhập thủ công.", true);
            }
        }, ContextCompat.getMainExecutor(this));
    }
    
    private void startScanTimeout() {
        mainHandler.postDelayed(() -> {
            if (!isProcessing.get()) {
                updateStatus("Không tìm thấy mã QR. Vui lòng thử lại.", false);
                showTimeoutDialog();
            }
        }, SCAN_TIMEOUT_MS);
    }
    
    private void showTimeoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Không quét được mã QR")
                .setMessage("Không thể quét được mã QR sau 30 giây. Bạn có muốn:\n\n" +
                           "• Thử lại quét\n" +
                           "• Nhập thông tin thủ công\n" +
                           "• Kiểm tra:\n" +
                           "  - Ánh sáng đủ sáng\n" +
                           "  - Mã QR rõ ràng, không bị mờ\n" +
                           "  - Khoảng cách phù hợp (20-30cm)")
                .setPositiveButton("Thử lại", (dialog, which) -> {
                    scanStartTime = System.currentTimeMillis();
                    startScanTimeout();
                    updateStatus("Đang quét mã QR...", false);
                })
                .setNeutralButton("Nhập thủ công", (dialog, which) -> showManualInputDialog())
                .setNegativeButton("Hủy", (dialog, which) -> finish())
                .show();
    }

    private void bindCameraUseCases() {
        Log.d(TAG, "Binding camera use cases");
        
        Preview preview = new Preview.Builder()
                .build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Configure ImageAnalysis with optimal settings for QR scanning
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .setTargetResolution(new android.util.Size(1280, 720)) // Optimal resolution for QR scanning
                .build();

        // Set analyzer with proper error handling
        imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
            try {
                if (!isProcessing.get()) {
                    processImageProxy(imageProxy);
                } else {
                    imageProxy.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in analyzer", e);
                imageProxy.close();
            }
        });

        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        try {
            cameraProvider.unbindAll();
            camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis);
            cameraControl = camera.getCameraControl();
            
            Log.d(TAG, "Camera bound successfully");
            
            // Check if flash is available
            if (camera.getCameraInfo().hasFlashUnit()) {
                btnFlash.setVisibility(View.VISIBLE);
                Log.d(TAG, "Flash unit available");
            } else {
                btnFlash.setVisibility(View.GONE);
                Log.d(TAG, "No flash unit available");
            }
            
            updateStatus("Đang quét mã QR...", false);
            updateInstructions("Đặt mã QR CCCD vào khung hình. Giữ điện thoại cách 20-30cm.");
            
            Log.d(TAG, "Camera setup complete. Ready to scan QR codes.");
        } catch (Exception e) {
            Log.e(TAG, "Error binding camera use cases", e);
            updateStatus("Lỗi khởi động camera", false);
            showErrorDialog("Không thể khởi động camera: " + e.getMessage(), true);
        }
    }
    
    private void toggleFlash() {
        if (cameraControl == null || !camera.getCameraInfo().hasFlashUnit()) {
            return;
        }
        
        boolean newState = !isFlashOn.get();
        isFlashOn.set(newState);
        
        try {
            cameraControl.enableTorch(newState);
            btnFlash.setImageResource(newState ? 
                R.drawable.ic_flash_on : R.drawable.ic_flash_off);
            Toast.makeText(this, newState ? "Đã bật đèn flash" : "Đã tắt đèn flash", 
                Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error toggling flash", e);
            Toast.makeText(this, "Không thể bật/tắt flash", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateStatus(String message, boolean showProgress) {
        mainHandler.post(() -> {
            if (tvStatus != null) {
                tvStatus.setText(message);
            }
            if (progressBar != null) {
                progressBar.setVisibility(showProgress ? View.VISIBLE : View.GONE);
            }
        });
    }
    
    private void updateInstructions(String message) {
        mainHandler.post(() -> {
            if (tvInstructions != null) {
                tvInstructions.setText(message);
            }
        });
    }

    private void processImageProxy(androidx.camera.core.ImageProxy imageProxy) {
        if (isProcessing.get()) {
            imageProxy.close();
            return;
        }
        
        try {
            // Create InputImage from ImageProxy
            InputImage image = InputImage.fromMediaImage(
                    imageProxy.getImage(), 
                    imageProxy.getImageInfo().getRotationDegrees());
            
            Log.v(TAG, "Processing image. Rotation: " + imageProxy.getImageInfo().getRotationDegrees());

            // Process image with barcode scanner
            barcodeScanner.process(image)
                    .addOnSuccessListener(barcodes -> {
                        try {
                            if (isProcessing.get()) {
                                imageProxy.close();
                                return;
                            }
                            
                            Log.d(TAG, "Barcode detection result: " + barcodes.size() + " barcodes found");
                            
                            if (barcodes.isEmpty()) {
                                imageProxy.close();
                                return;
                            }
                            
                            for (Barcode barcode : barcodes) {
                                Log.d(TAG, "Barcode type: " + barcode.getValueType() + 
                                          ", Format: " + barcode.getFormat());
                                
                                // Accept all barcode types that might contain QR data
                                // QR codes can be detected as TYPE_TEXT, TYPE_UNKNOWN, or other types
                                String qrData = barcode.getRawValue();
                                
                                if (qrData != null && !qrData.isEmpty()) {
                                    Log.d(TAG, "QR code scanned successfully!");
                                    Log.d(TAG, "QR data length: " + qrData.length());
                                    Log.d(TAG, "QR data preview (first 100 chars): " + 
                                          (qrData.length() > 100 ? qrData.substring(0, 100) : qrData));
                                    
                                    isProcessing.set(true);
                                    
                                    // Return result and finish
                                    Intent resultIntent = new Intent();
                                    resultIntent.putExtra("qr_data", qrData);
                                    setResult(RESULT_OK, resultIntent);
                                    
                                    updateStatus("Đã quét thành công!", false);
                                    
                                    // Small delay to show success message
                                    mainHandler.postDelayed(() -> {
                                        finish();
                                    }, 500);
                                    
                                    imageProxy.close();
                                    return;
                                } else {
                                    Log.w(TAG, "Barcode found but raw value is null or empty");
                                }
                            }
                            
                            imageProxy.close();
                        } catch (Exception e) {
                            Log.e(TAG, "Error in success listener", e);
                            imageProxy.close();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error processing barcode: " + e.getMessage(), e);
                        // Don't show error for every frame, just log it
                        imageProxy.close();
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error creating InputImage: " + e.getMessage(), e);
            imageProxy.close();
        }
    }
    
    private void showManualInputDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Nhập thông tin thủ công")
                .setMessage("Bạn có muốn quay lại màn hình đăng ký để nhập thông tin thủ công không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    setResult(RESULT_CANCELED);
                    finish();
                })
                .setNegativeButton("Không", null)
                .show();
    }
    
    private void showErrorDialog(String message, boolean allowManual) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Lỗi")
                .setMessage(message)
                .setPositiveButton("Thử lại", (dialog, which) -> {
                    if (checkCameraPermission()) {
                        startCamera();
                    } else {
                        requestCameraPermission();
                    }
                });
        
        if (allowManual) {
            builder.setNeutralButton("Nhập thủ công", (dialog, which) -> showManualInputDialog());
        }
        
        builder.setNegativeButton("Hủy", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Turn off flash when pausing
        if (cameraControl != null && isFlashOn.get()) {
            try {
                cameraControl.enableTorch(false);
                isFlashOn.set(false);
            } catch (Exception e) {
                Log.e(TAG, "Error turning off flash", e);
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Turn off flash
        if (cameraControl != null && isFlashOn.get()) {
            try {
                cameraControl.enableTorch(false);
            } catch (Exception e) {
                Log.e(TAG, "Error turning off flash", e);
            }
        }
        
        if (barcodeScanner != null) {
            barcodeScanner.close();
        }
        
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
        
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }
}

