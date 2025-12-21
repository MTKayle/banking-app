# Fix: OTP không gọi API tạo sổ tiết kiệm

## Vấn đề
Sau khi nhập OTP đúng, không có gì xảy ra. API tạo sổ tiết kiệm không được gọi.

## Nguyên nhân
1. `OtpVerificationActivity` đang đọc `"from"` hoặc `"flow"` từ intent
2. Nhưng `SavingConfirmActivity` đang truyền `"verificationType"`
3. Do đó `fromActivity` không khớp với "SAVING" → không return RESULT_OK

## Giải pháp

### 1. Cập nhật OtpVerificationActivity.java

**Thêm đọc "verificationType" từ intent**:
```java
phoneNumber = getIntent().getStringExtra("phone");
fromActivity = getIntent().getStringExtra("from");

// Lấy flow từ intent (ưu tiên "flow" và "verificationType" hơn "from")
String flow = getIntent().getStringExtra("flow");
if (flow != null && !flow.isEmpty()) {
    fromActivity = flow;
}

// Lấy verificationType (dùng cho SAVING, BILL_PAYMENT, etc.)
String verificationType = getIntent().getStringExtra("verificationType");
if (verificationType != null && !verificationType.isEmpty()) {
    fromActivity = verificationType;
}
```

**Thêm case SAVING trong onCreate để gửi OTP**:
```java
} else if ("SAVING".equals(fromActivity)) {
    // Luồng tạo sổ tiết kiệm - gửi OTP với Goixe247
    sendOtpWithGoixe();
}
```

**Thêm log để debug**:
```java
Log.d(TAG, "OTP Verification - fromActivity: " + fromActivity + ", phone: " + phoneNumber);
```

### 2. Cập nhật SavingConfirmActivity.java

**Thêm phone number vào intent**:
```java
private void navigateToOtpVerification() {
    Intent intent = new Intent(this, OtpVerificationActivity.class);
    intent.putExtra("verificationType", "SAVING");
    
    // Get phone number from DataManager
    DataManager dataManager = DataManager.getInstance(this);
    String phone = dataManager.getPhoneNumber();
    intent.putExtra("phone", phone);
    
    otpVerificationLauncher.launch(intent);
}
```

**Thêm log để debug**:
```java
android.util.Log.d("SavingConfirm", "Navigating to OTP verification");
android.util.Log.d("SavingConfirm", "OTP intent extras - verificationType: SAVING, phone: " + phone);
android.util.Log.d("SavingConfirm", "OTP result code: " + result.getResultCode());
android.util.Log.d("SavingConfirm", "OTP verified, calling createSaving()");
```

## Cách test lại

### 1. Build lại app
```bash
./gradlew clean assembleDebug
```

### 2. Chạy app và test

1. Vào trang Tiết kiệm
2. Click "Mở tài khoản"
3. Chọn kỳ hạn
4. Nhập số tiền
5. Xác nhận
6. [Face nếu >= 10M]
7. Nhập OTP

### 3. Xem Logcat

**Filter**: `SavingConfirm|OtpVerification`

**Logs mong đợi**:
```
SavingConfirm: Navigating to OTP verification
SavingConfirm: OTP intent extras - verificationType: SAVING, phone: 0123456789
OtpVerification: OTP Verification - fromActivity: SAVING, phone: 0123456789
OtpVerification: handleOtpSuccess - fromActivity: SAVING
OtpVerification: SAVING - Returning RESULT_OK
SavingConfirm: OTP result code: -1
SavingConfirm: OTP verified, calling createSaving()
SavingConfirm: Creating saving: account=..., amount=..., term=...
SavingConfirm: Response code: 200
```

**Nếu thấy log khác**:
- `fromActivity: null` → Không đọc được verificationType
- `fromActivity: register` hoặc khác → Intent extras bị sai
- `Unknown fromActivity` → Case SAVING không được xử lý
- Không thấy "OTP verified, calling createSaving()" → Callback không được gọi

## Các vấn đề có thể gặp

### Vấn đề 1: Phone number null
**Triệu chứng**: OTP không được gửi
**Giải pháp**: Đảm bảo DataManager có phone number
```java
DataManager dataManager = DataManager.getInstance(this);
String phone = dataManager.getPhoneNumber();
Log.d("SavingConfirm", "Phone from DataManager: " + phone);
```

### Vấn đề 2: fromActivity không phải "SAVING"
**Triệu chứng**: handleOtpSuccess không vào case SAVING
**Giải pháp**: Kiểm tra log "OTP Verification - fromActivity: ..."

### Vấn đề 3: RESULT_OK không được trả về
**Triệu chứng**: otpVerificationLauncher callback không được gọi
**Giải pháp**: Kiểm tra log "SAVING - Returning RESULT_OK"

### Vấn đề 4: API không được gọi
**Triệu chứng**: Không thấy log "Creating saving: ..."
**Giải pháp**: Kiểm tra callback có được gọi không

## Checklist

- ✅ OtpVerificationActivity đọc "verificationType" từ intent
- ✅ OtpVerificationActivity có case "SAVING" trong onCreate
- ✅ OtpVerificationActivity có case "SAVING" trong handleOtpSuccess
- ✅ SavingConfirmActivity truyền phone number vào intent
- ✅ Thêm logs để debug
- ✅ Build lại app
- ✅ Test và xem Logcat

## Kết quả mong đợi

Sau khi nhập OTP đúng:
1. OtpVerificationActivity return RESULT_OK
2. SavingConfirmActivity nhận callback
3. Gọi createSaving()
4. API POST /api/saving/create được gọi
5. Hiển thị màn hình thành công
