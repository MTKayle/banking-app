package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebanking.R;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.AuthApiService;
import com.example.mobilebanking.api.dto.AuthResponse;
import com.example.mobilebanking.api.dto.LoginRequest;
import com.example.mobilebanking.api.dto.RefreshTokenRequest;
import com.example.mobilebanking.api.dto.FeatureStatusResponse;
import com.example.mobilebanking.models.User;
import com.example.mobilebanking.utils.BiometricAuthManager;
import com.example.mobilebanking.utils.DataManager;
import com.example.mobilebanking.utils.SessionManager;
import com.example.mobilebanking.utils.FcmTokenManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Login Activity for user authentication
 */
public class LoginActivity extends BaseActivity {
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword, tvUserName, tvOtherAccount;
    private TextView tvBackText, tvUserNameBack;
    private ImageView btnBack;
    private android.view.View backButtonContainer;
    private CheckBox cbRememberMe;
    private ImageView ivFingerprint;
    private DataManager dataManager;
    private BiometricAuthManager biometricManager;
    private SessionManager sessionManager;
    private boolean isQuickLoginMode = false; // Flag to determine which layout is used

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo ApiClient
        ApiClient.init(this);

        dataManager = DataManager.getInstance(this);
        biometricManager = new BiometricAuthManager(this);
        sessionManager = SessionManager.getInstance(this);

        // KHÔNG check isLoggedIn nữa vì session đã hết hạn khi mở app lại
        // Session sẽ được kiểm tra bởi SessionManager

        // Kiểm tra xem đã có lần đăng nhập nào trước đó chưa
        String lastUsername = dataManager.getLastUsername();
        String lastFullName = dataManager.getLastFullName();
        
        if (lastUsername != null && !lastUsername.isEmpty()) {
            // Đã có lần đăng nhập trước đó → hiển thị quick login
            setContentView(R.layout.activity_login_quick);
            isQuickLoginMode = true;
        } else {
            // Lần đầu tải app → hiển thị full login (không có phần "Quay lại")
            setContentView(R.layout.activity_login);
            isQuickLoginMode = false;
        }

        initializeViews();
        setupListeners();
        loadLastUsername();
        loadLastUserInfo();
        setupBackButton();
        
        // Hide fingerprint icon if biometric is not available
        if (ivFingerprint != null && !biometricManager.isBiometricAvailable()) {
            ivFingerprint.setVisibility(android.view.View.GONE);
        }
    }

    private void initializeViews() {
        // Views that exist in both layouts
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        
        // Views that only exist in full login layout (activity_login.xml)
        // These may be null in quick login mode
        etUsername = findViewById(R.id.et_username);
        tvRegister = findViewById(R.id.tv_register);
        backButtonContainer = findViewById(R.id.back_button_container);
        btnBack = findViewById(R.id.btn_back);
        tvBackText = findViewById(R.id.tv_back_text);
        tvUserNameBack = findViewById(R.id.tv_user_name_back);
        
        // Views that only exist in quick login layout (activity_login_quick.xml)
        // These may be null in full login mode
        tvUserName = findViewById(R.id.tv_user_name);
        tvOtherAccount = findViewById(R.id.tv_other_account);
        ivFingerprint = findViewById(R.id.iv_fingerprint);
    }
    
    /**
     * Setup back button container visibility based on whether user has logged in before
     */
    private void setupBackButton() {
        if (backButtonContainer == null) {
            return; // Not in full login layout
        }
        
        // Kiểm tra xem đã có lần đăng nhập nào trước đó chưa
        String lastUsername = dataManager.getLastUsername();
        String lastFullName = dataManager.getLastFullName();
        
        if (lastUsername != null && !lastUsername.isEmpty()) {
            // Đã có lần đăng nhập → hiển thị phần "Quay lại người dùng"
            backButtonContainer.setVisibility(android.view.View.VISIBLE);
            
            // Hiển thị tên người dùng
            if (tvUserNameBack != null) {
                if (lastFullName != null && !lastFullName.isEmpty()) {
                    tvUserNameBack.setText(lastFullName.toUpperCase());
                } else {
                    tvUserNameBack.setText(lastUsername);
                }
            }
            
            // Setup click listener cho nút back
            if (btnBack != null) {
                btnBack.setOnClickListener(v -> switchToQuickLogin());
            }
            if (tvBackText != null) {
                tvBackText.setOnClickListener(v -> switchToQuickLogin());
            }
            if (tvUserNameBack != null) {
                tvUserNameBack.setOnClickListener(v -> switchToQuickLogin());
            }
        } else {
            // Lần đầu tải app → ẩn phần "Quay lại người dùng"
            backButtonContainer.setVisibility(android.view.View.GONE);
        }
    }
    
    /**
     * Chuyển từ full login về quick login
     */
    private void switchToQuickLogin() {
        setContentView(R.layout.activity_login_quick);
        isQuickLoginMode = true;
        
        // Re-initialize all components for the new layout
        initializeViews();
        setupListeners();
        loadLastUserInfo();
        
        // Clear password field when switching
        if (etPassword != null) {
            etPassword.setText("");
        }
        
        // Hide fingerprint icon if biometric is not available
        if (ivFingerprint != null && !biometricManager.isBiometricAvailable()) {
            ivFingerprint.setVisibility(android.view.View.GONE);
        }
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());

        if (tvRegister != null) {
            tvRegister.setOnClickListener(v -> {
                // Mở màn hình giới thiệu + banner (WelcomeBannerActivity)
                Intent intent = new Intent(LoginActivity.this, WelcomeBannerActivity.class);
                startActivity(intent);
            });
        }

        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            });
        }
        
        // Quick login mode: Other account button
        if (tvOtherAccount != null) {
            tvOtherAccount.setOnClickListener(v -> {
                // Switch to full login screen
                setContentView(R.layout.activity_login);
                isQuickLoginMode = false;
                
                // Re-initialize all components for the new layout
                initializeViews();
                setupListeners();
                loadLastUsername();
                setupBackButton(); // Setup back button với tên người dùng
                
                // Clear password field when switching
                if (etPassword != null) {
                    etPassword.setText("");
                }
            });
        }
        
        // Quick login mode: Fingerprint icon
        if (ivFingerprint != null) {
            ivFingerprint.setOnClickListener(v -> handleBiometricLogin());
        }
        
        // Khi người dùng nhập password và nhấn Enter, tự động đăng nhập nếu username đã có
        if (etPassword != null) {
            etPassword.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                    handleLogin();
                    return true;
                }
                return false;
            });
        }
    }
    
    /**
     * Tải username cuối cùng đã đăng nhập và điền vào ô username
     */
    private void loadLastUsername() {
        if (etUsername != null) {
            String lastUsername = dataManager.getLastUsername();
            if (lastUsername != null && !lastUsername.isEmpty()) {
                etUsername.setText(lastUsername);
                // Focus vào ô password để người dùng chỉ cần nhập password
                if (etPassword != null) {
                    etPassword.requestFocus();
                }
            }
        }
    }
    
    /**
     * Tải thông tin người dùng lần đăng nhập cuối (tên đầy đủ) để hiển thị
     * Chỉ hiển thị họ và tên, không hiển thị số điện thoại
     */
    private void loadLastUserInfo() {
        if (tvUserName != null) {
            String lastFullName = dataManager.getLastFullName();
            if (lastFullName != null && !lastFullName.isEmpty()) {
                tvUserName.setText(lastFullName);
            } else {
                // Nếu không có fullName, để trống hoặc hiển thị "Người dùng"
                tvUserName.setText("Người dùng");
            }
        }
    }

    private void handleLogin() {
        String phone = null;
        if (etUsername != null) {
            phone = etUsername.getText().toString().trim();
        }
        String password = etPassword.getText().toString().trim();
        
        // Nếu phone trống, thử lấy từ last username
        if (phone == null || phone.isEmpty()) {
            phone = dataManager.getLastUsername();
            if (phone != null && !phone.isEmpty() && etUsername != null) {
                etUsername.setText(phone);
            }
        }

        if (phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số điện thoại và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate phone format (10-11 digits)
        if (!phone.matches("^[0-9]{10,11}$")) {
            Toast.makeText(this, "Số điện thoại không hợp lệ (10-11 chữ số)", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // ⭐ THAY ĐỔI MỚI: Kiểm tra xem có phải tài khoản cuối cùng không
        String lastUsername = dataManager.getLastUsername();
        final String finalPhone = phone;
        final String finalPassword = password;
        
        // Chỉ khi đăng nhập lại ĐÚNG tài khoản cuối cùng thì mới không cần OTP
        if (lastUsername != null && !lastUsername.isEmpty() && finalPhone.equals(lastUsername)) {
            // Đăng nhập lại tài khoản cuối cùng → Đăng nhập bình thường (không cần OTP)
            performPasswordLogin(finalPhone, finalPassword);
            return;
        }

        // Các trường hợp khác → Yêu cầu OTP:
        // 1. Lần đầu tải app (lastUsername = null)
        // 2. Đăng nhập bằng tài khoản khác (finalPhone != lastUsername)
        verifyLoginAndRequestOtp(finalPhone, finalPassword);
    }
    
    /**
     * Xác thực thông tin đăng nhập trước khi yêu cầu OTP
     * Dùng cho trường hợp đăng nhập bằng tài khoản khác
     */
    private void verifyLoginAndRequestOtp(String phone, String password) {
        // Disable login button to prevent multiple clicks
        btnLogin.setEnabled(false);
        btnLogin.setText("Đang kiểm tra...");

        // Call API để verify thông tin đăng nhập
        LoginRequest loginRequest = new LoginRequest(phone, password);
        AuthApiService authApiService = ApiClient.getAuthApiService();
        
        Call<AuthResponse> call = authApiService.login(loginRequest);
        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Đăng nhập");
                
                if (response.isSuccessful() && response.body() != null) {
                    // Đăng nhập thành công → Chuyển sang OTP verification
                    new AlertDialog.Builder(LoginActivity.this)
                            .setTitle("Xác Thực OTP")
                            .setMessage("Thông tin đăng nhập chính xác. Vui lòng xác thực OTP để tiếp tục.")
                            .setPositiveButton("Xác Thực", (dialog, which) -> {
                                // Chuyển sang OtpVerificationActivity
                                Intent intent = new Intent(LoginActivity.this, OtpVerificationActivity.class);
                                intent.putExtra("flow", "login_verification");
                                intent.putExtra("phone", phone);
                                intent.putExtra("password", password);
                                startActivity(intent);
                            })
                            .setNegativeButton("Hủy", null)
                            .show();
                } else {
                    // Parse error message
                    String errorMessage = "Số điện thoại hoặc mật khẩu không chính xác";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            JsonObject jsonObject = JsonParser.parseString(errorBody).getAsJsonObject();
                            if (jsonObject.has("message")) {
                                errorMessage = jsonObject.get("message").getAsString();
                            } else if (jsonObject.has("error")) {
                                errorMessage = jsonObject.get("error").getAsString();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Đăng nhập");
                
                String errorMessage = "Không thể kết nối đến server";
                if (t.getMessage() != null) {
                    if (t.getMessage().contains("Failed to connect")) {
                        errorMessage = "Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng.";
                    } else {
                        errorMessage = "Lỗi: " + t.getMessage();
                    }
                }
                
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }
    
    private void performPasswordLogin(String phone, String password) {
        // Disable login button to prevent multiple clicks
        btnLogin.setEnabled(false);
        btnLogin.setText("Đang đăng nhập...");

        // Call API
        LoginRequest loginRequest = new LoginRequest(phone, password);
        AuthApiService authApiService = ApiClient.getAuthApiService();
        
        Call<AuthResponse> call = authApiService.login(loginRequest);
        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Đăng nhập");
                
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    
            // Save session
                    User.UserRole role = "CUSTOMER".equalsIgnoreCase(authResponse.getRole()) 
                            ? User.UserRole.CUSTOMER 
                            : User.UserRole.OFFICER;
                    dataManager.saveLoggedInUser(phone, role);
                    
                    // Lưu username (phone) cuối cùng để tự động điền lần sau
                    dataManager.saveLastUsername(phone);
                    
                    // Lưu tên đầy đủ (fullName) của người dùng để hiển thị lần sau
                    if (authResponse.getFullName() != null && !authResponse.getFullName().isEmpty()) {
                        dataManager.saveLastFullName(authResponse.getFullName());
                    }
                    
                    // Lưu đầy đủ thông tin từ AuthResponse
                    // Lưu userId
                    if (authResponse.getUserId() != null) {
                        dataManager.saveUserId(authResponse.getUserId());
                    }
                    
                    // Lưu phone từ AuthResponse (khác với lastUsername dùng cho auto-fill)
                    if (authResponse.getPhone() != null && !authResponse.getPhone().isEmpty()) {
                        dataManager.saveUserPhone(authResponse.getPhone());
                    }
                    
                    // Lưu fullName từ AuthResponse (khác với lastFullName dùng cho login screen)
                    if (authResponse.getFullName() != null && !authResponse.getFullName().isEmpty()) {
                        dataManager.saveUserFullName(authResponse.getFullName());
                    }
                    
                    // Lưu email từ AuthResponse
                    if (authResponse.getEmail() != null && !authResponse.getEmail().isEmpty()) {
                        dataManager.saveUserEmail(authResponse.getEmail());
                    }
                    
                    // ⭐ THAY ĐỔI MỚI: Luôn lưu refresh token
                    // Lưu token từ API response (access token + refresh token)
                    if (authResponse.getToken() != null && authResponse.getRefreshToken() != null) {
                        dataManager.saveTokens(authResponse.getToken(), authResponse.getRefreshToken());
                        
                        // Luôn lưu refresh token tạm thời để có thể bật fingerprint sau này
                        // Không cần check isBiometricEnabled() vì user có thể bật sau
                        saveRefreshTokenWithoutAuth(authResponse.getRefreshToken(), phone);
                    } else if (authResponse.getToken() != null) {
                        // Fallback: trong trường hợp backend chưa trả refresh token
                        dataManager.saveTokens(authResponse.getToken(), authResponse.getToken());
                        saveRefreshTokenWithoutAuth(authResponse.getToken(), phone);
                    }
                    
                    // Reset session khi đăng nhập thành công
                    sessionManager.onLoginSuccess();

                    // Đăng ký FCM token sau khi đăng nhập thành công
                    FcmTokenManager.registerFcmToken(LoginActivity.this);

                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    navigateToDashboard();
                } else {
                    // Parse error message
                    String errorMessage = "Đăng nhập thất bại";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            JsonObject jsonObject = JsonParser.parseString(errorBody).getAsJsonObject();
                            if (jsonObject.has("message")) {
                                errorMessage = jsonObject.get("message").getAsString();
                            } else if (jsonObject.has("error")) {
                                errorMessage = jsonObject.get("error").getAsString();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Đăng nhập");
                
                String errorMessage = "Không thể kết nối đến server";
                if (t.getMessage() != null) {
                    if (t.getMessage().contains("Failed to connect")) {
                        errorMessage = "Không thể kết nối đến server. Vui lòng kiểm tra:\n" +
                                "1. Backend đã chạy chưa?\n" +
                                "2. Địa chỉ IP trong ApiClient đúng chưa?";
        } else {
                        errorMessage = "Lỗi: " + t.getMessage();
                    }
                }
                
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }
    
    /**
     * Xử lý đăng nhập bằng vân tay
     * Luồng:
     * 1. Kiểm tra đã bật chức năng vân tay chưa
     * 2. Nếu chưa bật → Hiện thông báo, yêu cầu đăng nhập bằng mật khẩu
     * 3. Nếu đã bật → Yêu cầu quét vân tay ngay → Giải mã refresh token → Gửi về backend để lấy access token mới
     */
    private void handleBiometricLogin() {
        // Lấy số điện thoại hiện tại hoặc last username (để gửi lên backend kiểm tra)
        String phone = null;
        if (etUsername != null) {
            phone = etUsername.getText().toString().trim();
        }
        
        if (phone == null || phone.isEmpty()) {
            phone = dataManager.getLastUsername();
        }

        if (phone == null || phone.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số điện thoại trước khi dùng vân tay", Toast.LENGTH_LONG).show();
            return;
        }

        // Bước 1: Kiểm tra đã bật chức năng vân tay chưa
        if (!biometricManager.isBiometricEnabled()) {
            new AlertDialog.Builder(this)
                .setTitle("Chưa bật đăng nhập bằng vân tay")
                .setMessage("Bạn chưa bật chức năng đăng nhập bằng vân tay. Vui lòng vào Cài đặt để bật tính năng này, hoặc đăng nhập bằng mật khẩu.")
                .setPositiveButton("Đăng nhập bằng mật khẩu", (dialog, which) -> {
                    // Focus vào ô username nếu có
                    if (etUsername != null) {
                        etUsername.requestFocus();
                    } else if (etPassword != null) {
                        etPassword.requestFocus();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
            return;
        }

        // Bước 1.5: Hỏi backend xem tài khoản này đã bật fingerprint login chưa
        AuthApiService authApiService = ApiClient.getAuthApiService();
        Call<FeatureStatusResponse> checkCall = authApiService.checkFingerprintEnabled(phone);
        checkCall.enqueue(new Callback<FeatureStatusResponse>() {
            @Override
            public void onResponse(Call<FeatureStatusResponse> call, Response<FeatureStatusResponse> response) {
                if (!response.isSuccessful() || response.body() == null || !response.body().isEnabled()) {
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this,
                                "Tài khoản này chưa bật đăng nhập bằng vân tay trên hệ thống. Vui lòng đăng nhập bằng mật khẩu.",
                                Toast.LENGTH_LONG).show();
                    });
                    return;
                }

                // Backend xác nhận đã bật fingerprint login → tiến hành quét vân tay
                startBiometricFlow();
            }

            @Override
            public void onFailure(Call<FeatureStatusResponse> call, Throwable t) {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this,
                            "Không thể kiểm tra trạng thái vân tay. Vui lòng thử lại hoặc đăng nhập bằng mật khẩu.",
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /**
     * Bắt đầu luồng quét vân tay và gọi API refresh-token sau khi giải mã refresh token
     */
    private void startBiometricFlow() {
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
                
                // Lấy role từ session (nếu có) hoặc mặc định là CUSTOMER
                User.UserRole role = getUserRoleByPhone(username);
                if (role == null) {
                    role = User.UserRole.CUSTOMER; // Mặc định
                }
                dataManager.saveLoggedInUser(username, role);
                
                // Bước 4: Gửi refresh token về backend để lấy access token + refresh token mới
                AuthApiService authApiService = ApiClient.getAuthApiService();
                RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);
                Call<AuthResponse> call = authApiService.refreshToken(request);

                call.enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        if (response.isSuccessful() && response.body() != null &&
                                response.body().getToken() != null && response.body().getRefreshToken() != null) {

                            AuthResponse authResponse = response.body();

                            // Lưu token mới
                            dataManager.saveTokens(authResponse.getToken(), authResponse.getRefreshToken());
                            
                            // ⭐ THAY ĐỔI MỚI: Lưu userId và thông tin user từ AuthResponse
                            if (authResponse.getUserId() != null) {
                                dataManager.saveUserId(authResponse.getUserId());
                            }
                            if (authResponse.getPhone() != null) {
                                dataManager.saveUserPhone(authResponse.getPhone());
                            }
                            if (authResponse.getFullName() != null) {
                                dataManager.saveUserFullName(authResponse.getFullName());
                            }
                            if (authResponse.getEmail() != null) {
                                dataManager.saveUserEmail(authResponse.getEmail());
                            }

                            // Lưu lại refresh token mới vào temp storage
                            saveRefreshTokenWithoutAuth(authResponse.getRefreshToken(), username);
                            
                            // Reset session khi đăng nhập thành công
                            sessionManager.onLoginSuccess();

                            // Đăng ký FCM token sau khi đăng nhập thành công
                            FcmTokenManager.registerFcmToken(LoginActivity.this);

                            runOnUiThread(() -> {
                                Toast.makeText(LoginActivity.this, "Đăng nhập bằng vân tay thành công!", Toast.LENGTH_SHORT).show();
                                navigateToDashboard();
                            });
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(LoginActivity.this,
                                        "Không thể làm mới token. Vui lòng đăng nhập bằng mật khẩu.",
                                        Toast.LENGTH_LONG).show();
                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        runOnUiThread(() -> {
                            Toast.makeText(LoginActivity.this,
                                    "Lỗi kết nối khi làm mới token. Vui lòng đăng nhập bằng mật khẩu.",
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /**
     * Lấy user role từ phone (dùng khi đăng nhập bằng vân tay)
     * Trong trường hợp này, role được lưu trong session sau khi đăng nhập thành công
     */
    private User.UserRole getUserRoleByPhone(String phone) {
        // Role đã được lưu trong session, chỉ cần lấy từ DataManager
        return dataManager.getUserRole();
    }

    private void navigateToDashboard() {
        User.UserRole role = dataManager.getUserRole();
        Intent intent;
        
        if (role == User.UserRole.OFFICER) {
            intent = new Intent(LoginActivity.this, OfficerDashboardActivity.class);
        } else {
            // Chuyển đến UiHomeActivity thay vì CustomerDashboardActivity
            intent = new Intent(LoginActivity.this, com.example.mobilebanking.ui_home.UiHomeActivity.class);
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
    
    /**
     * LoginActivity không cần kiểm tra session
     */
    @Override
    protected boolean shouldCheckSession() {
        return false;
    }
}

