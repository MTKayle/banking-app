# Hướng Dẫn Test Đăng Nhập Bằng Vân Tay

## Điều Kiện Tiên Quyết
- Thiết bị hỗ trợ vân tay (hoặc Face ID)
- Đã đăng ký vân tay trong Settings của điện thoại
- Backend đang chạy và kết nối được

## Bước 1: Đăng Nhập Bằng Mật Khẩu
1. Mở app
2. Nhập số điện thoại và mật khẩu
3. Click "Đăng Nhập"
4. **Kiểm tra**: Đăng nhập thành công, vào được màn hình chính

**Điều gì xảy ra ở bước này:**
- App lưu `access_token` và `refresh_token` vào DataManager
- App lưu `temp_refresh_token` vào BiometricPrefs (hết hạn sau 7 ngày)
- App lưu `userId`, `phone`, `fullName` vào DataManager

## Bước 2: Bật Tính Năng Đăng Nhập Bằng Vân Tay
1. Vào màn hình "Hồ Sơ" (Profile/Settings)
2. Tìm mục "Cài đặt vân tay" hoặc "Cài đặt sinh trắc học"
3. Click vào mục đó
4. Quét vân tay khi được yêu cầu
5. **Kiểm tra**: Thấy thông báo "Đã bật xác thực sinh trắc học"

**Điều gì xảy ra ở bước này:**
- App lưu `biometric_enabled = true` vào BiometricPrefs (local)
- App gọi API `PATCH /users/{userId}/settings` để update backend
- Backend lưu `fingerprintLoginEnabled = true` vào database

**Nếu gặp lỗi:**
- "Không tìm thấy thông tin người dùng" → Chưa đăng nhập hoặc `userId` null
- "Không thể cập nhật cài đặt trên server" → Backend không phản hồi hoặc lỗi
- "Lỗi kết nối" → Không kết nối được backend

## Bước 3: Đăng Xuất
1. Ở màn hình Profile/Settings, kéo xuống dưới
2. Click nút "Đăng Xuất"
3. Xác nhận đăng xuất
4. **Kiểm tra**: Quay về màn hình đăng nhập

**Điều gì xảy ra ở bước này:**
- App xóa `access_token`, `userId`, `phone` khỏi DataManager
- App GIỮ LẠI `temp_refresh_token` và `biometric_enabled` trong BiometricPrefs
- App GIỮ LẠI `fingerprintLoginEnabled = true` trên backend

## Bước 4: Đăng Nhập Bằng Vân Tay
1. Ở màn hình đăng nhập, click vào icon vân tay (fingerprint)
2. **Kiểm tra các trường hợp:**

### Trường Hợp A: Thành Công
- App hiển thị prompt quét vân tay
- Quét vân tay
- Đăng nhập thành công, vào màn hình chính

### Trường Hợp B: Chưa Bật Fingerprint
- Thông báo: "Chưa bật đăng nhập bằng vân tay. Vui lòng vào Cài đặt..."
- **Nguyên nhân**: Chưa thực hiện Bước 2
- **Giải pháp**: Đăng nhập bằng mật khẩu, vào Settings bật fingerprint

### Trường Hợp C: Backend Chưa Bật
- Thông báo: "Tài khoản này chưa bật đăng nhập bằng vân tay trên hệ thống..."
- **Nguyên nhân**: Backend API `checkFingerprintEnabled` trả về `false`
- **Có thể do**:
  - Chưa thực hiện Bước 2 (chưa bật trong Settings)
  - API backend bị lỗi
  - Database chưa được update
- **Giải pháp**: Đăng nhập bằng mật khẩu, vào Settings bật lại fingerprint

### Trường Hợp D: Token Hết Hạn
- Thông báo: "Token đã hết hạn. Vui lòng đăng nhập bằng mật khẩu."
- **Nguyên nhân**: Đã quá 7 ngày kể từ lần đăng nhập cuối
- **Giải pháp**: Đăng nhập bằng mật khẩu để lấy token mới

### Trường Hợp E: Chưa Có Token
- Thông báo: "Chưa có thông tin đăng nhập. Vui lòng đăng nhập bằng mật khẩu."
- **Nguyên nhân**: Chưa từng đăng nhập bằng mật khẩu trên thiết bị này
- **Giải pháp**: Đăng nhập bằng mật khẩu ít nhất 1 lần

## Debug: Kiểm Tra Dữ Liệu Đã Lưu

### Kiểm Tra BiometricPrefs
Sử dụng Android Studio Device File Explorer:
```
/data/data/com.example.mobilebanking/shared_prefs/BiometricPrefs.xml
```

Nội dung cần có:
```xml
<boolean name="biometric_enabled" value="true" />
<string name="temp_refresh_token">eyJhbGc...</string>
<string name="biometric_username">0901234567</string>
<long name="refresh_token_expiry" value="1735123456789" />
```

### Kiểm Tra DataManager
Sử dụng Android Studio Device File Explorer:
```
/data/data/com.example.mobilebanking/shared_prefs/MobileBankingPrefs.xml
```

Sau khi đăng nhập, nội dung cần có:
```xml
<long name="user_id" value="123" />
<string name="user_phone">0901234567</string>
<string name="access_token">eyJhbGc...</string>
<string name="refresh_token">eyJhbGc...</string>
```

### Kiểm Tra Backend
Sử dụng Postman hoặc curl:
```bash
GET http://localhost:8089/api/auth/check-fingerprint-enabled?phone=0901234567
```

Response mong đợi:
```json
{
  "enabled": true
}
```

## Lưu Ý Quan Trọng

1. **Phải đăng nhập bằng mật khẩu trước** để có refresh token
2. **Phải bật fingerprint trong Settings** để update backend
3. **Token hết hạn sau 7 ngày** - cần đăng nhập lại bằng mật khẩu
4. **Mỗi lần đăng nhập bằng mật khẩu** sẽ tạo token mới (reset 7 ngày)
5. **Xóa app data sẽ mất tất cả token** - cần đăng nhập lại

## Troubleshooting

### Lỗi: "Token đã hết hạn"
**Nguyên nhân**: Đã quá 7 ngày kể từ lần đăng nhập cuối
**Giải pháp**: 
1. Đăng nhập bằng mật khẩu
2. Token mới sẽ được tạo và lưu
3. Có thể dùng vân tay trong 7 ngày tiếp theo

### Lỗi: "Chưa bật đăng nhập bằng vân tay trên hệ thống"
**Nguyên nhân**: Backend chưa có flag `fingerprintLoginEnabled = true`
**Giải pháp**:
1. Đăng nhập bằng mật khẩu
2. Vào Settings → Bật "Cài đặt vân tay"
3. Quét vân tay để xác thực
4. Đợi thông báo "Đã bật xác thực sinh trắc học"
5. Đăng xuất và thử lại

### Lỗi: "Không thể cập nhật cài đặt trên server"
**Nguyên nhân**: API backend không phản hồi
**Kiểm tra**:
1. Backend có đang chạy không?
2. IP/URL có đúng không? (kiểm tra `ApiClient.java`)
3. Token có hợp lệ không?
4. Endpoint `/users/{userId}/settings` có tồn tại không?

### Vân tay không hiện trong màn hình login
**Nguyên nhân**: Thiết bị không hỗ trợ hoặc chưa đăng ký vân tay
**Giải pháp**:
1. Vào Settings điện thoại → Security → Fingerprint
2. Đăng ký ít nhất 1 vân tay
3. Khởi động lại app
