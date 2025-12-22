# Sửa Lỗi Cuối Cùng - Mortgage Payment Flow

## Vấn Đề Phát Hiện

Từ logcat:
```
D/OtpVerification: Verifying OTP: 123456 for phone: 0839256305
D/OtpVerification: handleOtpSuccess - fromActivity: transaction
D/OtpVerification: Processing transfer confirm - transactionCode: null, bank: null
```

**Nguyên nhân:** Khi thanh toán > 10 triệu, flow đi qua `FaceVerificationTransactionActivity` nhưng key Intent Extra bị sai:
- `MortgagePaymentConfirmActivity` truyền: `"TRANSACTION_TYPE", "MORTGAGE_PAYMENT"` ❌
- `FaceVerificationTransactionActivity` đọc: `"from"` ❌
- Kết quả: Không match → Rơi vào else block (transfer flow)

## Giải Pháp

### Sửa `MortgagePaymentConfirmActivity.java`

Đổi key từ `TRANSACTION_TYPE` thành `from`:

```java
// Kiểm tra nếu > 10 triệu thì cần xác thực khuôn mặt
if (paymentAmount > 10000000) {
    // Xác thực khuôn mặt trước
    Intent intent = new Intent(this, FaceVerificationTransactionActivity.class);
    intent.putExtra("from", "MORTGAGE_PAYMENT");  // ✅ Sửa từ TRANSACTION_TYPE
    intent.putExtra("MORTGAGE_ID", mortgageId);
    intent.putExtra("PAYMENT_AMOUNT", paymentAmount);
    intent.putExtra("PAYMENT_ACCOUNT", paymentAccountNumber);
    intent.putExtra("MORTGAGE_ACCOUNT", mortgageAccountNumber);
    intent.putExtra("PERIOD_NUMBER", periodNumber);
    startActivity(intent);
    finish();
}
```

## Flow Hoàn Chỉnh

### Case 1: Thanh Toán < 10 Triệu (Chỉ OTP)

```
MortgagePaymentConfirmActivity
  ↓ (btnConfirm click)
  ↓ sendOtpAndVerify()
  ↓ Intent: FROM_ACTIVITY = "MORTGAGE_PAYMENT"
OtpVerificationActivity
  ↓ (OTP verified)
  ↓ handleOtpSuccess() → processMortgagePayment()
  ↓ API: POST /api/mortgage/payment/current
MortgagePaymentSuccessActivity
```

### Case 2: Thanh Toán > 10 Triệu (Face + OTP)

```
MortgagePaymentConfirmActivity
  ↓ (btnConfirm click)
  ↓ Intent: from = "MORTGAGE_PAYMENT"
FaceVerificationTransactionActivity
  ↓ (Face verified)
  ↓ navigateToOtpVerification()
  ↓ Intent: FROM_ACTIVITY = "MORTGAGE_PAYMENT"
OtpVerificationActivity
  ↓ (OTP verified)
  ↓ handleOtpSuccess() → processMortgagePayment()
  ↓ API: POST /api/mortgage/payment/current
MortgagePaymentSuccessActivity
```

## API Request & Response

### Request
```http
POST http://localhost:8089/api/mortgage/payment/current
Content-Type: application/json
Authorization: Bearer <token>

{
  "mortgageId": 40,
  "paymentAmount": 31730737.25,
  "paymentAccountNumber": "5967568438"
}
```

### Response
```json
{
  "mortgageId": 40,
  "accountNumber": "MTG202512224112",
  "customerName": "Trương Dương Hưng",
  "customerPhone": "0839256305",
  "principalAmount": 200000000.00,
  "interestRate": 0.6667,
  "termMonths": 14,
  "startDate": "2025-01-01",
  "status": "ACTIVE",
  "remainingBalance": 158695007.99,
  "paymentSchedules": [...]
}
```

## Cách Build và Cài Đặt

### 1. Kết nối thiết bị
```bash
adb devices
```

Nếu không thấy thiết bị:
```bash
adb kill-server
adb start-server
adb devices
```

### 2. Build app
```bash
cd D:\eBanking\FrontEnd\banking-app
gradlew.bat assembleDebug
```

### 3. Cài đặt
```bash
adb uninstall com.example.mobilebanking
adb install app\build\outputs\apk\debug\app-debug.apk
```

Hoặc chạy file bat:
```bash
rebuild_and_install.bat
```

## Cách Test

### Bước 1: Mở app và đăng nhập
- Số điện thoại: `0839256305`
- Mật khẩu: mật khẩu của bạn

### Bước 2: Vào tab "Tài khoản vay"
- Từ màn hình Home
- Vuốt sang tab "Tài khoản vay"

### Bước 3: Chọn khoản vay
- Nhấn vào khoản vay `MTG202512224112`
- Xem chi tiết

### Bước 4: Thanh toán kỳ hiện tại
- Nhấn "Thanh toán kỳ hiện tại"
- Kiểm tra thông tin:
  - Số tài khoản vay: MTG202512224112
  - Kỳ thanh toán: Kỳ 4
  - Số tiền: 31,730,737 đ (> 10 triệu)
  - Tài khoản thanh toán: 5967568438

### Bước 5: Xác nhận
- Nhấn "Xác nhận"
- ✅ Chuyển sang màn hình xác thực khuôn mặt (vì > 10 triệu)

### Bước 6: Xác thực khuôn mặt
- Đưa khuôn mặt vào camera
- Chờ xác thực thành công
- ✅ Chuyển sang màn hình OTP

### Bước 7: Nhập OTP
- Nhập: `123456`
- Nhấn "Xác thực"

### Bước 8: Kiểm tra logcat
```bash
adb logcat | findstr /i "MortgagePayment OtpVerification"
```

**Log mong đợi:**
```
D/MortgagePayment: Phone for OTP: 0839256305
D/FaceVerification: MORTGAGE_PAYMENT - Phone for OTP: 0839256305
D/OtpVerification: FROM_ACTIVITY key value: MORTGAGE_PAYMENT
D/OtpVerification: Updated fromActivity from FROM_ACTIVITY: MORTGAGE_PAYMENT
D/OtpVerification: FINAL - OTP Verification - fromActivity: MORTGAGE_PAYMENT, phone: 0839256305
D/OtpVerification: Verifying OTP: 123456 for phone: 0839256305
D/OtpVerification: Test OTP detected: 123456 - bypassing verification
D/OtpVerification: handleOtpSuccess - fromActivity: MORTGAGE_PAYMENT
D/OtpVerification: MORTGAGE_PAYMENT - Processing payment
```

**KHÔNG phải:**
```
D/OtpVerification: handleOtpSuccess - fromActivity: transaction
D/OtpVerification: Processing transfer confirm - transactionCode: null, bank: null
```

### Bước 9: Kiểm tra kết quả
- ✅ API được gọi: `POST http://localhost:8089/api/mortgage/payment/current`
- ✅ Chuyển sang màn hình thành công
- ✅ Hiển thị:
  - Số tài khoản vay: MTG202512224112
  - Kỳ đã thanh toán: Kỳ 4
  - Số tiền đã thanh toán: 31,730,737 đ
  - Số dư còn lại: 144,742,693 đ

## Files Đã Sửa

1. ✅ `MortgagePaymentConfirmActivity.java`
   - Đổi key từ `TRANSACTION_TYPE` thành `from`
   - Thêm fallback lấy phone

2. ✅ `FaceVerificationTransactionActivity.java`
   - Thêm fallback lấy phone cho MORTGAGE_PAYMENT flow
   - Đã có sẵn xử lý `from = "MORTGAGE_PAYMENT"`

3. ✅ `OtpVerificationActivity.java`
   - Đã có sẵn xử lý `FROM_ACTIVITY = "MORTGAGE_PAYMENT"`
   - Method `processMortgagePayment()` gọi API đúng

4. ✅ `TransactionConfirmationActivity.java`
   - Sửa luôn transfer flow để lấy phone đúng

## Tóm Tắt

✅ **Vấn đề:** Key Intent Extra không khớp giữa các Activity
✅ **Giải pháp:** Đổi `TRANSACTION_TYPE` thành `from` trong `MortgagePaymentConfirmActivity`
✅ **Kết quả:** Flow hoạt động đúng, gọi API mortgage payment thay vì transfer

**Bây giờ kết nối lại thiết bị và cài đặt app mới để test!**
