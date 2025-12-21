# Tích hợp API Xem trước Rút tiền Tiết kiệm

## Tổng quan
Đã tạo trang xác nhận rút tiền tiết kiệm với API preview để hiển thị thông tin chi tiết trước khi rút.

## API Endpoint

### GET /api/saving/{savingBookNumber}/withdraw-preview
**Headers**: `Authorization: Bearer {token}`

**Example**: `GET /api/saving/STK-20251222718/withdraw-preview`

**Response**:
```json
{
  "savingBookNumber": "STK-20251222718",
  "principalAmount": 10000000.00,
  "appliedInterestRate": 0.2000,
  "interestEarned": 0.00,
  "totalAmount": 10000000.00,
  "openedDate": "2025-12-22",
  "withdrawDate": "2025-12-22",
  "daysHeld": 0,
  "message": "Bạn đang tất toán trước hạn nên sẽ áp dụng lãi suất không kỳ hạn là 0.20%/năm",
  "earlyWithdrawal": true
}
```

## Luồng hoạt động

### 1. User click nút "Rút tiền"
- Từ danh sách tài khoản tiết kiệm
- Adapter navigate đến `SavingWithdrawConfirmActivity`
- Pass `savingBookNumber` qua Intent

### 2. Load thông tin preview
- Activity gọi API `GET /api/saving/{savingBookNumber}/withdraw-preview`
- Hiển thị thông tin chi tiết
- Nếu API lỗi → Toast và finish activity

### 3. Hiển thị thông tin
- Số sổ tiết kiệm
- Số tiền gốc (màu primary)
- Lãi suất áp dụng (màu accent)
- Tiền lãi (màu xanh)
- **Tổng tiền nhận** (màu xanh, size lớn, nền xanh nhạt)
- Ngày mở sổ
- Ngày rút tiền
- Số ngày gửi
- **Thông báo cảnh báo** (màu đỏ, nền đỏ nhạt)

### 4. Xác nhận rút tiền
- User click "Xác nhận rút tiền"
- Hiện tại: Toast "Tính năng đang phát triển"
- TODO: Implement API rút tiền thực tế

## UI Design

### Card thông tin chính
- Background: Trắng
- Border radius: 12dp
- Elevation: 2dp
- Các trường thông tin với divider

### Highlight: Tổng tiền nhận
- Background: `#E8F5E9` (xanh nhạt)
- Text color: `#388E3C` (xanh đậm)
- Font size: 18sp, bold

### Card cảnh báo
- Background: `#FFEBEE` (đỏ nhạt)
- Icon: ⚠️ (emoji)
- Text color: `#D32F2F` (đỏ)
- Border radius: 12dp

### Button xác nhận
- Background: `#D32F2F` (đỏ)
- Text: "Xác nhận rút tiền"
- Full width
- Padding: 14dp vertical

## Màu sắc

### Số tiền gốc
- Color: `#006837` (bidv_primary)
- Size: 16sp, bold

### Lãi suất
- Color: `#FFB900` (bidv_accent)
- Size: 14sp, bold

### Tiền lãi
- Color: `#388E3C` (green_positive)
- Size: 14sp, bold

### Tổng tiền nhận
- Color: `#388E3C` (green_positive)
- Size: 18sp, bold
- Background: `#E8F5E9`

### Thông báo cảnh báo
- Color: `#D32F2F` (red_negative)
- Size: 13sp
- Background card: `#FFEBEE`

## Files đã tạo

### 1. WithdrawPreviewResponse.java
DTO cho API response với các fields:
- savingBookNumber
- principalAmount
- appliedInterestRate
- interestEarned
- totalAmount
- openedDate
- withdrawDate
- daysHeld
- message
- earlyWithdrawal

### 2. SavingWithdrawConfirmActivity.java
Activity xử lý:
- Load withdraw preview từ API
- Hiển thị thông tin chi tiết
- Format số tiền và ngày tháng
- Xử lý click nút xác nhận
- Xử lý lỗi API

### 3. activity_saving_withdraw_confirm.xml
Layout với:
- Toolbar với back button
- ScrollView chứa nội dung
- Card thông tin chi tiết
- Card cảnh báo (nền đỏ nhạt)
- Button xác nhận (màu đỏ)

### 4. AccountApiService.java
Thêm endpoint:
```java
@GET("saving/{savingBookNumber}/withdraw-preview")
Call<WithdrawPreviewResponse> getWithdrawPreview(@Path("savingBookNumber") String savingBookNumber);
```

### 5. MySavingAccountAdapter.java
Cập nhật click listener:
```java
holder.btnWithdraw.setOnClickListener(v -> {
    Intent intent = new Intent(context, SavingWithdrawConfirmActivity.class);
    intent.putExtra("savingBookNumber", account.getSavingBookNumber());
    context.startActivity(intent);
});
```

## Xử lý trường hợp đặc biệt

### Rút trước hạn (earlyWithdrawal = true)
- Message màu đỏ
- Lãi suất thấp (0.20%)
- Tiền lãi ít hoặc = 0
- Hiển thị cảnh báo rõ ràng

### Rút đúng hạn (earlyWithdrawal = false)
- Message màu bình thường (nếu có)
- Lãi suất theo kỳ hạn
- Tiền lãi đầy đủ

### API lỗi
```java
@Override
public void onFailure(Call<WithdrawPreviewResponse> call, Throwable t) {
    Toast.makeText(this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
    finish(); // Đóng activity
}
```

### Response không thành công
```java
if (response.isSuccessful() && response.body() != null) {
    displayPreview(response.body());
} else {
    Toast.makeText(this, "Không thể tải thông tin rút tiền", Toast.LENGTH_SHORT).show();
    finish();
}
```

## Format dữ liệu

### Số tiền
```java
DecimalFormat numberFormatter = new DecimalFormat("#,###");
// 10000000 → "10.000.000"
```

### Lãi suất
```java
String.format(Locale.getDefault(), "%.2f%%/năm", appliedInterestRate)
// 0.2000 → "0.20%/năm"
```

### Ngày tháng
```java
// Input có thể là:
// 1. ISO: "2025-12-22" → Parse và format thành "22/12/2025"
// 2. Đã format: "22/12/2025" → Giữ nguyên
```

## Test

### Test case 1: Rút trước hạn (ngày 0)
1. Tạo sổ tiết kiệm mới
2. Ngay lập tức click "Rút tiền"
3. **Kết quả**:
   - Lãi suất: 0.20%/năm
   - Tiền lãi: 0 VNĐ
   - Tổng tiền: = Tiền gốc
   - Message màu đỏ: "Bạn đang tất toán trước hạn..."
   - earlyWithdrawal: true

### Test case 2: Rút sau vài ngày
1. Tạo sổ tiết kiệm
2. Đợi vài ngày (hoặc test với data cũ)
3. Click "Rút tiền"
4. **Kết quả**:
   - Số ngày gửi: > 0
   - Tiền lãi: > 0 (tính theo số ngày)
   - Message cảnh báo nếu chưa đến hạn

### Test case 3: Rút đúng hạn
1. Sổ tiết kiệm đã đến ngày đáo hạn
2. Click "Rút tiền"
3. **Kết quả**:
   - Lãi suất: Theo kỳ hạn (5.5%)
   - Tiền lãi: Đầy đủ
   - Không có message cảnh báo (hoặc message khác)
   - earlyWithdrawal: false

### Test case 4: API lỗi
1. Tắt backend
2. Click "Rút tiền"
3. **Kết quả**:
   - Toast "Lỗi kết nối"
   - Activity tự động đóng

### Test case 5: Click xác nhận
1. Xem preview thành công
2. Click "Xác nhận rút tiền"
3. **Kết quả**: Toast "Tính năng đang phát triển"

## TODO: Implement actual withdraw

Khi implement API rút tiền thực tế:

```java
private void confirmWithdraw() {
    if (isProcessing) return;
    
    isProcessing = true;
    btnConfirm.setEnabled(false);
    btnConfirm.setText("Đang xử lý...");
    
    // Call API POST /api/saving/{savingBookNumber}/withdraw
    // Có thể cần OTP verification trước
    // Sau khi thành công → Navigate to success screen
}
```

## Logs để debug

```
Filter: SavingWithdraw
```

Thêm logs trong activity:
```java
android.util.Log.d("SavingWithdraw", "Loading preview for: " + savingBookNumber);
android.util.Log.d("SavingWithdraw", "Preview loaded: earlyWithdrawal=" + preview.getEarlyWithdrawal());
```

## Hoàn thành ✅

- ✅ Tạo DTO WithdrawPreviewResponse
- ✅ Thêm API endpoint withdraw-preview
- ✅ Tạo SavingWithdrawConfirmActivity
- ✅ Tạo layout với design đẹp
- ✅ Highlight tổng tiền nhận (nền xanh)
- ✅ Cảnh báo rút trước hạn (màu đỏ, nền đỏ nhạt)
- ✅ Format số tiền, lãi suất, ngày tháng
- ✅ Xử lý lỗi API
- ✅ Navigate từ adapter
- ✅ Button xác nhận màu đỏ

## Next steps

- [ ] Implement API rút tiền thực tế
- [ ] Thêm OTP verification trước khi rút
- [ ] Tạo màn hình success sau khi rút
- [ ] Refresh danh sách sau khi rút thành công
