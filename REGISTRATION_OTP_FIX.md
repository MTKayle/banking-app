# Sửa Lỗi Xác Thực OTP Trong Đăng Ký

## Vấn Đề
Khi người dùng nhập số điện thoại đã tồn tại trong Step 1 của đăng ký, hệ thống vẫn chuyển sang Step 2 mặc dù đã hiển thị lỗi. Ngoài ra, luồng đăng ký thiếu bước xác thực OTP giữa Step 1 và Step 2.

## Luồng Yêu Cầu
1. Người dùng nhập thông tin cơ bản (số điện thoại, email, mật khẩu) → Click "Tiếp tục"
2. Kiểm tra số điện thoại có tồn tại không (API: `api/auth/check-phone-exists?phone=`)
   - **Nếu tồn tại**: Hiển thị lỗi, KHÔNG cho phép tiếp tục
   - **Nếu không tồn tại**: Chuyển sang trang xác thực OTP
3. Gửi OTP đến số điện thoại đã nhập
4. Người dùng nhập OTP
   - **Nếu OTP đúng**: Chuyển sang Step 2 (Quét QR căn cước)
   - **Nếu OTP sai**: Xóa input, yêu cầu nhập lại, KHÔNG cho phép chuyển sang Step 2

## Các Thay Đổi

### 1. Step1BasicInfoFragment.java
**File**: `FrontEnd/banking-app/app/src/main/java/com/example/mobilebanking/fragments/Step1BasicInfoFragment.java`

#### Sửa lỗi trong `checkPhoneExistsAndContinue()`:
**VẤN ĐỀ**: Code cũ vẫn gọi `saveDataAndContinue()` khi API trả về lỗi 404, dẫn đến việc chuyển sang trang OTP ngay cả khi số điện thoại đã tồn tại.

**TRƯỚC** (Code lỗi):
```java
if (response.isSuccessful() && response.body() != null) {
    FeatureStatusResponse result = response.body();
    
    if (result.isEnabled()) {
        tilPhone.setError("Số điện thoại này đã được đăng ký");
        Toast.makeText(getContext(), "Số điện thoại đã tồn tại. Vui lòng sử dụng số khác.", Toast.LENGTH_LONG).show();
    } else {
        saveDataAndContinue(phone, email, password, confirmPassword);
    }
} else {
    // Lỗi từ server
    if (response.code() == 404) {
        // BUG: Vẫn cho phép tiếp tục khi lỗi 404
        saveDataAndContinue(phone, email, password, confirmPassword);
    } else {
        Toast.makeText(getContext(), "Không thể kiểm tra số điện thoại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
    }
}
```

**SAU** (Code đã sửa):
```java
if (response.isSuccessful() && response.body() != null) {
    FeatureStatusResponse result = response.body();
    
    // Nếu enabled = true → Số điện thoại đã tồn tại → KHÔNG cho phép tiếp tục
    if (result.isEnabled()) {
        tilPhone.setError("Số điện thoại này đã được đăng ký");
        Toast.makeText(getContext(), "Số điện thoại đã tồn tại. Vui lòng sử dụng số khác.", Toast.LENGTH_LONG).show();
        // KHÔNG gọi saveDataAndContinue() - dừng lại ở đây
    } else {
        // Số điện thoại chưa tồn tại (enabled = false) → OK, tiếp tục
        saveDataAndContinue(phone, email, password, confirmPassword);
    }
} else {
    // Lỗi từ server - KHÔNG cho phép tiếp tục
    Log.e(TAG, "Check phone exists failed: " + response.code());
    Toast.makeText(getContext(), "Không thể kiểm tra số điện thoại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
}
```

**THAY ĐỔI CHÍNH**:
- ✅ Xóa logic xử lý 404 cho phép tiếp tục
- ✅ Chỉ cho phép tiếp tục khi `enabled = false` (số điện thoại chưa tồn tại)
- ✅ Mọi lỗi API đều KHÔNG cho phép tiếp tục

#### Thay đổi trong `saveDataAndContinue()`:
- **Trước**: Gọi trực tiếp `goToNextStep()` để chuyển sang Step 2
- **Sau**: Mở `OtpVerificationActivity` với flow="register" để xác thực OTP

```java
private void saveDataAndContinue(String phone, String email, String password, String confirmPassword) {
    // Lưu dữ liệu
    registrationData.setPhoneNumber(phone);
    registrationData.setEmail(email);
    registrationData.setPassword(password);
    registrationData.setConfirmPassword(confirmPassword);
    
    // Chuyển sang trang xác thực OTP
    Intent intent = new Intent(getActivity(), OtpVerificationActivity.class);
    intent.putExtra("phone", phone);
    intent.putExtra("flow", "register");
    startActivityForResult(intent, 100); // Request code 100
}
```

#### Thêm `onActivityResult()`:
Xử lý kết quả từ OTP verification:
- **RESULT_OK**: OTP đúng → Chuyển sang Step 2
- **Khác**: OTP sai hoặc bị hủy → Hiển thị thông báo lỗi

```java
@Override
public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    
    if (requestCode == 100) { // Registration OTP verification
        if (resultCode == Activity.RESULT_OK) {
            // OTP verified successfully, navigate to Step 2
            ((MainRegistrationActivity) getActivity()).goToNextStep();
        } else {
            // OTP verification failed
            Toast.makeText(getContext(), "Xác thực OTP thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
        }
    }
}
```

### 2. OtpVerificationActivity.java
**File**: `FrontEnd/banking-app/app/src/main/java/com/example/mobilebanking/activities/OtpVerificationActivity.java`

#### Thay đổi trong `onCreate()`:
Thêm xử lý riêng cho flow "register" - sử dụng eSMS API:

```java
if ("register".equals(fromActivity)) {
    // Luồng đăng ký - dùng eSMS
    if (esmsConfig.isConfigured()) {
        sendOtp();
    } else {
        showApiKeyConfigDialog();
    }
}
```

#### Thay đổi trong `verifyOtpWithESms()`:
Khi OTP đúng và flow là "register", trả về RESULT_OK thay vì chuyển sang LoginActivity:

```java
if ("register".equals(fromActivity)) {
    // Registration OTP verified - return to Step1BasicInfoFragment
    setResult(RESULT_OK);
    finish();
}
```

#### Thay đổi trong `resendOtp()`:
Thêm xử lý cho flow "register" - sử dụng eSMS:

```java
if ("forgot_password".equals(fromActivity) || "movie_booking".equals(fromActivity) || "login_verification".equals(fromActivity)) {
    sendOtpWithGoixe();
} else {
    // register flow và các flow khác dùng eSMS
    if (esmsConfig.isConfigured()) {
        sendOtp();
    }
}
```

## Các Flow OTP Hiện Tại

### 1. Register (Đăng ký)
- **API**: eSMS
- **Luồng**: Step1BasicInfoFragment → OtpVerificationActivity → Step2QRScanFragment
- **Khi OTP đúng**: Trả về RESULT_OK, Step1 chuyển sang Step 2

### 2. Forgot Password (Quên mật khẩu)
- **API**: Goixe247
- **Luồng**: ForgotPasswordActivity → OtpVerificationActivity → ResetPasswordActivity
- **Khi OTP đúng**: Chuyển sang ResetPasswordActivity

### 3. Movie Booking (Đặt vé phim)
- **API**: Goixe247
- **Luồng**: MoviePaymentActivity → OtpVerificationActivity → API booking → MovieTicketSuccessActivity
- **Khi OTP đúng**: Gọi API đặt vé, sau đó chuyển sang màn hình thành công

### 4. Login Verification (Xác thực đăng nhập)
- **API**: Goixe247
- **Luồng**: LoginActivity → OtpVerificationActivity → UiHomeActivity
- **Khi OTP đúng**: Gọi API đăng nhập, sau đó chuyển sang trang chủ

## Kiểm Tra Lỗi

### Lỗi đã được sửa:
✅ Số điện thoại đã tồn tại vẫn chuyển sang Step 2 → **ĐÃ SỬA**
✅ Số điện thoại đã tồn tại vẫn chuyển sang trang OTP → **ĐÃ SỬA**
✅ Thiếu bước xác thực OTP trong đăng ký → **ĐÃ SỬA**
✅ OTP sai vẫn cho phép chuyển sang Step 2 → **ĐÃ SỬA**
✅ Lỗi API 404 vẫn cho phép tiếp tục → **ĐÃ SỬA**

### Luồng hiện tại:
✅ Kiểm tra số điện thoại tồn tại
   - **Nếu tồn tại (enabled = true)**: Hiển thị lỗi, KHÔNG chuyển tiếp, KHÔNG mở OTP
   - **Nếu không tồn tại (enabled = false)**: Chuyển sang OTP verification
✅ Nếu API lỗi (404, 500, etc.): Hiển thị lỗi, KHÔNG cho phép tiếp tục
✅ OTP đúng → Chuyển sang Step 2
✅ OTP sai → Xóa input, yêu cầu nhập lại, KHÔNG chuyển sang Step 2

## Hướng Dẫn Test

### Test Case 1: Số điện thoại đã tồn tại
1. Mở app → Click "Đăng ký"
2. Nhập số điện thoại đã tồn tại (ví dụ: 0123456789)
3. Nhập email, mật khẩu hợp lệ
4. Click "Tiếp tục"
5. **Kết quả mong đợi**: 
   - Hiển thị lỗi "Số điện thoại này đã được đăng ký" (dưới ô input)
   - Toast: "Số điện thoại đã tồn tại. Vui lòng sử dụng số khác."
   - KHÔNG chuyển sang trang OTP
   - KHÔNG chuyển sang Step 2
   - Người dùng vẫn ở Step 1 để nhập lại số điện thoại khác

### Test Case 2: Số điện thoại chưa tồn tại - OTP đúng
1. Mở app → Click "Đăng ký"
2. Nhập số điện thoại chưa tồn tại (ví dụ: 0987654321)
3. Nhập email, mật khẩu hợp lệ
4. Click "Tiếp tục"
5. **Kết quả mong đợi**: Chuyển sang trang OTP verification
6. Nhập OTP đúng (123456 cho test)
7. Click "Xác nhận"
8. **Kết quả mong đợi**: Hiển thị "Xác thực OTP thành công!", chuyển sang Step 2 (Quét QR)

### Test Case 3: Số điện thoại chưa tồn tại - OTP sai
1. Mở app → Click "Đăng ký"
2. Nhập số điện thoại chưa tồn tại (ví dụ: 0987654321)
3. Nhập email, mật khẩu hợp lệ
4. Click "Tiếp tục"
5. **Kết quả mong đợi**: Chuyển sang trang OTP verification
6. Nhập OTP sai (ví dụ: 111111)
7. Click "Xác nhận"
8. **Kết quả mong đợi**: Hiển thị "Mã OTP không đúng hoặc đã hết hạn", xóa input, KHÔNG chuyển sang Step 2
9. Nhập lại OTP đúng (123456)
10. Click "Xác nhận"
11. **Kết quả mong đợi**: Chuyển sang Step 2

### Test Case 4: Gửi lại OTP
1. Thực hiện Test Case 2 đến bước 5
2. Đợi 60 giây
3. Click "Gửi lại"
4. **Kết quả mong đợi**: Xóa input, gửi lại OTP, reset timer
5. Nhập OTP mới
6. **Kết quả mong đợi**: Xác thực thành công, chuyển sang Step 2

## Ghi Chú
- OTP test mặc định: **123456** (cho môi trường test)
- Nếu cấu hình eSMS API, OTP thật sẽ được gửi qua SMS
- Các flow khác (forgot password, movie booking, login verification) sử dụng Goixe247 API
- Flow đăng ký sử dụng eSMS API
