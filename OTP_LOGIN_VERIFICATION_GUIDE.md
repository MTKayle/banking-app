# Hướng Dẫn Xác Thực OTP Khi Đăng Nhập Tài Khoản Khác

## Tổng Quan
Khi người dùng đăng nhập bằng tài khoản khác (không phải tài khoản cuối cùng), hệ thống sẽ yêu cầu xác thực OTP để đảm bảo an toàn.

## Flow Hoạt Động

### Trường Hợp 1: Đăng Nhập Bằng Tài Khoản Cuối Cùng
```
User nhập phone + password
  ↓
Check: phone == lastUsername? → YES
  ↓
Đăng nhập bình thường (không cần xác thực OTP)
  ↓
Vào màn hình chính
```

### Trường Hợp 2: Đăng Nhập Bằng Tài Khoản Khác
```
User nhập phone + password
  ↓
Check: phone == lastUsername? → NO
  ↓
Hiển thị dialog: "Bạn đang đăng nhập bằng tài khoản khác. Vui lòng xác thực OTP."
  ↓
User click "Xác Thực"
  ↓
Chuyển sang OtpVerificationActivity (flow: login_verification)
  ↓
Gửi OTP qua Goixe247 API
  ↓
User nhập OTP
  ↓
Xác thực OTP với Goixe247 API
  ↓
  ├─ Thành công → Gọi API login → Đăng nhập → Vào màn hình chính
  └─ Thất bại → Hiển thị lỗi → Cho nhập lại OTP
```

## Files Đã Sửa

### 1. LoginActivity.java
**Thay đổi:**
- Thêm logic kiểm tra tài khoản cuối cùng trong `handleLogin()`
- Nếu không phải tài khoản cuối cùng → Hiển thị dialog xác thực OTP
- Chuyển sang `OtpVerificationActivity` với flow `login_verification`

**Code Mới:**
```java
// Kiểm tra xem có phải tài khoản cuối cùng không
String lastUsername = dataManager.getLastUsername();

if (lastUsername != null && !lastUsername.isEmpty() && !phone.equals(lastUsername)) {
    // Không phải tài khoản cuối cùng → Yêu cầu xác thực OTP
    new AlertDialog.Builder(this)
        .setTitle("Xác Thực OTP")
        .setMessage("Bạn đang đăng nhập bằng tài khoản khác. Vui lòng xác thực OTP để tiếp tục.")
        .setPositiveButton("Xác Thực", (dialog, which) -> {
            Intent intent = new Intent(LoginActivity.this, OtpVerificationActivity.class);
            intent.putExtra("flow", "login_verification");
            intent.putExtra("phone", phone);
            intent.putExtra("password", password);
            startActivity(intent);
        })
        .setNegativeButton("Hủy", null)
        .show();
    return;
}

// Tài khoản cuối cùng → Đăng nhập bình thường
performPasswordLogin(phone, password);
```

### 2. OtpVerificationActivity.java
**Thay đổi:**
- Thêm field `password` để lưu password cho login_verification flow
- Thêm xử lý flow `login_verification` trong `onCreate()`
- Thêm xử lý `login_verification` trong `handleOtpVerification()`
- Thêm xử lý `login_verification` trong `verifyOtpWithGoixe()`
- Thêm method `performLogin()` để đăng nhập sau khi xác thực OTP thành công
- Cập nhật `resendOtp()` để hỗ trợ `login_verification`

**Code Mới:**
```java
// Trong onCreate()
String flow = getIntent().getStringExtra("flow");
if (flow != null && !flow.isEmpty()) {
    fromActivity = flow;
}
password = getIntent().getStringExtra("password");

if ("login_verification".equals(fromActivity)) {
    sendOtpWithGoixe();
}

// Trong verifyOtpWithGoixe()
if (otpResponse.isSuccess()) {
    if ("login_verification".equals(fromActivity)) {
        performLogin();
    }
}

// Method mới
private void performLogin() {
    // Gọi API login
    // Lưu session và user info
    // Navigate to dashboard
}
```

## API Sử dụng

### 1. Gửi OTP: POST https://otp.goixe247.com/api/otp/request
**Request:**
```json
{
  "user_id": "13",
  "api_key": "328945bfca039d9663890e71f4d9e2203669dd1e49fd3cb9a44fa86a48d915da",
  "phone": "0901234567"
}
```

**Response:**
```json
{
  "success": true,
  "message": "OTP sent successfully"
}
```

### 2. Xác Thực OTP: POST https://otp.goixe247.com/api/otp/verify
**Request:**
```json
{
  "user_id": "13",
  "api_key": "328945bfca039d9663890e71f4d9e2203669dd1e49fd3cb9a44fa86a48d915da",
  "phone": "0901234567",
  "otp": "123456"
}
```

**Response:**
```json
{
  "success": true,
  "message": "OTP verified successfully"
}
```

### 3. Đăng Nhập: POST /auth/login
**Request:**
```json
{
  "phone": "0901234567",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "userId": 123,
  "phone": "0901234567",
  "fullName": "Nguyen Van A",
  "email": "user@example.com",
  "role": "CUSTOMER"
}
```

## Giao Diện
Sử dụng layout: `activity_otp_verification.xml` (dùng chung với các flow khác)

## Testing

### Test Case 1: Đăng Nhập Tài Khoản Cuối Cùng
1. Đăng nhập bằng phone A + password
2. Đăng xuất
3. Đăng nhập lại bằng phone A + password
4. ✅ Đăng nhập thành công (không cần xác thực OTP)

### Test Case 2: Đăng Nhập Tài Khoản Khác - Thành Công
1. Đăng nhập bằng phone A + password
2. Đăng xuất
3. Đăng nhập bằng phone B + password (tài khoản khác)
4. ✅ Hiển thị dialog "Xác Thực OTP"
5. Click "Xác Thực"
6. ✅ Chuyển sang màn hình OTP
7. ✅ OTP được gửi đến phone B
8. Nhập OTP đúng
9. ✅ Xác thực thành công → Đăng nhập → Vào màn hình chính

### Test Case 3: Đăng Nhập Tài Khoản Khác - OTP Sai
1. Đăng nhập bằng phone B (tài khoản khác)
2. Click "Xác Thực"
3. Nhập OTP SAI
4. ✅ Hiển thị "Mã OTP không đúng. Vui lòng nhập lại."
5. ✅ Xóa các ô OTP, cho nhập lại
6. Nhập OTP ĐÚNG
7. ✅ Đăng nhập thành công

### Test Case 4: Hủy Xác Thực
1. Đăng nhập bằng phone B (tài khoản khác)
2. Hiển thị dialog "Xác Thực OTP"
3. Click "Hủy"
4. ✅ Quay lại màn hình login (không đăng nhập)

### Test Case 5: Gửi Lại OTP
1. Đăng nhập bằng phone B (tài khoản khác)
2. Click "Xác Thực"
3. Đợi 60 giây (timer hết)
4. Click "Gửi lại OTP"
5. ✅ OTP mới được gửi
6. ✅ Timer reset về 60 giây
7. Nhập OTP mới
8. ✅ Đăng nhập thành công

### Test Case 6: Lần Đầu Đăng Nhập
1. Cài app mới (chưa có lastUsername)
2. Đăng nhập bằng phone A + password
3. ✅ Đăng nhập thành công (không cần xác thực OTP)
4. Đăng xuất
5. Đăng nhập lại bằng phone A
6. ✅ Đăng nhập thành công (không cần xác thực)
7. Đăng nhập bằng phone B
8. ✅ Yêu cầu xác thực OTP

## Lưu Ý Quan Trọng

### 1. OTP API
- Sử dụng Goixe247 API (giống forgot_password và movie_booking)
- OTP có thời hạn (thường 5-10 phút)
- Có thể gửi lại OTP sau 60 giây

### 2. Password
- Password được truyền qua Intent (trong memory)
- Chỉ sử dụng để gọi API login sau khi xác thực OTP thành công
- Không lưu vào SharedPreferences hay file

### 3. Bảo Mật
- Kiểm tra tài khoản cuối cùng để quyết định có cần OTP không
- OTP được gửi đến số điện thoại đăng ký
- Chỉ đăng nhập được khi OTP đúng

### 4. UX
- Dialog không thể dismiss bằng cách click outside
- Phải click "Xác Thực" hoặc "Hủy"
- Timer 60 giây trước khi cho phép gửi lại OTP
- Có thể click "Hủy" để quay lại login bất cứ lúc nào

### 5. Flow Khác Nhau
- `register` - Dùng eSMS
- `forgot_password` - Dùng Goixe247
- `movie_booking` - Dùng Goixe247
- `login_verification` - Dùng Goixe247 (MỚI)

## Troubleshooting

### Lỗi: "Lỗi kết nối"
→ Goixe247 API không phản hồi. Kiểm tra:
- Internet connection
- API key và user_id có đúng không?
- Endpoint có đúng không?

### Lỗi: "Mã OTP không đúng"
→ OTP sai hoặc đã hết hạn. Giải pháp:
- Kiểm tra lại OTP trong tin nhắn
- Click "Gửi lại OTP" để lấy mã mới

### Lỗi: "Đăng nhập thất bại"
→ Sau khi xác thực OTP thành công nhưng login API thất bại. Kiểm tra:
- Phone và password có đúng không?
- Backend có đang chạy không?
- Token có được lưu đúng không?

## So Sánh: Face Login vs OTP Login

| Tiêu Chí | Face Login | OTP Login |
|----------|------------|-----------|
| Độ phức tạp | Cao (camera, ML Kit) | Thấp (API call) |
| Thời gian | ~5 giây | ~30 giây (chờ SMS) |
| Yêu cầu | Camera, quyền camera | Số điện thoại, mạng |
| Độ chính xác | Phụ thuộc ML model | 100% (nếu có OTP) |
| UX | Tốt (tự động) | Trung bình (phải nhập) |
| Chi phí | Miễn phí | Có phí SMS |

## Tóm Tắt
- ✅ Xác thực OTP khi đăng nhập bằng tài khoản khác
- ✅ Sử dụng Goixe247 API để gửi và xác thực OTP
- ✅ Sử dụng chung layout `activity_otp_verification.xml`
- ✅ Đăng nhập tự động sau khi xác thực OTP thành công
- ✅ Xử lý lỗi và cho phép thử lại
- ✅ Đơn giản hơn Face Login, dễ implement và test
