# Backup Code - Các Thay Đổi Quan Trọng

## 📌 Mục Đích
File này chứa code backup của các thay đổi quan trọng nhất.
Sử dụng khi cần implement lại nhanh chóng.

---

## 1. LoginActivity.java - handleLogin() Method

### Thay Đổi: Kiểm Tra Tài Khoản Cuối Cùng & OTP Verification

```java
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
    
    if (lastUsername != null && !lastUsername.isEmpty() && !finalPhone.equals(lastUsername)) {
        // Không phải tài khoản cuối cùng → Yêu cầu xác thực OTP
        new AlertDialog.Builder(this)
                .setTitle("Xác Thực OTP")
                .setMessage("Bạn đang đăng nhập bằng tài khoản khác. Vui lòng xác thực OTP để tiếp tục.")
                .setPositiveButton("Xác Thực", (dialog, which) -> {
                    // Chuyển sang OtpVerificationActivity
                    Intent intent = new Intent(LoginActivity.this, OtpVerificationActivity.class);
                    intent.putExtra("flow", "login_verification");
                    intent.putExtra("phone", finalPhone);
                    intent.putExtra("password", finalPassword);
                    startActivity(intent);
                })
                .setNegativeButton("Hủy", null)
                .show();
        return;
    }

    // Tài khoản cuối cùng hoặc lần đầu đăng nhập → Đăng nhập bình thường
    performPasswordLogin(finalPhone, finalPassword);
}
```

---

## 2. LoginActivity.java - Lưu Refresh Token

### Thay Đổi: Luôn Lưu Token (Không Cần Check isBiometricEnabled)

```java
// Trong method performPasswordLogin(), sau khi login thành công:

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
```

---

## 3. LoginActivity.java - Lưu userId Khi Refresh Token

### Thay Đổi: Lưu Đầy Đủ Thông Tin Từ AuthResponse

```java
// Trong method startBiometricFlow(), callback của refreshToken():

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

    runOnUiThread(() -> {
        Toast.makeText(LoginActivity.this, "Đăng nhập bằng vân tay thành công!", Toast.LENGTH_SHORT).show();
        navigateToDashboard();
    });
}
```

---

## 4. SettingsActivity.java - Gọi Backend API

### Thay Đổi: Update Backend Khi Bật/Tắt Fingerprint

```java
// Thêm vào class:
private UserApiService userApiService;

// Trong onCreate():
userApiService = ApiClient.getUserApiService();

// Method mới:
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
```

---

## 5. ApiClient.java - Thêm getUserApiService()

```java
// Thêm field:
private static UserApiService userApiService;

// Thêm method:
public static UserApiService getUserApiService() {
    if (userApiService == null) {
        userApiService = getRetrofitInstance().create(UserApiService.class);
    }
    return userApiService;
}

// Update reset():
public static void reset() {
    retrofit = null;
    authApiService = null;
    accountApiService = null;
    paymentApiService = null;
    biometricApiService = null;
    movieApiService = null;
    transactionApiService = null;
    userApiService = null; // ⭐ THÊM DÒNG NÀY
}
```

---

## 6. OtpVerificationActivity.java - Hỗ Trợ login_verification

### Thêm Field:
```java
private String password; // For login_verification flow
```

### Trong onCreate():
```java
// Lấy flow từ intent
String flow = getIntent().getStringExtra("flow");
if (flow != null && !flow.isEmpty()) {
    fromActivity = flow;
}

// Lấy password nếu là login_verification flow
password = getIntent().getStringExtra("password");

// Xử lý flow login_verification
if ("login_verification".equals(fromActivity)) {
    sendOtpWithGoixe();
}
```

### Trong handleOtpVerification():
```java
// Kiểm tra luồng
if ("forgot_password".equals(fromActivity) || "movie_booking".equals(fromActivity) || "login_verification".equals(fromActivity)) {
    verifyOtpWithGoixe(otp);
} else {
    verifyOtpWithESms(otp);
}
```

### Trong verifyOtpWithGoixe():
```java
if (otpResponse.isSuccess()) {
    Toast.makeText(OtpVerificationActivity.this, 
            "Xác thực OTP thành công!", Toast.LENGTH_SHORT).show();
    
    if ("forgot_password".equals(fromActivity)) {
        // ...
    } else if ("movie_booking".equals(fromActivity)) {
        // ...
    } else if ("login_verification".equals(fromActivity)) {
        performLogin(); // ⭐ THÊM DÒNG NÀY
    }
}
```

### Method Mới performLogin():
```java
private void performLogin() {
    if (phoneNumber == null || password == null) {
        Toast.makeText(this, "Lỗi: Thiếu thông tin đăng nhập", Toast.LENGTH_SHORT).show();
        return;
    }
    
    // Show loading
    if (progressBar != null) {
        progressBar.setVisibility(android.view.View.VISIBLE);
    }
    btnVerify.setEnabled(false);
    
    // Call login API
    LoginRequest loginRequest = new LoginRequest(phoneNumber, password);
    AuthApiService authApiService = ApiClient.getAuthApiService();
    
    Call<AuthResponse> call = authApiService.login(loginRequest);
    
    call.enqueue(new Callback<AuthResponse>() {
        @Override
        public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
            if (progressBar != null) {
                progressBar.setVisibility(android.view.View.GONE);
            }
            btnVerify.setEnabled(true);
            
            if (response.isSuccessful() && response.body() != null) {
                AuthResponse authResponse = response.body();
                
                // Save session and user info
                DataManager dataManager = DataManager.getInstance(OtpVerificationActivity.this);
                User.UserRole role = "CUSTOMER".equalsIgnoreCase(authResponse.getRole()) 
                        ? User.UserRole.CUSTOMER 
                        : User.UserRole.OFFICER;
                
                dataManager.saveLoggedInUser(phoneNumber, role);
                dataManager.saveLastUsername(phoneNumber);
                
                if (authResponse.getUserId() != null) {
                    dataManager.saveUserId(authResponse.getUserId());
                }
                if (authResponse.getPhone() != null) {
                    dataManager.saveUserPhone(authResponse.getPhone());
                }
                if (authResponse.getFullName() != null) {
                    dataManager.saveUserFullName(authResponse.getFullName());
                    dataManager.saveLastFullName(authResponse.getFullName());
                }
                if (authResponse.getEmail() != null) {
                    dataManager.saveUserEmail(authResponse.getEmail());
                }
                
                if (authResponse.getToken() != null && authResponse.getRefreshToken() != null) {
                    dataManager.saveTokens(authResponse.getToken(), authResponse.getRefreshToken());
                }
                
                SessionManager sessionManager = SessionManager.getInstance(OtpVerificationActivity.this);
                sessionManager.onLoginSuccess();
                
                Toast.makeText(OtpVerificationActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                
                // Navigate to dashboard
                Intent intent = new Intent(OtpVerificationActivity.this, 
                        com.example.mobilebanking.ui_home.UiHomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(OtpVerificationActivity.this, 
                        "Đăng nhập thất bại. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
            }
        }
        
        @Override
        public void onFailure(Call<AuthResponse> call, Throwable t) {
            if (progressBar != null) {
                progressBar.setVisibility(android.view.View.GONE);
            }
            btnVerify.setEnabled(true);
            
            Toast.makeText(OtpVerificationActivity.this, 
                    "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
        }
    });
}
```

---

## 📝 Ghi Chú
- Tất cả code trên đã được test và hoạt động
- Import statements cần được thêm vào đầu file
- Các DTO classes (SmartFlagsRequest, UserResponse) xem trong documentation
