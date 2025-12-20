# Quick Summary - Face Login Verification

## Tính Năng
Xác thực khuôn mặt khi đăng nhập bằng tài khoản khác (không phải tài khoản cuối cùng).

## Flow
```
Nhập phone + password
  ↓
phone != lastUsername?
  ├─ NO → Đăng nhập bình thường
  └─ YES → Dialog "Xác Thực Khuôn Mặt"
            ↓
            FaceLoginActivity
            ↓
            Quét khuôn mặt
            ↓
            API: login-with-face
            ↓
            Thành công → Đăng nhập
            Thất bại → Thử lại
```

## Files Mới
- `FaceLoginActivity.java` - Activity xác thực khuôn mặt

## Files Sửa
- `LoginActivity.java` - Thêm logic kiểm tra tài khoản
- `AndroidManifest.xml` - Đăng ký FaceLoginActivity

## API
- **POST** `/auth/login-with-face`
- **Body**: `phone` (text), `facePhoto` (image/jpeg)
- **Response**: `AuthResponse`

## Test
1. Đăng nhập phone A → Đăng xuất
2. Đăng nhập phone B (khác A)
3. ✅ Hiển thị dialog xác thực
4. ✅ Quét khuôn mặt → Đăng nhập thành công

## Layout
Sử dụng chung: `fragment_step5_face_verification.xml`
