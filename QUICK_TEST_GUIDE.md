# 🧪 Hướng Dẫn Test Nhanh

## 📅 Sau Khi Implement Lại Các Chức Năng

---

## 🚀 Bước 1: Build Project

```bash
# Trong Android Studio:
Build → Rebuild Project
```

Hoặc dùng Gradle:
```bash
cd FrontEnd/banking-app
./gradlew clean build
```

---

## 🧪 Bước 2: Test Từng Tính Năng

### Test 1: Fingerprint Backend Sync ⭐ QUAN TRỌNG

#### Bật Fingerprint:
1. Đăng nhập bằng mật khẩu (ví dụ: phone `0901234567`, password `123456`)
2. Vào **Settings** (icon Profile ở bottom navigation)
3. Tìm mục **"Cài đặt sinh trắc học"** hoặc **"Cài đặt vân tay"**
4. Click vào → Quét vân tay
5. ✅ **Kiểm tra:** 
   - Toast hiển thị "Đã bật xác thực sinh trắc học"
   - Backend nhận được request `PATCH /users/{userId}/settings` với `fingerprintLoginEnabled: true`

#### Tắt Fingerprint:
1. Vào **Settings**
2. Click vào **"Cài đặt sinh trắc học"**
3. Dialog hiển thị "Tắt xác thực sinh trắc học"
4. Click "Tắt"
5. ✅ **Kiểm tra:**
   - Toast hiển thị "Đã tắt xác thực sinh trắc học"
   - Backend nhận được request `PATCH /users/{userId}/settings` với `fingerprintLoginEnabled: false`

---

### Test 2: Fingerprint Login với userId ⭐ QUAN TRỌNG

1. Đăng nhập bằng mật khẩu → Bật fingerprint (như Test 1)
2. Đăng xuất
3. Ở màn hình login, click vào **icon vân tay**
4. Quét vân tay
5. ✅ **Kiểm tra:**
   - Đăng nhập thành công
   - userId được lưu (kiểm tra trong Settings → Không hiển thị lỗi "Không tìm thấy thông tin người dùng")
   - Có thể tắt fingerprint trong Settings (không bị lỗi userId null)

---

### Test 3: Refresh Token Luôn Được Lưu ⭐ QUAN TRỌNG

1. Đăng nhập bằng mật khẩu (chưa bật fingerprint)
2. Vào **Settings** → Bật fingerprint
3. Quét vân tay
4. ✅ **Kiểm tra:**
   - Fingerprint được bật thành công (không bị lỗi "Token đã hết hạn")
   - Đăng xuất → Đăng nhập bằng vân tay → Thành công

---

### Test 4: OTP Login Verification - Tài Khoản Khác ⭐ MỚI

#### Trường hợp 1: Đăng nhập tài khoản cuối cùng (KHÔNG cần OTP)
1. Đăng nhập phone A (ví dụ: `0901234567`)
2. Đăng xuất
3. Đăng nhập lại phone A
4. ✅ **Kiểm tra:**
   - Đăng nhập thành công NGAY (không yêu cầu OTP)

#### Trường hợp 2: Đăng nhập tài khoản khác (CẦN OTP)
1. Đăng nhập phone A (ví dụ: `0901234567`)
2. Đăng xuất
3. Đăng nhập phone B (ví dụ: `0987654321`)
4. ✅ **Kiểm tra:**
   - Dialog hiển thị "Xác Thực OTP"
   - Message: "Bạn đang đăng nhập bằng tài khoản khác. Vui lòng xác thực OTP để tiếp tục."
5. Click "Xác Thực"
6. ✅ **Kiểm tra:**
   - Chuyển sang màn hình OTP
   - OTP được gửi đến phone B (qua Goixe247 API)
   - Toast: "Mã OTP đã được gửi đến 0987654321"
7. Nhập OTP (kiểm tra SMS trên điện thoại)
8. ✅ **Kiểm tra:**
   - Xác thực thành công
   - Đăng nhập thành công
   - Chuyển sang màn hình chính

#### Trường hợp 3: OTP sai
1. Làm theo Trường hợp 2 đến bước 6
2. Nhập OTP SAI (ví dụ: `111111`)
3. ✅ **Kiểm tra:**
   - Toast: "Mã OTP không đúng. Vui lòng nhập lại."
   - Các ô OTP được xóa
   - Focus vào ô đầu tiên
4. Nhập OTP ĐÚNG
5. ✅ **Kiểm tra:**
   - Đăng nhập thành công

#### Trường hợp 4: Gửi lại OTP
1. Làm theo Trường hợp 2 đến bước 6
2. Đợi 60 giây (timer hết)
3. Click "Gửi lại OTP"
4. ✅ **Kiểm tra:**
   - OTP mới được gửi
   - Toast: "Mã OTP đã được gửi đến..."
   - Timer reset về 60 giây

---

### Test 5: Forgot Password (Goixe247 OTP)

1. Ở màn hình login, click "Quên mật khẩu"
2. Nhập số điện thoại
3. Click "Gửi OTP"
4. ✅ **Kiểm tra:**
   - Chuyển sang màn hình OTP
   - Toast: "Mã OTP đã được gửi đến..."
5. Nhập OTP đúng
6. ✅ **Kiểm tra:**
   - Chuyển sang màn hình ResetPasswordActivity
   - Có thể đặt lại mật khẩu

---

### Test 6: Movie Booking (Goixe247 OTP)

1. Chọn phim → Chọn suất chiếu → Chọn ghế
2. Nhập thông tin:
   - Họ và tên
   - Số điện thoại
   - Email
3. Check "Tôi đồng ý với điều khoản"
4. Click "Đặt vé"
5. ✅ **Kiểm tra:**
   - Chuyển sang màn hình OTP
   - OTP được gửi (Goixe247)
6. Nhập OTP đúng
7. ✅ **Kiểm tra:**
   - API đặt vé được gọi
   - Chuyển sang màn hình MovieTicketSuccessActivity
   - Hiển thị thông tin vé

---

## 🔍 Kiểm Tra Backend Logs

### Fingerprint Backend Sync:
```
# Khi bật fingerprint:
PATCH /users/{userId}/settings
Body: { "fingerprintLoginEnabled": true }

# Khi tắt fingerprint:
PATCH /users/{userId}/settings
Body: { "fingerprintLoginEnabled": false }
```

### OTP Login Verification:
```
# Gửi OTP:
POST https://otp.goixe247.com/api/otp/request
Body: { "user_id": "13", "api_key": "...", "phone": "0987654321" }

# Xác thực OTP:
POST https://otp.goixe247.com/api/otp/verify
Body: { "user_id": "13", "api_key": "...", "phone": "0987654321", "otp": "123456" }

# Đăng nhập:
POST /auth/login
Body: { "phone": "0987654321", "password": "..." }
```

---

## ⚠️ Lỗi Thường Gặp

### Lỗi 1: "Không tìm thấy thông tin người dùng"
**Nguyên nhân:** userId bị null

**Giải pháp:**
1. Kiểm tra LoginActivity có lưu userId không
2. Kiểm tra backend có trả userId trong AuthResponse không
3. Xem log: `Log.d("LoginActivity", "userId: " + authResponse.getUserId())`

### Lỗi 2: "Token đã hết hạn"
**Nguyên nhân:** Refresh token không được lưu

**Giải pháp:**
1. Kiểm tra LoginActivity có gọi `saveRefreshTokenWithoutAuth()` không
2. Kiểm tra SharedPreferences có `temp_refresh_token` không
3. Xem log: `Log.d("LoginActivity", "Saved refresh token")`

### Lỗi 3: "Chưa bật đăng nhập bằng vân tay trên hệ thống"
**Nguyên nhân:** Backend chưa được update

**Giải pháp:**
1. Kiểm tra SettingsActivity có gọi `enableFingerprintOnBackend()` không
2. Kiểm tra backend có nhận request `PATCH /users/{userId}/settings` không
3. Xem log backend

### Lỗi 4: "Mã OTP không đúng"
**Nguyên nhân:** OTP sai hoặc hết hạn

**Giải pháp:**
1. Kiểm tra SMS trên điện thoại
2. Nhập đúng 6 số OTP
3. Nếu hết hạn, click "Gửi lại OTP"

### Lỗi 5: "Lỗi kết nối"
**Nguyên nhân:** Không kết nối được backend hoặc Goixe247 API

**Giải pháp:**
1. Kiểm tra backend có đang chạy không
2. Kiểm tra IP trong ApiClient có đúng không
3. Kiểm tra internet connection
4. Kiểm tra Goixe247 API key có đúng không

---

## 📊 Checklist Tổng Hợp

### Fingerprint:
- [ ] Bật fingerprint → Backend được update
- [ ] Tắt fingerprint → Backend được update
- [ ] Đăng nhập bằng vân tay → userId được lưu
- [ ] Refresh token luôn được lưu khi đăng nhập

### OTP Login:
- [ ] Đăng nhập tài khoản cuối cùng → Không cần OTP
- [ ] Đăng nhập tài khoản khác → Yêu cầu OTP
- [ ] OTP đúng → Đăng nhập thành công
- [ ] OTP sai → Hiển thị lỗi, cho nhập lại
- [ ] Gửi lại OTP → Nhận OTP mới

### Forgot Password:
- [ ] Gửi OTP → Nhận được SMS (Goixe247)
- [ ] OTP đúng → Chuyển sang ResetPassword

### Movie Booking:
- [ ] Gửi OTP → Nhận được SMS (Goixe247)
- [ ] OTP đúng → Đặt vé thành công

---

## 🎉 Kết Luận

Nếu tất cả các test case đều PASS:
✅ **Các chức năng đã được implement lại thành công!**

Nếu có test case FAIL:
❌ Xem phần "Lỗi Thường Gặp" để troubleshoot

**Good luck! 🚀**

