package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebanking.R;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.UserApiService;
import com.example.mobilebanking.api.dto.SmartFlagsRequest;
import com.example.mobilebanking.api.dto.UserResponse;
import com.example.mobilebanking.utils.BiometricAuthManager;
import com.example.mobilebanking.utils.DataManager;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Settings Activity - Combined Profile & Settings screen
 * Design according to BIDV style with collapsible sections
 */
public class SettingsActivity extends BaseActivity {
    private DataManager dataManager;
    private BiometricAuthManager biometricManager;
    private UserApiService userApiService;
    
    // Header views
    private TextView tvGreeting;
    private TextView tvUserName;
    
    // Section expand/collapse
    private LinearLayout headerPersonal, contentPersonal;
    private LinearLayout headerSecurity, contentSecurity;
    private ImageView ivExpandPersonal, ivExpandSecurity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_combined);

        dataManager = DataManager.getInstance(this);
        biometricManager = new BiometricAuthManager(this);
        userApiService = ApiClient.getUserApiService();

        initViews();
        setupUserInfo();
        setupSectionCollapse();
        setupItemClickListeners();
        setupBottomNavigation();
        setupLogout();
    }
    
    private void initViews() {
        tvGreeting = findViewById(R.id.tv_greeting);
        tvUserName = findViewById(R.id.tv_user_name);
        
        // Section headers
        headerPersonal = findViewById(R.id.header_personal);
        headerSecurity = findViewById(R.id.header_security);
        
        // Section contents
        contentPersonal = findViewById(R.id.content_personal);
        contentSecurity = findViewById(R.id.content_security);
        
        // Expand icons
        ivExpandPersonal = findViewById(R.id.iv_expand_personal);
        ivExpandSecurity = findViewById(R.id.iv_expand_security);
    }
    
    private void setupUserInfo() {
        // Greeting based on time of day
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        
        String greeting;
        if (hour >= 5 && hour < 12) {
            greeting = "Chào buổi sáng!";
        } else if (hour >= 12 && hour < 18) {
            greeting = "Chào buổi chiều!";
        } else {
            greeting = "Chào buổi tối!";
        }
        
        if (tvGreeting != null) {
            tvGreeting.setText(greeting);
        }
        
        // User name from DataManager
        String fullName = dataManager.getUserFullName();
        if (fullName == null || fullName.isEmpty()) {
            fullName = dataManager.getLastFullName();
        }
        if (fullName == null || fullName.isEmpty()) {
            fullName = dataManager.getLoggedInUser();
        }
        if (fullName == null || fullName.isEmpty()) {
            fullName = "KHÁCH HÀNG";
        }
        
        if (tvUserName != null) {
            tvUserName.setText(fullName.toUpperCase());
        }
    }
    
    private void setupItemClickListeners() {
        // Item: Cài đặt sinh trắc học -> Toggle biometric
        View itemBiometric = findViewById(R.id.item_biometric);
        if (itemBiometric != null) {
            itemBiometric.setOnClickListener(v -> toggleBiometric());
        }
        
        // Item: Cài đặt vân tay -> Toggle biometric
        View itemFingerprint = findViewById(R.id.item_fingerprint);
        if (itemFingerprint != null) {
            itemFingerprint.setOnClickListener(v -> toggleBiometric());
        }
        
        // Các item khác: Toast "đang phát triển"
        int[] developingItems = {
            R.id.item_avatar, R.id.item_theme, R.id.item_wallpaper,
            R.id.item_language, R.id.item_watch,
            R.id.item_otp, R.id.item_login, R.id.item_password
        };
        
        for (int itemId : developingItems) {
            View item = findViewById(itemId);
            if (item != null) {
                item.setOnClickListener(v -> {
                    Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
                });
            }
        }
    }
    
    private void toggleBiometric() {
        if (!biometricManager.isBiometricAvailable()) {
            Toast.makeText(this, "Thiết bị không hỗ trợ xác thực sinh trắc học", Toast.LENGTH_SHORT).show();
            return;
        }
        
        boolean isEnabled = biometricManager.isBiometricEnabled();
        if (isEnabled) {
            new AlertDialog.Builder(this)
                .setTitle("Tắt xác thực sinh trắc học")
                .setMessage("Bạn có chắc chắn muốn tắt chức năng này?")
                .setPositiveButton("Tắt", (dialog, which) -> {
                    disableFingerprintOnBackend();
                })
                .setNegativeButton("Hủy", null)
                .show();
        } else {
            biometricManager.enableBiometric(this, new BiometricAuthManager.BiometricAuthCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        enableFingerprintOnBackend();
                    });
                }
                
                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(SettingsActivity.this, "Không thể bật chức năng: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            });
        }
    }
    
    private void enableFingerprintOnBackend() {
        Long userId = dataManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            biometricManager.disableBiometric();
            return;
        }
        
        SmartFlagsRequest request = new SmartFlagsRequest();
        request.setFingerprintLoginEnabled(true);
        
        userApiService.updateSmartFlags(userId, request).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    runOnUiThread(() -> {
                        Toast.makeText(SettingsActivity.this, "Đã bật xác thực sinh trắc học", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(SettingsActivity.this, "Không thể cập nhật cài đặt trên server", Toast.LENGTH_LONG).show();
                        biometricManager.disableBiometric();
                    });
                }
            }
            
            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                runOnUiThread(() -> {
                    Toast.makeText(SettingsActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    biometricManager.disableBiometric();
                });
            }
        });
    }
    
    private void disableFingerprintOnBackend() {
        Long userId = dataManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }
        
        SmartFlagsRequest request = new SmartFlagsRequest();
        request.setFingerprintLoginEnabled(false);
        
        userApiService.updateSmartFlags(userId, request).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    runOnUiThread(() -> {
                        biometricManager.disableBiometric();
                        Toast.makeText(SettingsActivity.this, "Đã tắt xác thực sinh trắc học", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(SettingsActivity.this, "Không thể cập nhật cài đặt trên server", Toast.LENGTH_LONG).show();
                    });
                }
            }
            
            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                runOnUiThread(() -> {
                    Toast.makeText(SettingsActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    private void setupSectionCollapse() {
        // Personal section
        if (headerPersonal != null) {
            headerPersonal.setOnClickListener(v -> {
                toggleSection(contentPersonal, ivExpandPersonal);
            });
        }
        
        // Security section
        if (headerSecurity != null) {
            headerSecurity.setOnClickListener(v -> {
                toggleSection(contentSecurity, ivExpandSecurity);
            });
        }
    }
    
    private void toggleSection(LinearLayout content, ImageView expandIcon) {
        if (content == null) return;
        
        if (content.getVisibility() == View.VISIBLE) {
            content.setVisibility(View.GONE);
            if (expandIcon != null) expandIcon.setRotation(0);
        } else {
            content.setVisibility(View.VISIBLE);
            if (expandIcon != null) expandIcon.setRotation(180);
        }
    }
    
    private void setupBottomNavigation() {
        // Trang chủ - về UiHomeActivity (màn hình xanh lá)
        View navHome = findViewById(R.id.nav_home);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(this, com.example.mobilebanking.ui_home.UiHomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            });
        }
        
        // Quét QR
        View navQr = findViewById(R.id.nav_qr);
        if (navQr != null) {
            navQr.setOnClickListener(v -> {
                startActivity(new Intent(this, QrScannerActivity.class));
            });
        }
        
        // Thẻ
        View navCard = findViewById(R.id.nav_card);
        if (navCard != null) {
            navCard.setOnClickListener(v -> {
                Toast.makeText(this, "Tính năng Thẻ đang phát triển", Toast.LENGTH_SHORT).show();
            });
        }
        
        // Hồ sơ - đang ở đây rồi
    }
    
    private void setupLogout() {
        View btnLogout = findViewById(R.id.btn_logout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> showLogoutDialog());
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng Xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng Xuất", (dialog, which) -> performLogout())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void performLogout() {
        dataManager.logout();
        dataManager.clearTokens();
        Toast.makeText(this, "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
