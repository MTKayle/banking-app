package com.example.mobilebanking.fragments;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mobilebanking.R;
import com.example.mobilebanking.activities.CccdAutoScannerActivity;
import com.example.mobilebanking.activities.MainRegistrationActivity;
import com.example.mobilebanking.models.RegistrationData;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.IOException;
import java.util.List;

/**
 * Step 3: Capture Front Side of CCCD
 */
public class Step3FrontCardFragment extends Fragment {
    private static final int REQUEST_IMAGE_CAPTURE = 300;
    private static final int REQUEST_IMAGE_CAPTURE_FULL = 301;
    private static final int REQUEST_AUTO_SCAN = 302;
    
    private static final String TAG = "Step3FrontCard";
    private static final int MIN_IMAGE_SIZE = 600; // Minimum size for saved portrait
    private static final int TARGET_IMAGE_SIZE = 800; // Target size for portrait (reduced to prevent overflow)
    
    private RegistrationData registrationData;
    
    private ImageView ivFrontCard;
    private TextView tvInstruction;
    private Button btnCapture, btnContinue, btnSaveImage, btnAutoScan;
    private ProgressBar progressBar;
    
    private FaceDetector faceDetector;
    private Uri imageUri; // For full-size image
    
    public static Step3FrontCardFragment newInstance(RegistrationData data) {
        Step3FrontCardFragment fragment = new Step3FrontCardFragment();
        fragment.registrationData = data;
        return fragment;
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_step3_front_card, container, false);
        
        // Ensure registrationData is not null
        ensureRegistrationData();
        
        initializeViews(view);
        setupFaceDetector();
        setupListeners();
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
    
    private void setupFaceDetector() {
        // Configure face detector for portrait detection with better accuracy
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE) // Use ACCURATE instead of FAST
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                .setMinFaceSize(0.1f) // Lower minimum face size to detect smaller faces
                .enableTracking()
                .build();
        
        faceDetector = FaceDetection.getClient(options);
    }
    
    private void initializeViews(View view) {
        ivFrontCard = view.findViewById(R.id.iv_front_card);
        tvInstruction = view.findViewById(R.id.tv_instruction);
        btnCapture = view.findViewById(R.id.btn_capture);
        btnContinue = view.findViewById(R.id.btn_continue);
        btnSaveImage = view.findViewById(R.id.btn_save_image);
        btnAutoScan = view.findViewById(R.id.btn_auto_scan);
        progressBar = view.findViewById(R.id.progress_bar);
    }
    
    private void setupListeners() {
        btnCapture.setOnClickListener(v -> captureImage());
        btnContinue.setOnClickListener(v -> continueToNextStep());
        btnSaveImage.setOnClickListener(v -> savePortraitImage());
        if (btnAutoScan != null) {
            btnAutoScan.setOnClickListener(v -> startAutoScan());
        }
    }
    
    private void startAutoScan() {
        Intent intent = new Intent(getActivity(), CccdAutoScannerActivity.class);
        startActivityForResult(intent, REQUEST_AUTO_SCAN);
    }
    
    private void loadData() {
        ensureRegistrationData();
        
        // Show front card image if exists (not portrait)
        if (registrationData != null && registrationData.getFrontCardImage() != null) {
            ivFrontCard.setImageBitmap(registrationData.getFrontCardImage());
            btnCapture.setText("Chụp lại");
            btnContinue.setVisibility(View.VISIBLE);
            btnSaveImage.setVisibility(View.GONE); // Hide save button
        } else {
            btnContinue.setVisibility(View.GONE);
            btnSaveImage.setVisibility(View.GONE);
        }
    }
    
    private void captureImage() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        
        // Create file to save full-size image
        try {
            File photoFile = createImageFile();
            if (photoFile != null) {
                imageUri = androidx.core.content.FileProvider.getUriForFile(
                    getActivity(),
                    getActivity().getPackageName() + ".fileprovider",
                    photoFile
                );
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_FULL);
            } else {
                // Fallback to thumbnail if file creation fails
                if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                } else {
                    Toast.makeText(getActivity(), "Không thể mở camera", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating image file", e);
            // Fallback to thumbnail
            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } else {
                Toast.makeText(getActivity(), "Không thể mở camera", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private File createImageFile() throws IOException {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String imageFileName = "CCCD_Front_" + timeStamp;
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
        return imageFile;
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == android.app.Activity.RESULT_OK) {
            // Handle auto scanner result
            if (requestCode == REQUEST_AUTO_SCAN && data != null) {
                String frontImagePath = data.getStringExtra("front_image_path");
                String portraitPath = data.getStringExtra("portrait_path");
                
                Log.d(TAG, "Received result - front_image_path: " + frontImagePath);
                Log.d(TAG, "Received result - portrait_path: " + portraitPath);
                
                // Load and set front card image (required for continueToNextStep)
                if (frontImagePath != null) {
                    Bitmap frontImage = BitmapFactory.decodeFile(frontImagePath);
                    if (frontImage != null) {
                        registrationData.setFrontCardImage(frontImage);
                        Log.d(TAG, "Front card image loaded and set. Size: " + frontImage.getWidth() + "x" + frontImage.getHeight());
                    } else {
                        Log.e(TAG, "Failed to decode front image from path: " + frontImagePath);
                    }
                } else {
                    Log.e(TAG, "front_image_path is null in result");
                }
                
                // Load and set portrait (for backend)
                if (portraitPath != null) {
                    Bitmap portrait = BitmapFactory.decodeFile(portraitPath);
                    if (portrait != null) {
                        // Save portrait
                        registrationData.setPortraitImage(portrait);
                        Log.d(TAG, "Portrait loaded and set. Size: " + portrait.getWidth() + "x" + portrait.getHeight());
                    } else {
                        Log.e(TAG, "Failed to decode portrait from path: " + portraitPath);
                    }
                }
                
                // Display portrait (or front image if portrait not available)
                Bitmap displayImage = registrationData.getPortraitImage();
                if (displayImage == null) {
                    displayImage = registrationData.getFrontCardImage();
                }
                
                if (displayImage != null) {
                    displayPortrait(displayImage, null);
                    Toast.makeText(getActivity(), "Đã quét CCCD và trích xuất ảnh chân dung thành công!", 
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), "Không thể tải ảnh đã chụp", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            
            Bitmap imageBitmap = null;
            
            if (requestCode == REQUEST_IMAGE_CAPTURE_FULL && imageUri != null) {
                // Load full-size image from file
                try {
                    imageBitmap = loadFullSizeImage(imageUri);
                } catch (Exception e) {
                    Log.e(TAG, "Error loading full-size image", e);
                }
            } else if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                // Fallback: use thumbnail
                Bundle extras = data.getExtras();
                imageBitmap = (Bitmap) extras.get("data");
            }
            
            if (imageBitmap != null) {
                // Scale up if too small
                imageBitmap = ensureMinimumSize(imageBitmap);
                
                // Save full image
                registrationData.setFrontCardImage(imageBitmap);
                
                // Show progress
                progressBar.setVisibility(View.VISIBLE);
                btnCapture.setEnabled(false);
                
                // Extract portrait from front card using ML Kit Face Detection
                extractPortrait(imageBitmap);
            } else {
                Toast.makeText(getActivity(), "Không thể tải ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private Bitmap loadFullSizeImage(Uri imageUri) throws IOException {
        android.content.ContentResolver resolver = getActivity().getContentResolver();
        android.graphics.BitmapFactory.Options options = new android.graphics.BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        
        android.os.ParcelFileDescriptor parcelFileDescriptor = resolver.openFileDescriptor(imageUri, "r");
        if (parcelFileDescriptor == null) {
            return null;
        }
        
        java.io.FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
        
        // Load at higher resolution for better quality
        // Use smaller inSampleSize to get larger image
        options.inSampleSize = calculateInSampleSize(options, TARGET_IMAGE_SIZE * 2, TARGET_IMAGE_SIZE * 2);
        options.inJustDecodeBounds = false;
        
        // Use RGB_565 for better memory efficiency while maintaining quality
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        
        Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
        parcelFileDescriptor.close();
        
        return bitmap;
    }
    
    private int calculateInSampleSize(android.graphics.BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        
        // Only downscale if image is significantly larger than required
        // This ensures we get the highest quality possible
        if (height > reqHeight * 2 || width > reqWidth * 2) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        
        return inSampleSize;
    }
    
    private Bitmap ensureMinimumSize(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        // Scale up to target size for better quality
        int targetSize = Math.max(width, height) < MIN_IMAGE_SIZE ? TARGET_IMAGE_SIZE : 
                        Math.max(width, height) < TARGET_IMAGE_SIZE ? TARGET_IMAGE_SIZE : 
                        Math.max(width, height);
        
        if (width < targetSize || height < targetSize) {
            // Scale up to target size with high quality
            float scale = Math.max((float) targetSize / width, (float) targetSize / height);
            int newWidth = (int) (width * scale);
            int newHeight = (int) (height * scale);
            
            // Use high-quality scaling
            return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        }
        
        return bitmap;
    }
    
    private void extractPortrait(Bitmap fullImage) {
        if (fullImage == null) {
            Log.e(TAG, "Full image is null, cannot extract portrait");
            progressBar.setVisibility(View.GONE);
            btnCapture.setEnabled(true);
            Toast.makeText(getActivity(), "Lỗi: Không thể xử lý ảnh", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Log.d(TAG, "Starting face detection. Image size: " + fullImage.getWidth() + "x" + fullImage.getHeight());
        
        // Check if face detector is initialized
        if (faceDetector == null) {
            Log.w(TAG, "Face detector is null, reinitializing...");
            setupFaceDetector();
        }
        
        try {
            InputImage image = InputImage.fromBitmap(fullImage, 0);
            
            // Set timeout for face detection (5 seconds)
            android.os.Handler handler = new android.os.Handler();
            Runnable timeoutRunnable = () -> {
                Log.w(TAG, "Face detection timeout, using smart crop");
                Bitmap portrait = cropCccdPortraitLocation(fullImage);
                if (portrait != null) {
                    displayPortrait(portrait, fullImage);
                } else {
                    displayPortrait(ensureMinimumSize(fullImage), fullImage);
                }
            };
            handler.postDelayed(timeoutRunnable, 5000);
            
            faceDetector.process(image)
                    .addOnSuccessListener(faces -> {
                        handler.removeCallbacks(timeoutRunnable); // Cancel timeout
                        
                        Log.d(TAG, "Face detection completed. Found " + faces.size() + " face(s)");
                        
                        Bitmap portrait = null;
                        
                        if (faces == null || faces.isEmpty()) {
                            // No face detected, use smart crop for Vietnamese CCCD portrait location
                            Log.d(TAG, "No face detected, using smart crop for CCCD portrait location");
                            portrait = cropCccdPortraitLocation(fullImage);
                        } else {
                            // Find the largest face (usually the portrait on CCCD)
                            Face largestFace = null;
                            float largestArea = 0;
                            for (Face face : faces) {
                                if (face != null) {
                                    Rect bounds = face.getBoundingBox();
                                    if (bounds != null && bounds.width() > 0 && bounds.height() > 0) {
                                        float area = bounds.width() * bounds.height();
                                        Log.d(TAG, "Found face at: " + bounds.left + "," + bounds.top + 
                                              " size: " + bounds.width() + "x" + bounds.height() + 
                                              " area: " + area);
                                        if (area > largestArea) {
                                            largestArea = area;
                                            largestFace = face;
                                        }
                                    }
                                }
                            }
                            
                            if (largestFace != null && largestFace.getBoundingBox() != null) {
                                Rect bounds = largestFace.getBoundingBox();
                                if (bounds.width() > 0 && bounds.height() > 0) {
                                    Log.d(TAG, "Using largest face for portrait extraction. Area: " + largestArea);
                                    try {
                                        portrait = cropFacePortrait(fullImage, largestFace);
                                        if (portrait == null) {
                                            Log.w(TAG, "Face crop returned null, using smart crop");
                                            portrait = cropCccdPortraitLocation(fullImage);
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error cropping face portrait", e);
                                        portrait = cropCccdPortraitLocation(fullImage);
                                    }
                                } else {
                                    Log.w(TAG, "Invalid face bounds, using smart crop");
                                    portrait = cropCccdPortraitLocation(fullImage);
                                }
                            } else {
                                Log.d(TAG, "No valid face found, using smart crop");
                                portrait = cropCccdPortraitLocation(fullImage);
                            }
                        }
                        
                        // Always display portrait - ensure we have a valid bitmap
                        if (portrait != null && !portrait.isRecycled()) {
                            displayPortrait(portrait, fullImage);
                        } else {
                            Log.e(TAG, "Portrait is null or recycled, using fallback");
                            portrait = cropCccdPortraitLocation(fullImage);
                            if (portrait != null && !portrait.isRecycled()) {
                                displayPortrait(portrait, fullImage);
                            } else {
                                // Last resort: use full image
                                Bitmap fallback = ensureMinimumSize(fullImage);
                                if (fallback != null && !fallback.isRecycled()) {
                                    displayPortrait(fallback, fullImage);
                                } else {
                                    // Ultimate fallback: show error but still try to display something
                                    Log.e(TAG, "All fallbacks failed, cannot display portrait");
                                    progressBar.setVisibility(View.GONE);
                                    btnCapture.setEnabled(true);
                                    Toast.makeText(getActivity(), "Lỗi: Không thể trích xuất ảnh chân dung. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        handler.removeCallbacks(timeoutRunnable); // Cancel timeout
                        Log.e(TAG, "Face detection failed", e);
                        // Fallback: use smart crop for CCCD
                        Bitmap portrait = cropCccdPortraitLocation(fullImage);
                        if (portrait != null && !portrait.isRecycled()) {
                            displayPortrait(portrait, fullImage);
                        } else {
                            // Last resort: use full image scaled
                            Bitmap fallback = ensureMinimumSize(fullImage);
                            if (fallback != null && !fallback.isRecycled()) {
                                displayPortrait(fallback, fullImage);
                            } else {
                                progressBar.setVisibility(View.GONE);
                                btnCapture.setEnabled(true);
                                Toast.makeText(getActivity(), "Lỗi: Không thể trích xuất ảnh chân dung. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in extractPortrait", e);
            // Fallback: use smart crop
            Bitmap portrait = cropCccdPortraitLocation(fullImage);
            if (portrait != null && !portrait.isRecycled()) {
                displayPortrait(portrait, fullImage);
            } else {
                Bitmap fallback = ensureMinimumSize(fullImage);
                if (fallback != null && !fallback.isRecycled()) {
                    displayPortrait(fallback, fullImage);
                } else {
                    progressBar.setVisibility(View.GONE);
                    btnCapture.setEnabled(true);
                    Toast.makeText(getActivity(), "Lỗi: Không thể trích xuất ảnh chân dung. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    
    private Bitmap cropFacePortrait(Bitmap fullImage, Face face) {
        Rect bounds = face.getBoundingBox();
        
        if (bounds == null || bounds.width() <= 0 || bounds.height() <= 0) {
            Log.w(TAG, "Invalid face bounds, using smart crop");
            return cropCccdPortraitLocation(fullImage);
        }
        
        Log.d(TAG, "Face detected at: " + bounds.left + "," + bounds.top + " size: " + bounds.width() + "x" + bounds.height());
        
        // Calculate appropriate padding based on face size
        // Use moderate padding to include hair and shoulders
        int paddingX = Math.max(bounds.width() / 3, 20); // 33% padding, minimum 20px
        int paddingY = Math.max(bounds.height() / 2, 30); // 50% padding, minimum 30px
        
        // Don't make padding too large relative to image size
        int maxPaddingX = fullImage.getWidth() / 5; // Max 20% of image width
        int maxPaddingY = fullImage.getHeight() / 5; // Max 20% of image height
        paddingX = Math.min(paddingX, maxPaddingX);
        paddingY = Math.min(paddingY, maxPaddingY);
        
        int left = Math.max(0, bounds.left - paddingX);
        int top = Math.max(0, bounds.top - paddingY);
        int right = Math.min(fullImage.getWidth(), bounds.right + paddingX);
        int bottom = Math.min(fullImage.getHeight(), bounds.bottom + paddingY);
        
        // Calculate crop dimensions
        int cropWidth = right - left;
        int cropHeight = bottom - top;
        
        // Make it square (portrait style) - use the larger dimension
        int size = Math.max(cropWidth, cropHeight);
        
        // Ensure reasonable size - don't make it too large
        int maxSize = Math.min(TARGET_IMAGE_SIZE * 2, Math.min(fullImage.getWidth(), fullImage.getHeight()) / 2);
        if (size > maxSize) {
            size = maxSize;
        }
        
        // Center the crop around the face
        int centerX = (bounds.left + bounds.right) / 2;
        int centerY = (bounds.top + bounds.bottom) / 2;
        left = Math.max(0, centerX - size / 2);
        top = Math.max(0, centerY - size / 2);
        right = Math.min(fullImage.getWidth(), left + size);
        bottom = Math.min(fullImage.getHeight(), top + size);
        
        // Adjust if out of bounds
        if (right - left < size) {
            if (right >= fullImage.getWidth()) {
                left = Math.max(0, fullImage.getWidth() - size);
            } else {
                right = Math.min(fullImage.getWidth(), left + size);
            }
        }
        if (bottom - top < size) {
            if (bottom >= fullImage.getHeight()) {
                top = Math.max(0, fullImage.getHeight() - size);
            } else {
                bottom = Math.min(fullImage.getHeight(), top + size);
            }
        }
        
        try {
            int finalWidth = right - left;
            int finalHeight = bottom - top;
            
            Log.d(TAG, "Cropping face portrait: " + left + "," + top + " size: " + finalWidth + "x" + finalHeight);
            
            Bitmap cropped = Bitmap.createBitmap(fullImage, left, top, finalWidth, finalHeight);
            
            // Scale to reasonable size
            return scaleToReasonableSize(cropped);
        } catch (Exception e) {
            Log.e(TAG, "Error cropping face", e);
            return cropCccdPortraitLocation(fullImage);
        }
    }
    
    /**
     * Smart crop for Vietnamese CCCD portrait location
     * Portrait on Vietnamese CCCD is typically in the top-left area
     */
    private Bitmap cropCccdPortraitLocation(Bitmap fullImage) {
        if (fullImage == null) {
            Log.e(TAG, "Full image is null in cropCccdPortraitLocation");
            return null;
        }
        
        int width = fullImage.getWidth();
        int height = fullImage.getHeight();
        
        if (width <= 0 || height <= 0) {
            Log.e(TAG, "Invalid image dimensions: " + width + "x" + height);
            return null;
        }
        
        Log.d(TAG, "Cropping CCCD portrait. Image size: " + width + "x" + height);
        
        // Vietnamese CCCD portrait location - try multiple strategies:
        // Strategy 1: Top-left area (most common)
        // Strategy 2: Center-left area (alternative)
        // Strategy 3: Center area (fallback)
        
        Bitmap portrait = tryCropTopLeft(fullImage, width, height);
        if (portrait != null) return portrait;
        
        portrait = tryCropCenterLeft(fullImage, width, height);
        if (portrait != null) return portrait;
        
        portrait = tryCropCenter(fullImage, width, height);
        if (portrait != null) return portrait;
        
        // Last resort: use full image scaled
        Log.w(TAG, "All crop strategies failed, using full image");
        return ensureMinimumSize(fullImage);
    }
    
    private Bitmap tryCropTopLeft(Bitmap fullImage, int width, int height) {
        try {
            // Vietnamese CCCD portrait location analysis:
            // - Portrait is typically in the top-left corner
            // - Takes about 20-30% of width from left edge (not 35% which is too large)
            // - Takes about 30-40% of height from top edge (not 45% which is too large)
            // - Portrait is usually square or slightly rectangular
            // - Starts very close to the left edge (1-3% margin)
            // - Starts close to top edge (3-8% margin)
            // - The portrait area is typically smaller than we were cropping
            
            // Try multiple variations with more accurate positioning for Vietnamese CCCD
            int[][] variations = {
                // Variation 1: Standard CCCD portrait (most accurate for new CCCD)
                {(int)(width * 0.02f), (int)(height * 0.05f), (int)(width * 0.25f), (int)(height * 0.35f)},
                // Variation 2: Slightly larger area
                {(int)(width * 0.01f), (int)(height * 0.03f), (int)(width * 0.28f), (int)(height * 0.38f)},
                // Variation 3: From corner (minimal margin)
                {(int)(width * 0.015f), (int)(height * 0.04f), (int)(width * 0.26f), (int)(height * 0.36f)},
                // Variation 4: Slightly more to the right
                {(int)(width * 0.03f), (int)(height * 0.06f), (int)(width * 0.24f), (int)(height * 0.34f)},
                // Variation 5: Smaller, more precise
                {(int)(width * 0.025f), (int)(height * 0.05f), (int)(width * 0.22f), (int)(height * 0.32f)},
                // Variation 6: For older CCCD format (slightly different position)
                {(int)(width * 0.04f), (int)(height * 0.08f), (int)(width * 0.30f), (int)(height * 0.40f)}
            };
            
            for (int i = 0; i < variations.length; i++) {
                int[] vars = variations[i];
                int left = vars[0];
                int top = vars[1];
                int portraitWidth = vars[2];
                int portraitHeight = vars[3];
                
                // Make it square (portrait is usually square)
                int size = Math.min(portraitWidth, portraitHeight);
                
                // Ensure valid bounds
                if (left < 0) left = 0;
                if (top < 0) top = 0;
                if (left + size > width) {
                    size = width - left;
                    if (size <= 0) continue;
                }
                if (top + size > height) {
                    size = Math.min(size, height - top);
                    if (size <= 0) continue;
                }
                if (size < 100) continue; // Too small, skip
                
                // Don't crop too large - limit to reasonable size
                int maxSize = Math.min(TARGET_IMAGE_SIZE * 2, Math.min(width, height) / 2);
                if (size > maxSize) {
                    size = maxSize;
                }
                
                try {
                    Log.d(TAG, "Trying top-left crop variation " + (i+1) + " at: " + left + "," + top + " size: " + size);
                    Bitmap cropped = Bitmap.createBitmap(fullImage, left, top, size, size);
                    if (cropped != null && !cropped.isRecycled()) {
                        // Scale to reasonable size, not too large
                        Bitmap scaled = scaleToReasonableSize(cropped);
                        if (scaled != null) {
                            Log.d(TAG, "Successfully cropped portrait with variation " + (i+1) + " at " + left + "," + top);
                            return scaled;
                        }
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Crop variation " + (i+1) + " failed, trying next", e);
                    continue;
                }
            }
            
            Log.w(TAG, "All top-left crop variations failed");
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error in tryCropTopLeft", e);
            return null;
        }
    }
    
    private Bitmap tryCropCenterLeft(Bitmap fullImage, int width, int height) {
        try {
            // Center-left region (alternative portrait location)
            // Try smaller area, more precise
            int portraitWidth = (int) (width * 0.28f);
            int portraitHeight = (int) (height * 0.38f);
            int left = (int) (width * 0.08f);
            int top = (int) (height * 0.15f);
            int size = Math.min(portraitWidth, portraitHeight);
            
            if (left < 0) left = 0;
            if (top < 0) top = 0;
            if (left + size > width) size = width - left;
            if (top + size > height) size = Math.min(size, height - top);
            if (size <= 0 || size < 100) return null;
            
            // Limit size to prevent overflow
            int maxSize = Math.min(TARGET_IMAGE_SIZE * 2, Math.min(width, height) / 2);
            if (size > maxSize) {
                size = maxSize;
            }
            
            Log.d(TAG, "Trying center-left crop at: " + left + "," + top + " size: " + size);
            Bitmap cropped = Bitmap.createBitmap(fullImage, left, top, size, size);
            if (cropped != null && !cropped.isRecycled()) {
                return scaleToReasonableSize(cropped);
            }
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error in tryCropCenterLeft", e);
            return null;
        }
    }
    
    private Bitmap tryCropCenter(Bitmap fullImage, int width, int height) {
        try {
            // Center region
            int size = Math.min(width, height) / 2;
            int left = (width - size) / 2;
            int top = (height - size) / 2;
            
            if (size <= 0) return null;
            
            // Limit size to prevent overflow
            if (size > TARGET_IMAGE_SIZE * 1.5f) {
                size = (int)(TARGET_IMAGE_SIZE * 1.5f);
                left = (width - size) / 2;
                top = (height - size) / 2;
            }
            
            Log.d(TAG, "Trying center crop at: " + left + "," + top + " size: " + size);
            Bitmap cropped = Bitmap.createBitmap(fullImage, left, top, size, size);
            return scaleToReasonableSize(cropped);
        } catch (Exception e) {
            Log.e(TAG, "Error in tryCropCenter", e);
            return null;
        }
    }
    
    private Bitmap scaleToTargetSize(Bitmap bitmap) {
        return scaleToReasonableSize(bitmap);
    }
    
    /**
     * Scale bitmap to reasonable size - not too large to prevent overflow
     */
    private Bitmap scaleToReasonableSize(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            Log.e(TAG, "Bitmap is null or recycled in scaleToReasonableSize");
            return null;
        }
        
        int currentWidth = bitmap.getWidth();
        int currentHeight = bitmap.getHeight();
        
        if (currentWidth <= 0 || currentHeight <= 0) {
            Log.e(TAG, "Invalid bitmap dimensions: " + currentWidth + "x" + currentHeight);
            return null;
        }
        
        // If already in reasonable range, return as is
        if (currentWidth >= MIN_IMAGE_SIZE && currentWidth <= TARGET_IMAGE_SIZE &&
            currentHeight >= MIN_IMAGE_SIZE && currentHeight <= TARGET_IMAGE_SIZE) {
            return bitmap;
        }
        
        // Determine target size based on current size
        int targetSize;
        if (currentWidth < MIN_IMAGE_SIZE || currentHeight < MIN_IMAGE_SIZE) {
            // Scale up to minimum size
            targetSize = MIN_IMAGE_SIZE;
        } else if (currentWidth > TARGET_IMAGE_SIZE || currentHeight > TARGET_IMAGE_SIZE) {
            // Scale down to target size if too large
            targetSize = TARGET_IMAGE_SIZE;
        } else {
            // Already in range, return as is
            return bitmap;
        }
        
        try {
            Log.d(TAG, "Scaling from " + currentWidth + "x" + currentHeight + " to " + targetSize + "x" + targetSize);
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, targetSize, targetSize, true);
            if (scaled != null && !scaled.isRecycled()) {
                return scaled;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error scaling bitmap", e);
        }
        
        return bitmap; // Return original if scaling fails
    }
    
    
    private void displayPortrait(Bitmap portrait, Bitmap fullImage) {
        if (portrait == null) {
            Log.e(TAG, "Portrait is null, cannot save");
            progressBar.setVisibility(View.GONE);
            btnCapture.setEnabled(true);
            Toast.makeText(getActivity(), "Lỗi: Không thể trích xuất ảnh chân dung", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // Save portrait image silently (for face verification later)
            registrationData.setPortraitImage(portrait);
            
            Log.d(TAG, "Portrait saved silently. Size: " + portrait.getWidth() + "x" + portrait.getHeight());
            
            // Display front card image (not portrait) - user doesn't need to see extracted portrait
            if (fullImage != null) {
                ivFrontCard.setImageBitmap(fullImage);
            } else {
                ivFrontCard.setImageBitmap(registrationData.getFrontCardImage());
            }
            ivFrontCard.setScaleType(ImageView.ScaleType.FIT_CENTER);
            
            // Force layout update to ensure proper display
            ivFrontCard.post(() -> {
                ivFrontCard.invalidate();
                ivFrontCard.requestLayout();
            });
            
            btnCapture.setText("Chụp lại");
            btnContinue.setVisibility(View.VISIBLE);
            btnSaveImage.setVisibility(View.GONE); // Hide save button - portrait is saved automatically
            progressBar.setVisibility(View.GONE);
            btnCapture.setEnabled(true);
            
            Toast.makeText(getActivity(), "Đã chụp ảnh mặt trước CCCD thành công!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error saving portrait", e);
            progressBar.setVisibility(View.GONE);
            btnCapture.setEnabled(true);
            Toast.makeText(getActivity(), "Lỗi xử lý ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void savePortraitImage() {
        Bitmap portrait = registrationData.getPortraitImage();
        if (portrait == null) {
            Toast.makeText(getActivity(), "Chưa có ảnh chân dung để lưu", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            String savedImagePath = saveImageToGallery(portrait);
            if (savedImagePath != null) {
                Toast.makeText(getActivity(), "Đã lưu ảnh vào: " + savedImagePath, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), "Lỗi khi lưu ảnh", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving image", e);
            Toast.makeText(getActivity(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private String saveImageToGallery(Bitmap bitmap) {
        String savedImagePath = null;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ (API 29+): Use MediaStore
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, "CCCD_Portrait_" + System.currentTimeMillis() + ".jpg");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/CCCD");
            values.put(MediaStore.Images.Media.IS_PENDING, 1);
            
            Uri uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            
            if (uri != null) {
                try {
                    OutputStream outputStream = getActivity().getContentResolver().openOutputStream(uri);
                    if (outputStream != null) {
                        // Use highest quality
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        outputStream.flush();
                        outputStream.close();
                        
                        values.clear();
                        values.put(MediaStore.Images.Media.IS_PENDING, 0);
                        getActivity().getContentResolver().update(uri, values, null, null);
                        
                        savedImagePath = "Pictures/CCCD/" + values.get(MediaStore.Images.Media.DISPLAY_NAME);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error saving image to MediaStore", e);
                }
            }
        } else {
            // Android 9 and below: Use File API
            String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
            File cccdDir = new File(imagesDir, "CCCD");
            if (!cccdDir.exists()) {
                cccdDir.mkdirs();
            }
            
            String fileName = "CCCD_Portrait_" + System.currentTimeMillis() + ".jpg";
            File imageFile = new File(cccdDir, fileName);
            
            try {
                FileOutputStream fos = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
                
                // Notify media scanner
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(imageFile);
                mediaScanIntent.setData(contentUri);
                getActivity().sendBroadcast(mediaScanIntent);
                
                savedImagePath = imageFile.getAbsolutePath();
            } catch (IOException e) {
                Log.e(TAG, "Error saving image to file", e);
            }
        }
        
        return savedImagePath;
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (faceDetector != null) {
            faceDetector.close();
        }
    }
    
    private void continueToNextStep() {
        Log.d(TAG, "continueToNextStep() called");
        Log.d(TAG, "Front card image: " + (registrationData.getFrontCardImage() != null ? "exists" : "null"));
        Log.d(TAG, "Portrait image: " + (registrationData.getPortraitImage() != null ? "exists" : "null"));
        
        if (registrationData.getFrontCardImage() == null) {
            Log.e(TAG, "Front card image is null, cannot continue");
            Toast.makeText(getActivity(), "Vui lòng chụp ảnh mặt trước CCCD", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Log.d(TAG, "Calling goToNextStep() on MainRegistrationActivity");
        // Navigate to next step
        if (getActivity() instanceof MainRegistrationActivity) {
            ((MainRegistrationActivity) getActivity()).goToNextStep();
        } else {
            Log.e(TAG, "Activity is not instance of MainRegistrationActivity: " + (getActivity() != null ? getActivity().getClass().getName() : "null"));
        }
    }
}

