# Cập Nhật Luồng Quên Mật Khẩu

## Tổng Quan
Đã cập nhật luồng quên mật khẩu để sử dụng `OtpVerificationActivity` thay vì `ForgotOtpVerificationActivity`, với logic gửi và xác thực OTP giống như `ForgotOtpVerificationActivity` (sử dụng Goixe247 API).

## Luồng Mới

### 1. LoginActivity
```
User nhấn "Quên mật khẩu"
↓
Chuyển đến ForgotPasswordActivity
```

### 2. ForgotPasswordActivity
```
User nhập số điện thoại
↓
Validate format (^(0|84)[2-9][0-9]{8}$)
↓
Gọi Goixe247 API để gửi OTP
↓
Thành công → Chuyển đến OtpVerificationActivity
  - Extra: phone = số điện thoại
  - Extra: from = "forgot_password"
```

### 3. OtpVerificationActivity (Cập nhật)
```
Nhận intent với from = "forgot_password"
↓
Hiển thị màn hình nhập OTP (activity_otp_verification.xml)
↓
User nhập 6 số OTP
↓
Gọi Goixe247 API để xác thực OTP
↓
Thành công → Chuyển đến ResetPasswordActivity
Thất bại → Xóa OTP và yêu cầu nhập lại
```

### 4. ResetPasswordActivity
```
User nhập mật khẩu mới
↓
Xác nhận mật khẩu
↓
Lưu mật khẩu mới (backend)
↓
Quay về LoginActivity
```

## Các File Đã Thay Đổi

### 1. ForgotPasswordActivity.java
**Thay đổi:**
- Chuyển từ `ForgotOtpVerificationActivity` sang `OtpVerificationActivity`
- Thêm extra `from = "forgot_password"` để phân biệt luồng

**Code:**
```java
// Sau khi gửi OTP thành công, chuyển sang màn hình nhập OTP
Intent intent = new Intent(ForgotPasswordActivity.this, OtpVerificationActivity.class);
intent.putExtra("phone", phone);
intent.putExtra("from", "forgot_password");
startActivity(intent);
```

### 2. OtpVerificationActivity.java
**Thay đổi:**
- Thêm hỗ trợ Goixe247 API cho luồng quên mật khẩu
- Phân biệt 2 luồng: `register` (eSMS) và `forgot_password` (Goixe247)
- Thêm methods:
  - `initGoixeService()` - Khởi tạo Retrofit cho Goixe247
  - `verifyOtpWithGoixe(String otp)` - Xác thực OTP với Goixe247
  - `resendOtpWithGoixe()` - Gửi lại OTP với Goixe247
  - `verifyOtpWithESms(String otp)` - Xác thực OTP với eSMS (luồng đăng ký)
  - `clearOtpInputs()` - Xóa tất cả các ô nhập OTP

**Luồng xử lý:**
```java
onCreate() {
    if ("forgot_password".equals(fromActivity)) {
        // Luồng quên mật khẩu - đã gửi OTP từ ForgotPasswordActivity
        Toast.makeText(this, "Mã OTP đã được gửi đến " + phoneNumber, Toast.LENGTH_SHORT).show();
    } else {
        // Luồng đăng ký - dùng eSMS
        if (esmsConfig.isConfigured()) {
            sendOtp();
        } else {
            showApiKeyConfigDialog();
        }
    }
}

handleOtpVerification() {
    if ("forgot_password".equals(fromActivity)) {
        verifyOtpWithGoixe(otp);
    } else {
        verifyOtpWithESms(otp);
    }
}

resendOtp() {
    if ("forgot_password".equals(fromActivity)) {
        resendOtpWithGoixe();
    } else {
        if (esmsConfig.isConfigured()) {
            sendOtp();
        }
    }
}
```

## API Goixe247

### Cấu hình
```java
private static final String GOIXE_BASE_URL = "https://otp.goixe247.com/";
private static final String GOIXE_USER_ID = "13";
private static final String GOIXE_API_KEY = "328945bfca039d9663890e71f4d9e2203669dd1e49fd3cb9a44fa86a48d915da";
```

### Gửi OTP
```java
Call<OtpResponse> call = otpApiService.requestOtp(
    GOIXE_USER_ID,
    GOIXE_API_KEY,
    phoneNumber
);
```

### Xác thực OTP
```java
Call<OtpResponse> call = otpApiService.verifyOtp(
    GOIXE_USER_ID,
    GOIXE_API_KEY,
    phoneNumber,
    otp
);
```

## Layout

### activity_otp_verification.xml
Được sử dụng cho cả 2 luồng:
- Đăng ký (register) - eSMS
- Quên mật khẩu (forgot_password) - Goixe247

**Các thành phần:**
- 6 ô nhập OTP (et_otp_1 đến et_otp_6)
- TextView hiển thị số điện thoại (tv_phone)
- TextView hiển thị đếm ngược (tv_timer)
- Button xác thực (btn_verify)
- Button gửi lại OTP (btn_resend)
- ProgressBar (progress_bar)

## So Sánh 2 Luồng

| Tính năng | Đăng ký (register) | Quên mật khẩu (forgot_password) |
|-----------|-------------------|--------------------------------|
| API | eSMS | Goixe247 |
| Gửi OTP | Tự động khi vào màn hình | Đã gửi từ ForgotPasswordActivity |
| Xác thực OTP | eSMS hoặc fake "123456" | Goixe247 API |
| Gửi lại OTP | eSMS | Goixe247 API |
| Sau khi thành công | LoginActivity | ResetPasswordActivity |

## Ưu Điểm

### 1. Tái sử dụng code
- Chỉ cần 1 Activity (OtpVerificationActivity) cho cả 2 luồng
- Chỉ cần 1 layout (activity_otp_verification.xml)
- Giảm duplicate code

### 2. Dễ bảo trì
- Logic tập trung ở 1 nơi
- Dễ debug và fix bug
- Dễ thêm tính năng mới

### 3. Linh hoạt
- Có thể dễ dàng thêm luồng mới (ví dụ: xác thực giao dịch)
- Có thể chuyển đổi giữa các API khác nhau

## Test

### Test Case 1: Quên mật khẩu - Gửi OTP
1. Mở app → Nhấn "Quên mật khẩu"
2. Nhập số điện thoại: `0901234567`
3. Nhấn "Gửi OTP"
4. **Kết quả mong đợi:**
   - Gọi Goixe247 API thành công
   - Chuyển đến OtpVerificationActivity
   - Hiển thị "Mã OTP đã được gửi đến 0901234567"

### Test Case 2: Quên mật khẩu - Xác thực OTP
1. Tiếp tục từ Test Case 1
2. Nhập mã OTP nhận được từ SMS
3. **Kết quả mong đợi:**
   - Gọi Goixe247 verify API
   - Nếu đúng: Chuyển đến ResetPasswordActivity
   - Nếu sai: Hiển thị lỗi và xóa OTP

### Test Case 3: Quên mật khẩu - Gửi lại OTP
1. Tiếp tục từ Test Case 1
2. Đợi 60 giây
3. Nhấn "Gửi lại OTP"
4. **Kết quả mong đợi:**
   - Gọi Goixe247 API lại
   - Hiển thị "Đã gửi lại mã OTP thành công!"
   - Đếm ngược 60 giây lại

### Test Case 4: Đăng ký - Vẫn hoạt động bình thường
1. Mở app → Nhấn "Đăng ký"
2. Nhập thông tin → Nhấn "Đăng ký"
3. Chuyển đến OtpVerificationActivity
4. **Kết quả mong đợi:**
   - Gọi eSMS API (nếu đã cấu hình)
   - Hoặc hiển thị dialog cấu hình API key
   - Xác thực OTP với eSMS hoặc fake "123456"

## Lưu Ý

### 1. API Key
- API key Goixe247 đang được hardcode trong code
- **Khuyến nghị:** Nên lưu trong file config hoặc backend

### 2. Bảo mật
- Không nên lưu OTP trong SharedPreferences
- Nên xác thực OTP qua backend

### 3. Timeout
- OTP có thời gian hết hạn (thường 5-10 phút)
- Nên hiển thị thời gian hết hạn cho user

### 4. Rate limiting
- Nên giới hạn số lần gửi OTP (ví dụ: 3 lần/ngày)
- Nên giới hạn số lần nhập sai OTP (ví dụ: 5 lần)

## Troubleshooting

### Vấn đề: Không nhận được OTP
- Kiểm tra số điện thoại có đúng format không
- Kiểm tra API key Goixe247 có đúng không
- Kiểm tra kết nối internet
- Xem log trong Logcat

### Vấn đề: OTP luôn báo sai
- Kiểm tra OTP có đúng 6 số không
- Kiểm tra OTP có hết hạn không
- Kiểm tra API verify có hoạt động không
- Xem response từ Goixe247 API

### Vấn đề: Không chuyển được sang ResetPasswordActivity
- Kiểm tra ResetPasswordActivity có tồn tại không
- Kiểm tra AndroidManifest.xml đã khai báo chưa
- Kiểm tra intent có đúng không

## Kết Luận

Đã cập nhật thành công luồng quên mật khẩu:
- ✅ Sử dụng OtpVerificationActivity thay vì ForgotOtpVerificationActivity
- ✅ Tích hợp Goixe247 API cho gửi và xác thực OTP
- ✅ Tái sử dụng layout activity_otp_verification.xml
- ✅ Phân biệt 2 luồng: register (eSMS) và forgot_password (Goixe247)
- ✅ Dễ bảo trì và mở rộng

Luồng quên mật khẩu giờ đây hoạt động giống như ForgotOtpVerificationActivity nhưng sử dụng chung code với OtpVerificationActivity!
