package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebanking.R;
import com.example.mobilebanking.models.User;
import com.example.mobilebanking.utils.BiometricAuthManager;
import com.example.mobilebanking.utils.DataManager;

/**
 * Login Activity for user authentication
 */
public class LoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private Button btnLogin, btnBiometric;
    private TextView tvRegister, tvForgotPassword;
    private CheckBox cbRememberMe;
    private DataManager dataManager;
    private BiometricAuthManager biometricManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dataManager = DataManager.getInstance(this);
        biometricManager = new BiometricAuthManager(this);

        // Check if already logged in
        if (dataManager.isLoggedIn()) {
            navigateToDashboard();
            return;
        }

        initializeViews();
        setupListeners();
        loadLastUsername();
        
        // Kiểm tra và hiển thị nút vân tay nếu thiết bị hỗ trợ
        if (!biometricManager.isBiometricAvailable()) {
            btnBiometric.setVisibility(android.view.View.GONE);
        }
    }

    private void initializeViews() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnBiometric = findViewById(R.id.btn_biometric);
        tvRegister = findViewById(R.id.tv_register);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        cbRememberMe = findViewById(R.id.cb_remember_me);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());
        
        btnBiometric.setOnClickListener(v -> handleBiometricLogin());

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, MainRegistrationActivity.class);
            startActivity(intent);
        });

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
        
        // Khi người dùng nhập password và nhấn Enter, tự động đăng nhập nếu username đã có
        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                handleLogin();
                return true;
            }
            return false;
        });
    }
    
    /**
     * Tải username cuối cùng đã đăng nhập và điền vào ô username
     */
    private void loadLastUsername() {
        String lastUsername = dataManager.getLastUsername();
        if (lastUsername != null && !lastUsername.isEmpty()) {
            etUsername.setText(lastUsername);
            // Focus vào ô password để người dùng chỉ cần nhập password
            etPassword.requestFocus();
        }
    }

    private void handleLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        // Nếu username trống, thử lấy từ last username
        if (username.isEmpty()) {
            username = dataManager.getLastUsername();
            if (username != null && !username.isEmpty()) {
                etUsername.setText(username);
            }
        }

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên đăng nhập và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate credentials with mock data
        User authenticatedUser = validateCredentials(username, password);

        if (authenticatedUser != null) {
            // Save session
            dataManager.saveLoggedInUser(username, authenticatedUser.getRole());
            
            // Lưu username cuối cùng để tự động điền lần sau
            dataManager.saveLastUsername(username);
            
            // Tạo và lưu token mới (mock - trong production sẽ lấy từ API)
            dataManager.generateNewTokens(username);
            
            // Nếu đã bật chức năng vân tay, lưu refresh token tạm thời (không yêu cầu quét vân tay)
            // Token sẽ được lưu vào Keystore khi đăng nhập bằng vân tay lần sau
            if (biometricManager.isBiometricEnabled()) {
                String refreshToken = dataManager.getRefreshToken();
                if (refreshToken != null) {
                    // Lưu refresh token tạm thời vào SharedPreferences (không mã hóa)
                    // Khi đăng nhập bằng vân tay lần sau, sẽ lưu vào Keystore với mã hóa
                    saveRefreshTokenWithoutAuth(refreshToken, username);
                }
            }

            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
            navigateToDashboard();
        } else {
            Toast.makeText(this, "Tên đăng nhập hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Xử lý đăng nhập bằng vân tay
     * Luồng:
     * 1. Kiểm tra đã bật chức năng vân tay chưa
     * 2. Nếu chưa bật → Hiện thông báo, yêu cầu đăng nhập bằng mật khẩu
     * 3. Nếu đã bật → Yêu cầu quét vân tay ngay → Giải mã refresh token → Gửi về backend để lấy access token mới
     */
    private void handleBiometricLogin() {
        // Bước 1: Kiểm tra đã bật chức năng vân tay chưa
        if (!biometricManager.isBiometricEnabled()) {
            new AlertDialog.Builder(this)
                .setTitle("Chưa bật đăng nhập bằng vân tay")
                .setMessage("Bạn chưa bật chức năng đăng nhập bằng vân tay. Vui lòng vào Cài đặt để bật tính năng này, hoặc đăng nhập bằng mật khẩu.")
                .setPositiveButton("Đăng nhập bằng mật khẩu", (dialog, which) -> {
                    // Focus vào ô username
                    etUsername.requestFocus();
                })
                .setNegativeButton("Hủy", null)
                .show();
            return;
        }
        
        // Bước 2: Yêu cầu quét vân tay ngay (không check hasRefreshToken nữa)
        // Nếu chưa có refresh token, sẽ báo lỗi trong getRefreshToken
        biometricManager.getRefreshToken(this, new BiometricAuthManager.BiometricAuthCallback() {
            @Override
            public void onSuccess() {
                // Method này không được sử dụng trong getRefreshToken
                // Nhưng phải override vì là abstract method
            }
            
            @Override
            public void onTokenRetrieved(String refreshToken) {
                // Lấy username từ biometric manager (đã lưu khi đăng nhập bằng mật khẩu)
                String username = biometricManager.getBiometricUsername();
                if (username == null) {
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this, "Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại bằng mật khẩu.", Toast.LENGTH_LONG).show();
                    });
                    return;
                }
                
                // Lưu username vào session TRƯỚC khi refresh token (vì refreshAccessToken cần username)
                User authenticatedUser = getUserByUsername(username);
                if (authenticatedUser != null) {
                    dataManager.saveLoggedInUser(username, authenticatedUser.getRole());
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this, "Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại bằng mật khẩu.", Toast.LENGTH_LONG).show();
                    });
                    return;
                }
                
                // Bước 4: Gửi refresh token về backend để lấy access token mới
                // Mock: Trong production sẽ gọi API
                boolean success = dataManager.refreshAccessToken(refreshToken);
                
                if (success) {
                    // Lưu lại refresh token mới vào temp storage (không yêu cầu quét vân tay)
                    // Vì mỗi lần đăng nhập đều cấp token mới
                    String newRefreshToken = dataManager.getRefreshToken();
                    if (newRefreshToken != null) {
                        saveRefreshTokenWithoutAuth(newRefreshToken, username);
                    }
                    
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this, "Đăng nhập bằng vân tay thành công!", Toast.LENGTH_SHORT).show();
                        navigateToDashboard();
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this, "Không thể làm mới token. Vui lòng đăng nhập bằng mật khẩu.", Toast.LENGTH_LONG).show();
                    });
                }
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private User validateCredentials(String username, String password) {
        for (User user : dataManager.getMockUsers()) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }
    
    /**
     * Lấy user từ username (dùng khi đăng nhập bằng vân tay)
     */
    private User getUserByUsername(String username) {
        for (User user : dataManager.getMockUsers()) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    private void navigateToDashboard() {
        User.UserRole role = dataManager.getUserRole();
        Intent intent;
        
        if (role == User.UserRole.OFFICER) {
            intent = new Intent(LoginActivity.this, OfficerDashboardActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, CustomerDashboardActivity.class);
        }
        
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    /**
     * Lưu refresh token vào Keystore mà không yêu cầu quét vân tay
     * Sử dụng cách lưu tạm thời vào SharedPreferences (không mã hóa)
     * Khi đăng nhập bằng vân tay lần sau, sẽ lưu vào Keystore với mã hóa
     */
    private void saveRefreshTokenWithoutAuth(String refreshToken, String username) {
        // Lưu tạm thời vào SharedPreferences (không mã hóa)
        // Khi đăng nhập bằng vân tay lần sau, sẽ lưu vào Keystore với mã hóa
        android.content.SharedPreferences prefs = getSharedPreferences("BiometricPrefs", MODE_PRIVATE);
        long expiryTime = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L); // 7 ngày
        
        prefs.edit()
            .putString("temp_refresh_token", refreshToken)
            .putString("biometric_username", username)
            .putLong("refresh_token_expiry", expiryTime)
            .apply();
        
        // Nếu có refresh token cũ trong Keystore, giữ nguyên
        // Nếu chưa có, sẽ lưu vào Keystore khi đăng nhập bằng vân tay lần sau
    }
}

