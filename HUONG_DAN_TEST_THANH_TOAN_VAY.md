# Hướng Dẫn Test Thanh Toán Kỳ Vay - Đã Sửa Lỗi

## ✅ Đã Cài Đặt App Mới

App đã được build và cài đặt thành công với các fix:
- ✅ Lấy phone từ `DataManager.getUserPhone()` và fallback sang `getLastUsername()`
- ✅ Xử lý đúng flow MORTGAGE_PAYMENT trong OtpVerificationActivity
- ✅ Gọi API thanh toán sau khi xác thực OTP thành công

## Cách Test Nhanh

### Bước 1: Chạy logcat để theo dõi
```bash
test_mortgage_payment.bat
```

Hoặc:
```bash
adb logcat | findstr /i "MortgagePayment OtpVerification"
```

### Bước 2: Test trên điện thoại

1. **Mở app** (đã được cài đặt mới)

2. **Đăng nhập** với tài khoản có khoản vay
   - Số điện thoại: `0123456789` (hoặc tài khoản của bạn)
   - Mật khẩu: mật khẩu của bạn

3. **Vào tab "Tài khoản vay"**
   - Từ màn hình Home
   - Vuốt sang tab "Tài khoản vay"

4. **Chọn một khoản vay**
   - Nhấn vào khoản vay trong danh sách
   - Xem chi tiết

5. **Thanh toán kỳ hiện tại**
   - Nhấn nút "Thanh toán kỳ hiện tại"
   - Kiểm tra thông tin xác nhận

6. **Xác nhận thanh toán**
   - Nhấn "Xác nhận"
   - ✅ Chuyển sang màn hình OTP (nếu < 10 triệu)
   - ✅ Hoặc xác thực khuôn mặt trước (nếu > 10 triệu)

7. **Nhập OTP test**
   - Nhập: `123456`
   - Nhấn "Xác thực"

8. **Kiểm tra kết quả**
   - ✅ Chuyển sang màn hình thành công
   - ✅ Hiển thị thông tin thanh toán

## Log Mong Đợi

Khi test thành công, bạn sẽ thấy trong logcat:

```
D/MortgagePayment: Phone for OTP: 0123456789
D/OtpVerification: Step 4 - FROM_ACTIVITY key value: MORTGAGE_PAYMENT
D/OtpVerification: Step 5 - Updated fromActivity from FROM_ACTIVITY: MORTGAGE_PAYMENT
D/OtpVerification: Step 6 - PHONE_NUMBER key value: 0123456789
D/OtpVerification: Step 7 - Updated phoneNumber from PHONE_NUMBER: 0123456789
D/OtpVerification: FINAL - OTP Verification - fromActivity: MORTGAGE_PAYMENT, phone: 0123456789
D/OtpVerification: Sending OTP to: 0123456789
D/OtpVerification: Verifying OTP: 123456 for phone: 0123456789
D/OtpVerification: Test OTP detected: 123456 - bypassing verification
D/OtpVerification: handleOtpSuccess - fromActivity: MORTGAGE_PAYMENT
D/OtpVerification: MORTGAGE_PAYMENT - Processing payment
```

## So Sánh Log Cũ vs Mới

### ❌ Log Cũ (Lỗi)
```
D/OtpVerification: Verifying OTP: 123456 for phone: null
D/OtpVerification: handleOtpSuccess - fromActivity: transfer
W/OtpVerification: Unknown fromActivity: transfer
```

### ✅ Log Mới (Đúng)
```
D/MortgagePayment: Phone for OTP: 0123456789
D/OtpVerification: FINAL - OTP Verification - fromActivity: MORTGAGE_PAYMENT, phone: 0123456789
D/OtpVerification: MORTGAGE_PAYMENT - Processing payment
```

## Nếu Vẫn Thấy Log Cũ

Có nghĩa là app cũ vẫn đang chạy. Hãy:

1. **Gỡ app cũ hoàn toàn**
```bash
adb uninstall com.example.mobilebanking
```

2. **Build và cài lại**
```bash
rebuild_and_install.bat
```

Hoặc thủ công:
```bash
cd FrontEnd\banking-app
gradlew.bat assembleDebug
adb install app\build\outputs\apk\debug\app-debug.apk
```

## API Request

Sau khi nhập OTP thành công, app sẽ gọi:

```http
POST http://localhost:8089/api/mortgage/payment/current
Content-Type: application/json
Authorization: Bearer <token>

{
  "mortgageId": 1,
  "paymentAmount": 5000000.0,
  "paymentAccount": "1234567890"
}
```

## Files Đã Sửa

1. ✅ `MortgagePaymentConfirmActivity.java`
   - Method `sendOtpAndVerify()` - Thêm fallback lấy phone

2. ✅ `FaceVerificationTransactionActivity.java`
   - Flow MORTGAGE_PAYMENT - Thêm fallback lấy phone

3. ✅ `OtpVerificationActivity.java`
   - Method `processMortgagePayment()` - Đã có sẵn, gọi API và chuyển success

## Lưu Ý

- Mã OTP test: `123456` (luôn được chấp nhận)
- Backend phải chạy ở `http://localhost:8089`
- Cần có khoản vay trong database
- Tài khoản thanh toán phải có đủ số dư

## Nếu Gặp Lỗi

### Lỗi: "Số điện thoại không hợp lệ"
- Đăng xuất và đăng nhập lại
- Kiểm tra log xem phone có được lưu không

### Lỗi: Device offline
```bash
adb kill-server
adb start-server
adb devices
```

### Lỗi: Build failed
```bash
cd FrontEnd\banking-app
gradlew.bat clean
gradlew.bat assembleDebug
```
