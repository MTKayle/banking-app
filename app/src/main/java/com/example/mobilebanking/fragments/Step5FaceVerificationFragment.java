package com.example.mobilebanking.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import androidx.fragment.app.Fragment;

import com.example.mobilebanking.R;
import com.example.mobilebanking.activities.MainRegistrationActivity;
import com.example.mobilebanking.activities.LoginActivity;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.AuthApiService;
import com.example.mobilebanking.api.dto.AuthResponse;
import com.example.mobilebanking.models.RegistrationData;
import com.example.mobilebanking.models.User;
import com.example.mobilebanking.utils.DataManager;
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
 * Step 5: Face Verification
 * - Capture selfie using integrated camera
 * - Compare with portrait from CCCD front side
 * - Show result: XÁC THỰC THÀNH CÔNG or XÁC THỰC THẤT BẠI
 * - If successful, call register-with-face API
 */
public class Step5FaceVerificationFragment extends Fragment {
    private static final String TAG = "Step5FaceVerification";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 500;
    private static final int MIN_FACE_SIZE_PERCENTAGE = 60; // 60% of frame
    private static final int MIN_IMAGE_WIDTH = 640;
    private static final int MIN_IMAGE_HEIGHT = 480;
    private static final long CAPTURE_DELAY_MS = 3000; // Wait 3 seconds when ready before capturing
    
    private RegistrationData registrationData;
    
    private PreviewView previewView;
    private FaceDetectionOverlay overlayView;
    private TextView tvInstruction;
    private ProgressBar progressBar;
    
    private ProcessCameraProvider cameraProvider;
    private Camera camera;
    private ImageCapture imageCapture;
    private FaceDetector faceDetector;
    private ExecutorService imageAnalysisExecutor;
    private Handler mainHandler;
    
    private boolean isScanning = false;
    private boolean isCapturing = false;
    private boolean isVerifying = false;
    private boolean isDialogShowing = false;
    private boolean isCameraStarting = false; // Prevent multiple camera start attempts
    
    // Face detection state
    private Rect currentFaceRect;
    private float currentFaceSizePercentage = 0f;
    private boolean isReadyToCapture = false;
    private long readyToCaptureTime = 0;
    
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
        mainHandler = new Handler(Looper.getMainLooper());
        
        // Only create new executor if one doesn't exist
        if (imageAnalysisExecutor == null || imageAnalysisExecutor.isShutdown()) {
            imageAnalysisExecutor = Executors.newSingleThreadExecutor();
        }
        
        initializeViews(view);
        setupFaceDetector();
        
        // Reset states
        isScanning = false;
        isCapturing = false;
        isReadyToCapture = false;
        
        // Don't start camera here - let onViewCreated() and onResume() handle it
        // This ensures camera is started when fragment is actually visible
        Log.d(TAG, "onCreateView completed, camera will be started in onViewCreated() or onResume()");
        
        return view;
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated called");
        
        // Ensure views are initialized
        if (previewView == null) {
            initializeViews(view);
        }
        
        // Start camera if fragment is already visible (onResume might have been called before onViewCreated)
        // But only if camera is not already starting
        if (isResumed() && registrationData != null && registrationData.getSelfieImage() == null && 
            !isCameraStarting && !isScanning) {
            Log.d(TAG, "onViewCreated - Fragment is resumed, will start camera");
            view.postDelayed(() -> {
                if (isResumed() && isAdded() && getActivity() != null && !getActivity().isFinishing() && 
                    !isCameraStarting && !isScanning) {
                    if (checkCameraPermission()) {
                        startCamera();
                    } else {
                        requestCameraPermission();
                    }
                }
            }, 500);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
        Log.d(TAG, "onResume - isAdded: " + isAdded() + ", getView(): " + (getView() != null) + 
              ", previewView: " + (previewView != null) + ", selfieImage: " + 
              (registrationData != null && registrationData.getSelfieImage() != null) + 
              ", isScanning: " + isScanning + ", cameraProvider: " + (cameraProvider != null));
        
        // Ensure registration data is available
        ensureRegistrationData();
        
        // Wait for view to be created if it hasn't been yet
        if (getView() == null) {
            Log.d(TAG, "onResume - View not created yet, will wait for onViewCreated()");
            return;
        }
        
        // Ensure views are initialized
        if (previewView == null) {
            initializeViews(getView());
            Log.d(TAG, "onResume - Re-initialized views");
        }
        
        // Restart camera if needed
        if (registrationData.getSelfieImage() == null && 
            isAdded() && getActivity() != null && !getActivity().isFinishing() &&
            checkCameraPermission()) {
            
            Log.d(TAG, "onResume - Conditions met, preparing to start camera");
            
            // Reset scanning state if camera provider is null (camera was stopped)
            if (cameraProvider == null) {
                isScanning = false;
                Log.d(TAG, "onResume - Reset isScanning to false because cameraProvider is null");
            }
            
            if (previewView != null) {
                // Ensure preview view is visible
                previewView.setVisibility(View.VISIBLE);
                if (overlayView != null) {
                    overlayView.setVisibility(View.VISIBLE);
                }
                Log.d(TAG, "onResume - PreviewView visibility set to VISIBLE");
                
                // Delay to ensure previous camera is fully released and view is laid out
                getView().postDelayed(() -> {
                    if (isAdded() && getActivity() != null && !getActivity().isFinishing() && 
                        previewView != null && previewView.getWidth() > 0 && previewView.getHeight() > 0) {
                        // Unbind any existing camera first
                        if (cameraProvider != null) {
                            try {
                                cameraProvider.unbindAll();
                                cameraProvider = null;
                                isScanning = false;
                                Log.d(TAG, "onResume - Unbound existing camera");
                            } catch (Exception e) {
                                Log.w(TAG, "Error unbinding camera in onResume", e);
                            }
                        }
                        // Start camera after a short delay
                        previewView.postDelayed(() -> {
                            if (isAdded() && getActivity() != null && !getActivity().isFinishing() && 
                                previewView != null && !isScanning && !isCameraStarting) {
                                Log.d(TAG, "onResume - Starting camera now");
                                startCamera();
                            } else {
                                Log.d(TAG, "onResume - Conditions changed, not starting camera (isScanning: " + 
                                      isScanning + ", isCameraStarting: " + isCameraStarting + ")");
                            }
                        }, 500); // Delay to ensure previous camera is fully released
                    } else {
                        Log.d(TAG, "onResume - Conditions not met after delay, not starting camera");
                        if (previewView != null && (previewView.getWidth() == 0 || previewView.getHeight() == 0)) {
                            Log.d(TAG, "onResume - PreviewView not laid out yet, waiting...");
                            // Wait for layout
                            previewView.getViewTreeObserver().addOnGlobalLayoutListener(new android.view.ViewTreeObserver.OnGlobalLayoutListener() {
                                @Override
                                public void onGlobalLayout() {
                                    previewView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                    if (isAdded() && getActivity() != null && !getActivity().isFinishing() && 
                                        previewView != null && previewView.getWidth() > 0 && previewView.getHeight() > 0 && 
                                        !isScanning && !isCameraStarting) {
                                        Log.d(TAG, "onResume - PreviewView laid out, starting camera");
                                        startCamera();
                                    }
                                }
                            });
                        }
                    }
                }, 300);
            } else {
                Log.e(TAG, "onResume - previewView is null, cannot start camera");
            }
        } else {
            Log.d(TAG, "onResume - Conditions not met for starting camera");
            if (registrationData.getSelfieImage() != null) {
                Log.d(TAG, "onResume - Selfie already captured, will verify");
                // If selfie already captured, verify immediately
                verifyFace();
            }
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause called");
        // Stop camera when fragment is paused
        if (cameraProvider != null) {
            try {
                cameraProvider.unbindAll();
                Log.d(TAG, "Camera unbound in onPause");
            } catch (Exception e) {
                Log.e(TAG, "Error unbinding camera in onPause", e);
            }
        }
        isScanning = false;
        isCapturing = false;
        isCameraStarting = false;
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        
        // Unbind camera before destroying view
        if (cameraProvider != null) {
            try {
                cameraProvider.unbindAll();
                cameraProvider = null;
                Log.d(TAG, "Camera unbound in onDestroyView");
            } catch (Exception e) {
                Log.e(TAG, "Error unbinding camera in onDestroyView", e);
            }
        }
        
        if (imageAnalysisExecutor != null) {
            imageAnalysisExecutor.shutdown();
            imageAnalysisExecutor = null;
        }
        
        camera = null;
        imageCapture = null;
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
        overlayView = view.findViewById(R.id.overlay_view);
        tvInstruction = view.findViewById(R.id.tv_instruction);
        progressBar = view.findViewById(R.id.progress_bar);
        
        // Don't set visibility to GONE here - let startCamera() handle it
        // This ensures camera can start properly
        if (previewView != null) {
            // Keep visibility as set in XML initially, will be set to VISIBLE in startCamera()
            Log.d(TAG, "PreviewView found, current visibility: " + previewView.getVisibility());
        }
        if (overlayView != null) {
            // Keep visibility as set in XML initially, will be set to VISIBLE in startCamera()
            Log.d(TAG, "OverlayView found, current visibility: " + overlayView.getVisibility());
        }
        progressBar.setVisibility(View.GONE);
        
        // Text is already set in XML, no need to set here
    }
    
    private void setupFaceDetector() {
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setMinFaceSize(0.1f)
                .enableTracking()
                .build();
        
        faceDetector = FaceDetection.getClient(options);
    }
    
    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED;
    }
    
    private void requestCameraPermission() {
        requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isAdded() && getActivity() != null && !getActivity().isFinishing()) {
                    if (getView() != null) {
                        getView().postDelayed(() -> {
                            if (isAdded() && getActivity() != null && !getActivity().isFinishing()) {
                                startCamera();
                            }
                        }, 100);
                    }
                }
            } else {
                Toast.makeText(getActivity(), "Cần quyền camera để xác thực khuôn mặt", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private void startCamera() {
        if (isCameraStarting || isScanning) {
            Log.d(TAG, "Camera is already starting or scanning, skipping startCamera()");
            return;
        }
        
        if (previewView == null) {
            Log.e(TAG, "PreviewView is null, cannot start camera");
            return;
        }
        
        if (!isAdded() || getActivity() == null || getActivity().isFinishing()) {
            Log.e(TAG, "Fragment not attached or activity finishing, cannot start camera");
            return;
        }
        
        isCameraStarting = true;
        isScanning = true;
        Log.d(TAG, "startCamera() - Set isCameraStarting=true, isScanning=true");
        
        Log.d(TAG, "=== Starting camera initialization ===");
        Log.d(TAG, "PreviewView state: " + (previewView != null ? "exists" : "null"));
        Log.d(TAG, "PreviewView visibility: " + (previewView.getVisibility() == View.VISIBLE ? "VISIBLE" : "GONE"));
        
        // CRITICAL: Ensure preview view is visible BEFORE binding camera
        // This must be done on the main thread and before any camera operations
        previewView.setVisibility(View.VISIBLE);
        if (overlayView != null) {
            overlayView.setVisibility(View.VISIBLE);
        }
        Log.d(TAG, "PreviewView visibility set to VISIBLE");
        
        // Force a layout pass to ensure view is measured
        previewView.requestLayout();
        
        // Wait for view to be measured and laid out before binding camera
        previewView.post(() -> {
            // Double-check visibility
            if (previewView.getVisibility() != View.VISIBLE) {
                previewView.setVisibility(View.VISIBLE);
                Log.d(TAG, "PreviewView visibility corrected to VISIBLE");
            }
            
            int width = previewView.getWidth();
            int height = previewView.getHeight();
            Log.d(TAG, "PreviewView dimensions after post: " + width + "x" + height);
            
            // If view hasn't been laid out yet, wait for layout
            if (width == 0 || height == 0) {
                previewView.getViewTreeObserver().addOnGlobalLayoutListener(new android.view.ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        previewView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        int w = previewView.getWidth();
                        int h = previewView.getHeight();
                        Log.d(TAG, "PreviewView dimensions after layout: " + w + "x" + h);
                        if (w > 0 && h > 0) {
                            // Ensure visibility is still VISIBLE
                            if (previewView.getVisibility() != View.VISIBLE) {
                                previewView.setVisibility(View.VISIBLE);
                            }
                            initializeCameraProvider();
                        } else {
                            Log.e(TAG, "PreviewView still has zero dimensions after layout");
                            isCameraStarting = false;
                            isScanning = false;
                            Toast.makeText(getActivity(), "Không thể khởi động camera. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } else {
                // Ensure visibility is still VISIBLE before initializing
                if (previewView.getVisibility() != View.VISIBLE) {
                    previewView.setVisibility(View.VISIBLE);
                }
                initializeCameraProvider();
            }
        });
    }
    
    private void initializeCameraProvider() {
        if (!isAdded() || getActivity() == null || getActivity().isFinishing()) {
            Log.e(TAG, "Fragment not attached, cannot initialize camera provider");
            isCameraStarting = false;
            isScanning = false;
            return;
        }
        
        // First, ensure any existing camera provider is unbound
        if (cameraProvider != null) {
            try {
                cameraProvider.unbindAll();
                Log.d(TAG, "Unbound existing camera provider");
            } catch (Exception e) {
                Log.w(TAG, "Error unbinding existing camera provider", e);
            }
        }
        
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());
        
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                Log.d(TAG, "Camera provider obtained, binding use cases...");
                
                // Ensure we're still attached
                if (!isAdded() || getActivity() == null || getActivity().isFinishing()) {
                    Log.e(TAG, "Fragment detached during camera initialization");
                    return;
                }
                
                // Ensure preview view is visible before binding
                if (previewView != null) {
                    if (previewView.getVisibility() != View.VISIBLE) {
                        previewView.setVisibility(View.VISIBLE);
                        Log.d(TAG, "PreviewView visibility set to VISIBLE before binding");
                    }
                }
                
                // Bind camera on main thread
                if (previewView != null && previewView.getWidth() > 0 && previewView.getHeight() > 0) {
                    previewView.post(() -> {
                        // Double-check visibility
                        if (previewView.getVisibility() != View.VISIBLE) {
                            previewView.setVisibility(View.VISIBLE);
                        }
                        Log.d(TAG, "PreviewView ready, binding camera... width: " + previewView.getWidth() + ", height: " + previewView.getHeight());
                        bindCameraUseCases();
                    });
                } else {
                    Log.e(TAG, "PreviewView not ready for binding - width: " + 
                          (previewView != null ? previewView.getWidth() : 0) + 
                          ", height: " + (previewView != null ? previewView.getHeight() : 0));
                    Toast.makeText(getActivity(), "Lỗi khởi động camera. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
                }
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera", e);
                e.printStackTrace();
                isCameraStarting = false;
                isScanning = false;
                Toast.makeText(getActivity(), "Không thể khởi động camera. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
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
        
        if (!isAdded() || getActivity() == null || getActivity().isFinishing()) {
            Log.e(TAG, "Fragment not attached, cannot bind camera");
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
        
        // CRITICAL: Ensure preview view is visible and ready BEFORE binding
        // This must be done on main thread
        if (previewView.getVisibility() != View.VISIBLE) {
            previewView.setVisibility(View.VISIBLE);
            Log.d(TAG, "PreviewView visibility set to VISIBLE in bindCameraUseCases");
        }
        if (overlayView != null && overlayView.getVisibility() != View.VISIBLE) {
            overlayView.setVisibility(View.VISIBLE);
        }
        
        // Wait a moment to ensure visibility change is applied
        previewView.post(() -> {
            // Double-check visibility one more time
            if (previewView.getVisibility() != View.VISIBLE) {
                previewView.setVisibility(View.VISIBLE);
                Log.w(TAG, "PreviewView visibility was not VISIBLE, corrected");
            }
            
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
            Log.d(TAG, "PreviewView visibility after binding: " + 
                  (previewView.getVisibility() == View.VISIBLE ? "VISIBLE" : "GONE"));
            
            // Reset camera starting flag since camera is now bound
            isCameraStarting = false;
            Log.d(TAG, "Camera bound successfully, reset isCameraStarting=false");
            
            // Force preview view to request layout and invalidate
            previewView.post(() -> {
                previewView.requestLayout();
                previewView.invalidate();
                Log.d(TAG, "PreviewView layout requested and invalidated");
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error binding camera use cases", e);
            e.printStackTrace();
            isCameraStarting = false;
            isScanning = false;
            Toast.makeText(getActivity(), "Không thể khởi động camera. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
        }
        });
    }
    
    private void analyzeImage(ImageProxy image) {
        if (!isScanning || isCapturing) {
            image.close();
            return;
        }
        
        InputImage inputImage = InputImage.fromMediaImage(
                image.getImage(),
                image.getImageInfo().getRotationDegrees()
        );
        
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
        
        int faceCount = faces != null ? faces.size() : 0;
        
        if (faceCount == 0) {
            currentFaceRect = null;
            currentFaceSizePercentage = 0f;
            isReadyToCapture = false;
            mainHandler.post(() -> {
                if (overlayView != null) {
                    overlayView.updateFaceRect(null, 0f, false);
                }
            });
            return;
        }
        
        if (faceCount > 1) {
            mainHandler.post(() -> {
                if (overlayView != null) {
                    overlayView.clearFaceRect();
                }
            });
            return;
        }
        
        Face face = faces.get(0);
        Rect bounds = face.getBoundingBox();
        
        if (bounds == null || bounds.width() <= 0 || bounds.height() <= 0) {
            return;
        }
        
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        float faceArea = bounds.width() * bounds.height();
        float imageArea = imageWidth * imageHeight;
        float faceSizePercentage = (faceArea / imageArea) * 100f;
        
        int centerX = imageWidth / 2;
        int centerY = imageHeight / 2;
        int faceCenterX = bounds.centerX();
        int faceCenterY = bounds.centerY();
        
        boolean isCentered = Math.abs(faceCenterX - centerX) < imageWidth * 0.2f &&
                            Math.abs(faceCenterY - centerY) < imageHeight * 0.2f;
        
        float faceAngleY = Math.abs(face.getHeadEulerAngleY());
        boolean isStraight = faceAngleY <= 15f;
        
        boolean hasOcclusion = false;
        float leftEyeProb = face.getLeftEyeOpenProbability();
        float rightEyeProb = face.getRightEyeOpenProbability();
        
        if (leftEyeProb >= 0f && leftEyeProb < 0.3f) {
            hasOcclusion = true;
        }
        if (rightEyeProb >= 0f && rightEyeProb < 0.3f) {
            hasOcclusion = true;
        }
        
        boolean ready = faceSizePercentage >= MIN_FACE_SIZE_PERCENTAGE && 
                       isCentered && 
                       isStraight && 
                       !hasOcclusion;
        
        currentFaceRect = bounds;
        currentFaceSizePercentage = faceSizePercentage;
        
        if (ready && !isReadyToCapture) {
            isReadyToCapture = true;
            readyToCaptureTime = System.currentTimeMillis();
        } else if (!ready) {
            isReadyToCapture = false;
            readyToCaptureTime = 0;
            if (overlayView != null) {
                overlayView.setCountdown(0);
            }
        }
        
        mainHandler.post(() -> {
            if (overlayView != null) {
                overlayView.updateFaceRect(bounds, faceSizePercentage, ready);
            }
            
            if (ready) {
                long timeSinceReady = System.currentTimeMillis() - readyToCaptureTime;
                long remainingTime = CAPTURE_DELAY_MS - timeSinceReady;
                
                if (timeSinceReady >= CAPTURE_DELAY_MS && !isCapturing) {
                    captureImage();
                } else if (remainingTime > 0) {
                    int secondsRemaining = (int) Math.ceil(remainingTime / 1000.0);
                    if (overlayView != null) {
                        overlayView.setCountdown(secondsRemaining);
                    }
                }
            }
        });
    }
    
    private void captureImage() {
        if (isCapturing || imageCapture == null) return;
        
        isCapturing = true;
        progressBar.setVisibility(View.VISIBLE);
        
        File outputFile = new File(requireContext().getCacheDir(), "face_capture_" + System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(outputFile).build();
        
        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(requireContext()),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                        Bitmap bitmap = BitmapFactory.decodeFile(outputFile.getAbsolutePath());
                        
                        if (bitmap == null) {
                            Toast.makeText(getActivity(), "Không thể chụp ảnh. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                            resetScan();
                            return;
                        }
                        
                        // Rotate bitmap if needed (front camera usually needs 270 degree rotation)
                        int displayRotation = previewView.getDisplay().getRotation();
                        int rotationAngle = calculateFrontCameraRotation(displayRotation);
                        if (rotationAngle != 0) {
                            bitmap = rotateBitmap(bitmap, rotationAngle);
                        }
                        
                        // Validate and scale image if needed
                        if (bitmap.getWidth() < MIN_IMAGE_WIDTH || bitmap.getHeight() < MIN_IMAGE_HEIGHT) {
                            float scaleX = (float) MIN_IMAGE_WIDTH / bitmap.getWidth();
                            float scaleY = (float) MIN_IMAGE_HEIGHT / bitmap.getHeight();
                            float scale = Math.max(scaleX, scaleY);
                            int newWidth = (int) (bitmap.getWidth() * scale);
                            int newHeight = (int) (bitmap.getHeight() * scale);
                            bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
                        }
                        
                        // Save selfie and verify
                        registrationData.setSelfieImage(bitmap);
                        
                        if (previewView != null) {
                            previewView.setVisibility(View.GONE);
                        }
                        if (overlayView != null) {
                            overlayView.setVisibility(View.GONE);
                        }
                        
                        progressBar.setVisibility(View.VISIBLE);
                        
                        outputFile.delete();
                        
                        // Auto-verify after capture
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            if (isAdded() && getActivity() != null && !getActivity().isFinishing()) {
                                verifyFace();
                            }
                        }, 500);
                    }
                    
                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Image capture failed", exception);
                        Toast.makeText(getActivity(), "Không thể chụp ảnh. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                        resetScan();
                    }
                }
        );
    }
    
    private int calculateFrontCameraRotation(int displayRotation) {
        // Front camera rotation calculation
        // 0 = ROTATION_0, 1 = ROTATION_90, 2 = ROTATION_180, 3 = ROTATION_270
        int rotation = 0;
        switch (displayRotation) {
            case android.view.Surface.ROTATION_0:
                rotation = 270; // Front camera needs 270 for portrait
                break;
            case android.view.Surface.ROTATION_90:
                rotation = 0;
                break;
            case android.view.Surface.ROTATION_180:
                rotation = 90;
                break;
            case android.view.Surface.ROTATION_270:
                rotation = 180;
                break;
        }
        return rotation;
    }
    
    private Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        if (degrees == 0) return bitmap;
        
        android.graphics.Matrix matrix = new android.graphics.Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
    
    private void resetScan() {
        isScanning = false;
        isCapturing = false;
        isReadyToCapture = false;
        readyToCaptureTime = 0;
        progressBar.setVisibility(View.GONE);
        
        if (cameraProvider != null) {
            try {
                cameraProvider.unbindAll();
            } catch (Exception e) {
                Log.e(TAG, "Error unbinding camera", e);
            }
        }
        
        // Restart camera
        if (isAdded() && getActivity() != null && !getActivity().isFinishing()) {
            if (getView() != null) {
                getView().postDelayed(() -> {
                    if (isAdded() && getActivity() != null && !getActivity().isFinishing()) {
                        startCamera();
                    }
                }, 500);
            }
        }
    }
    
    
    /**
     * Verify face by calling backend API
     * Backend will compare selfie with portrait from CCCD
     */
    private void verifyFace() {
        if (isVerifying) {
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
            if (getView() != null) {
                progressBar.setVisibility(View.GONE);
            }
            Toast.makeText(getActivity(), "Lỗi: Không tìm thấy ảnh CCCD", Toast.LENGTH_LONG).show();
            return;
        }
        
        final File cccdPhotoFile = saveBitmapToFile(cccdImageToSend, "cccd_full");
        final File selfiePhotoFile = saveBitmapToFile(registrationData.getSelfieImage(), "selfie");
        
        if (cccdPhotoFile == null || selfiePhotoFile == null) {
            isVerifying = false;
            if (getView() != null) {
                progressBar.setVisibility(View.GONE);
            }
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
                progressBar.setVisibility(View.GONE);
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
                progressBar.setVisibility(View.GONE);
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
                        
                        // Reset all flags
                        isScanning = false;
                        isCapturing = false;
                        isVerifying = false;
                        isCameraStarting = false;
                        isReadyToCapture = false;
                        
                        // Unbind camera first
                        if (cameraProvider != null) {
                            try {
                                cameraProvider.unbindAll();
                                cameraProvider = null;
                                Log.d(TAG, "Camera unbound before retry");
                            } catch (Exception e) {
                                Log.e(TAG, "Error unbinding camera before retry", e);
                            }
                        }
                        
                        // Ensure views are visible
                        if (previewView != null) {
                            previewView.setVisibility(View.VISIBLE);
                        }
                        if (overlayView != null) {
                            overlayView.setVisibility(View.VISIBLE);
                            overlayView.clearFaceRect();
                        }
                        if (tvInstruction != null) {
                            tvInstruction.setText("Đặt khuôn mặt vào khung hình");
                        }
                        
                        // Restart camera after a short delay
                        if (isAdded() && getActivity() != null && !getActivity().isFinishing()) {
                            if (previewView != null) {
                                previewView.postDelayed(() -> {
                                    if (isAdded() && getActivity() != null && !getActivity().isFinishing()) {
                                        if (checkCameraPermission()) {
                                            startCamera();
                                        } else {
                                            requestCameraPermission();
                                        }
                                    }
                                }, 300);
                            }
                        }
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

