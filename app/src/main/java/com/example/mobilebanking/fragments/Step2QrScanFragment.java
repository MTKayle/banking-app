package com.example.mobilebanking.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.mobilebanking.R;
import com.example.mobilebanking.activities.MainRegistrationActivity;
import com.example.mobilebanking.models.RegistrationData;
import com.example.mobilebanking.utils.CccdQrParser;
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
 * Step 2: QR Code Scanning and Confirmation (embedded scanner)
 */
public class Step2QrScanFragment extends Fragment {
    private static final String TAG = "Step2QrScanFragment";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final long SCAN_TIMEOUT_MS = 30000; // 30s

    private RegistrationData registrationData;

    // UI
    private Button btnConfirm;
    private LinearLayout llInfoContainer;
    private TextView tvFullName, tvIdNumber, tvDateOfBirth, tvGender, tvAddress, tvIssueDate;
    private TextView tvStatus;
    private ProgressBar progressBar;
    private PreviewView previewView;
    private View overlayFrame;

    // Camera / ML Kit
    private ExecutorService cameraExecutor;
    private ProcessCameraProvider cameraProvider;
    private Camera camera;
    private CameraControl cameraControl;
    private com.google.mlkit.vision.barcode.BarcodeScanner barcodeScanner;
    private AtomicBoolean isProcessing = new AtomicBoolean(false);
    private Handler mainHandler;
    private long scanStartTime;

    public static Step2QrScanFragment newInstance(RegistrationData data) {
        Step2QrScanFragment fragment = new Step2QrScanFragment();
        fragment.registrationData = data;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_step2_qr_scan, container, false);

        // Ensure registrationData is not null
        ensureRegistrationData();

        mainHandler = new Handler(Looper.getMainLooper());

        initializeViews(view);
        setupListeners();
        setupBarcodeScanner();
        startCameraIfPermitted();
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
        previewView = view.findViewById(R.id.preview_view);
        overlayFrame = view.findViewById(R.id.overlay_frame);
        tvStatus = view.findViewById(R.id.tv_status);
        progressBar = view.findViewById(R.id.progress_bar);

        btnConfirm = view.findViewById(R.id.btn_confirm);
        llInfoContainer = view.findViewById(R.id.ll_info_container);
        tvFullName = view.findViewById(R.id.tv_full_name);
        tvIdNumber = view.findViewById(R.id.tv_id_number);
        tvDateOfBirth = view.findViewById(R.id.tv_date_of_birth);
        tvGender = view.findViewById(R.id.tv_gender);
        tvAddress = view.findViewById(R.id.tv_address);
        tvIssueDate = view.findViewById(R.id.tv_issue_date);

        // Better preview performance
        previewView.setImplementationMode(PreviewView.ImplementationMode.PERFORMANCE);
    }

    private void setupListeners() {
        btnConfirm.setOnClickListener(v -> confirmAndContinue());
    }

    private void loadData() {
        ensureRegistrationData();

        // Display CCCD data if already scanned
        if (registrationData != null && registrationData.isStep2Complete()) {
            displayCccdData();
        }
    }

    private void setupBarcodeScanner() {
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
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
    }

    private void startCameraIfPermitted() {
        if (checkCameraPermission()) {
            startCamera();
        } else {
            updateStatus("Yêu cầu quyền truy cập camera...", true);
            requestCameraPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(requireContext(),
                        "Bạn cần cấp quyền camera để quét mã QR", Toast.LENGTH_LONG).show();
                updateStatus("Không có quyền camera", false);
            }
        }
    }

    private void startCamera() {
        updateStatus("Đang khởi động camera...", true);

        cameraExecutor = Executors.newSingleThreadExecutor();

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                if (cameraProvider == null) {
                    updateStatus("Không khởi động được camera", false);
                    return;
                }
                bindCameraUseCases();
                scanStartTime = System.currentTimeMillis();
                startScanTimeout();
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera", e);
                updateStatus("Lỗi khởi động camera", false);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void startScanTimeout() {
        mainHandler.postDelayed(() -> {
            if (!isProcessing.get()) {
                updateStatus("Không tìm thấy mã QR. Vui lòng thử lại.", false);
            }
        }, SCAN_TIMEOUT_MS);
    }

    private void bindCameraUseCases() {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .setTargetResolution(new android.util.Size(1280, 720))
                .build();

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
                    getViewLifecycleOwner(), cameraSelector, preview, imageAnalysis);
            cameraControl = camera.getCameraControl();

            updateStatus("Đang quét mã QR...", false);
        } catch (Exception e) {
            Log.e(TAG, "Error binding camera use cases", e);
            updateStatus("Lỗi khởi động camera", false);
        }
    }

    private void processImageProxy(androidx.camera.core.ImageProxy imageProxy) {
        if (isProcessing.get()) {
            imageProxy.close();
            return;
        }

        try {
            if (imageProxy.getImage() == null) {
                imageProxy.close();
                return;
            }

            InputImage image = InputImage.fromMediaImage(
                    imageProxy.getImage(),
                    imageProxy.getImageInfo().getRotationDegrees());

            barcodeScanner.process(image)
                    .addOnSuccessListener(barcodes -> {
                        try {
                            if (isProcessing.get()) {
                                imageProxy.close();
                                return;
                            }

                            if (barcodes.isEmpty()) {
                                imageProxy.close();
                                return;
                            }

                            for (Barcode barcode : barcodes) {
                                String qrData = barcode.getRawValue();
                                if (qrData != null && !qrData.isEmpty()) {
                                    isProcessing.set(true);
                                    updateStatus("Đã quét thành công!", false);
                                    processQrData(qrData);
                                    imageProxy.close();
                                    return;
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
                        imageProxy.close();
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error creating InputImage: " + e.getMessage(), e);
            imageProxy.close();
        }
    }

    private void updateStatus(String message, boolean showProgress) {
        if (mainHandler == null) return;
        mainHandler.post(() -> {
            if (tvStatus != null) {
                tvStatus.setText(message);
            }
            if (progressBar != null) {
                progressBar.setVisibility(showProgress ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void processQrData(String qrData) {
        CccdQrParser.CccdData cccdData = CccdQrParser.parseQrData(qrData);

        if (cccdData == null) {
            Toast.makeText(requireContext(), "Không thể đọc thông tin từ mã QR", Toast.LENGTH_SHORT).show();
            isProcessing.set(false);
            updateStatus("Không thể đọc thông tin từ mã QR. Vui lòng thử lại.", false);
            return;
        }

        // Save data
        registrationData.setFullName(cccdData.getFullName());
        registrationData.setIdNumber(cccdData.getIdNumber());
        registrationData.setDateOfBirth(cccdData.getDateOfBirth());
        registrationData.setGender(cccdData.getGender());
        registrationData.setPermanentAddress(cccdData.getPermanentAddress());
        registrationData.setIssueDate(cccdData.getIssueDate());

        // Display data
        displayCccdData();
        Toast.makeText(requireContext(), "Đã lấy thông tin từ CCCD thành công", Toast.LENGTH_SHORT).show();
    }

    private void displayCccdData() {
        llInfoContainer.setVisibility(View.VISIBLE);
        btnConfirm.setVisibility(View.VISIBLE);

        // Highlight title and confirm button with primary (green) color theme
        if (btnConfirm != null) {
            btnConfirm.setBackgroundTintList(
                    ContextCompat.getColorStateList(requireContext(), R.color.primary_color));
            btnConfirm.setTextColor(
                    ContextCompat.getColor(requireContext(), android.R.color.white));
        }

        // Hide scanner UI once data has been captured
        if (previewView != null) {
            previewView.setVisibility(View.GONE);
        }
        if (overlayFrame != null) {
            overlayFrame.setVisibility(View.GONE);
        }
        if (tvStatus != null) {
            tvStatus.setVisibility(View.GONE);
        }
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        // Stop camera and analysis to save resources
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            cameraProvider = null;
        }
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
            cameraExecutor = null;
        }
        if (barcodeScanner != null) {
            barcodeScanner.close();
            barcodeScanner = null;
        }

        tvFullName.setText(registrationData.getFullName() != null ? registrationData.getFullName() : "Chưa có");
        tvIdNumber.setText(registrationData.getIdNumber() != null ? registrationData.getIdNumber() : "Chưa có");
        tvDateOfBirth.setText(registrationData.getDateOfBirth() != null ? registrationData.getDateOfBirth() : "Chưa có");
        tvGender.setText(registrationData.getGender() != null ? registrationData.getGender() : "Chưa có");
        tvAddress.setText(registrationData.getPermanentAddress() != null ? registrationData.getPermanentAddress() : "Chưa có");
        tvIssueDate.setText(registrationData.getIssueDate() != null ? registrationData.getIssueDate() : "Chưa có");
    }

    private void confirmAndContinue() {
        if (!registrationData.isStep2Complete()) {
            Toast.makeText(requireContext(), "Vui lòng quét mã QR CCCD trước", Toast.LENGTH_SHORT).show();
            return;
        }

        android.util.Log.d("Step2QrScanFragment", "confirmAndContinue called, calling goToNextStep");
        if (getActivity() instanceof MainRegistrationActivity) {
            ((MainRegistrationActivity) getActivity()).goToNextStep();
        } else {
            android.util.Log.e("Step2QrScanFragment", "Activity is not MainRegistrationActivity");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (barcodeScanner != null) {
            barcodeScanner.close();
            barcodeScanner = null;
        }

        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
            cameraExecutor = null;
        }

        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            cameraProvider = null;
        }
    }
}

