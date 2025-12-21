# Tích hợp API Tạo Sổ Tiết Kiệm

## ✅ HOÀN THÀNH

Đã hoàn thành tích hợp API tạo sổ tiết kiệm với luồng xác thực Face (nếu >= 10 triệu) và OTP.

## Luồng hoạt động

### 1. Người dùng nhập thông tin
- **Activity**: `SavingDepositActivity`
- Nhập số tiền (tự động format với dấu chấm: 10.000)
- Hiển thị số tiền bằng chữ màu xanh
- Chọn kỳ hạn từ danh sách API

### 2. Xác nhận thông tin
- **Activity**: `SavingConfirmActivity`
- Hiển thị đầy đủ thông tin:
  - Tài khoản nguồn
  - Số tiền gửi
  - Kỳ hạn
  - Lãi suất
  - Ngày đáo hạn
  - Lãi dự kiến
- Click "Xác nhận" → Bắt đầu luồng xác thực

### 3. Luồng xác thực

#### Nếu số tiền >= 10.000.000 VNĐ:
1. **Face Verification** (`FaceVerificationTransactionActivity`)
   - Xác thực khuôn mặt
   - Nếu thành công → Chuyển sang OTP
   - Nếu thất bại → Dừng lại

2. **OTP Verification** (`OtpVerificationActivity`)
   - Nhập mã OTP
   - Nếu thành công → Gọi API tạo sổ
   - Nếu thất bại → Dừng lại

#### Nếu số tiền < 10.000.000 VNĐ:
1. **OTP Verification** (bỏ qua Face)
   - Nhập mã OTP
   - Nếu thành công → Gọi API tạo sổ

### 4. Gọi API tạo sổ tiết kiệm

**Endpoint**: `POST /api/saving/create`

**Request Body**:
```json
{
  "senderAccountNumber": "5967568438",
  "amount": 10000000,
  "term": "TWELVE_MONTHS"
}
```

**Response**:
```json
{
  "savingId": 21,
  "savingBookNumber": "STK-20251222718",
  "accountNumber": "SAV2069848784",
  "balance": 10000000,
  "term": "12 tháng",
  "termMonths": 12,
  "interestRate": 5.5000,
  "openedDate": "2025-12-22",
  "maturityDate": "2026-12-22",
  "status": "ACTIVE",
  "userId": 5,
  "userFullName": "Trương Dương Hưng"
}
```

### 5. Hiển thị kết quả thành công
- **Activity**: `SavingSuccessActivity`
- Hiển thị thông tin từ API response:
  - Số tiền
  - Số sổ tiết kiệm (savingBookNumber)
  - Kỳ hạn
  - Lãi suất
  - Mã tham chiếu (savingId)
- Các tính năng:
  - Chia sẻ giao dịch
  - Lưu ảnh (đang phát triển)
  - Giới thiệu (đang phát triển)
  - Hoàn tất → Về trang chủ

## Files liên quan

### Activities
- `SavingDepositActivity.java` - Nhập số tiền và chọn kỳ hạn
- `SavingConfirmActivity.java` - Xác nhận và xử lý luồng xác thực
- `FaceVerificationTransactionActivity.java` - Xác thực khuôn mặt
- `OtpVerificationActivity.java` - Xác thực OTP
- `SavingSuccessActivity.java` - Hiển thị kết quả

### DTOs
- `CreateSavingRequest.java` - Request body cho API
- `CreateSavingResponse.java` - Response từ API
- `SavingTermDTO.java` - Thông tin kỳ hạn
- `SavingTermsResponse.java` - Danh sách kỳ hạn từ API

### API Service
- `AccountApiService.java` - Interface định nghĩa endpoint

## Xử lý lỗi

### Validation trước khi gọi API
```java
if (sourceAccountNumber == null || sourceAccountNumber.isEmpty()) {
    Toast.makeText(this, "Thiếu thông tin tài khoản nguồn", Toast.LENGTH_SHORT).show();
    return;
}

if (termType == null || termType.isEmpty()) {
    Toast.makeText(this, "Thiếu thông tin kỳ hạn", Toast.LENGTH_SHORT).show();
    return;
}
```

### Xử lý response lỗi
```java
if (response.isSuccessful() && response.body() != null) {
    navigateToSuccess(response.body());
} else {
    String errorMsg = "Giao dịch thất bại (code: " + response.code() + ")";
    if (response.errorBody() != null) {
        String errorBody = response.errorBody().string();
        // Parse và hiển thị error message
    }
    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
}
```

### Xử lý lỗi kết nối
```java
@Override
public void onFailure(Call<CreateSavingResponse> call, Throwable t) {
    Toast.makeText(SavingConfirmActivity.this, 
            "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
}
```

## Trạng thái xử lý

### Prevent double submission
```java
private boolean isProcessing = false;

private void createSaving() {
    if (isProcessing) return;
    
    isProcessing = true;
    btnConfirm.setEnabled(false);
    btnConfirm.setText("Đang xử lý...");
    
    // ... API call ...
    
    // Reset trong onResponse/onFailure
    isProcessing = false;
    btnConfirm.setEnabled(true);
    btnConfirm.setText("Xác nhận");
}
```

## Testing

### Test case 1: Số tiền < 10 triệu
1. Nhập số tiền: 5.000.000 VNĐ
2. Chọn kỳ hạn: 12 tháng
3. Click "Tiếp tục"
4. Xác nhận thông tin
5. **Chỉ xác thực OTP** (bỏ qua Face)
6. Nhập OTP đúng
7. API được gọi → Hiển thị thành công

### Test case 2: Số tiền >= 10 triệu
1. Nhập số tiền: 10.000.000 VNĐ
2. Chọn kỳ hạn: 12 tháng
3. Click "Tiếp tục"
4. Xác nhận thông tin
5. **Xác thực Face trước**
6. Face thành công → Xác thực OTP
7. OTP đúng → API được gọi → Hiển thị thành công

### Test case 3: Xác thực thất bại
1. Nhập thông tin hợp lệ
2. Face verification thất bại → Dừng lại, hiển thị lỗi
3. Hoặc OTP sai → Dừng lại, hiển thị lỗi

### Test case 4: API lỗi
1. Hoàn thành xác thực
2. API trả về lỗi (400, 500, etc.)
3. Hiển thị thông báo lỗi chi tiết
4. Button "Xác nhận" được enable lại

## Logs để debug

```java
android.util.Log.d("SavingConfirm", "Creating saving: account=" + sourceAccountNumber 
        + ", amount=" + amount + ", term=" + termType);

android.util.Log.d("SavingConfirm", "Response code: " + response.code());

android.util.Log.e("SavingConfirm", "Error: " + errorBody);
```

## Hoàn thành ✅

- ✅ Luồng xác thực Face + OTP
- ✅ Gọi API tạo sổ tiết kiệm
- ✅ Xử lý response và hiển thị thành công
- ✅ Xử lý lỗi đầy đủ
- ✅ Prevent double submission
- ✅ Pass đầy đủ dữ liệu từ API response
- ✅ Clear back stack khi thành công


## Thay đổi cuối cùng

### OtpVerificationActivity.java
Đã thêm xử lý cho verification type "SAVING":

```java
} else if ("SAVING".equals(fromActivity)) {
    // Xác thực thành công → Return result to SavingConfirmActivity
    Intent resultIntent = new Intent();
    resultIntent.putExtra("OTP_VERIFIED", true);
    setResult(RESULT_OK, resultIntent);
    finish();
}
```

Khi OTP verification thành công với type "SAVING", activity sẽ:
1. Tạo Intent result với flag OTP_VERIFIED = true
2. Set result code = RESULT_OK
3. Finish và trả về SavingConfirmActivity
4. SavingConfirmActivity nhận RESULT_OK → Gọi `createSaving()` → API

## Tóm tắt flow hoàn chỉnh

```
SavingDepositActivity (nhập số tiền)
    ↓
SavingConfirmActivity (xác nhận)
    ↓
[Nếu >= 10M] FaceVerificationTransactionActivity
    ↓ (RESULT_OK)
OtpVerificationActivity (verificationType = "SAVING")
    ↓ (RESULT_OK)
SavingConfirmActivity.createSaving()
    ↓ (API call)
POST /api/saving/create
    ↓ (Response)
SavingSuccessActivity (hiển thị kết quả)
```

## Sẵn sàng để test! 🚀
