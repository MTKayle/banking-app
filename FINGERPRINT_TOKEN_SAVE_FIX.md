# Fix Lỗi "Token Đã Hết Hạn" Ngay Sau Khi Bật Fingerprint

## Vấn Đề
Khi làm theo flow:
1. Đăng nhập bằng mật khẩu
2. Vào Settings → Bật fingerprint
3. Đăng xuất
4. Click icon vân tay để đăng nhập
5. **Lỗi**: "Token đã hết hạn. Vui lòng đăng nhập bằng mật khẩu."

Lỗi này xảy ra ngay cả khi vừa mới đăng nhập bằng mật khẩu (chưa đến 7 ngày).

## Nguyên Nhân

### Flow Cũ (SAI)
```
1. Đăng nhập bằng mật khẩu
   ↓
   Check: biometricManager.isBiometricEnabled() = false
   ↓
   KHÔNG lưu temp_refresh_token ✗
   ↓
2. Vào Settings → Bật fingerprint
   ↓
   Set: biometric_enabled = true
   (Nhưng không có temp_refresh_token)
   ↓
3. Đăng xuất → Click vân tay
   ↓
   Check: biometric_enabled = true ✓
   Check: refresh_token_expiry = 0 (chưa được set) ✗
   ↓
   Lỗi: "Token đã hết hạn"
```

### Vấn Đề Cụ Thể
Trong `LoginActivity.handleLogin()`, code chỉ lưu `temp_refresh_token` khi:
```java
if (biometricManager.isBiometricEnabled()) {
    String refreshToken = dataManager.getRefreshToken();
    if (refreshToken != null) {
        saveRefreshTokenWithoutAuth(refreshToken, finalPhone);
    }
}
```

Nhưng khi đăng nhập lần đầu, `biometric_enabled = false` → Không lưu token → Sau đó bật fingerprint → Không có token để dùng.

## Giải Pháp

### Flow Mới (ĐÚNG)
```
1. Đăng nhập bằng mật khẩu
   ↓
   LUÔN lưu temp_refresh_token (không cần check biometric_enabled)
   Lưu: temp_refresh_token, refresh_token_expiry (7 ngày)
   ↓
2. Vào Settings → Bật fingerprint
   ↓
   Set: biometric_enabled = true
   (Token đã có sẵn từ bước 1)
   ↓
3. Đăng xuất → Click vân tay
   ↓
   Check: biometric_enabled = true ✓
   Check: refresh_token_expiry > now ✓
   Check: temp_refresh_token exists ✓
   ↓
   Đăng nhập thành công!
```

### Code Cũ
```java
// Lưu token từ API response
if (authResponse.getToken() != null && authResponse.getRefreshToken() != null) {
    dataManager.saveTokens(authResponse.getToken(), authResponse.getRefreshToken());
}

// Chỉ lưu temp token nếu đã bật fingerprint
if (biometricManager.isBiometricEnabled()) {
    String refreshToken = dataManager.getRefreshToken();
    if (refreshToken != null) {
        saveRefreshTokenWithoutAuth(refreshToken, finalPhone);
    }
}
```

### Code Mới
```java
// Lưu token từ API response
if (authResponse.getToken() != null && authResponse.getRefreshToken() != null) {
    dataManager.saveTokens(authResponse.getToken(), authResponse.getRefreshToken());
    
    // LUÔN lưu refresh token tạm thời để có thể bật fingerprint sau này
    // Không cần check isBiometricEnabled() vì user có thể bật sau
    saveRefreshTokenWithoutAuth(authResponse.getRefreshToken(), finalPhone);
}
```

## Lợi Ích

### 1. Linh Hoạt Hơn
User có thể:
- Đăng nhập bằng mật khẩu trước
- Bật fingerprint sau (bất cứ lúc nào trong 7 ngày)
- Không cần đăng nhập lại

### 2. UX Tốt Hơn
User không bị bắt buộc phải:
- Bật fingerprint ngay khi đăng nhập
- Đăng nhập lại sau khi bật fingerprint

### 3. Đơn Giản Hơn
Không cần logic phức tạp để check `isBiometricEnabled()` trước khi lưu token.

## So Sánh

### Trước Fix
| Bước | Hành Động | Kết Quả |
|------|-----------|---------|
| 1 | Đăng nhập bằng mật khẩu | Token KHÔNG được lưu (vì chưa bật fingerprint) |
| 2 | Bật fingerprint trong Settings | biometric_enabled = true (nhưng không có token) |
| 3 | Đăng xuất → Click vân tay | ❌ Lỗi "Token đã hết hạn" |

### Sau Fix
| Bước | Hành Động | Kết Quả |
|------|-----------|---------|
| 1 | Đăng nhập bằng mật khẩu | Token LUÔN được lưu (hết hạn sau 7 ngày) |
| 2 | Bật fingerprint trong Settings | biometric_enabled = true (token đã có sẵn) |
| 3 | Đăng xuất → Click vân tay | ✅ Đăng nhập thành công |

## Files Modified
- `FrontEnd/banking-app/app/src/main/java/com/example/mobilebanking/activities/LoginActivity.java`
  - Method: `handleLogin()` → `onResponse()` callback
  - Thay đổi: Luôn gọi `saveRefreshTokenWithoutAuth()` sau khi đăng nhập thành công

## Testing

### Test Case 1: Bật Fingerprint Sau Khi Đăng Nhập
1. Đăng nhập bằng mật khẩu
2. Vào Settings → Bật fingerprint
3. Đăng xuất
4. Click icon vân tay
5. ✅ Đăng nhập thành công (không còn lỗi "Token đã hết hạn")

### Test Case 2: Bật Fingerprint Sau Vài Ngày
1. Đăng nhập bằng mật khẩu (ngày 1)
2. Sử dụng app bình thường
3. Ngày 3: Vào Settings → Bật fingerprint
4. Đăng xuất
5. Click icon vân tay
6. ✅ Đăng nhập thành công (token còn hạn 4 ngày)

### Test Case 3: Token Thực Sự Hết Hạn
1. Đăng nhập bằng mật khẩu
2. Đợi 7 ngày (hoặc thay đổi expiry time trong code để test)
3. Click icon vân tay
4. ❌ Lỗi "Token đã hết hạn" (đúng như mong đợi)
5. Đăng nhập bằng mật khẩu để lấy token mới

## Lưu Ý

### Token Luôn Được Lưu
- Mỗi lần đăng nhập bằng mật khẩu → Token được lưu (hết hạn sau 7 ngày)
- User có thể bật fingerprint bất cứ lúc nào trong 7 ngày
- Sau 7 ngày, phải đăng nhập bằng mật khẩu để lấy token mới

### Bảo Mật
- Token được lưu trong `BiometricPrefs` (SharedPreferences)
- Khi quét vân tay lần đầu, token được chuyển vào Keystore (mã hóa)
- Chỉ có thể giải mã khi quét vân tay

### Storage
- `temp_refresh_token` - Token tạm thời (SharedPreferences)
- `encrypted_refresh_token` - Token mã hóa (Keystore)
- `refresh_token_expiry` - Thời gian hết hạn (timestamp)
- `biometric_enabled` - Flag bật/tắt fingerprint (boolean)

## Tóm Tắt
- **Vấn đề**: Token không được lưu khi đăng nhập nếu chưa bật fingerprint
- **Nguyên nhân**: Code check `isBiometricEnabled()` trước khi lưu token
- **Giải pháp**: Luôn lưu token sau khi đăng nhập, không cần check
- **Kết quả**: User có thể bật fingerprint bất cứ lúc nào và dùng ngay
