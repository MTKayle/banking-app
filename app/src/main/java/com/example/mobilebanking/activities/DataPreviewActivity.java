package com.example.mobilebanking.activities;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.mobilebanking.R;
import com.example.mobilebanking.models.RegistrationData;
import com.example.mobilebanking.utils.ImageEncryptionUtil;

import java.io.File;

/**
 * Activity to display all captured registration data for testing and verification
 */
public class DataPreviewActivity extends AppCompatActivity {
    private static final String TAG = "DataPreview";
    
    private static final int PERMISSION_REQUEST_CODE = 200;
    
    private ScrollView scrollView;
    private ImageView ivFrontCard, ivBackCard, ivPortrait, ivSelfie;
    private TextView tvDataInfo, tvBase64Info;
    private Button btnClose, btnCopyBase64, btnSaveImage;
    
    private RegistrationData registrationData;
    private String selfieImagePath;
    private String frontCardImagePath;
    private String backCardImagePath;
    
    private Handler mainHandler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_preview);
        
        mainHandler = new Handler(Looper.getMainLooper());
        
        // Get data from intent
        if (getIntent() != null) {
            selfieImagePath = getIntent().getStringExtra("selfie_image_path");
            frontCardImagePath = getIntent().getStringExtra("front_card_image_path");
            backCardImagePath = getIntent().getStringExtra("back_card_image_path");
            
            // Get registration data from MainRegistrationActivity
            if (getApplicationContext() instanceof android.app.Application) {
                // Try to get from activity
                if (getParent() instanceof MainRegistrationActivity) {
                    registrationData = ((MainRegistrationActivity) getParent()).getRegistrationData();
                }
            }
        }
        
        initializeViews();
        loadAndDisplayData();
    }
    
    private void initializeViews() {
        scrollView = findViewById(R.id.scroll_view);
        ivFrontCard = findViewById(R.id.iv_front_card);
        ivBackCard = findViewById(R.id.iv_back_card);
        ivPortrait = findViewById(R.id.iv_portrait);
        ivSelfie = findViewById(R.id.iv_selfie);
        tvDataInfo = findViewById(R.id.tv_data_info);
        tvBase64Info = findViewById(R.id.tv_base64_info);
        btnClose = findViewById(R.id.btn_close);
        btnCopyBase64 = findViewById(R.id.btn_copy_base64);
        btnSaveImage = findViewById(R.id.btn_save_image);
        
        btnClose.setOnClickListener(v -> finish());
        btnCopyBase64.setOnClickListener(v -> copyBase64ToClipboard());
        btnSaveImage.setOnClickListener(v -> showSaveImageDialog());
    }
    
    private void loadAndDisplayData() {
        StringBuilder dataInfo = new StringBuilder();
        dataInfo.append("=== THÔNG TIN ĐĂNG KÝ ===\n\n");
        
        // Load and display images
        if (registrationData != null) {
            // Front card image
            if (registrationData.getFrontCardImage() != null) {
                ivFrontCard.setImageBitmap(registrationData.getFrontCardImage());
                dataInfo.append("✓ Ảnh mặt trước CCCD: ").append(registrationData.getFrontCardImage().getWidth())
                        .append("x").append(registrationData.getFrontCardImage().getHeight()).append(" pixels\n");
            } else if (frontCardImagePath != null) {
                Bitmap frontBitmap = loadImageFromPath(frontCardImagePath);
                if (frontBitmap != null) {
                    ivFrontCard.setImageBitmap(frontBitmap);
                    dataInfo.append("✓ Ảnh mặt trước CCCD: ").append(frontBitmap.getWidth())
                            .append("x").append(frontBitmap.getHeight()).append(" pixels\n");
                    dataInfo.append("  Path: ").append(frontCardImagePath).append("\n");
                }
            }
            
            // Back card image
            if (registrationData.getBackCardImage() != null) {
                ivBackCard.setImageBitmap(registrationData.getBackCardImage());
                dataInfo.append("✓ Ảnh mặt sau CCCD: ").append(registrationData.getBackCardImage().getWidth())
                        .append("x").append(registrationData.getBackCardImage().getHeight()).append(" pixels\n");
            } else if (backCardImagePath != null) {
                Bitmap backBitmap = loadImageFromPath(backCardImagePath);
                if (backBitmap != null) {
                    ivBackCard.setImageBitmap(backBitmap);
                    dataInfo.append("✓ Ảnh mặt sau CCCD: ").append(backBitmap.getWidth())
                            .append("x").append(backBitmap.getHeight()).append(" pixels\n");
                    dataInfo.append("  Path: ").append(backCardImagePath).append("\n");
                }
            }
            
            // Portrait from front card
            if (registrationData.getPortraitImage() != null) {
                ivPortrait.setImageBitmap(registrationData.getPortraitImage());
                dataInfo.append("✓ Ảnh chân dung từ CCCD: ").append(registrationData.getPortraitImage().getWidth())
                        .append("x").append(registrationData.getPortraitImage().getHeight()).append(" pixels\n");
            }
            
            // Text data
            dataInfo.append("\n=== THÔNG TIN VĂN BẢN ===\n\n");
            if (registrationData.getFullName() != null) {
                dataInfo.append("Họ và tên: ").append(registrationData.getFullName()).append("\n");
            }
            if (registrationData.getIdNumber() != null) {
                dataInfo.append("Số CCCD: ").append(registrationData.getIdNumber()).append("\n");
            }
            if (registrationData.getDateOfBirth() != null) {
                dataInfo.append("Ngày sinh: ").append(registrationData.getDateOfBirth()).append("\n");
            }
            if (registrationData.getGender() != null) {
                dataInfo.append("Giới tính: ").append(registrationData.getGender()).append("\n");
            }
            if (registrationData.getPermanentAddress() != null) {
                dataInfo.append("Địa chỉ thường trú: ").append(registrationData.getPermanentAddress()).append("\n");
            }
            if (registrationData.getIssueDate() != null) {
                dataInfo.append("Ngày cấp: ").append(registrationData.getIssueDate()).append("\n");
            }
            if (registrationData.getPhoneNumber() != null) {
                dataInfo.append("Số điện thoại: ").append(registrationData.getPhoneNumber()).append("\n");
            }
            if (registrationData.getEmail() != null) {
                dataInfo.append("Email: ").append(registrationData.getEmail()).append("\n");
            }
        }
        
        // Selfie image
        if (selfieImagePath != null) {
            Bitmap selfieBitmap = loadImageFromPath(selfieImagePath);
            if (selfieBitmap != null) {
                ivSelfie.setImageBitmap(selfieBitmap);
                dataInfo.append("\n✓ Ảnh selfie: ").append(selfieBitmap.getWidth())
                        .append("x").append(selfieBitmap.getHeight()).append(" pixels\n");
                dataInfo.append("  Path: ").append(selfieImagePath).append("\n");
            }
        }
        
        tvDataInfo.setText(dataInfo.toString());
        
        // Generate base64 strings
        generateBase64Strings();
    }
    
    private Bitmap loadImageFromPath(String path) {
        try {
            if (path == null || path.isEmpty()) {
                return null;
            }
            File file = new File(path);
            if (!file.exists()) {
                Log.e(TAG, "File does not exist: " + path);
                return null;
            }
            return BitmapFactory.decodeFile(path);
        } catch (Exception e) {
            Log.e(TAG, "Error loading image from path: " + path, e);
            return null;
        }
    }
    
    private void generateBase64Strings() {
        StringBuilder base64Info = new StringBuilder();
        base64Info.append("=== BASE64 ENCODED DATA ===\n\n");
        
        try {
            // Front card base64
            if (registrationData != null && registrationData.getFrontCardImage() != null) {
                String frontBase64 = bitmapToBase64(registrationData.getFrontCardImage());
                base64Info.append("Front Card Base64 (first 100 chars):\n");
                base64Info.append(frontBase64.substring(0, Math.min(100, frontBase64.length()))).append("...\n");
                base64Info.append("Length: ").append(frontBase64.length()).append(" chars\n\n");
            } else if (frontCardImagePath != null) {
                String frontBase64 = fileToBase64(frontCardImagePath);
                if (frontBase64 != null) {
                    base64Info.append("Front Card Base64 (first 100 chars):\n");
                    base64Info.append(frontBase64.substring(0, Math.min(100, frontBase64.length()))).append("...\n");
                    base64Info.append("Length: ").append(frontBase64.length()).append(" chars\n\n");
                }
            }
            
            // Back card base64
            if (registrationData != null && registrationData.getBackCardImage() != null) {
                String backBase64 = bitmapToBase64(registrationData.getBackCardImage());
                base64Info.append("Back Card Base64 (first 100 chars):\n");
                base64Info.append(backBase64.substring(0, Math.min(100, backBase64.length()))).append("...\n");
                base64Info.append("Length: ").append(backBase64.length()).append(" chars\n\n");
            } else if (backCardImagePath != null) {
                String backBase64 = fileToBase64(backCardImagePath);
                if (backBase64 != null) {
                    base64Info.append("Back Card Base64 (first 100 chars):\n");
                    base64Info.append(backBase64.substring(0, Math.min(100, backBase64.length()))).append("...\n");
                    base64Info.append("Length: ").append(backBase64.length()).append(" chars\n\n");
                }
            }
            
            // Portrait base64
            if (registrationData != null && registrationData.getPortraitImage() != null) {
                String portraitBase64 = bitmapToBase64(registrationData.getPortraitImage());
                base64Info.append("Portrait Base64 (first 100 chars):\n");
                base64Info.append(portraitBase64.substring(0, Math.min(100, portraitBase64.length()))).append("...\n");
                base64Info.append("Length: ").append(portraitBase64.length()).append(" chars\n\n");
            }
            
            // Selfie base64
            if (selfieImagePath != null) {
                String selfieBase64 = fileToBase64(selfieImagePath);
                if (selfieBase64 != null) {
                    base64Info.append("Selfie Base64 (first 100 chars):\n");
                    base64Info.append(selfieBase64.substring(0, Math.min(100, selfieBase64.length()))).append("...\n");
                    base64Info.append("Length: ").append(selfieBase64.length()).append(" chars\n\n");
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error generating base64 strings", e);
            base64Info.append("Error generating base64: ").append(e.getMessage());
        }
        
        tvBase64Info.setText(base64Info.toString());
    }
    
    private String bitmapToBase64(Bitmap bitmap) {
        if (bitmap == null) return null;
        try {
            android.graphics.Bitmap.CompressFormat format = android.graphics.Bitmap.CompressFormat.JPEG;
            int quality = 85;
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            bitmap.compress(format, quality, outputStream);
            byte[] imageBytes = outputStream.toByteArray();
            return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
        } catch (Exception e) {
            Log.e(TAG, "Error converting bitmap to base64", e);
            return null;
        }
    }
    
    private String fileToBase64(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return null;
            }
            java.io.FileInputStream fis = new java.io.FileInputStream(file);
            byte[] fileBytes = new byte[(int) file.length()];
            fis.read(fileBytes);
            fis.close();
            return Base64.encodeToString(fileBytes, Base64.NO_WRAP);
        } catch (Exception e) {
            Log.e(TAG, "Error converting file to base64: " + filePath, e);
            return null;
        }
    }
    
    private void copyBase64ToClipboard() {
        String fullBase64 = tvBase64Info.getText().toString();
        if (fullBase64 != null && !fullBase64.isEmpty()) {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) 
                    getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Base64 Data", fullBase64);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Đã sao chép Base64 vào clipboard", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showSaveImageDialog() {
        String[] options = {"Lưu ảnh Selfie", "Lưu ảnh Mặt Trước CCCD", "Lưu ảnh Mặt Sau CCCD", "Lưu ảnh Chân Dung"};
        
        new AlertDialog.Builder(this)
                .setTitle("Lưu ảnh")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            saveImageToGallery(getSelfieBitmap(), "selfie");
                            break;
                        case 1:
                            saveImageToGallery(getFrontCardBitmap(), "cccd_front");
                            break;
                        case 2:
                            saveImageToGallery(getBackCardBitmap(), "cccd_back");
                            break;
                        case 3:
                            saveImageToGallery(getPortraitBitmap(), "portrait");
                            break;
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    
    private Bitmap getSelfieBitmap() {
        if (selfieImagePath != null) {
            return loadImageFromPath(selfieImagePath);
        }
        if (registrationData != null && registrationData.getPortraitImage() != null) {
            return registrationData.getPortraitImage();
        }
        return null;
    }
    
    private Bitmap getFrontCardBitmap() {
        if (registrationData != null && registrationData.getFrontCardImage() != null) {
            return registrationData.getFrontCardImage();
        }
        if (frontCardImagePath != null) {
            return loadImageFromPath(frontCardImagePath);
        }
        return null;
    }
    
    private Bitmap getBackCardBitmap() {
        if (registrationData != null && registrationData.getBackCardImage() != null) {
            return registrationData.getBackCardImage();
        }
        if (backCardImagePath != null) {
            return loadImageFromPath(backCardImagePath);
        }
        return null;
    }
    
    private Bitmap getPortraitBitmap() {
        if (registrationData != null && registrationData.getPortraitImage() != null) {
            return registrationData.getPortraitImage();
        }
        return null;
    }
    
    private void saveImageToGallery(Bitmap bitmap, String imageType) {
        if (bitmap == null) {
            Toast.makeText(this, "Không có ảnh để lưu", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Android 10+ (API 29+) uses scoped storage with MediaStore - no permission needed
        // Only Android < 10 needs WRITE_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != 
                PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 
                    PERMISSION_REQUEST_CODE);
                return;
            }
        }
        // For Android 10+, MediaStore API doesn't require permission, proceed directly
        
        // Save image in background thread
        new Thread(() -> {
            try {
                String fileName = imageType + "_" + System.currentTimeMillis() + ".jpg";
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
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
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
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                    fos.flush();
                    fos.close();
                    
                    // Notify media scanner
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    imageUri = Uri.fromFile(imageFile);
                    mediaScanIntent.setData(imageUri);
                    sendBroadcast(mediaScanIntent);
                }
                
                final Uri finalImageUri = imageUri;
                final String finalFileName = fileName;
                
                mainHandler.post(() -> {
                    if (finalImageUri != null) {
                        Toast.makeText(this, "Đã lưu ảnh vào thư viện: " + finalFileName, Toast.LENGTH_LONG).show();
                        Log.d(TAG, "Image saved to gallery: " + finalImageUri.toString());
                    } else {
                        Toast.makeText(this, "Không thể lưu ảnh. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error saving image to gallery", e);
                mainHandler.post(() -> {
                    Toast.makeText(this, "Lỗi khi lưu ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, but user needs to select image again from dialog
                Toast.makeText(this, "Quyền đã được cấp. Vui lòng chọn ảnh để lưu lại.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Cần quyền để lưu ảnh. Vui lòng cấp quyền trong Cài đặt.", Toast.LENGTH_LONG).show();
            }
        }
    }
}

