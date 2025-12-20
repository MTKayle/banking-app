# Lỗi "Token Đã Hết Hạn" Khi Đăng Nhập Bằng Vân Tay

## Vấn Đề
Khi click vào icon vân tay trong màn hình login, nhận được thông báo:
> "Token đã hết hạn. Vui lòng đăng nhập bằng mật khẩu."

## Nguyên Nhân

### 1. Token Thực Sự Đã Hết Hạn (Phổ Biến Nhất)
Refresh token có thời hạn **7 ngày** kể từ lần đăng nhập cuối bằng mật khẩu.

**Kiểm tra:**
- Lần cuối đăng nhập bằng mật khẩu là khi nào?
- Đã quá 7 ngày chưa?

### 2. Chưa Từng Đăng Nhập Bằng Mật Khẩu
Nếu mới cài app hoặc xóa data, chưa có refresh token nào được lưu.

### 3. Dữ Liệu Bị Xóa
- Xóa app data/cache
- Uninstall và reinstall app
- Clear storage trong Settings điện thoại

## Giải Pháp

### Bước 1: Đăng Nhập Bằng Mật Khẩu
1. Ở màn hình login, nhập số điện thoại và mật khẩu
2. Click "Đăng Nhập"
3. Đăng nhập thành công

**Điều gì xảy ra:**
- App lưu refresh token mới (hết hạn sau 7 ngày)
- App lưu access token
- App lưu thông tin user (userId, phone, fullName)

### Bước 2: Kiểm Tra Fingerprint Đã Bật Chưa
1. Vào màn hình "Hồ Sơ" (Profile/Settings)
2. Tìm "Cài đặt vân tay" hoặc "Cài đặt sinh trắc học"
3. Nếu chưa bật, click vào và quét vân tay để bật
4. Đợi thông báo "Đã bật xác thực sinh trắc học"

### Bước 3: Đăng Xuất và Thử Lại
1. Đăng xuất khỏi app
2. Ở màn hình login, click icon vân tay
3. Quét vân tay
4. Đăng nhập thành công

## Lưu Ý Quan Trọng

### Token Hết Hạn Sau 7 Ngày
- Mỗi lần đăng nhập bằng mật khẩu = reset lại 7 ngày
- Đăng nhập bằng vân tay KHÔNG reset thời hạn
- Sau 7 ngày, BẮT BUỘC phải đăng nhập bằng mật khẩu 1 lần

### Ví Dụ Timeline
```
Ngày 1: Đăng nhập bằng mật khẩu → Token hết hạn ngày 8
Ngày 2-7: Có thể dùng vân tay
Ngày 8: Token hết hạn → Phải đăng nhập bằng mật khẩu
Ngày 8 (sau login): Token mới hết hạn ngày 15
```

### Tại Sao Có Giới Hạn 7 Ngày?
- **Bảo mật**: Refresh token không nên tồn tại mãi mãi
- **Best practice**: Yêu cầu xác thực lại định kỳ
- **Tuân thủ**: Nhiều ngân hàng yêu cầu đăng nhập lại sau 7-30 ngày

## Cách Kiểm Tra Token Còn Hạn Không

### Sử dụng Android Studio
1. Mở Device File Explorer
2. Đi đến: `/data/data/com.example.mobilebanking/shared_prefs/BiometricPrefs.xml`
3. Tìm dòng: `<long name="refresh_token_expiry" value="..." />`
4. Copy giá trị (ví dụ: `1735123456789`)
5. Vào https://www.epochconverter.com/
6. Paste giá trị và convert
7. So sánh với thời gian hiện tại

### Ví Dụ
```xml
<long name="refresh_token_expiry" value="1735123456789" />
```
- Convert: `2024-12-25 10:30:56 GMT`
- Nếu hôm nay là `2024-12-26` → Token đã hết hạn
- Nếu hôm nay là `2024-12-24` → Token còn hạn

## Thay Đổi Thời Hạn Token (Nếu Cần)

### Trong BiometricAuthManager.java
```java
// Dòng 51-52
private static final long REFRESH_TOKEN_VALIDITY_DAYS = 7;
private static final long REFRESH_TOKEN_VALIDITY_MS = REFRESH_TOKEN_VALIDITY_DAYS * 24 * 60 * 60 * 1000L;
```

Thay đổi `7` thành số ngày mong muốn (ví dụ: `30` cho 30 ngày).

**Lưu ý**: Thời hạn càng dài, bảo mật càng thấp.

## Tóm Tắt
- **Lỗi**: Token đã hết hạn
- **Nguyên nhân**: Đã quá 7 ngày kể từ lần đăng nhập cuối bằng mật khẩu
- **Giải pháp**: Đăng nhập bằng mật khẩu để lấy token mới
- **Phòng tránh**: Đăng nhập bằng mật khẩu ít nhất 1 lần mỗi 7 ngày
