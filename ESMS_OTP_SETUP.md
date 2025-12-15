# Hướng Dẫn Cấu Hình eSMS API để Gửi OTP

## Tổng Quan

Ứng dụng đã được tích hợp eSMS API để gửi mã OTP thực tế qua SMS. Bạn cần cấu hình ApiKey và SecretKey để sử dụng.

## Các Bước Cấu Hình

### 1. Đăng Ký Tài Khoản eSMS

1. Truy cập: https://esms.vn/
2. Đăng ký tài khoản mới hoặc đăng nhập
3. Lấy **ApiKey** và **SecretKey** từ trang quản lý

### 2. Cấu Hình Trong App

**Cách 1: Cấu hình khi chạy app (Khuyên dùng)**
- Khi mở màn hình OTP Verification lần đầu, app sẽ hiển thị dialog yêu cầu nhập ApiKey và SecretKey
- Nhập thông tin và bấm "Lưu"
- App sẽ tự động lưu và sử dụng cho các lần sau

**Cách 2: Cấu hình trong code**
- Mở file: `app/src/main/java/com/example/mobilebanking/utils/ESmsConfig.java`
- Thay đổi các giá trị mặc định:
  ```java
  private static final String DEFAULT_API_KEY = "YOUR_API_KEY_HERE";
  private static final String DEFAULT_SECRET_KEY = "YOUR_SECRET_KEY_HERE";
  ```

### 3. Chế Độ Test (Sandbox)

- **Sandbox = 1**: Môi trường test, không gửi tin thực, không trừ tiền
- **Sandbox = 0**: Môi trường thực, gửi tin thực, trừ tiền

Mặc định app sử dụng **Sandbox = 1** để test. Khi đã test xong, đổi sang **Sandbox = 0**.

## Lưu Ý Quan Trọng

### Nội Dung Tin Nhắn Test

Theo tài liệu eSMS, nội dung tin nhắn test phải đúng như sau:
```
"{CODE} la ma xac minh dang ky Baotrixemay cua ban"
```

- `{CODE}` sẽ được thay thế bằng mã OTP 6 số
- Brandname mặc định: `Baotrixemay` (để test)
- **KHÔNG ĐƯỢC** thay đổi nội dung này nếu chưa đăng ký với eSMS

### Để Sử Dụng Brandname và Nội Dung Riêng

1. Liên hệ nhân viên chăm sóc khách hàng eSMS
2. Đăng ký Brandname và nội dung tin nhắn của bạn
3. Sau khi được duyệt, cập nhật trong code:
   - `ESmsConfig.setBrandname("YourBrandname")`
   - Cập nhật nội dung trong `SmsService.java`

## Cách Test

### Test với Sandbox (Khuyên dùng)

1. Đảm bảo `Sandbox = 1` trong `ESmsConfig.java`
2. Nhập ApiKey và SecretKey (có thể dùng bất kỳ giá trị nào khi test)
3. Gửi OTP - sẽ không gửi tin thực nhưng API sẽ trả về thành công
4. Mã OTP sẽ hiển thị trong Toast và Logcat để bạn test

### Test với Môi Trường Thực

1. Đổi `Sandbox = 0` trong `ESmsConfig.java`
2. Nhập ApiKey và SecretKey thực từ eSMS
3. Gửi OTP - sẽ gửi tin thực đến số điện thoại
4. Kiểm tra tin nhắn trên điện thoại

## Cấu Trúc Code

### Files Đã Tạo

1. **ESmsApiService.java** - Interface Retrofit cho eSMS API
2. **ESmsRequest.java** - Model request
3. **ESmsResponse.java** - Model response
4. **ESmsConfig.java** - Quản lý cấu hình (ApiKey, SecretKey)
5. **OtpManager.java** - Quản lý OTP (generate, save, verify)
6. **SmsService.java** - Service gửi OTP qua eSMS
7. **OtpVerificationActivity.java** - Đã cập nhật để gửi OTP thực

### Luồng Hoạt Động

1. User nhập số điện thoại → App gọi `SmsService.sendOtp()`
2. `SmsService` generate mã OTP 6 số
3. Lưu OTP vào SharedPreferences với expiry time (5 phút)
4. Gửi request đến eSMS API
5. eSMS gửi SMS đến số điện thoại
6. User nhập OTP → App verify với OTP đã lưu
7. Nếu đúng → Xác thực thành công

## Troubleshooting

### Lỗi "Brand name code is not exist" (Code 104)
- Brandname chưa được đăng ký hoặc chưa active
- Giải pháp: Sử dụng Brandname mặc định "Baotrixemay" để test

### Lỗi "ApiKey/SecretKey không đúng"
- Kiểm tra lại ApiKey và SecretKey
- Đảm bảo đã copy đầy đủ, không có khoảng trắng

### Không nhận được SMS
- Kiểm tra số điện thoại có đúng format không (0901234567)
- Kiểm tra tài khoản eSMS còn tiền không
- Kiểm tra Sandbox mode (nếu = 1 thì không gửi tin thực)

### OTP hết hạn
- OTP có thời hạn 5 phút
- Sau 5 phút phải gửi lại OTP mới

## Liên Hệ Hỗ Trợ

- Website: https://esms.vn/
- Hỗ trợ kỹ thuật: Liên hệ qua website sau khi đăng nhập

