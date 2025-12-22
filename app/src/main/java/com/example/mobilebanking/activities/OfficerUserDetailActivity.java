package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.mobilebanking.R;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.UserApiService;
import com.example.mobilebanking.api.dto.LockAccountRequest;
import com.example.mobilebanking.api.dto.UserResponse;
import com.google.android.material.appbar.MaterialToolbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * OfficerUserDetailActivity - Chi tiết người dùng cho Officer
 * Features:
 * - Hiển thị đầy đủ thông tin user
 * - Button: "Cập nhật", "Khóa/Mở khóa"
 * - Show dialog xác nhận khi khóa/mở khóa
 * - Toast thành công khi cập nhật
 */
public class OfficerUserDetailActivity extends BaseActivity {
    
    private static final String TAG = "OfficerUserDetail";
    
    private MaterialToolbar toolbar;
    private TextView tvFullName, tvRole, tvPhone, tvEmail, tvCccd, tvDob, tvUserId, tvLockStatus;
    private Button btnToggleLock, btnUpdate;
    private ProgressBar progressBar;
    
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
        progressBar = findViewById(R.id.progress_bar);
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
                    () -> callLockAccountApi(false)
                );
            } else {
                showConfirmDialog(
                    "Khóa tài khoản",
                    "Bạn có chắc chắn muốn khóa tài khoản của " + fullName + "?\n\nNgười dùng sẽ không thể đăng nhập sau khi bị khóa.",
                    "Khóa",
                    () -> callLockAccountApi(true)
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
            startActivityForResult(intent, 100);
        });
    }
    
    /**
     * Gọi API khóa/mở khóa tài khoản
     * Endpoint: PATCH /api/users/{userId}/lock
     */
    private void callLockAccountApi(boolean lock) {
        showLoading(true);
        
        UserApiService userApiService = ApiClient.getUserApiService();
        LockAccountRequest request = new LockAccountRequest(lock);
        
        Call<UserResponse> call = userApiService.lockAccount(userId, request);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse userResponse = response.body();
                    isLocked = userResponse.getIsLocked() != null ? userResponse.getIsLocked() : lock;
                    updateLockStatus();
                    
                    String message = isLocked ? "Đã khóa tài khoản thành công!" : "Đã mở khóa tài khoản thành công!";
                    Toast.makeText(OfficerUserDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Lock account success: userId=" + userId + ", isLocked=" + isLocked);
                } else {
                    String errorMsg = "Lỗi: " + response.code();
                    if (response.code() == 403) {
                        errorMsg = "Bạn không có quyền thực hiện thao tác này";
                    } else if (response.code() == 404) {
                        errorMsg = "Không tìm thấy người dùng";
                    }
                    Toast.makeText(OfficerUserDetailActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Lock account failed: " + response.code() + " - " + response.message());
                }
            }
            
            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(OfficerUserDetailActivity.this, 
                    "Không thể kết nối server: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Lock account API call failed: " + t.getMessage(), t);
            }
        });
    }
    
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        btnToggleLock.setEnabled(!show);
        btnUpdate.setEnabled(!show);
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
            fetchUserData();
        }
    }
    
    /**
     * Gọi API lấy thông tin user mới nhất
     * Endpoint: GET /api/users/{userId}
     */
    private void fetchUserData() {
        showLoading(true);
        
        UserApiService userApiService = ApiClient.getUserApiService();
        
        Call<UserResponse> call = userApiService.getUserById(userId);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse user = response.body();
                    
                    // Cập nhật data local - UserResponse dùng getPhone()
                    fullName = user.getFullName();
                    phone = user.getPhone();
                    email = user.getEmail();
                    cccd = user.getCccdNumber();
                    dob = user.getDateOfBirth();
                    isLocked = user.getIsLocked() != null ? user.getIsLocked() : false;
                    role = user.getRole();
                    
                    updateUI();
                    
                    Toast.makeText(OfficerUserDetailActivity.this, 
                        "Thông tin đã được cập nhật", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Fetch user data success: userId=" + userId);
                } else {
                    Toast.makeText(OfficerUserDetailActivity.this, 
                        "Không thể tải thông tin mới: " + response.code(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Fetch user data failed: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(OfficerUserDetailActivity.this, 
                    "Không thể kết nối server: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Fetch user data API call failed: " + t.getMessage(), t);
            }
        });
    }
    
    private void updateUI() {
        tvFullName.setText(fullName);
        tvPhone.setText(phone);
        tvEmail.setText(email);
        tvCccd.setText(cccd != null && !cccd.isEmpty() ? cccd : "Chưa cập nhật");
        tvDob.setText(dob != null && !dob.isEmpty() ? dob : "Chưa cập nhật");
        tvUserId.setText("#" + userId);
        
        if ("officer".equalsIgnoreCase(role)) {
            tvRole.setText("NHÂN VIÊN");
            tvRole.setBackgroundResource(R.drawable.bg_rounded_orange);
        } else {
            tvRole.setText("KHÁCH HÀNG");
            tvRole.setBackgroundResource(R.drawable.bg_rounded_primary);
        }
        
        updateLockStatus();
    }
}
