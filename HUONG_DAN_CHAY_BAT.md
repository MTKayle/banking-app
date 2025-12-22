# Hướng Dẫn Chạy File .BAT

## Các File .BAT Đã Tạo

### 1. `sync_gradle.bat` - Sync Gradle Dependencies
**Mục đích:** Fix lỗi compile, sync dependencies, download thư viện mới

**Khi nào dùng:**
- Khi gặp lỗi "Cannot resolve symbol 'FirebaseMessaging'"
- Sau khi thêm dependency mới vào `build.gradle.kts`
- Khi Android Studio báo lỗi sync

**Cách chạy:**
```cmd
cd FrontEnd\banking-app
sync_gradle.bat
```

**Kết quả:**
- Download tất cả dependencies
- Verify build thành công
- Sẵn sàng để mở trong Android Studio

---

### 2. `clean_and_build.bat` - Clean và Build Project
**Mục đích:** Xóa cache cũ, build lại project từ đầu

**Khi nào dùng:**
- Khi gặp lỗi build không rõ nguyên nhân
- Sau khi sửa code nhiều
- Trước khi release APK

**Cách chạy:**
```cmd
cd FrontEnd\banking-app
clean_and_build.bat
```

**Kết quả:**
- Clean project (xóa folder `build/`)
- Build lại toàn bộ project
- Tạo APK debug tại: `app\build\outputs\apk\debug\app-debug.apk`

---

### 3. `install_and_run.bat` - Cài Đặt và Chạy App
**Mục đích:** Build APK, cài lên điện thoại, và chạy app

**Khi nào dùng:**
- Sau khi sửa code, muốn test trên điện thoại
- Muốn cài app nhanh mà không mở Android Studio

**Yêu cầu:**
- Điện thoại đã kết nối USB
- Đã bật USB Debugging
- Đã authorize máy tính

**Cách chạy:**
```cmd
cd FrontEnd\banking-app
install_and_run.bat
```

**Kết quả:**
- Build APK debug
- Cài đặt lên điện thoại (ghi đè nếu đã có)
- Tự động mở app

---

### 4. `view_logs.bat` - Xem Logs Realtime
**Mục đích:** Xem logs của app đang chạy trên điện thoại

**Khi nào dùng:**
- Debug lỗi
- Xem FCM token
- Xem API response
- Theo dõi flow đăng nhập

**Cách chạy:**
```cmd
cd FrontEnd\banking-app
view_logs.bat
```

**Kết quả:**
- Hiển thị logs realtime
- Filter chỉ logs quan trọng:
  - FcmTokenManager (FCM token)
  - OtpVerification (OTP flow)
  - LoginActivity (đăng nhập)
  - okhttp.OkHttpClient (API calls)
  - Errors (*:E)

**Dừng xem logs:** Nhấn `Ctrl+C`

---

## Workflow Thông Dụng

### Workflow 1: Fix Lỗi Compile
```cmd
# Bước 1: Sync Gradle
sync_gradle.bat

# Bước 2: Nếu vẫn lỗi, clean và build lại
clean_and_build.bat

# Bước 3: Mở Android Studio và check lại
```

### Workflow 2: Test Code Mới
```cmd
# Bước 1: Build và cài app
install_and_run.bat

# Bước 2: Xem logs để debug
view_logs.bat

# Bước 3: Test trên điện thoại
```

### Workflow 3: Debug FCM Token
```cmd
# Bước 1: Cài app
install_and_run.bat

# Bước 2: Xem logs
view_logs.bat

# Bước 3: Đăng nhập và xem log "FCM Token: ..."
```

---

## Troubleshooting

### Lỗi: "gradlew.bat is not recognized"
**Nguyên nhân:** Chưa cd đúng thư mục

**Giải pháp:**
```cmd
cd /d D:\eBanking\FrontEnd\banking-app
# Hoặc
cd /d %~dp0
```

### Lỗi: "adb is not recognized"
**Nguyên nhân:** ADB chưa được thêm vào PATH

**Giải pháp:**
1. Tìm thư mục Android SDK (thường ở `C:\Users\[User]\AppData\Local\Android\Sdk`)
2. Thêm vào PATH:
   - `C:\Users\[User]\AppData\Local\Android\Sdk\platform-tools`
3. Hoặc dùng đường dẫn đầy đủ:
```cmd
"C:\Users\[User]\AppData\Local\Android\Sdk\platform-tools\adb.exe" devices
```

### Lỗi: "No devices/emulators found"
**Nguyên nhân:** Điện thoại chưa kết nối hoặc chưa bật USB Debugging

**Giải pháp:**
1. Kết nối điện thoại qua USB
2. Bật USB Debugging:
   - Settings → About Phone → Tap "Build Number" 7 lần
   - Settings → Developer Options → USB Debugging → ON
3. Authorize máy tính khi có popup trên điện thoại
4. Kiểm tra:
```cmd
adb devices
```

### Lỗi: "Build failed"
**Nguyên nhân:** Code có lỗi syntax hoặc thiếu dependency

**Giải pháp:**
1. Xem log lỗi chi tiết
2. Fix lỗi trong code
3. Chạy lại:
```cmd
clean_and_build.bat
```

### Lỗi: "Installation failed"
**Nguyên nhân:** 
- App đang chạy
- Signature không khớp
- Không đủ dung lượng

**Giải pháp:**
1. Gỡ app cũ trên điện thoại
2. Chạy lại:
```cmd
install_and_run.bat
```

---

## Tips

### Tip 1: Chạy Nhanh Từ Bất Kỳ Đâu
Tạo file `quick_run.bat` ở thư mục gốc:
```batch
@echo off
cd /d D:\eBanking\FrontEnd\banking-app
call install_and_run.bat
```

### Tip 2: Xem Logs Cụ Thể
Sửa `view_logs.bat` để xem logs khác:
```batch
REM Xem tất cả logs
adb logcat

REM Xem chỉ errors
adb logcat *:E

REM Xem logs của một tag cụ thể
adb logcat -s "FcmTokenManager:D"

REM Xem logs và lưu vào file
adb logcat > logs.txt
```

### Tip 3: Build Release APK
Tạo file `build_release.bat`:
```batch
@echo off
echo Building release APK...
call gradlew.bat assembleRelease
echo.
echo APK location: app\build\outputs\apk\release\app-release-unsigned.apk
pause
```

### Tip 4: Uninstall App
Tạo file `uninstall_app.bat`:
```batch
@echo off
echo Uninstalling app...
adb uninstall com.example.mobilebanking
echo Done!
pause
```

---

## Tóm Tắt Lệnh

| File | Mục Đích | Thời Gian |
|------|----------|-----------|
| `sync_gradle.bat` | Sync dependencies | ~2-5 phút |
| `clean_and_build.bat` | Clean + Build | ~3-10 phút |
| `install_and_run.bat` | Cài app lên điện thoại | ~1-3 phút |
| `view_logs.bat` | Xem logs realtime | Realtime |

---

## Lưu Ý Quan Trọng

⚠️ **Trước khi chạy bất kỳ file .bat nào:**
1. Đảm bảo đang ở đúng thư mục `FrontEnd\banking-app`
2. Đảm bảo có file `gradlew.bat` trong thư mục
3. Đảm bảo có kết nối internet (để download dependencies)

✅ **Sau khi chạy thành công:**
- Có thể mở project trong Android Studio
- Có thể test app trên điện thoại
- Có thể xem logs để debug

🔥 **Nếu gặp lỗi:**
1. Đọc log lỗi kỹ
2. Google lỗi đó
3. Hoặc hỏi tôi với log đầy đủ
