# Sửa Lỗi Chức Năng Quên Mật Khẩu - OTP Verification

## Vấn Đề
Trước đây, chức năng quên mật khẩu sử dụng 2 activity riêng biệt:
- `ForgotPasswordActivity` → `ForgotOtpVerificationActivity` → `ResetPasswordActivity`

Điều này gây ra sự phức tạp không cần thiết và không nhất quán với các flow khác trong app.

## Giải Pháp
Đã sửa lại để sử dụng `OtpVerificationActivity` chung cho tất cả các flow, bao gồm:
- Register (eSMS)
- Forgot Password (Goixe247)
- Movie Booking (Goixe247)
- Login Verification (Goixe247)

## Các Thay Đổi

### 1. ForgotPasswordActivity.java
**Thay đổi:** Chuyển sang `OtpVerificationActivity` thay vì `ForgotOtpVerificationActivity`

```java
// Trước:
Intent intent = new Intent(ForgotPasswordActivity.this, ForgotOtpVerificationActivity.class);
intent.putExtra("phone", phone);
startActivity(intent);

// Sau:
Intent intent = new Intent(ForgotPasswordActivity.this, OtpVerificationActivity.class);
intent.putExtra("phone", phone);
intent.putExtra("from", "forgot_password");
startActivity(intent);
```

### 2. OtpVerificationActivity.java
**Các cải tiến:**

#### a. Logic xử lý OTP sai
- Khi OTP sai, xóa tất cả các ô input
- Focus vào ô đầu tiên để người dùng nhập lại
- Hiển thị thông báo rõ ràng

```java
if (otpResponse.isSuccess()) {
    // OTP đúng → chuyển sang ResetPasswordActivity
    Intent intent = new Intent(OtpVerificationActivity.this, ResetPasswordActivity.class);
    intent.putExtra("phone", phoneNumber);
    startActivity(intent);
    finish();
} else {
    // OTP sai → xóa input và yêu cầu nhập lại
    Toast.makeText(OtpVerificationActivity.this, 
            "Mã OTP không đúng. Vui lòng nhập lại.", Toast.LENGTH_LONG).show();
    clearOtpInputs();
    etOtp1.requestFocus();
}
```

#### b. Cải thiện chức năng Resend OTP
- Xóa các ô input khi gửi lại OTP
- Reset timer đếm ngược
- Focus vào ô đầu tiên

```java
private void resendOtp() {
    if ("forgot_password".equals(fromActivity) || "movie_booking".equals(fromActivity) || "login_verification".equals(fromActivity)) {
        // Gửi lại OTP với Goixe247
        sendOtpWithGoixe();
    } else {
        if (esmsConfig.isConfigured()) {
            sendOtp();
        } else {
            Toast.makeText(this, "Đã gửi lại OTP đến " + phoneNumber, Toast.LENGTH_SHORT).show();
        }
    }
    // Xóa các ô input và reset timer
    clearOtpInputs();
    etOtp1.requestFocus();
    startTimer();
}
```

#### c. Thêm logging cho debug
```java
@Override
public void onFailure(Call<OtpResponse> call, Throwable t) {
    // ... existing code ...
    Log.e(TAG, "OTP verification failed", t);
}
```

## Flow Hoàn Chỉnh

### Luồng Quên Mật Khẩu
1. **LoginActivity** → Click "Quên mật khẩu"
2. **ForgotPasswordActivity** → Nhập số điện thoại → Gửi OTP (Goixe247)
3. **OtpVerificationActivity** (activity_otp_verification.xml)
   - Nhập 6 số OTP
   - Nếu đúng → Chuyển sang ResetPasswordActivity
   - Nếu sai → Xóa input, yêu cầu nhập lại
   - Có thể gửi lại OTP
4. **ResetPasswordActivity** → Nhập mật khẩu mới → Đổi mật khẩu
5. **LoginActivity** → Đăng nhập với mật khẩu mới

## Layout Sử Dụng
- `activity_otp_verification.xml` - Layout chung cho tất cả các flow OTP
- Có đầy đủ các view cần thiết:
  - `tv_phone` - Hiển thị số điện thoại
  - `et_otp_1` đến `et_otp_6` - 6 ô nhập OTP
  - `btn_verify` - Nút xác nhận
  - `btn_resend` - Nút gửi lại OTP
  - `tv_timer` - Đếm ngược thời gian
  - `progress_bar` - Loading indicator

## API Sử Dụng
- **Gửi OTP:** `https://otp.goixe247.com/request_otp.php`
- **Xác thực OTP:** `https://otp.goixe247.com/verify_otp.php`
- **Đổi mật khẩu:** Backend API `/api/password/change`

## Lưu Ý
- `ForgotOtpVerificationActivity` vẫn tồn tại nhưng không được sử dụng nữa
- Có thể xóa file này trong tương lai để giữ code sạch
- Tất cả các flow OTP giờ đây đều sử dụng `OtpVerificationActivity` thống nhất

## Test Cases
1. ✅ Nhập số điện thoại hợp lệ → Nhận OTP
2. ✅ Nhập OTP đúng → Chuyển sang đổi mật khẩu
3. ✅ Nhập OTP sai → Xóa input, yêu cầu nhập lại
4. ✅ Gửi lại OTP → Nhận OTP mới, xóa input cũ
5. ✅ Đổi mật khẩu thành công → Về màn hình đăng nhập
