# Sửa Lỗi Gửi Lại OTP và Xác Nhận OTP

## Vấn Đề Phát Hiện

### 1. Lỗi Import Sai Interface
**OtpVerificationActivity** đang import sai `OtpApiService`:
- ❌ Import: `com.example.mobilebanking.api.OtpApiService` (sai)
- ✅ Cần import: `com.example.mobilebanking.utils.OtpApiService` (đúng)

### 2. Sự Khác Biệt Giữa 2 Interface

#### Interface 1: `com.example.mobilebanking.api.OtpApiService`
```java
// Endpoint: /api/otp/request
@FormUrlEncoded
@POST("api/otp/request")
Call<OtpResponse> requestOtp(
    @Field("user_id") String userId,
    @Field("api_key") String apiKey,
    @Field("phone") String phone  // ❌ Sai field name
);

// Endpoint: /api/otp/verify
@FormUrlEncoded
@POST("api/otp/verify")
Call<OtpResponse> verifyOtp(
    @Field("user_id") String userId,
    @Field("api_key") String apiKey,
    @Field("phone") String phone,  // ❌ Sai field name
    @Field("otp") String otp       // ❌ Sai field name
);
```

#### Interface 2: `com.example.mobilebanking.utils.OtpApiService` (ĐÚNG)
```java
// Endpoint: request_otp.php
@FormUrlEncoded
@POST("request_otp.php")
Call<OtpResponse> requestOtp(
    @Field("user_id") String userId,
    @Field("api_key") String apiKey,
    @Field("recipient_phone") String phone  // ✅ Đúng field name
);

// Endpoint: verify_otp.php
@FormUrlEncoded
@POST("verify_otp.php")
Call<OtpResponse> verifyOtp(
    @Field("user_id") String userId,
    @Field("api_key") String apiKey,
    @Field("recipient_phone") String phone,  // ✅ Đúng field name
    @Field("otp_code") String otpCode        // ✅ Đúng field name
);
```

### 3. Tại Sao Lỗi?
- API Goixe247 thực tế sử dụng:
  - Endpoint: `request_otp.php` và `verify_otp.php`
  - Field names: `recipient_phone` và `otp_code`
- Khi sử dụng sai interface → gửi sai field names → API trả về lỗi

## Các Sửa Đổi

### 1. OtpVerificationActivity.java

#### a. Sửa Import
```java
// Trước (SAI):
import com.example.mobilebanking.api.OtpApiService;
import com.example.mobilebanking.api.dto.OtpResponse;

// Sau (ĐÚNG):
import com.example.mobilebanking.utils.OtpApiService;
import com.example.mobilebanking.utils.OtpResponse;
```

#### b. Thêm Logging Chi Tiết
```java
private void sendOtpWithGoixe() {
    Log.d(TAG, "Sending OTP to: " + phoneNumber);
    
    // ... existing code ...
    
    call.enqueue(new Callback<OtpResponse>() {
        @Override
        public void onResponse(Call<OtpResponse> call, Response<OtpResponse> response) {
            Log.d(TAG, "OTP Request Response Code: " + response.code());
            
            if (response.isSuccessful() && response.body() != null) {
                OtpResponse otpResponse = response.body();
                Log.d(TAG, "OTP Response: success=" + otpResponse.isSuccess() + 
                           ", message=" + otpResponse.getMessage());
                // ...
            }
        }
        
        @Override
        public void onFailure(Call<OtpResponse> call, Throwable t) {
            Log.e(TAG, "OTP Request failed", t);
            // ...
        }
    });
}
```

#### c. Cải Thiện verifyOtpWithGoixe()
```java
private void verifyOtpWithGoixe(String otp) {
    Log.d(TAG, "Verifying OTP: " + otp + " for phone: " + phoneNumber);
    
    // ... existing code ...
    
    call.enqueue(new Callback<OtpResponse>() {
        @Override
        public void onResponse(Call<OtpResponse> call, Response<OtpResponse> response) {
            Log.d(TAG, "OTP Verify Response Code: " + response.code());
            
            if (response.isSuccessful() && response.body() != null) {
                OtpResponse otpResponse = response.body();
                Log.d(TAG, "OTP Verify Response: success=" + otpResponse.isSuccess() + 
                           ", message=" + otpResponse.getMessage());
                
                if (otpResponse.isSuccess()) {
                    // Xác thực thành công
                } else {
                    // Hiển thị message từ server
                    String errorMsg = otpResponse.getMessage() != null ? 
                                    otpResponse.getMessage() : "Mã OTP không đúng";
                    Toast.makeText(OtpVerificationActivity.this, 
                            errorMsg + ". Vui lòng nhập lại.", Toast.LENGTH_LONG).show();
                }
            } else {
                // Log error body để debug
                Log.e(TAG, "OTP Verify failed: " + response.code());
                try {
                    if (response.errorBody() != null) {
                        String errorBody = response.errorBody().string();
                        Log.e(TAG, "Error body: " + errorBody);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error reading error body", e);
                }
            }
        }
    });
}
```

#### d. Cải Thiện resendOtp()
```java
private void resendOtp() {
    Log.d(TAG, "Resending OTP for flow: " + fromActivity);
    
    // Xóa các ô input TRƯỚC khi gửi
    clearOtpInputs();
    
    if ("forgot_password".equals(fromActivity) || 
        "movie_booking".equals(fromActivity) || 
        "login_verification".equals(fromActivity)) {
        // Gửi lại OTP với Goixe247
        sendOtpWithGoixe();
    } else {
        if (esmsConfig.isConfigured()) {
            sendOtp();
        } else {
            Toast.makeText(this, "Đã gửi lại OTP đến " + phoneNumber, Toast.LENGTH_SHORT).show();
        }
    }
    
    // Cancel timer cũ trước khi start timer mới
    if (countDownTimer != null) {
        countDownTimer.cancel();
    }
    startTimer();
    etOtp1.requestFocus();
}
```

## Kiểm Tra Lỗi

### 1. Kiểm Tra Logcat
Khi test, kiểm tra log để xem:
```
D/OtpVerification: Sending OTP to: 0123456789
D/OtpVerification: OTP Request Response Code: 200
D/OtpVerification: OTP Response: success=true, message=OTP sent successfully

D/OtpVerification: Verifying OTP: 123456 for phone: 0123456789
D/OtpVerification: OTP Verify Response Code: 200
D/OtpVerification: OTP Verify Response: success=true, message=OTP verified
```

### 2. Nếu Vẫn Lỗi
Kiểm tra:
- ✅ Response code (200 = OK, 400 = Bad Request, 500 = Server Error)
- ✅ Response body (success, message)
- ✅ Error body nếu có
- ✅ Network connection
- ✅ API credentials (user_id, api_key)

## API Goixe247 Đúng

### Request OTP
```
POST https://otp.goixe247.com/request_otp.php
Content-Type: application/x-www-form-urlencoded

user_id=13
api_key=328945bfca039d9663890e71f4d9e2203669dd1e49fd3cb9a44fa86a48d915da
recipient_phone=0123456789
```

### Verify OTP
```
POST https://otp.goixe247.com/verify_otp.php
Content-Type: application/x-www-form-urlencoded

user_id=13
api_key=328945bfca039d9663890e71f4d9e2203669dd1e49fd3cb9a44fa86a48d915da
recipient_phone=0123456789
otp_code=123456
```

### Response Format
```json
{
  "success": true,
  "message": "OTP sent successfully"
}
```

hoặc

```json
{
  "success": false,
  "message": "Invalid OTP code"
}
```

## Test Cases

### 1. Test Gửi OTP
1. Nhập số điện thoại
2. Click "Gửi OTP"
3. Kiểm tra log:
   - ✅ "Sending OTP to: [phone]"
   - ✅ "OTP Request Response Code: 200"
   - ✅ "OTP Response: success=true"
4. Kiểm tra SMS nhận được OTP

### 2. Test Xác Nhận OTP Đúng
1. Nhập OTP từ SMS
2. Click "Xác nhận"
3. Kiểm tra log:
   - ✅ "Verifying OTP: [otp] for phone: [phone]"
   - ✅ "OTP Verify Response Code: 200"
   - ✅ "OTP Verify Response: success=true"
4. Chuyển sang màn hình ResetPassword

### 3. Test Xác Nhận OTP Sai
1. Nhập OTP sai (ví dụ: 111111)
2. Click "Xác nhận"
3. Kiểm tra:
   - ✅ Thông báo lỗi hiển thị
   - ✅ Các ô OTP bị xóa
   - ✅ Focus vào ô đầu tiên
   - ✅ KHÔNG chuyển màn hình

### 4. Test Gửi Lại OTP
1. Click "Gửi lại OTP"
2. Kiểm tra:
   - ✅ Log "Resending OTP for flow: forgot_password"
   - ✅ Các ô OTP bị xóa
   - ✅ Timer reset về 60s
   - ✅ Nhận SMS OTP mới
   - ✅ Focus vào ô đầu tiên

## Kết Quả

### Trước Khi Sửa
- ❌ Không gửi được OTP (sai endpoint/field names)
- ❌ Không xác nhận được OTP (sai endpoint/field names)
- ❌ Không có log để debug
- ❌ Không hiển thị message lỗi từ server

### Sau Khi Sửa
- ✅ Gửi OTP thành công
- ✅ Xác nhận OTP thành công
- ✅ Gửi lại OTP hoạt động đúng
- ✅ Có log chi tiết để debug
- ✅ Hiển thị message lỗi rõ ràng từ server
- ✅ UX tốt hơn (xóa input, focus, timer)

## Lưu Ý Quan Trọng

### 1. Không Xóa Interface Cũ
File `com.example.mobilebanking.api.OtpApiService` vẫn tồn tại nhưng không được sử dụng. Có thể:
- Giữ lại nếu có kế hoạch sử dụng API khác
- Xóa đi để tránh nhầm lẫn
- Đổi tên thành `OtpApiServiceV2` để phân biệt

### 2. Kiểm Tra Các Activity Khác
Các activity khác sử dụng OTP cũng cần kiểm tra:
- ✅ ForgotPasswordActivity - đã đúng
- ✅ ForgotOtpVerificationActivity - đã đúng
- ⚠️ Các activity khác cần kiểm tra

### 3. Production Considerations
- Không log sensitive data (OTP code) trong production
- Sử dụng ProGuard để obfuscate API keys
- Implement rate limiting để tránh spam OTP
- Thêm captcha nếu cần
