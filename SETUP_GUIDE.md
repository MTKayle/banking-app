# 🚀 HƯỚNG DẪN SETUP FRONTEND (FE) - iBanking Android

## 📋 Yêu cầu hệ thống

- **Android Studio** (Arctic Fox trở lên)
- **JDK 8** hoặc cao hơn
- **Android SDK** (API Level 26 trở lên)
- **Gradle** (đã có sẵn trong project)

## 🔧 Bước 1: Mở project trong Android Studio

1. Mở **Android Studio**
2. Chọn **File > Open**
3. Chọn thư mục `Ibanking-Moblie-App/ibanking fe`
4. Android Studio sẽ tự động sync Gradle và download dependencies

## 🔧 Bước 2: Cấu hình API Base URL

### 2.1. Mở file ApiClient.java

File: `app/src/main/java/com/example/mobilebanking/api/ApiClient.java`

### 2.2. Cập nhật BASE_URL

**Cho Android Emulator:**
```java
private static final String BASE_URL = "http://10.0.2.2:8089/api/";
```
- `10.0.2.2` là alias cho `localhost` của máy host khi chạy trên emulator

**Cho thiết bị thật:**
1. Tìm IP máy tính của bạn:
   - **Windows:** Mở CMD, gõ `ipconfig`, tìm `IPv4 Address`
   - **Mac/Linux:** Mở Terminal, gõ `ifconfig` hoặc `ip addr`
   
2. Cập nhật BASE_URL:
```java
private static final String BASE_URL = "http://192.168.1.100:8089/api/";
// Thay 192.168.1.100 bằng IP máy tính của bạn
```

**Lưu ý quan trọng:**
- Máy tính và điện thoại phải cùng mạng WiFi
- Tắt Firewall hoặc cho phép port 8089
- Backend phải chạy và accessible từ mạng local

## 🔧 Bước 3: Cấu hình Internet Permission

File `AndroidManifest.xml` đã có permission INTERNET, nhưng hãy kiểm tra:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

## 🔧 Bước 4: Cấu hình Network Security Config (Nếu cần)

Nếu bạn gặp lỗi `Cleartext HTTP traffic not permitted`, cần tạo file network security config:

1. Tạo file: `app/src/main/res/xml/network_security_config.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="true">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
</network-security-config>
```

2. Thêm vào `AndroidManifest.xml` trong tag `<application>`:
```xml
<application
    ...
    android:networkSecurityConfig="@xml/network_security_config">
```

## 🔧 Bước 5: Build và chạy ứng dụng

### Cách 1: Chạy từ Android Studio
1. Kết nối thiết bị hoặc khởi động emulator
2. Click **Run** (Shift + F10) hoặc click nút ▶️
3. Chọn thiết bị và chờ app build xong

### Cách 2: Build APK
```bash
cd "Ibanking-Moblie-App/ibanking fe"
./gradlew assembleDebug
```
APK sẽ được tạo tại: `app/build/outputs/apk/debug/app-debug.apk`

## ✅ Kiểm tra kết nối API

1. **Đảm bảo Backend đã chạy:**
   - Mở trình duyệt, truy cập: `http://localhost:8089/api/auth/login`
   - Nếu thấy lỗi 405 hoặc 400 → Backend đã chạy ✅

2. **Test từ app:**
   - Mở app
   - Thử đăng ký hoặc đăng nhập
   - Xem Logcat trong Android Studio để kiểm tra request/response

3. **Kiểm tra Logcat:**
   - Mở tab **Logcat** trong Android Studio
   - Filter: `OkHttp` hoặc `ApiClient`
   - Xem request/response logs

## 🐛 Troubleshooting

### Lỗi: Failed to connect to /10.0.2.2:8089
**Nguyên nhân:**
- Backend chưa chạy
- BASE_URL sai
- Firewall chặn port 8089

**Giải pháp:**
1. Kiểm tra Backend đã chạy chưa
2. Kiểm tra BASE_URL trong `ApiClient.java`
3. Tắt Firewall hoặc cho phép port 8089
4. Nếu dùng thiết bị thật, đảm bảo cùng mạng WiFi

### Lỗi: Cleartext HTTP traffic not permitted
**Giải pháp:**
- Tạo file `network_security_config.xml` như hướng dẫn ở Bước 4

### Lỗi: 401 Unauthorized
**Nguyên nhân:**
- Token hết hạn hoặc không hợp lệ
- Chưa đăng nhập

**Giải pháp:**
- Đăng nhập lại để lấy token mới

### Lỗi: 400 Bad Request
**Nguyên nhân:**
- Dữ liệu gửi lên không đúng format
- Thiếu field bắt buộc

**Giải pháp:**
- Kiểm tra Logcat để xem error message từ server
- Kiểm tra validation trong code

### Lỗi: Build failed - Gradle sync failed
**Giải pháp:**
1. **File > Invalidate Caches / Restart**
2. Xóa thư mục `.gradle` và `build` trong project
3. Chạy lại: `./gradlew clean build`

## 📱 Cấu trúc API đã được tích hợp

### Authentication APIs
- ✅ `POST /api/auth/register` - Đăng ký tài khoản
- ✅ `POST /api/auth/login` - Đăng nhập
- ⏳ `POST /api/auth/register-with-face` - Đăng ký với xác thực khuôn mặt (chưa tích hợp)
- ⏳ `POST /api/auth/login-with-face` - Đăng nhập bằng khuôn mặt (chưa tích hợp)

### Account APIs
- ⏳ `GET /api/accounts/{userId}/checking` - Lấy thông tin tài khoản (chưa tích hợp)

### Payment APIs
- ⏳ `POST /api/payment/checking/deposit` - Nạp tiền (chưa tích hợp)

## 🔐 JWT Token Management

Token được tự động quản lý:
- Lưu vào `SharedPreferences` sau khi đăng nhập thành công
- Tự động thêm vào header `Authorization: Bearer {token}` cho mọi request
- Xóa khi logout

## 📝 Lưu ý quan trọng

1. **BASE_URL:** Luôn kiểm tra BASE_URL trước khi chạy app
2. **Backend phải chạy:** App không thể hoạt động nếu Backend chưa chạy
3. **Cùng mạng:** Máy tính và điện thoại phải cùng mạng WiFi (nếu dùng thiết bị thật)
4. **Logcat:** Luôn kiểm tra Logcat để debug API calls

## 🎯 Bước tiếp theo

Sau khi setup thành công:
1. Test đăng ký tài khoản mới
2. Test đăng nhập
3. Tích hợp các API khác (Account, Payment, etc.)
4. Test trên thiết bị thật

## 📚 Tài liệu tham khảo

- Backend API Documentation: `Ibanking-Moblie-App/ibanking be/README.md`
- Backend Setup Guide: `Ibanking-Moblie-App/ibanking be/SETUP_GUIDE.md`

---

**Chúc bạn setup thành công! 🎉**


