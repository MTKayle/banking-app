package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.mobilebanking.R;
import com.example.mobilebanking.ui_home.OfficerHomeActivity;
import com.example.mobilebanking.utils.BiometricAuthManager;
import com.example.mobilebanking.utils.DataManager;

import java.util.Calendar;

/**
 * Officer Settings Activity - Trang Hồ sơ cho Officer
 * Bottom navigation giống trang chủ Officer: Trang chủ, Quản lý User, Quản lý Vay, Hồ sơ
 */
public class OfficerSettingsActivity extends BaseActivity {
    private DataManager dataManager;
    private BiometricAuthManager biometricManager;
    
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
        setContentView(R.layout.activity_officer_settings);

        dataManager = DataManager.getInstance(this);
        biometricManager = new BiometricAuthManager(this);

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
        
        headerPersonal = findViewById(R.id.header_personal);
        contentPersonal = findViewById(R.id.content_personal);
        ivExpandPersonal = findViewById(R.id.iv_expand_personal);
        
        headerSecurity = findViewById(R.id.header_security);
        contentSecurity = findViewById(R.id.content_security);
        ivExpandSecurity = findViewById(R.id.iv_expand_security);
    }
    
    private void setupUserInfo() {
        // Greeting based on time
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour < 12) {
            greeting = "Chào buổi sáng!";
        } else if (hour < 18) {
            greeting = "Chào buổi chiều!";
        } else {
            greeting = "Chào buổi tối!";
        }
        
        if (tvGreeting != null) {
            tvGreeting.setText(greeting);
        }
        
        // User name
        String fullName = dataManager.getUserFullName();
        if (fullName == null || fullName.isEmpty()) {
            fullName = "OFFICER";
        }
        
        if (tvUserName != null) {
            tvUserName.setText(fullName.toUpperCase());
        }
    }
    
    private void setupItemClickListeners() {
        // Đổi ảnh đại diện
        View itemAvatar = findViewById(R.id.item_change_avatar);
        if (itemAvatar != null) {
            itemAvatar.setOnClickListener(v -> {
                Toast.makeText(this, "Chức năng đổi ảnh đại diện đang phát triển", Toast.LENGTH_SHORT).show();
            });
        }
        
        // Đổi giao diện
        View itemTheme = findViewById(R.id.item_change_theme);
        if (itemTheme != null) {
            itemTheme.setOnClickListener(v -> {
                Toast.makeText(this, "Chức năng đổi giao diện đang phát triển", Toast.LENGTH_SHORT).show();
            });
        }
        
        // Ngôn ngữ
        View itemLanguage = findViewById(R.id.item_language);
        if (itemLanguage != null) {
            itemLanguage.setOnClickListener(v -> {
                Toast.makeText(this, "Chức năng đổi ngôn ngữ đang phát triển", Toast.LENGTH_SHORT).show();
            });
        }
        
        // Xác thực sinh trắc học
        View itemBiometric = findViewById(R.id.item_biometric);
        if (itemBiometric != null) {
            itemBiometric.setOnClickListener(v -> toggleBiometric());
        }
        
        // Đổi mật khẩu
        View itemPassword = findViewById(R.id.item_change_password);
        if (itemPassword != null) {
            itemPassword.setOnClickListener(v -> {
                Toast.makeText(this, "Chức năng đổi mật khẩu đang phát triển", Toast.LENGTH_SHORT).show();
            });
        }
    }
    
    private void toggleBiometric() {
        boolean isEnabled = biometricManager.isBiometricEnabled();
        if (isEnabled) {
            new AlertDialog.Builder(this)
                .setTitle("Tắt xác thực sinh trắc học")
                .setMessage("Bạn có chắc chắn muốn tắt chức năng này?")
                .setPositiveButton("Tắt", (dialog, which) -> {
                    biometricManager.disableBiometric();
                    Toast.makeText(this, "Đã tắt xác thực sinh trắc học", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
        } else {
            biometricManager.enableBiometric(this, new BiometricAuthManager.BiometricAuthCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        Toast.makeText(OfficerSettingsActivity.this, "Đã bật xác thực sinh trắc học", Toast.LENGTH_SHORT).show();
                    });
                }
                
                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(OfficerSettingsActivity.this, "Không thể bật chức năng: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            });
        }
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
        // Trang chủ - về OfficerHomeActivity
        View navHome = findViewById(R.id.officer_nav_home);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(this, OfficerHomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            });
        }
        
        // Quản lý User
        View navUser = findViewById(R.id.officer_nav_user);
        if (navUser != null) {
            navUser.setOnClickListener(v -> {
                startActivity(new Intent(this, OfficerUserListActivity.class));
            });
        }
        
        // Quản lý Vay
        View navMortgage = findViewById(R.id.officer_nav_mortgage);
        if (navMortgage != null) {
            navMortgage.setOnClickListener(v -> {
                startActivity(new Intent(this, OfficerMortgageListActivity.class));
            });
        }
        
        // Hồ sơ - đang ở đây rồi, không cần xử lý
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
        // Về trang chủ Officer
        Intent intent = new Intent(this, OfficerHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}

