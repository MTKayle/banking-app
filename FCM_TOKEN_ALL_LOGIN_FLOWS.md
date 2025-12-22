# FCM Token Registration - Tất Cả Các Flow Đăng Nhập

## Tổng Quan
Đã cập nhật để đăng ký FCM token cho **tất cả các trường hợp đăng nhập và đăng ký thành công**, đảm bảo người dùng luôn nhận được thông báo push notification.

## API Endpoint
```
POST http://localhost:8089/api/notifications/register-token
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "fcmToken": "d65KcUD0RR6DB4Lz4qrEUn:APA91b..."
}
```

## Các Flow Đã Được Tích Hợp

### 1. Đăng Nhập Bằng Mật Khẩu (LoginActivity)
**File:** `LoginActivity.java`  
**Method:** `performPasswordLogin()`  
**Line:** ~457

```java
// Reset session khi đăng nhập thành công
sessionManager.onLoginSuccess();

// Đăng ký FCM token sau khi đăng nhập thành công
FcmTokenManager.registerFcmToken(LoginActivity.this);

Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
navigateToDashboard();
```

**Áp dụng cho:**
- ✅ Lần đầu tải app (activity_login.xml)
- ✅ Đăng nhập nhanh (activity_login_quick.xml)
- ✅ Đăng nhập bằng tài khoản khác

### 2. Đăng Nhập Bằng Vân Tay (LoginActivity)
**File:** `LoginActivity.java`  
**Method:** `startBiometricFlow()` → callback  
**Line:** ~643

```java
// Reset session khi đăng nhập thành công
sessionManager.onLoginSuccess();

// Đăng ký FCM token sau khi đăng nhập thành công
FcmTokenManager.registerFcmToken(LoginActivity.this);

runOnUiThread(() -> {
    Toast.makeText(LoginActivity.this, "Đăng nhập bằng vân tay thành công!", Toast.LENGTH_SHORT).show();
    navigateToDashboard();
});
```

**Áp dụng cho:**
- ✅ Đăng nhập bằng vân tay từ activity_login_quick.xml

### 3. Đăng Nhập Qua OTP Verification (OtpVerificationActivity)
**File:** `OtpVerificationActivity.java`  
**Method:** `performLogin()`  
**Line:** ~608

```java
SessionManager sessionManager = SessionManager.getInstance(OtpVerificationActivity.this);
sessionManager.onLoginSuccess();

// Đăng ký FCM token sau khi đăng nhập thành công
com.example.mobilebanking.utils.FcmTokenManager.registerFcmToken(OtpVerificationActivity.this);

Toast.makeText(OtpVerificationActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

// Navigate to dashboard
Intent intent = new Intent(OtpVerificationActivity.this, 
        com.example.mobilebanking.ui_home.UiHomeActivity.class);
intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
startActivity(intent);
finish();
```

**Áp dụng cho:**
- ✅ Đăng nhập lần đầu (sau khi verify OTP)
- ✅ Đăng nhập bằng tài khoản khác (sau khi verify OTP)

### 4. Đăng Ký Tài Khoản Thành Công (Step5FaceVerificationFragment)
**File:** `Step5FaceVerificationFragment.java`  
**Method:** `verifyFaceAndRegister()` → callback  
**Line:** ~1057

```java
// Save session
if (getActivity() != null) {
    DataManager dataManager = DataManager.getInstance(getActivity());
    User.UserRole role = "CUSTOMER".equalsIgnoreCase(authResponse.getRole())
            ? User.UserRole.CUSTOMER
            : User.UserRole.OFFICER;
    dataManager.saveLoggedInUser(registrationData.getPhoneNumber(), role);
    dataManager.saveLastUsername(registrationData.getPhoneNumber());
    
    // Save token
    if (authResponse.getToken() != null) {
        dataManager.saveTokens(authResponse.getToken(), authResponse.getToken());
    }
    
    // Đăng ký FCM token sau khi đăng ký thành công
    com.example.mobilebanking.utils.FcmTokenManager.registerFcmToken(getActivity());
}

// Show success dialog
showVerificationSuccessDialog();
```

**Áp dụng cho:**
- ✅ Đăng ký tài khoản mới thành công (với xác thực khuôn mặt)

## FcmTokenManager Implementation

### Method: `registerFcmToken(Context context)`
**File:** `FcmTokenManager.java`

```java
public static void registerFcmToken(Context context) {
    FirebaseMessaging.getInstance().getToken()
        .addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                return;
            }

            // Get new FCM registration token
            String token = task.getResult();
            Log.d(TAG, "FCM Token: " + token);

            // Send token to backend
            sendTokenToBackend(context, token);
        });
}

private static void sendTokenToBackend(Context context, String fcmToken) {
    DataManager dataManager = DataManager.getInstance(context);
    String accessToken = dataManager.getAccessToken();
    
    if (accessToken == null || accessToken.isEmpty()) {
        Log.w(TAG, "No access token available, cannot register FCM token");
        return;
    }

    NotificationApiService apiService = ApiClient.getNotificationApiService();
    FcmTokenRequest request = new FcmTokenRequest(fcmToken);
    
    Call<FcmTokenResponse> call = apiService.registerToken(request);
    call.enqueue(new Callback<FcmTokenResponse>() {
        @Override
        public void onResponse(Call<FcmTokenResponse> call, Response<FcmTokenResponse> response) {
            if (response.isSuccessful() && response.body() != null) {
                Log.d(TAG, "Token registered successfully: " + response.body().getMessage());
            } else {
                Log.e(TAG, "Failed to register token: " + response.code());
            }
        }

        @Override
        public void onFailure(Call<FcmTokenResponse> call, Throwable t) {
            Log.e(TAG, "Error registering token", t);
        }
    });
}
```

## Luồng Hoạt Động

### Khi Người Dùng Đăng Nhập/Đăng Ký Thành Công:
1. **Lưu session và token** vào DataManager
2. **Gọi `FcmTokenManager.registerFcmToken(context)`**
3. FcmTokenManager lấy FCM token từ Firebase
4. Gửi FCM token lên backend qua API `POST /api/notifications/register-token`
5. Backend lưu FCM token vào database, liên kết với userId
6. Người dùng có thể nhận push notification

### Backend Xử Lý:
- Nhận FCM token từ request
- Lấy userId từ JWT token (Authorization header)
- Lưu/cập nhật FCM token trong database
- Trả về response thành công

## Test Cases

### Test Case 1: Đăng Nhập Lần Đầu
1. Xóa data app
2. Mở app → Nhập số điện thoại và mật khẩu
3. Verify OTP
4. Đăng nhập thành công

**Kết quả mong đợi:**
- Log hiển thị: "FCM Token: d65KcUD0RR6DB4Lz4qrEUn:APA91b..."
- API call: `POST /api/notifications/register-token` với status 200
- Log hiển thị: "Token registered successfully"

### Test Case 2: Đăng Nhập Nhanh (Quick Login)
1. Đã đăng nhập trước đó
2. Mở app lại → Nhập mật khẩu
3. Đăng nhập thành công

**Kết quả mong đợi:**
- FCM token được đăng ký lại
- API call thành công

### Test Case 3: Đăng Nhập Bằng Vân Tay
1. Đã bật fingerprint login
2. Mở app → Nhấn icon vân tay
3. Quét vân tay thành công

**Kết quả mong đợi:**
- FCM token được đăng ký
- API call thành công

### Test Case 4: Đăng Nhập Tài Khoản Khác
1. Đã đăng nhập với tài khoản A
2. Đăng xuất → Chọn "Đăng nhập bằng tài khoản khác"
3. Nhập tài khoản B → Verify OTP
4. Đăng nhập thành công

**Kết quả mong đợi:**
- FCM token của tài khoản B được đăng ký
- Backend cập nhật FCM token cho userId của tài khoản B

### Test Case 5: Đăng Ký Tài Khoản Mới
1. Chọn "Tạo tài khoản"
2. Điền thông tin → Quét CCCD → Chụp selfie
3. Verify face → Đăng ký thành công

**Kết quả mong đợi:**
- FCM token được đăng ký ngay sau khi đăng ký thành công
- API call thành công

## Lợi Ích

✅ **Đầy đủ:** Tất cả các flow đăng nhập/đăng ký đều đăng ký FCM token  
✅ **Nhất quán:** Người dùng luôn nhận được thông báo sau khi đăng nhập  
✅ **Tự động:** Không cần người dùng thao tác thêm  
✅ **Cập nhật:** FCM token được cập nhật mỗi lần đăng nhập  

## Files Đã Thay Đổi

1. `FrontEnd/banking-app/app/src/main/java/com/example/mobilebanking/activities/LoginActivity.java`
   - Đã có sẵn FCM registration (không thay đổi)

2. `FrontEnd/banking-app/app/src/main/java/com/example/mobilebanking/activities/OtpVerificationActivity.java`
   - ✅ Thêm FCM registration trong `performLogin()`

3. `FrontEnd/banking-app/app/src/main/java/com/example/mobilebanking/fragments/Step5FaceVerificationFragment.java`
   - ✅ Thêm FCM registration sau khi đăng ký thành công

## Dependencies

```gradle
// Firebase Cloud Messaging
implementation 'com.google.firebase:firebase-messaging:23.1.0'
```

## Lưu Ý

- FCM token chỉ được gửi khi có access token (đã đăng nhập)
- Nếu không có access token, log sẽ hiển thị warning
- FCM token có thể thay đổi, nên đăng ký lại mỗi lần đăng nhập là tốt nhất
- Backend cần xử lý trường hợp một user có nhiều device (nhiều FCM token)
