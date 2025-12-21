# Saving Verification Flow Update

## Tổng quan
Đã cập nhật luồng xác nhận gửi tiết kiệm trong `SavingConfirmActivity` để sử dụng xác thực khuôn mặt (nếu số tiền >= 10 triệu) và OTP, giống như luồng chuyển tiền.

## Thay đổi

### SavingConfirmActivity.java

#### 1. Thêm imports
```java
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
```

#### 2. Thêm fields
```java
// Activity result launchers
private ActivityResultLauncher<Intent> faceVerificationLauncher;
private ActivityResultLauncher<Intent> otpVerificationLauncher;

private static final double FACE_VERIFICATION_THRESHOLD = 10000000; // 10 triệu
```

#### 3. Setup Activity Result Launchers
```java
private void setupActivityResultLaunchers() {
    // Face verification launcher
    faceVerificationLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Face verification successful, proceed to OTP
                    navigateToOtpVerification();
                } else {
                    // Face verification failed or cancelled
                    Toast.makeText(this, "Xác thực khuôn mặt thất bại", Toast.LENGTH_SHORT).show();
                }
            });

    // OTP verification launcher
    otpVerificationLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // OTP verification successful, create saving
                    createSaving();
                } else {
                    // OTP verification failed or cancelled
                    Toast.makeText(this, "Xác thực OTP thất bại", Toast.LENGTH_SHORT).show();
                }
            });
}
```

#### 4. Luồng xác thực mới
```java
private void startVerificationFlow() {
    if (amount >= FACE_VERIFICATION_THRESHOLD) {
        // Amount >= 10 triệu: Face verification first, then OTP
        navigateToFaceVerification();
    } else {
        // Amount < 10 triệu: OTP only
        navigateToOtpVerification();
    }
}
```

## Luồng hoạt động

### Trường hợp 1: Số tiền < 10 triệu
```
Confirm Button
    ↓
OTP Verification
    ↓
Create Saving API
    ↓
Success Screen
```

### Trường hợp 2: Số tiền >= 10 triệu
```
Confirm Button
    ↓
Face Verification
    ↓
OTP Verification
    ↓
Create Saving API
    ↓
Success Screen
```

## So sánh với luồng cũ

### Luồng cũ
```
Confirm Button
    ↓
Create Saving API (trực tiếp)
    ↓
Success Screen
```

### Luồng mới
```
Confirm Button
    ↓
[Face Verification] (nếu >= 10 triệu)
    ↓
OTP Verification
    ↓
Create Saving API
    ↓
Success Screen
```

## Chi tiết xác thực

### Face Verification (nếu >= 10 triệu)
- **Activity**: `FaceVerificationTransactionActivity`
- **Intent extras**:
  - `transaction_type`: "SAVING"
  - `amount`: Số tiền gửi
- **Result**: 
  - `RESULT_OK`: Xác thực thành công → Chuyển sang OTP
  - Khác: Xác thực thất bại → Hiển thị toast lỗi

### OTP Verification
- **Activity**: `OtpVerificationActivity`
- **Intent extras**:
  - `verificationType`: "SAVING"
  - `amount`: Số tiền gửi
  - `termType`: Loại kỳ hạn
  - `termMonths`: Số tháng
- **Result**:
  - `RESULT_OK`: Xác thực thành công → Gọi API tạo sổ tiết kiệm
  - Khác: Xác thực thất bại → Hiển thị toast lỗi

### Create Saving API
- Chỉ được gọi sau khi xác thực thành công
- Request: `CreateSavingRequest`
- Response: `CreateSavingResponse`
- Success: Navigate to `SavingSuccessActivity`

## Ngưỡng xác thực

| Số tiền | Face Verification | OTP Verification |
|---------|-------------------|------------------|
| < 10.000.000 VNĐ | ❌ Không | ✅ Có |
| >= 10.000.000 VNĐ | ✅ Có | ✅ Có |

## Ví dụ

### Ví dụ 1: Gửi 5.000.000 VNĐ
1. Người dùng click "Xác nhận"
2. Chuyển đến OTP verification
3. Nhập OTP đúng
4. Gọi API tạo sổ tiết kiệm
5. Chuyển đến màn hình thành công

### Ví dụ 2: Gửi 15.000.000 VNĐ
1. Người dùng click "Xác nhận"
2. Chuyển đến Face verification
3. Xác thực khuôn mặt thành công
4. Chuyển đến OTP verification
5. Nhập OTP đúng
6. Gọi API tạo sổ tiết kiệm
7. Chuyển đến màn hình thành công

## Error Handling

### Face Verification Failed
- **Hiển thị**: Toast "Xác thực khuôn mặt thất bại"
- **Hành động**: Người dùng ở lại màn hình confirm, có thể thử lại

### OTP Verification Failed
- **Hiển thị**: Toast "Xác thực OTP thất bại"
- **Hành động**: Người dùng ở lại màn hình confirm, có thể thử lại

### API Create Saving Failed
- **Hiển thị**: Toast với thông báo lỗi từ server
- **Hành động**: Người dùng ở lại màn hình confirm, có thể thử lại

## Files đã chỉnh sửa
1. `SavingConfirmActivity.java` - Thêm luồng xác thực Face và OTP

## Testing

### Test Case 1: Gửi tiết kiệm < 10 triệu (chỉ OTP)
1. Nhập số tiền: 5.000.000 VNĐ
2. Chọn kỳ hạn: 6 tháng
3. Click "Tiếp tục" → Màn hình xác nhận
4. Click "Xác nhận"
5. **Kết quả mong đợi**:
   - Chuyển đến OTP verification (không có Face verification)
   - Nhập OTP đúng
   - Tạo sổ tiết kiệm thành công

### Test Case 2: Gửi tiết kiệm >= 10 triệu (Face + OTP)
1. Nhập số tiền: 15.000.000 VNĐ
2. Chọn kỳ hạn: 12 tháng
3. Click "Tiếp tục" → Màn hình xác nhận
4. Click "Xác nhận"
5. **Kết quả mong đợi**:
   - Chuyển đến Face verification
   - Xác thực khuôn mặt thành công
   - Chuyển đến OTP verification
   - Nhập OTP đúng
   - Tạo sổ tiết kiệm thành công

### Test Case 3: Face Verification Failed
1. Nhập số tiền: 15.000.000 VNĐ
2. Click "Xác nhận"
3. Xác thực khuôn mặt thất bại
4. **Kết quả mong đợi**:
   - Hiển thị toast "Xác thực khuôn mặt thất bại"
   - Ở lại màn hình confirm
   - Có thể thử lại

### Test Case 4: OTP Verification Failed
1. Nhập số tiền: 5.000.000 VNĐ
2. Click "Xác nhận"
3. Nhập OTP sai
4. **Kết quả mong đợi**:
   - Hiển thị toast "Xác thực OTP thất bại"
   - Ở lại màn hình confirm
   - Có thể thử lại

## Lưu ý
- Ngưỡng 10 triệu được định nghĩa trong constant `FACE_VERIFICATION_THRESHOLD`
- Có thể thay đổi ngưỡng này nếu cần
- Luồng xác thực giống hệt với luồng chuyển tiền để đảm bảo tính nhất quán

## Status
✅ Hoàn thành - Luồng xác thực Face và OTP đã được tích hợp vào gửi tiết kiệm
