# Hướng Dẫn Test Chức Năng Quên Mật Khẩu

## Chuẩn Bị
1. Đảm bảo backend đang chạy
2. Đảm bảo app đã được build và cài đặt trên thiết bị
3. Có một tài khoản đã đăng ký với số điện thoại hợp lệ

## Các Bước Test

### 1. Vào Màn Hình Quên Mật Khẩu
1. Mở app
2. Ở màn hình đăng nhập, click vào "Quên mật khẩu?"
3. Màn hình `ForgotPasswordActivity` sẽ hiển thị

### 2. Nhập Số Điện Thoại
1. Nhập số điện thoại đã đăng ký (ví dụ: `0123456789`)
2. Click nút "Gửi OTP"
3. Kiểm tra:
   - ✅ Loading indicator hiển thị
   - ✅ Nhận được thông báo "Đã gửi mã OTP thành công"
   - ✅ Chuyển sang màn hình OTP Verification

### 3. Màn Hình OTP Verification
Kiểm tra giao diện:
- ✅ Tiêu đề "Xác Thực OTP"
- ✅ Hiển thị số điện thoại đã nhập
- ✅ 6 ô nhập OTP
- ✅ Nút "Xác nhận"
- ✅ Nút "Gửi lại OTP"
- ✅ Đếm ngược thời gian (60 giây)

### 4. Test Nhập OTP Đúng
1. Kiểm tra SMS trên điện thoại để lấy mã OTP
2. Nhập 6 số OTP vào các ô
3. Click nút "Xác nhận"
4. Kiểm tra:
   - ✅ Loading indicator hiển thị
   - ✅ Thông báo "Xác thực OTP thành công!"
   - ✅ Chuyển sang màn hình Reset Password

### 5. Test Nhập OTP Sai
1. Nhập 6 số OTP sai (ví dụ: `111111`)
2. Click nút "Xác nhận"
3. Kiểm tra:
   - ✅ Thông báo "Mã OTP không đúng. Vui lòng nhập lại."
   - ✅ Tất cả các ô OTP bị xóa
   - ✅ Focus vào ô đầu tiên
   - ✅ KHÔNG chuyển sang màn hình khác
   - ✅ Có thể nhập lại OTP

### 6. Test Gửi Lại OTP
1. Đợi 60 giây hoặc nhập sai OTP
2. Click nút "Gửi lại OTP"
3. Kiểm tra:
   - ✅ Loading indicator hiển thị
   - ✅ Thông báo "Mã OTP đã được gửi đến [số điện thoại]"
   - ✅ Tất cả các ô OTP bị xóa
   - ✅ Timer reset về 60 giây
   - ✅ Focus vào ô đầu tiên
   - ✅ Nhận được SMS OTP mới

### 7. Test Auto-Focus
1. Nhập số vào ô đầu tiên
2. Kiểm tra:
   - ✅ Tự động chuyển sang ô thứ 2
3. Tiếp tục nhập cho đến ô thứ 6
4. Kiểm tra:
   - ✅ Mỗi ô chỉ chấp nhận 1 số
   - ✅ Tự động chuyển focus khi nhập xong

### 8. Test Đổi Mật Khẩu
1. Sau khi xác thực OTP thành công
2. Màn hình Reset Password hiển thị
3. Nhập mật khẩu mới (ít nhất 6 ký tự)
4. Nhập lại mật khẩu xác nhận
5. Click "Đặt lại mật khẩu"
6. Kiểm tra:
   - ✅ Thông báo "Đổi mật khẩu thành công! Vui lòng đăng nhập lại."
   - ✅ Chuyển về màn hình đăng nhập
   - ✅ Có thể đăng nhập với mật khẩu mới

## Test Cases Đặc Biệt

### Test Lỗi Mạng
1. Tắt wifi/data
2. Thử gửi OTP hoặc xác thực
3. Kiểm tra:
   - ✅ Thông báo lỗi kết nối rõ ràng
   - ✅ Không bị crash
   - ✅ Có thể thử lại khi có mạng

### Test Số Điện Thoại Không Hợp Lệ
1. Nhập số điện thoại sai định dạng
2. Click "Gửi OTP"
3. Kiểm tra:
   - ✅ Thông báo lỗi "Số điện thoại không hợp lệ"
   - ✅ Không gọi API

### Test Back Button
1. Ở màn hình OTP, click nút Back
2. Kiểm tra:
   - ✅ Quay về màn hình Forgot Password
   - ✅ Timer bị hủy (không leak memory)

## Kết Quả Mong Đợi

### Flow Thành Công
```
LoginActivity 
  → ForgotPasswordActivity (nhập SĐT)
  → OtpVerificationActivity (nhập OTP đúng)
  → ResetPasswordActivity (đổi mật khẩu)
  → LoginActivity (đăng nhập với mật khẩu mới)
```

### Flow OTP Sai
```
OtpVerificationActivity (nhập OTP sai)
  → Xóa input
  → Yêu cầu nhập lại
  → Vẫn ở màn hình OtpVerificationActivity
```

### Flow Gửi Lại OTP
```
OtpVerificationActivity
  → Click "Gửi lại OTP"
  → Xóa input cũ
  → Nhận OTP mới
  → Nhập OTP mới
  → Xác thực thành công
```

## Lưu Ý Quan Trọng
- ⚠️ OTP có thời hạn (thường 5-10 phút)
- ⚠️ Mỗi số điện thoại chỉ có thể gửi OTP giới hạn số lần trong 1 ngày
- ⚠️ Mật khẩu mới phải khác mật khẩu cũ (tùy backend)
- ⚠️ Mật khẩu mới phải ít nhất 6 ký tự

## Troubleshooting

### Không Nhận Được OTP
1. Kiểm tra số điện thoại đã đúng định dạng
2. Kiểm tra kết nối mạng
3. Kiểm tra backend có đang chạy
4. **Kiểm tra Logcat:**
   ```
   D/OtpVerification: Sending OTP to: [phone]
   D/OtpVerification: OTP Request Response Code: 200
   D/OtpVerification: OTP Response: success=true, message=...
   ```
5. Nếu Response Code khác 200 → Kiểm tra API credentials
6. Nếu success=false → Đọc message để biết lỗi

### OTP Luôn Sai
1. Kiểm tra OTP trong SMS
2. Đảm bảo nhập đúng 6 số
3. Kiểm tra OTP chưa hết hạn (thường 5-10 phút)
4. **Kiểm tra Logcat:**
   ```
   D/OtpVerification: Verifying OTP: [otp] for phone: [phone]
   D/OtpVerification: OTP Verify Response Code: 200
   D/OtpVerification: OTP Verify Response: success=false, message=...
   ```
5. Thử gửi lại OTP mới

### Không Gửi Lại Được OTP
1. Đợi hết countdown (60 giây)
2. **Kiểm tra Logcat:**
   ```
   D/OtpVerification: Resending OTP for flow: forgot_password
   D/OtpVerification: Sending OTP to: [phone]
   ```
3. Kiểm tra kết nối mạng
4. Kiểm tra có giới hạn số lần gửi OTP không

### Không Chuyển Màn Hình
1. Kiểm tra log để xem lỗi
2. Kiểm tra response từ API
3. Đảm bảo backend trả về đúng format
4. **Kiểm tra Logcat để xem có exception không**

## API Endpoints
- **Gửi OTP:** `POST https://otp.goixe247.com/request_otp.php`
  - Fields: `user_id`, `api_key`, `recipient_phone`
- **Xác thực OTP:** `POST https://otp.goixe247.com/verify_otp.php`
  - Fields: `user_id`, `api_key`, `recipient_phone`, `otp_code`
- **Đổi mật khẩu:** `POST [Backend]/api/password/change`

## Debug với Logcat

### Xem Log OTP
```bash
adb logcat | grep OtpVerification
```

### Log Mẫu Khi Thành Công
```
D/OtpVerification: Sending OTP to: 0123456789
D/OtpVerification: OTP Request Response Code: 200
D/OtpVerification: OTP Response: success=true, message=OTP sent successfully

D/OtpVerification: Verifying OTP: 123456 for phone: 0123456789
D/OtpVerification: OTP Verify Response Code: 200
D/OtpVerification: OTP Verify Response: success=true, message=OTP verified
```

### Log Mẫu Khi Lỗi
```
D/OtpVerification: Verifying OTP: 111111 for phone: 0123456789
D/OtpVerification: OTP Verify Response Code: 200
D/OtpVerification: OTP Verify Response: success=false, message=Invalid OTP code
```
