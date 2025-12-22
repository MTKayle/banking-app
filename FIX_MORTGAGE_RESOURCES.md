# Sửa lỗi Resource cho Mortgage Loan

## Lỗi đã gặp
```
error: resource color/bidv_bg_gray not found
error: resource drawable/ic_profile not found
```

## Đã sửa

### 1. Thêm màu bidv_bg_gray vào colors.xml
```xml
<color name="bidv_bg_gray">#EEEEEE</color>
```

### 2. Tạo icon ic_profile.xml
Đã tạo file `app/src/main/res/drawable/ic_profile.xml` với icon người dùng.

### 3. Tạo circle_background.xml
Đã tạo file `app/src/main/res/drawable/circle_background.xml` cho background hình tròn.

## Hướng dẫn build

### Cách 1: Sử dụng Android Studio
1. Mở Android Studio
2. File → Sync Project with Gradle Files
3. Build → Rebuild Project
4. Run app

### Cách 2: Sử dụng Command Line
Mở Command Prompt (CMD) trong thư mục `FrontEnd/banking-app`:

```cmd
gradlew.bat assembleDebug
```

Hoặc build và install trực tiếp:
```cmd
gradlew.bat installDebug
```

### Cách 3: Sử dụng file bat có sẵn
```cmd
sync_gradle.bat
```

## Files đã tạo/cập nhật
1. ✅ `app/src/main/res/values/colors.xml` - Thêm bidv_bg_gray
2. ✅ `app/src/main/res/drawable/ic_profile.xml` - Icon người dùng
3. ✅ `app/src/main/res/drawable/circle_background.xml` - Background hình tròn
4. ✅ `app/src/main/res/drawable/bg_status_pending.xml` - Badge chờ duyệt
5. ✅ `app/src/main/res/drawable/bg_status_active.xml` - Badge đang vay
6. ✅ `app/src/main/res/drawable/bg_status_rejected.xml` - Badge từ chối
7. ✅ `app/src/main/res/drawable/bg_status_completed.xml` - Badge hoàn thành

## Kiểm tra
Sau khi build thành công, bạn có thể:
1. Chạy app
2. Đăng nhập
3. Vào màn hình "Tài khoản"
4. Chọn tab "Tiền vay"
5. Kiểm tra giao diện mới với TabLayout và danh sách khoản vay

## Lưu ý
- Tất cả resource đã được thêm đầy đủ
- Không còn lỗi resource linking
- App sẵn sàng để build và test
