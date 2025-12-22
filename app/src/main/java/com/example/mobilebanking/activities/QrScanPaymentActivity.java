package com.example.mobilebanking.activities;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.QrApiService;
import com.example.mobilebanking.api.dto.QrScanRequest;
import com.example.mobilebanking.api.dto.QrScanResponse;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * QR Code Scanner Activity for Payment
 * Modern UI with camera preview and QR scanning capabilities
 */
public class QrScanPaymentActivity extends BaseActivity {
    private static final String TAG = "QrScanPaymentActivity";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int PICK_IMAGE_REQUEST = 101;
    
    private PreviewView previewView;
    private ImageView btnBack, btnFlash;
    private LinearLayout btnPasteQr, btnChooseImage;
    private ProgressBar progressBar;
    
    private ExecutorService cameraExecutor;
    private ProcessCameraProvider cameraProvider;
    private Camera camera;
    private CameraControl cameraControl;
    
    private com.google.mlkit.vision.barcode.BarcodeScanner barcodeScanner;
    private AtomicBoolean isProcessing = new AtomicBoolean(false);
    private AtomicBoolean isFlashOn = new AtomicBoolean(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scan_payment);

        initializeViews();
        setupBarcodeScanner();
        setupClickListeners();
        
        if (checkCameraPermission()) {
            startCamera();
        } else {
            requestCameraPermission();
        }
    }
    
    private void initializeViews() {
        previewView = findViewById(R.id.preview_view);
        btnBack = findViewById(R.id.btn_back);
        btnFlash = findViewById(R.id.btn_flash);
        btnPasteQr = findViewById(R.id.btn_paste_qr);
        btnChooseImage = findViewById(R.id.btn_choose_image);
        progressBar = findViewById(R.id.progress_bar);
        
        previewView.setImplementationMode(PreviewView.ImplementationMode.PERFORMANCE);
        cameraExecutor = Executors.newSingleThreadExecutor();
        
        Log.d(TAG, "Views initialized");
    }
    
    private void setupBarcodeScanner() {
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build();
        
        barcodeScanner = BarcodeScanning.getClient(options);
        Log.d(TAG, "Barcode scanner initialized");
    }
    
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnFlash.setOnClickListener(v -> toggleFlash());
        
        btnPasteQr.setOnClickListener(v -> pasteQrFromClipboard());
        
        btnChooseImage.setOnClickListener(v -> chooseImageFromGallery());
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
                .setMessage("Ứng dụng cần quyền truy cập camera để quét mã QR.")
                .setPositiveButton("Cài đặt", (dialog, which) -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Hủy", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void startCamera() {
        Log.d(TAG, "Starting camera...");
        
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
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
            if (!isProcessing.get()) {
                processImageProxy(imageProxy);
            } else {
                imageProxy.close();
            }
        });

        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        try {
            cameraProvider.unbindAll();
            camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis);
            cameraControl = camera.getCameraControl();
            
            if (camera.getCameraInfo().hasFlashUnit()) {
                btnFlash.setVisibility(View.VISIBLE);
            }
            
            Log.d(TAG, "Camera bound successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error binding camera", e);
            Toast.makeText(this, "Lỗi khởi động camera", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void processImageProxy(androidx.camera.core.ImageProxy imageProxy) {
        try {
            InputImage image = InputImage.fromMediaImage(
                    imageProxy.getImage(), 
                    imageProxy.getImageInfo().getRotationDegrees());

            barcodeScanner.process(image)
                    .addOnSuccessListener(barcodes -> {
                        if (!barcodes.isEmpty() && !isProcessing.get()) {
                            for (Barcode barcode : barcodes) {
                                String qrData = barcode.getRawValue();
                                if (qrData != null && !qrData.isEmpty()) {
                                    isProcessing.set(true);
                                    handleQrCodeScanned(qrData);
                                    break;
                                }
                            }
                        }
                        imageProxy.close();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error processing barcode", e);
                        imageProxy.close();
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error creating InputImage", e);
            imageProxy.close();
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
        } catch (Exception e) {
            Log.e(TAG, "Error toggling flash", e);
        }
    }
    
    private void pasteQrFromClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && clipboard.hasPrimaryClip()) {
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            String qrData = item.getText().toString();
            
            if (qrData != null && !qrData.isEmpty()) {
                handleQrCodeScanned(qrData);
            } else {
                Toast.makeText(this, "Clipboard trống", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Không có dữ liệu trong clipboard", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void chooseImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                scanQrFromImage(imageUri);
            }
        }
    }
    
    private void scanQrFromImage(Uri imageUri) {
        progressBar.setVisibility(View.VISIBLE);
        
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            
            InputImage image = InputImage.fromBitmap(bitmap, 0);
            
            barcodeScanner.process(image)
                    .addOnSuccessListener(barcodes -> {
                        progressBar.setVisibility(View.GONE);
                        
                        if (!barcodes.isEmpty()) {
                            String qrData = barcodes.get(0).getRawValue();
                            if (qrData != null && !qrData.isEmpty()) {
                                handleQrCodeScanned(qrData);
                            } else {
                                Toast.makeText(this, "Không tìm thấy mã QR trong ảnh", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Không tìm thấy mã QR trong ảnh", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "Error scanning image", e);
                        Toast.makeText(this, "Lỗi khi quét ảnh", Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            Log.e(TAG, "Error loading image", e);
            Toast.makeText(this, "Không thể tải ảnh", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void handleQrCodeScanned(String qrData) {
        Log.d(TAG, "QR Code scanned: " + qrData);
        
        // Show progress
        runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));
        
        // Call API to parse QR code
        QrApiService qrApiService = ApiClient.getQrApiService();
        QrScanRequest request = new QrScanRequest(qrData);
        
        Call<QrScanResponse> call = qrApiService.scanQr(request);
        call.enqueue(new Callback<QrScanResponse>() {
            @Override
            public void onResponse(Call<QrScanResponse> call, Response<QrScanResponse> response) {
                runOnUiThread(() -> progressBar.setVisibility(View.GONE));
                
                if (response.isSuccessful() && response.body() != null) {
                    QrScanResponse qrInfo = response.body();
                    
                    // Navigate to TransferActivity with data
                    Intent intent = new Intent(QrScanPaymentActivity.this, TransferActivity.class);
                    intent.putExtra("BANK_CODE", qrInfo.getBankCode());
                    intent.putExtra("BANK_NAME", qrInfo.getBankName());
                    intent.putExtra("BANK_BIN", qrInfo.getBankBin());
                    intent.putExtra("ACCOUNT_NUMBER", qrInfo.getAccountNumber());
                    intent.putExtra("ACCOUNT_HOLDER_NAME", qrInfo.getAccountHolderName());
                    
                    if (qrInfo.getAmount() != null && qrInfo.getAmount() > 0) {
                        intent.putExtra("AMOUNT", qrInfo.getAmount());
                    }
                    
                    if (qrInfo.getDescription() != null && !qrInfo.getDescription().isEmpty()) {
                        intent.putExtra("DESCRIPTION", qrInfo.getDescription());
                    }
                    
                    startActivity(intent);
                    finish();
                } else {
                    // API error
                    String errorMessage = "Không thể đọc mã QR";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
                    Toast.makeText(QrScanPaymentActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }
            
            @Override
            public void onFailure(Call<QrScanResponse> call, Throwable t) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error calling QR scan API", t);
                    Toast.makeText(QrScanPaymentActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
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
