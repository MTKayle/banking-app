# My QR Account Info Update

## Tổng quan
Đã cập nhật MyQRActivity để lấy thông tin tài khoản từ API và hiển thị đúng số tài khoản, tên chủ tài khoản. Thêm chức năng bật/tắt hiển thị số tài khoản.

## Thay đổi

### 1. API Integration
**File**: `MyQRActivity.java`

#### Thêm import
```java
import com.example.mobilebanking.api.dto.CheckingAccountInfoResponse;
import com.example.mobilebanking.utils.DataManager;
```

#### Thêm method `loadAccountInfo()`
- Lấy `userId` từ `DataManager`
- Lấy tên chủ tài khoản từ `DataManager.getLastFullName()` (đã lưu khi login)
- Gọi API `GET /api/accounts/checking/info/{userId}`
- Nhận response với thông tin:
  - `accountNumber`: Số tài khoản
  - `balance`: Số dư (không dùng trong màn hình này)
- Cập nhật UI với thông tin
- Tải mã QR từ backend

**Lưu ý**: Tên chủ tài khoản được lấy từ `DataManager` thay vì từ API vì `CheckingAccountInfoResponse` không có field này.

#### Cập nhật `onCreate()`
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_my_qr);
    
    // Initialize API service
    accountApiService = ApiClient.getAccountApiService();
    
    initViews();
    setupToolbar();
    loadAccountInfo(); // Load account info from API
    setupClickListeners();
}
```

### 2. Toggle Account Number Visibility
**Chức năng**: Click vào icon con mắt để bật/tắt hiển thị số tài khoản

#### Trạng thái
- **Masked (ẩn)**: 
  - Hiển thị: `*******305` (7 dấu * + 3 số cuối)
  - Icon: `ic_eye_open` (mắt mở)
  - Ý nghĩa: Click để hiện số tài khoản đầy đủ
  
- **Unmasked (hiện)**:
  - Hiển thị: `0839256305` (số tài khoản đầy đủ)
  - Icon: `ic_eye_closed` (mắt nhắm/có gạch chéo)
  - Ý nghĩa: Click để ẩn số tài khoản

#### Code
```java
private void updateMaskedAccount() {
    if (accountNumber != null && accountNumber.length() > 3) {
        if (isMasked) {
            // Đang ẩn -> hiển thị dấu * và icon mắt mở
            String last3 = accountNumber.substring(accountNumber.length() - 3);
            tvMaskedAccount.setText("*******" + last3);
            ivToggleMask.setImageResource(R.drawable.ic_eye_open);
        } else {
            // Đang hiện -> hiển thị số đầy đủ và icon mắt nhắm
            tvMaskedAccount.setText(accountNumber);
            ivToggleMask.setImageResource(R.drawable.ic_eye_closed);
        }
    }
}
```

### 3. Icon mới
**File**: `ic_eye_closed.xml`

Tạo icon mắt nhắm (có gạch chéo) để hiển thị khi số tài khoản đang được hiện đầy đủ.

## Layout Updates

### TextView IDs trong `activity_my_qr.xml`
- `@+id/tv_account_number`: Số tài khoản ở phần header (luôn hiển thị đầy đủ)
- `@+id/tv_account_holder_name`: Tên chủ tài khoản (in hoa)
- `@+id/tv_masked_account`: Số tài khoản có thể ẩn/hiện (trong card QR)
- `@+id/iv_toggle_mask`: Icon con mắt để toggle

## API Endpoint

### Get Checking Account Info
```
GET /api/accounts/checking/info/{userId}
```

**Response**:
```json
{
  "checkingId": 1,
  "accountNumber": "0839256305",
  "balance": 50000000.00,
  "userId": 1,
  "userPhone": "0123456789"
}
```

**Lưu ý**: Tên chủ tài khoản (`accountHolderName`) được lấy từ `DataManager.getLastFullName()` vì API không trả về field này.

## Luồng hoạt động

1. **Khởi động activity**:
   - Lấy `userId` từ `DataManager`
   - Lấy tên chủ tài khoản từ `DataManager.getLastFullName()` (đã lưu khi login)
   - Gọi API để lấy số tài khoản
   - Hiển thị tên chủ tài khoản (in hoa)
   - Hiển thị số tài khoản đầy đủ ở header
   - Hiển thị số tài khoản ẩn trong card QR (mặc định)
   - Tải mã QR từ backend

2. **Toggle hiển thị số tài khoản**:
   - Click icon con mắt
   - Chuyển đổi giữa ẩn (*******305) và hiện (0839256305)
   - Đổi icon tương ứng

3. **Copy số tài khoản**:
   - Click icon copy
   - Copy số tài khoản đầy đủ vào clipboard
   - Hiển thị toast xác nhận

## Files đã chỉnh sửa
1. `MyQRActivity.java` - Thêm API integration và toggle logic
2. `ic_eye_closed.xml` - Icon mới cho trạng thái unmasked

## Testing

### Test Case 1: Load Account Info
1. Đăng nhập vào app
2. Vào trang "QR của tôi"
3. **Kết quả mong đợi**:
   - Hiển thị tên chủ tài khoản đúng (in hoa)
   - Hiển thị số tài khoản đúng ở header
   - Hiển thị số tài khoản ẩn trong card (*******305)
   - Mã QR được tải từ backend

### Test Case 2: Toggle Account Number
1. Vào trang "QR của tôi"
2. Click vào icon con mắt
3. **Kết quả mong đợi**:
   - Số tài khoản hiện đầy đủ
   - Icon đổi thành mắt nhắm
4. Click lại icon con mắt
5. **Kết quả mong đợi**:
   - Số tài khoản ẩn lại (*******305)
   - Icon đổi thành mắt mở

### Test Case 3: Copy Account Number
1. Vào trang "QR của tôi"
2. Click icon copy
3. **Kết quả mong đợi**:
   - Toast hiển thị "Đã sao chép số tài khoản"
   - Số tài khoản đầy đủ được copy vào clipboard

## Status
✅ Hoàn thành - MyQRActivity đã được cập nhật với API integration và toggle account visibility
