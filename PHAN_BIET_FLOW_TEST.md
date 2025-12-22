# Phân Biệt Các Flow Test - Quan Trọng!

## ⚠️ LƯU Ý QUAN TRỌNG

Bạn đang nhầm lẫn giữa 2 flow khác nhau:

### 1. Flow Transfer (Chuyển Tiền) ❌ KHÔNG PHẢI MORTGAGE
```
Home → Chuyển tiền → Nhập thông tin → Xác nhận → OTP
Log: fromActivity: transfer hoặc transaction
```

### 2. Flow Mortgage Payment (Thanh Toán Vay) ✅ ĐÂY MỚI ĐÚNG
```
Home → Tab "Tài khoản vay" → Chọn khoản vay → Thanh toán kỳ hiện tại → Xác nhận → OTP
Log: fromActivity: MORTGAGE_PAYMENT
```

## Cách Phân Biệt Trong Logcat

### ❌ Nếu thấy log này = Bạn đang test TRANSFER (SAI)
```
D/OtpVerification: handleOtpSuccess - fromActivity: transfer
D/OtpVerification: handleOtpSuccess - fromActivity: transaction
```

### ✅ Nếu thấy log này = Bạn đang test MORTGAGE PAYMENT (ĐÚNG)
```
D/MortgagePayment: Phone for OTP: 0123456789
D/OtpVerification: FROM_ACTIVITY key value: MORTGAGE_PAYMENT
D/OtpVerification: handleOtpSuccess - fromActivity: MORTGAGE_PAYMENT
D/OtpVerification: MORTGAGE_PAYMENT - Processing payment
```

## Hướng Dẫn Test Đúng Flow Mortgage Payment

### Bước 1: Mở App
- Đăng nhập với tài khoản có khoản vay

### Bước 2: Vào Tab "Tài khoản vay"
- Từ màn hình Home
- **QUAN TRỌNG**: Vuốt sang tab "Tài khoản vay" (không phải "Chuyển tiền")
- Hoặc nhấn icon "Tài khoản vay" ở bottom navigation

### Bước 3: Chọn Khoản Vay
- Nhấn vào một khoản vay trong danh sách
- Xem chi tiết khoản vay

### Bước 4: Thanh Toán
- Nhấn nút "Thanh toán kỳ hiện tại"
- Kiểm tra thông tin xác nhận
- Nhấn "Xác nhận"

### Bước 5: Nhập OTP
- Nhập: `123456`
- Nhấn "Xác thực"

### Bước 6: Kiểm Tra Logcat
```bash
adb logcat | findstr /i "MortgagePayment OtpVerification"
```

Phải thấy:
```
D/MortgagePayment: Phone for OTP: 0123456789
D/OtpVerification: MORTGAGE_PAYMENT - Processing payment
```

## Nếu Vẫn Thấy "transfer" Trong Log

Có 2 khả năng:

### 1. Bạn đang test sai flow
- Bạn đang test "Chuyển tiền" thay vì "Thanh toán vay"
- Hãy làm lại từ đầu theo hướng dẫn trên

### 2. App cũ vẫn đang chạy
```bash
# Gỡ app cũ hoàn toàn
adb uninstall com.example.mobilebanking

# Cài app mới
adb install D:\eBanking\FrontEnd\banking-app\app\build\outputs\apk\debug\app-debug.apk

# Hoặc chạy file bat
rebuild_and_install.bat
```

## So Sánh 2 Flow

| Đặc điểm | Transfer | Mortgage Payment |
|----------|----------|------------------|
| Màn hình bắt đầu | Home → Chuyển tiền | Home → Tab Tài khoản vay |
| Activity | TransferActivity | MortgageDetailActivity |
| Confirm Activity | TransactionConfirmationActivity | MortgagePaymentConfirmActivity |
| Intent Extra Key | `"from", "transaction"` | `"FROM_ACTIVITY", "MORTGAGE_PAYMENT"` |
| Phone Key | `"phone"` | `"PHONE_NUMBER"` |
| Log Tag | `transfer` hoặc `transaction` | `MORTGAGE_PAYMENT` |
| API Endpoint | `/api/transfer/...` | `/api/mortgage/payment/current` |

## Files Đã Sửa (Cả 2 Flow)

### Flow Transfer (Chuyển Tiền)
1. ✅ `TransactionConfirmationActivity.java` - Lấy phone từ DataManager
2. ✅ `FaceVerificationTransactionActivity.java` - Thêm fallback lấy phone cho transfer

### Flow Mortgage Payment (Thanh Toán Vay)
1. ✅ `MortgagePaymentConfirmActivity.java` - Lấy phone từ DataManager
2. ✅ `FaceVerificationTransactionActivity.java` - Thêm fallback lấy phone cho mortgage
3. ✅ `OtpVerificationActivity.java` - Xử lý MORTGAGE_PAYMENT

## Kết Luận

**Bạn cần test đúng flow Mortgage Payment, không phải Transfer!**

Hãy làm theo hướng dẫn trên và kiểm tra logcat để đảm bảo đang test đúng flow.
