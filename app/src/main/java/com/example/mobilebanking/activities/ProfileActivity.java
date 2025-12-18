package com.example.mobilebanking.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebanking.R;
import com.example.mobilebanking.models.Account;
import com.example.mobilebanking.models.User;
import com.example.mobilebanking.utils.BiometricAuthManager;
import com.example.mobilebanking.utils.DataManager;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Profile Activity - Professional dark mode user profile screen
 * Banking app style with eKYC verification, account overview, and security settings
 */
public class ProfileActivity extends AppCompatActivity {
    // Header views
    private TextView tvFullName, tvCustomerId, tvEkycStatus;
    private View viewEkycIndicator;
    private Button btnEditProfile;
    
    // Personal information views
    private TextView tvPhone, tvEmail, tvNationalId, tvAddress;
    
    // eKYC section
    private Button btnStartEkyc;
    
    // Account views
    private TextView tvCheckingAccount, tvCheckingBalance;
    private TextView tvSavingAccount, tvSavingBalance;
    private TextView tvMortgageAccount, tvMortgageBalance;
    private LinearLayout llCheckingAccount, llSavingAccount, llMortgageAccount;
    
    // Security views
    private LinearLayout llChangePassword, llTransactionPin, llBiometricAuth;
    
    // Bottom actions
    private Button btnTransactionHistory, btnLogout;
    
    private DataManager dataManager;
    private BiometricAuthManager biometricManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_dark);

        dataManager = DataManager.getInstance(this);
        biometricManager = new BiometricAuthManager(this);

        initializeViews();
        loadUserData();
        loadAccountData();
        setupClickListeners();
        setupEkycStatus();
    }

    private void initializeViews() {
        // Header
        tvFullName = findViewById(R.id.tv_full_name);
        tvCustomerId = findViewById(R.id.tv_customer_id);
        tvEkycStatus = findViewById(R.id.tv_ekyc_status);
        viewEkycIndicator = findViewById(R.id.view_ekyc_indicator);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        
        // Personal information
        tvPhone = findViewById(R.id.tv_phone);
        tvEmail = findViewById(R.id.tv_email);
        tvNationalId = findViewById(R.id.tv_national_id);
        tvAddress = findViewById(R.id.tv_address);
        
        // eKYC
        btnStartEkyc = findViewById(R.id.btn_start_ekyc);
        
        // Accounts
        tvCheckingAccount = findViewById(R.id.tv_checking_account);
        tvCheckingBalance = findViewById(R.id.tv_checking_balance);
        tvSavingAccount = findViewById(R.id.tv_saving_account);
        tvSavingBalance = findViewById(R.id.tv_saving_balance);
        tvMortgageAccount = findViewById(R.id.tv_mortgage_account);
        tvMortgageBalance = findViewById(R.id.tv_mortgage_balance);
        llCheckingAccount = findViewById(R.id.ll_checking_account);
        llSavingAccount = findViewById(R.id.ll_saving_account);
        llMortgageAccount = findViewById(R.id.ll_mortgage_account);
        
        // Security
        llChangePassword = findViewById(R.id.ll_change_password);
        llTransactionPin = findViewById(R.id.ll_transaction_pin);
        llBiometricAuth = findViewById(R.id.ll_biometric_auth);
        
        // Bottom actions
        btnTransactionHistory = findViewById(R.id.btn_transaction_history);
        btnLogout = findViewById(R.id.btn_logout);
    }

    private void loadUserData() {
        String username = dataManager.getLoggedInUser();
        User currentUser = null;
        
        // Get user data from mock users
        for (User user : dataManager.getMockUsers()) {
            if (user.getUsername().equals(username)) {
                currentUser = user;
                break;
            }
        }
        
        if (currentUser != null) {
            // Header
            tvFullName.setText(currentUser.getFullName());
            tvCustomerId.setText("Mã khách hàng: " + currentUser.getUsername().toUpperCase());
            
            // Personal information
            tvPhone.setText(maskPhone(currentUser.getPhoneNumber()));
            tvEmail.setText(currentUser.getEmail());
            tvNationalId.setText(maskIdNumber(currentUser.getIdNumber()));
            tvAddress.setText("123 Đường ABC, Quận XYZ, TP.HCM"); // Mock address
        } else {
            // Fallback data
            tvFullName.setText("Nguyễn Văn A");
            tvCustomerId.setText("Mã khách hàng: KH123456789");
            tvPhone.setText("09** *** 5678");
            tvEmail.setText("user@example.com");
            tvNationalId.setText("*** *** 123");
            tvAddress.setText("123 Đường ABC, Quận XYZ, TP.HCM");
        }
    }

    private void loadAccountData() {
        List<Account> accounts = dataManager.getMockAccounts("U001");
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        
        for (Account account : accounts) {
            String maskedAccount = maskAccountNumber(account.getAccountNumber());
            
            switch (account.getType()) {
                case CHECKING:
                    tvCheckingAccount.setText(maskedAccount);
                    tvCheckingBalance.setText(formatter.format(account.getBalance()));
                    break;
                case SAVINGS:
                    tvSavingAccount.setText(maskedAccount);
                    tvSavingBalance.setText(formatter.format(account.getBalance()));
                    break;
                case MORTGAGE:
                    tvMortgageAccount.setText(maskedAccount);
                    double remaining = Math.abs(account.getBalance());
                    tvMortgageBalance.setText("Còn lại: " + formatter.format(remaining));
                    break;
            }
        }
    }

    private void setupEkycStatus() {
        // Mock eKYC status - in real app, get from API
        boolean isEkycVerified = true; // Change to false or pending for testing
        
        if (isEkycVerified) {
            tvEkycStatus.setText("Đã xác thực eKYC");
            tvEkycStatus.setTextColor(Color.parseColor("#4CAF50"));
            viewEkycIndicator.setBackgroundColor(Color.parseColor("#4CAF50"));
            btnStartEkyc.setText("Xác thực lại eKYC");
        } else {
            tvEkycStatus.setText("Chưa xác thực eKYC");
            tvEkycStatus.setTextColor(Color.parseColor("#F44336"));
            viewEkycIndicator.setBackgroundColor(Color.parseColor("#F44336"));
            btnStartEkyc.setText("Bắt đầu xác thực eKYC");
        }
    }

    private void setupClickListeners() {
        // Edit Profile
        btnEditProfile.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng chỉnh sửa hồ sơ đang phát triển", Toast.LENGTH_SHORT).show();
        });
        
        // eKYC Button
        btnStartEkyc.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng xác thực eKYC đang phát triển", Toast.LENGTH_SHORT).show();
        });
        
        // Account clicks
        llCheckingAccount.setOnClickListener(v -> {
            Intent intent = new Intent(this, AccountDetailActivity.class);
            intent.putExtra("account_type", Account.AccountType.CHECKING.name());
            startActivity(intent);
        });
        
        llSavingAccount.setOnClickListener(v -> {
            Intent intent = new Intent(this, AccountDetailActivity.class);
            intent.putExtra("account_type", Account.AccountType.SAVINGS.name());
            startActivity(intent);
        });
        
        llMortgageAccount.setOnClickListener(v -> {
            Intent intent = new Intent(this, AccountDetailActivity.class);
            intent.putExtra("account_type", Account.AccountType.MORTGAGE.name());
            startActivity(intent);
        });
        
        // Security actions
        llChangePassword.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng đổi mật khẩu đang phát triển", Toast.LENGTH_SHORT).show();
        });
        
        llTransactionPin.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng mã PIN giao dịch đang phát triển", Toast.LENGTH_SHORT).show();
        });
        
        llBiometricAuth.setOnClickListener(v -> {
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
                        biometricManager.disableBiometric();
                        Toast.makeText(this, "Đã tắt xác thực sinh trắc học", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
            } else {
                enableBiometric();
            }
        });
        
        // Transaction History
        btnTransactionHistory.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng lịch sử giao dịch đang phát triển", Toast.LENGTH_SHORT).show();
        });
        
        // Logout
        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }
    
    private void enableBiometric() {
        biometricManager.enableBiometric(this, new BiometricAuthManager.BiometricAuthCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(ProfileActivity.this, "Đã bật xác thực sinh trắc học", Toast.LENGTH_SHORT).show();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(ProfileActivity.this, "Không thể bật chức năng: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
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

    // Helper methods for masking sensitive data
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return phone;
        return phone.substring(0, 2) + "** *** " + phone.substring(phone.length() - 4);
    }

    private String maskIdNumber(String idNumber) {
        if (idNumber == null || idNumber.length() < 3) return idNumber;
        return "*** *** " + idNumber.substring(idNumber.length() - 3);
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) return accountNumber;
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
