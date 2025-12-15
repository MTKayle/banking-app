package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.example.mobilebanking.R;
import com.example.mobilebanking.models.User;
import com.example.mobilebanking.utils.BiometricAuthManager;
import com.example.mobilebanking.utils.DataManager;

/**
 * Profile Activity - User profile information
 */
public class ProfileActivity extends AppCompatActivity {
    private TextView tvFullName, tvUsername, tvEmail, tvPhone, tvIdNumber;
    private TextView tvBiometricDescription;
    private Button btnLogout;
    private SwitchCompat switchBiometric;
    private DataManager dataManager;
    private BiometricAuthManager biometricManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dataManager = DataManager.getInstance(this);
        biometricManager = new BiometricAuthManager(this);

        setupToolbar();
        initializeViews();
        loadUserData();
        setupBiometricSwitch();
        setupLogoutButton();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Profile");
        }
    }

    private void initializeViews() {
        tvFullName = findViewById(R.id.tv_full_name);
        tvUsername = findViewById(R.id.tv_username);
        tvEmail = findViewById(R.id.tv_email);
        tvPhone = findViewById(R.id.tv_phone);
        tvIdNumber = findViewById(R.id.tv_id_number);
        tvBiometricDescription = findViewById(R.id.tv_biometric_description);
        btnLogout = findViewById(R.id.btn_logout);
        switchBiometric = findViewById(R.id.switch_biometric);
    }

    private void loadUserData() {
        String username = dataManager.getLoggedInUser();
        
        // Get user data from mock users
        for (User user : dataManager.getMockUsers()) {
            if (user.getUsername().equals(username)) {
                tvFullName.setText(user.getFullName());
                tvUsername.setText(user.getUsername());
                tvEmail.setText(user.getEmail());
                tvPhone.setText(user.getPhoneNumber());
                tvIdNumber.setText(user.getIdNumber());
                break;
            }
        }
    }

    private void setupBiometricSwitch() {
        // Kiểm tra thiết bị có hỗ trợ vân tay không
        if (!biometricManager.isBiometricAvailable()) {
            switchBiometric.setEnabled(false);
            tvBiometricDescription.setText("Thiết bị không hỗ trợ vân tay");
            return;
        }
        
        // Hiển thị trạng thái hiện tại
        boolean isEnabled = biometricManager.isBiometricEnabled();
        switchBiometric.setChecked(isEnabled);
        
        // Lắng nghe sự kiện thay đổi
        switchBiometric.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Bật chức năng vân tay
                enableBiometric();
            } else {
                // Tắt chức năng vân tay
                disableBiometric();
            }
        });
    }
    
    private void enableBiometric() {
        biometricManager.enableBiometric(this, new BiometricAuthManager.BiometricAuthCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(ProfileActivity.this, "Đã bật đăng nhập bằng vân tay", Toast.LENGTH_SHORT).show();
                    // Nếu đã đăng nhập, lưu refresh token vào Keystore
                    String refreshToken = dataManager.getRefreshToken();
                    if (refreshToken != null) {
                        String username = dataManager.getLoggedInUser();
                        if (username != null) {
                            biometricManager.saveRefreshToken(ProfileActivity.this, refreshToken, username, 
                                new BiometricAuthManager.BiometricAuthCallback() {
                                    @Override
                                    public void onSuccess() {
                                        runOnUiThread(() -> {
                                            Toast.makeText(ProfileActivity.this, "Đã lưu thông tin đăng nhập", Toast.LENGTH_SHORT).show();
                                        });
                                    }
                                    
                                    @Override
                                    public void onError(String error) {
                                        runOnUiThread(() -> {
                                            Toast.makeText(ProfileActivity.this, "Không thể lưu thông tin: " + error, Toast.LENGTH_LONG).show();
                                        });
                                    }
                                });
                        }
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    switchBiometric.setChecked(false);
                    Toast.makeText(ProfileActivity.this, "Không thể bật chức năng: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    private void disableBiometric() {
        new AlertDialog.Builder(this)
            .setTitle("Tắt đăng nhập bằng vân tay")
            .setMessage("Bạn có chắc chắn muốn tắt chức năng đăng nhập bằng vân tay?")
            .setPositiveButton("Tắt", (dialog, which) -> {
                biometricManager.disableBiometric();
                Toast.makeText(this, "Đã tắt đăng nhập bằng vân tay", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Hủy", (dialog, which) -> {
                switchBiometric.setChecked(true);
            })
            .show();
    }
    
    private void setupLogoutButton() {
        btnLogout.setOnClickListener(v -> showLogoutDialog());
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            showLogoutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

