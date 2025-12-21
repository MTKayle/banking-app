# Fix Lỗi "Không Tìm Thấy Thông Tin Người Dùng" Trong Settings

## Vấn Đề
Khi vào Settings để tắt fingerprint, nhận được lỗi:
> "Không tìm thấy thông tin người dùng"

Điều này xảy ra khi:
1. Đăng nhập bằng vân tay (fingerprint login)
2. Vào Settings
3. Cố gắng tắt fingerprint

## Nguyên Nhân
Khi đăng nhập bằng vân tay, `LoginActivity` gọi API `refresh-token` để lấy access token mới. Tuy nhiên, code chỉ lưu `token` và `refreshToken`, **KHÔNG lưu `userId`** và các thông tin khác từ `AuthResponse`.

### Flow Gây Lỗi
```
1. Đăng nhập bằng mật khẩu → userId được lưu ✓
2. Đăng xuất → userId bị xóa
3. Đăng nhập bằng vân tay → Gọi refresh-token API
4. API trả về AuthResponse với userId
5. Code chỉ lưu token, KHÔNG lưu userId ✗
6. Vào Settings → userId = null → Lỗi!
```

## Giải Pháp
Cập nhật `LoginActivity.java` để lưu đầy đủ thông tin từ `AuthResponse` khi đăng nhập bằng vân tay.

### Code Cũ (Thiếu)
```java
AuthResponse authResponse = response.body();

// Lưu token mới
dataManager.saveTokens(authResponse.getToken(), authResponse.getRefreshToken());

// Lưu lại refresh token mới vào temp storage
saveRefreshTokenWithoutAuth(authResponse.getRefreshToken(), username);
```

### Code Mới (Đầy Đủ)
```java
AuthResponse authResponse = response.body();

// Lưu token mới
dataManager.saveTokens(authResponse.getToken(), authResponse.getRefreshToken());

// Lưu userId và thông tin user từ AuthResponse
if (authResponse.getUserId() != null) {
    dataManager.saveUserId(authResponse.getUserId());
}
if (authResponse.getPhone() != null) {
    dataManager.saveUserPhone(authResponse.getPhone());
}
if (authResponse.getFullName() != null) {
    dataManager.saveUserFullName(authResponse.getFullName());
}
if (authResponse.getEmail() != null) {
    dataManager.saveUserEmail(authResponse.getEmail());
}

// Lưu lại refresh token mới vào temp storage
saveRefreshTokenWithoutAuth(authResponse.getRefreshToken(), username);
```

## Backend API Response
API `POST /auth/refresh-token` trả về `AuthResponse` với các field:
```json
{
  "token": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "type": "Bearer",
  "userId": 123,
  "email": "user@example.com",
  "fullName": "Nguyen Van A",
  "phone": "0901234567",
  "role": "CUSTOMER"
}
```

Tất cả các field này đều cần được lưu vào `DataManager` để sử dụng trong app.

## Files Modified
- `FrontEnd/banking-app/app/src/main/java/com/example/mobilebanking/activities/LoginActivity.java`
  - Method: `startBiometricFlow()` → `onResponse()` callback của `refreshToken()` API

## Testing
### Bước 1: Đăng nhập bằng mật khẩu
1. Mở app
2. Nhập số điện thoại và mật khẩu
3. Đăng nhập thành công

### Bước 2: Bật fingerprint
1. Vào Settings
2. Bật "Cài đặt vân tay"
3. Quét vân tay
4. Thấy thông báo "Đã bật xác thực sinh trắc học"

### Bước 3: Đăng xuất
1. Ở Settings, click "Đăng Xuất"
2. Quay về màn hình login

### Bước 4: Đăng nhập bằng vân tay
1. Click icon vân tay
2. Quét vân tay
3. Đăng nhập thành công

### Bước 5: Kiểm tra Settings (TEST CHÍNH)
1. Vào Settings
2. Click "Cài đặt vân tay" để tắt
3. **Kiểm tra**: KHÔNG còn lỗi "Không tìm thấy thông tin người dùng"
4. Xác nhận tắt
5. Thấy thông báo "Đã tắt xác thực sinh trắc học"

## So Sánh: Đăng Nhập Bằng Mật Khẩu vs Vân Tay

### Đăng Nhập Bằng Mật Khẩu
- API: `POST /auth/login`
- Response: `AuthResponse` với đầy đủ thông tin
- Code: Lưu đầy đủ userId, phone, fullName, email, tokens ✓

### Đăng Nhập Bằng Vân Tay (Trước Fix)
- API: `POST /auth/refresh-token`
- Response: `AuthResponse` với đầy đủ thông tin
- Code: Chỉ lưu tokens, KHÔNG lưu userId ✗

### Đăng Nhập Bằng Vân Tay (Sau Fix)
- API: `POST /auth/refresh-token`
- Response: `AuthResponse` với đầy đủ thông tin
- Code: Lưu đầy đủ userId, phone, fullName, email, tokens ✓

## Lưu Ý
- Fix này đảm bảo rằng dù đăng nhập bằng cách nào (mật khẩu hoặc vân tay), app đều có đầy đủ thông tin user
- `userId` là bắt buộc để gọi API `PATCH /users/{userId}/settings` khi bật/tắt fingerprint
- Nếu thiếu `userId`, mọi tính năng cần userId (Settings, Profile, v.v.) đều sẽ bị lỗi

## Tóm Tắt
- **Lỗi**: "Không tìm thấy thông tin người dùng" khi tắt fingerprint trong Settings
- **Nguyên nhân**: Đăng nhập bằng vân tay không lưu userId
- **Giải pháp**: Lưu đầy đủ thông tin từ AuthResponse khi refresh token
- **Kết quả**: Có thể bật/tắt fingerprint bình thường sau khi đăng nhập bằng vân tay
