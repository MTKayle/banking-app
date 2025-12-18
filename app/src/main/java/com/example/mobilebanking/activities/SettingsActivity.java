package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebanking.R;
import com.example.mobilebanking.utils.DataManager;

/**
 * Settings Activity - Professional dark mode settings screen
 * Banking app style with account, security, and system settings
 */
public class SettingsActivity extends AppCompatActivity {
    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_dark);

        dataManager = DataManager.getInstance(this);

        setupClickListeners();
        loadAppVersion();
    }

    private void setupClickListeners() {
        // Account Settings
        LinearLayout llChangePhone = findViewById(R.id.ll_change_phone);
        LinearLayout llChangeEmail = findViewById(R.id.ll_change_email);
        LinearLayout llLinkedDevices = findViewById(R.id.ll_linked_devices);

        if (llChangePhone != null) {
            llChangePhone.setOnClickListener(v -> {
                Toast.makeText(this, "Tính năng đổi số điện thoại đang phát triển", Toast.LENGTH_SHORT).show();
            });
        }

        if (llChangeEmail != null) {
            llChangeEmail.setOnClickListener(v -> {
                Toast.makeText(this, "Tính năng đổi email đang phát triển", Toast.LENGTH_SHORT).show();
            });
        }

        if (llLinkedDevices != null) {
            llLinkedDevices.setOnClickListener(v -> {
                Toast.makeText(this, "Tính năng quản lý thiết bị đang phát triển", Toast.LENGTH_SHORT).show();
            });
        }

        // Security Settings
        LinearLayout llChangePassword = findViewById(R.id.ll_change_password);
        LinearLayout llChangePin = findViewById(R.id.ll_change_pin);

        if (llChangePassword != null) {
            llChangePassword.setOnClickListener(v -> {
                Toast.makeText(this, "Tính năng đổi mật khẩu đang phát triển", Toast.LENGTH_SHORT).show();
            });
        }

        if (llChangePin != null) {
            llChangePin.setOnClickListener(v -> {
                Toast.makeText(this, "Tính năng đổi mã PIN đang phát triển", Toast.LENGTH_SHORT).show();
            });
        }

        // Display & Language
        LinearLayout llLanguage = findViewById(R.id.ll_language);
        LinearLayout llFontSize = findViewById(R.id.ll_font_size);

        if (llLanguage != null) {
            llLanguage.setOnClickListener(v -> {
                Toast.makeText(this, "Tính năng chọn ngôn ngữ đang phát triển", Toast.LENGTH_SHORT).show();
            });
        }

        if (llFontSize != null) {
            llFontSize.setOnClickListener(v -> {
                Toast.makeText(this, "Tính năng cỡ chữ đang phát triển", Toast.LENGTH_SHORT).show();
            });
        }

        // Privacy & Legal
        LinearLayout llPrivacyPolicy = findViewById(R.id.ll_privacy_policy);
        LinearLayout llTerms = findViewById(R.id.ll_terms);
        LinearLayout llPermissions = findViewById(R.id.ll_permissions);

        if (llPrivacyPolicy != null) {
            llPrivacyPolicy.setOnClickListener(v -> {
                Toast.makeText(this, "Chính sách bảo mật", Toast.LENGTH_SHORT).show();
            });
        }

        if (llTerms != null) {
            llTerms.setOnClickListener(v -> {
                Toast.makeText(this, "Điều khoản sử dụng", Toast.LENGTH_SHORT).show();
            });
        }

        if (llPermissions != null) {
            llPermissions.setOnClickListener(v -> {
                Toast.makeText(this, "Quyền ứng dụng", Toast.LENGTH_SHORT).show();
            });
        }

        // Support & System
        LinearLayout llHelp = findViewById(R.id.ll_help);
        LinearLayout llContact = findViewById(R.id.ll_contact);

        if (llHelp != null) {
            llHelp.setOnClickListener(v -> {
                Toast.makeText(this, "Trợ giúp / FAQ", Toast.LENGTH_SHORT).show();
            });
        }

        if (llContact != null) {
            llContact.setOnClickListener(v -> {
                Toast.makeText(this, "Liên hệ ngân hàng", Toast.LENGTH_SHORT).show();
            });
        }

        // Bottom Actions
        Button btnLogout = findViewById(R.id.btn_logout);
        Button btnLockAccount = findViewById(R.id.btn_lock_account);

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> showLogoutDialog());
        }

        if (btnLockAccount != null) {
            btnLockAccount.setOnClickListener(v -> {
                Toast.makeText(this, "Tính năng khóa tài khoản đang phát triển", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void loadAppVersion() {
        TextView tvAppVersion = findViewById(R.id.tv_app_version);
        if (tvAppVersion != null) {
            try {
                String versionName = getPackageManager()
                        .getPackageInfo(getPackageName(), 0).versionName;
                tvAppVersion.setText(versionName);
            } catch (Exception e) {
                tvAppVersion.setText("1.0.0");
            }
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

