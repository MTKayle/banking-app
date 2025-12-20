package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.mobilebanking.R;
import com.google.android.material.appbar.MaterialToolbar;

/**
 * OfficerUserDetailActivity - Chi tiết người dùng cho Officer
 * Features:
 * - Hiển thị đầy đủ thông tin user
 * - Button: "Cập nhật", "Khóa/Mở khóa"
 * - Show dialog xác nhận khi khóa/mở khóa
 * - Toast thành công khi cập nhật
 */
public class OfficerUserDetailActivity extends BaseActivity {
    
    private MaterialToolbar toolbar;
    private TextView tvFullName, tvRole, tvPhone, tvEmail, tvCccd, tvDob, tvUserId, tvLockStatus;
    private Button btnToggleLock, btnUpdate;
    
    private Long userId;
    private String fullName, role, phone, email, cccd, dob;
    private boolean isLocked;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officer_user_detail);
        
        initViews();
        setupToolbar();
        loadData();
        setupButtons();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvFullName = findViewById(R.id.tv_full_name);
        tvRole = findViewById(R.id.tv_role);
        tvPhone = findViewById(R.id.tv_phone);
        tvEmail = findViewById(R.id.tv_email);
        tvCccd = findViewById(R.id.tv_cccd);
        tvDob = findViewById(R.id.tv_dob);
        tvUserId = findViewById(R.id.tv_user_id);
        tvLockStatus = findViewById(R.id.tv_lock_status);
        btnToggleLock = findViewById(R.id.btn_toggle_lock);
        btnUpdate = findViewById(R.id.btn_update);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    private void loadData() {
        // Lấy data từ Intent
        userId = getIntent().getLongExtra("user_id", 0L);
        fullName = getIntent().getStringExtra("user_name");
        phone = getIntent().getStringExtra("user_phone");
        email = getIntent().getStringExtra("user_email");
        role = getIntent().getStringExtra("user_role");
        isLocked = getIntent().getBooleanExtra("user_locked", false);
        cccd = getIntent().getStringExtra("user_cccd");
        dob = getIntent().getStringExtra("user_dob");
        
        // Display data
        tvFullName.setText(fullName);
        tvPhone.setText(phone);
        tvEmail.setText(email);
        tvCccd.setText(cccd != null && !cccd.isEmpty() ? cccd : "Chưa cập nhật");
        tvDob.setText(dob != null && !dob.isEmpty() ? dob : "Chưa cập nhật");
        tvUserId.setText("#" + userId);
        
        // Role badge
        if ("officer".equalsIgnoreCase(role)) {
            tvRole.setText("NHÂN VIÊN");
            tvRole.setBackgroundResource(R.drawable.bg_rounded_orange);
        } else {
            tvRole.setText("KHÁCH HÀNG");
            tvRole.setBackgroundResource(R.drawable.bg_rounded_primary);
        }
        
        // Lock status
        updateLockStatus();
    }
    
    private void updateLockStatus() {
        if (isLocked) {
            tvLockStatus.setText("● Đã khóa");
            tvLockStatus.setTextColor(getResources().getColor(R.color.red, null));
            btnToggleLock.setText("Mở khóa tài khoản");
            btnToggleLock.setBackgroundResource(R.drawable.bg_rounded_primary);
        } else {
            tvLockStatus.setText("● Đang hoạt động");
            tvLockStatus.setTextColor(getResources().getColor(R.color.green, null));
            btnToggleLock.setText("Khóa tài khoản");
            btnToggleLock.setBackgroundResource(R.drawable.bg_rounded_orange);
        }
    }
    
    private void setupButtons() {
        // Toggle Lock button
        btnToggleLock.setOnClickListener(v -> {
            if (isLocked) {
                showConfirmDialog(
                    "Mở khóa tài khoản",
                    "Bạn có chắc chắn muốn mở khóa tài khoản của " + fullName + "?",
                    "Mở khóa",
                    () -> {
                        isLocked = false;
                        updateLockStatus();
                        Toast.makeText(this, "Đã mở khóa tài khoản thành công!", Toast.LENGTH_SHORT).show();
                    }
                );
            } else {
                showConfirmDialog(
                    "Khóa tài khoản",
                    "Bạn có chắc chắn muốn khóa tài khoản của " + fullName + "?\n\nNgười dùng sẽ không thể đăng nhập sau khi bị khóa.",
                    "Khóa",
                    () -> {
                        isLocked = true;
                        updateLockStatus();
                        Toast.makeText(this, "Đã khóa tài khoản thành công!", Toast.LENGTH_SHORT).show();
                    }
                );
            }
        });
        
        // Update button - Mở màn hình cập nhật thông tin
        btnUpdate.setOnClickListener(v -> {
            Intent intent = new Intent(OfficerUserDetailActivity.this, OfficerUserUpdateActivity.class);
            intent.putExtra("user_id", userId);
            intent.putExtra("user_name", fullName);
            intent.putExtra("user_phone", phone);
            intent.putExtra("user_email", email);
            intent.putExtra("user_cccd", cccd);
            intent.putExtra("user_dob", dob);
            startActivityForResult(intent, 100); // Request code 100 để refresh khi quay lại
        });
    }
    
    private void showConfirmDialog(String title, String message, String positiveText, Runnable onConfirm) {
        new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveText, (dialog, which) -> {
                if (onConfirm != null) {
                    onConfirm.run();
                }
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Refresh data nếu cần (hiện tại mock data nên không cần)
            Toast.makeText(this, "Thông tin đã được cập nhật", Toast.LENGTH_SHORT).show();
            // TODO: Reload user data from API if needed
        }
    }
}

