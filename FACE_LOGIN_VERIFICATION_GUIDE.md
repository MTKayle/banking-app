# Hướng Dẫn Xác Thực Khuôn Mặt Khi Đăng Nhập

## Tổng Quan
Khi người dùng đăng nhập bằng tài khoản khác (không phải tài khoản cuối cùng), hệ thống sẽ yêu cầu xác thực khuôn mặt để đảm bảo an toàn.

## Flow Hoạt Động

### Trường Hợp 1: Đăng Nhập Bằng Tài Khoản Cuối Cùng
```
User nhập phone + password
  ↓
Check: phone == lastUsername? → YES
  ↓
Đăng nhập bình thường (không cần xác thực khuôn mặt)
  ↓
Vào màn hình chính
```

### Trường Hợp 2: Đăng Nhập Bằng Tài Khoản Khác
```
User nhập phone + password
  ↓
Check: phone == lastUsername? → NO
  ↓
Hiển thị dialog: "Bạn đang đăng nhập bằng tài khoản khác. Vui lòng xác thực khuôn mặt."
  ↓
User click "Xác Thực"
  ↓
Chuyển sang FaceLoginActivity
  ↓
User đặt khuôn mặt vào khung hình
  ↓
Hệ thống tự động chụp ảnh
  ↓
Gọi API: POST /auth/login-with-face
  ↓
Backend xác thực khuôn mặt
  ↓
  ├─ Thành công → Đăng nhập → Vào màn hình chính
  └─ Thất bại → Hiển thị dialog "Xác Thực Thất Bại" → Cho thử lại
```

## Files Tạo Mới

### 1. FaceLoginActivity.java
**Chức năng:**
- Sử dụng layout `fragment_step5_face_verification.xml` (dùng chung với step 5 đăng ký)
- Mở camera trước để quét khuôn mặt
- Phát hiện khuôn mặt bằng ML Kit Face Detection
- Tự động chụp ảnh khi khuôn mặt đủ lớn (≥60% khung hình)
- Gọi API `login-with-face` để xác thực
- Xử lý kết quả: thành công → đăng nhập, thất bại → cho thử lại

**Các Method Chính:**
- `startCamera()` - Khởi động camera
- `analyzeFace()` - Phát hiện khuôn mặt realtime
- `captureFace()` - Chụp ảnh khuôn mặt
- `verifyFaceWithApi()` - Gọi API xác thực
- `handleLoginSuccess()` - Xử lý đăng nhập thành công
- `handleLoginFailure()` - Xử lý xác thực thất bại

## Files Đã Sửa

### 1. LoginActivity.java
**Thay đổi:**
- Thêm logic kiểm tra tài khoản cuối cùng trong `handleLogin()`
- Nếu không phải tài khoản cuối cùng → Hiển thị dialog xác thực khuôn mặt
- Tách logic đăng nhập thành method `performPasswordLogin()`

**Code Mới:**
```java
// Kiểm tra xem có phải tài khoản cuối cùng không
String lastUsername = dataManager.getLastUsername();

if (lastUsername != null && !lastUsername.isEmpty() && !phone.equals(lastUsername)) {
    // Không phải tài khoản cuối cùng → Yêu cầu xác thực khuôn mặt
    new AlertDialog.Builder(this)
        .setTitle("Xác Thực Khuôn Mặt")
        .setMessage("Bạn đang đăng nhập bằng tài khoản khác. Vui lòng xác thực khuôn mặt để tiếp tục.")
        .setPositiveButton("Xác Thực", (dialog, which) -> {
            Intent intent = new Intent(LoginActivity.this, FaceLoginActivity.class);
            intent.putExtra(FaceLoginActivity.EXTRA_PHONE, phone);
            intent.putExtra(FaceLoginActivity.EXTRA_PASSWORD, password);
            startActivity(intent);
        })
        .setNegativeButton("Hủy", null)
        .show();
    return;
}

// Tài khoản cuối cùng → Đăng nhập bình thường
performPasswordLogin(phone, password);
```

### 2. AndroidManifest.xml
**Thêm:**
```xml
<activity
    android:name="com.example.mobilebanking.activities.FaceLoginActivity"
    android:parentActivityName="com.example.mobilebanking.activities.LoginActivity"
    android:screenOrientation="portrait" />
```

## Backend API

### Endpoint: POST /auth/login-with-face
**Request:**
- Method: POST
- Content-Type: multipart/form-data
- Body:
  - `phone` (text/plain): Số điện thoại
  - `facePhoto` (image/jpeg): Ảnh khuôn mặt

**Response (Success):**
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

**Response (Failure):**
```json
{
  "message": "Khuôn mặt không khớp",
  "error": "FACE_MISMATCH"
}
```

## Giao Diện

### Layout: fragment_step5_face_verification.xml
**Các thành phần:**
- `PreviewView` - Hiển thị camera preview
- `FaceDetectionOverlay` - Vẽ khung khuôn mặt
- `TextView (tv_title)` - Tiêu đề "Xác Thực Khuôn Mặt"
- `TextView (tv_instruction)` - Hướng dẫn người dùng
- `ProgressBar` - Hiển thị khi đang xử lý

**Trạng thái hướng dẫn:**
- "Đặt khuôn mặt vào khung hình" - Chưa phát hiện khuôn mặt
- "Không phát hiện khuôn mặt" - Không có khuôn mặt trong khung
- "Di chuyển gần hơn" - Khuôn mặt quá nhỏ (<60%)
- "Giữ nguyên tư thế..." - Khuôn mặt đủ lớn, đang đếm ngược
- "Đang chụp..." - Đang chụp ảnh
- "Đang xác thực..." - Đang gọi API

## Testing

### Test Case 1: Đăng Nhập Tài Khoản Cuối Cùng
1. Đăng nhập bằng phone A + password
2. Đăng xuất
3. Đăng nhập lại bằng phone A + password
4. ✅ Đăng nhập thành công (không cần xác thực khuôn mặt)

### Test Case 2: Đăng Nhập Tài Khoản Khác - Thành Công
1. Đăng nhập bằng phone A + password
2. Đăng xuất
3. Đăng nhập bằng phone B + password (tài khoản khác)
4. ✅ Hiển thị dialog "Xác Thực Khuôn Mặt"
5. Click "Xác Thực"
6. ✅ Chuyển sang màn hình xác thực khuôn mặt
7. Đặt khuôn mặt vào khung hình
8. ✅ Tự động chụp ảnh
9. ✅ Xác thực thành công → Đăng nhập

### Test Case 3: Đăng Nhập Tài Khoản Khác - Thất Bại
1. Đăng nhập bằng phone A + password
2. Đăng xuất
3. Đăng nhập bằng phone B + password (tài khoản khác)
4. Click "Xác Thực"
5. Đặt khuôn mặt KHÁC vào khung hình
6. ✅ Hiển thị dialog "Xác Thực Thất Bại"
7. Click "Thử Lại"
8. ✅ Quay lại màn hình xác thực
9. Đặt khuôn mặt ĐÚNG vào khung hình
10. ✅ Xác thực thành công → Đăng nhập

### Test Case 4: Hủy Xác Thực
1. Đăng nhập bằng phone B (tài khoản khác)
2. Hiển thị dialog "Xác Thực Khuôn Mặt"
3. Click "Hủy"
4. ✅ Quay lại màn hình login (không đăng nhập)

### Test Case 5: Lần Đầu Đăng Nhập
1. Cài app mới (chưa có lastUsername)
2. Đăng nhập bằng phone A + password
3. ✅ Đăng nhập thành công (không cần xác thực khuôn mặt)
4. Đăng xuất
5. Đăng nhập lại bằng phone A
6. ✅ Đăng nhập thành công (không cần xác thực)
7. Đăng nhập bằng phone B
8. ✅ Yêu cầu xác thực khuôn mặt

## Lưu Ý Quan Trọng

### 1. Quyền Camera
- App cần quyền CAMERA để xác thực khuôn mặt
- Nếu user từ chối → Không thể xác thực → Quay lại login

### 2. Điều Kiện Xác Thực
- Khuôn mặt phải chiếm ≥60% khung hình
- Giữ nguyên tư thế 3 giây trước khi chụp
- Chỉ chụp 1 khuôn mặt (nếu có nhiều khuôn mặt, chọn khuôn mặt đầu tiên)

### 3. Xử Lý Lỗi
- Lỗi camera → Toast thông báo
- Lỗi chụp ảnh → Cho thử lại
- Lỗi API → Hiển thị dialog với nút "Thử Lại" và "Hủy"
- Lỗi kết nối → Hiển thị thông báo lỗi

### 4. Bảo Mật
- Password được truyền qua Intent (trong memory, không lưu file)
- Ảnh khuôn mặt được lưu tạm trong cache, tự động xóa khi app đóng
- Backend xác thực khuôn mặt với ảnh đã đăng ký

### 5. UX
- Dialog không thể dismiss bằng cách click outside
- Phải click "Xác Thực" hoặc "Hủy" để đóng dialog
- Khi xác thực thất bại, cho phép thử lại nhiều lần
- Có thể click "Hủy" để quay lại login bất cứ lúc nào

## Troubleshooting

### Lỗi: "Không thể khởi động camera"
→ Kiểm tra quyền CAMERA đã được cấp chưa

### Lỗi: "Không phát hiện khuôn mặt"
→ Đảm bảo có đủ ánh sáng và khuôn mặt nhìn thẳng vào camera

### Lỗi: "Xác thực khuôn mặt thất bại"
→ Khuôn mặt không khớp với ảnh đã đăng ký. Kiểm tra:
- Đúng tài khoản không?
- Đã đăng ký khuôn mặt chưa?
- Backend có ảnh khuôn mặt của user không?

### Lỗi: "Lỗi kết nối"
→ Backend không phản hồi. Kiểm tra:
- Backend có đang chạy không?
- IP/URL có đúng không?
- Endpoint `/auth/login-with-face` có tồn tại không?

## Tóm Tắt
- ✅ Xác thực khuôn mặt khi đăng nhập bằng tài khoản khác
- ✅ Sử dụng chung layout với step 5 đăng ký
- ✅ Tự động phát hiện và chụp khuôn mặt
- ✅ Gọi API `login-with-face` để xác thực
- ✅ Xử lý thành công/thất bại với UX tốt
- ✅ Cho phép thử lại nhiều lần khi thất bại
