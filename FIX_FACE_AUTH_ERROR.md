# 🔧 HƯỚNG DẪN SỬA LỖI XÁC THỰC KHUÔN MẶT

## ❌ Lỗi bạn đang gặp:

1. **Lỗi kết nối:** `failed to connect to / 10.0.221.236 (port 8089) from / 10.0.220.70`
2. **Lỗi xác thực:** "Khuôn mặt không khớp với ảnh trên CCCD"

## 🔍 Nguyên nhân:

- **IP máy tính:** `10.0.221.236` (subnet 10.0.221.x)
- **IP điện thoại:** `10.0.220.70` (subnet 10.0.220.x)
- **Vấn đề:** Điện thoại và máy tính **KHÔNG CÙNG MẠNG Wi-Fi**!

## ✅ Giải pháp:

### Bước 1: Kiểm tra kết nối mạng

1. **Trên máy tính:**
   - Mở CMD (Command Prompt)
   - Chạy: `ipconfig`
   - Tìm `Wireless LAN adapter Wi-Fi` → `IPv4 Address`
   - Ghi lại IP này (ví dụ: `10.0.221.236`)

2. **Trên điện thoại:**
   - Vào **Settings** → **Wi-Fi**
   - Nhấn vào mạng Wi-Fi đang kết nối
   - Xem **IP Address** (ví dụ: `10.0.220.70`)

3. **So sánh:**
   - Nếu IP máy tính là `10.0.221.x` và IP điện thoại là `10.0.220.x` → **KHÔNG CÙNG MẠNG!**
   - Cả hai phải cùng subnet (ví dụ: cả hai đều `10.0.221.x`)

### Bước 2: Kết nối cùng mạng Wi-Fi

**QUAN TRỌNG:** Điện thoại và máy tính **PHẢI** kết nối cùng một mạng Wi-Fi!

1. **Trên máy tính:**
   - Kiểm tra tên mạng Wi-Fi đang kết nối
   - Ví dụ: `MyWiFi-5G`

2. **Trên điện thoại:**
   - Vào **Settings** → **Wi-Fi**
   - Kết nối vào **CÙNG mạng Wi-Fi** với máy tính
   - Đợi kết nối xong

3. **Kiểm tra lại IP:**
   - Sau khi kết nối, kiểm tra lại IP điện thoại
   - IP phải cùng subnet với máy tính (ví dụ: cả hai đều `10.0.221.x`)

### Bước 3: Chạy script kiểm tra

1. Mở CMD **với quyền Administrator** (Right-click → Run as administrator)
2. Chạy script:
   ```bash
   cd "D:\duancuoikiandroid\Ibanking-Moblie-App\ibanking fe"
   fix_face_auth_connection.bat
   ```

3. Script sẽ:
   - Kiểm tra IP hiện tại
   - Mở firewall port 8089
   - Test kết nối backend
   - Báo cáo nếu IP không khớp

### Bước 4: Cập nhật IP trong code (nếu cần)

Nếu IP máy tính thay đổi:

1. Mở file: `app/src/main/java/com/example/mobilebanking/api/ApiClient.java`
2. Tìm dòng:
   ```java
   private static final String IP_MÁY_TÍNH_CỦA_BẠN = "10.0.221.236";
   ```
3. Thay đổi thành IP mới của máy tính:
   ```java
   private static final String IP_MÁY_TÍNH_CỦA_BẠN = "10.0.221.XXX"; // IP mới
   ```

### Bước 5: Đảm bảo Backend đang chạy

1. **Kiểm tra Backend:**
   - Mở trình duyệt
   - Truy cập: `http://localhost:8089/api/auth/register`
   - Nếu thấy lỗi 405 hoặc 400 → Backend đã chạy ✅
   - Nếu không kết nối được → Khởi động Backend trong IntelliJ IDEA

2. **Kiểm tra cấu hình:**
   - File: `ibanking be/src/main/resources/application.properties`
   - Phải có: `server.address=0.0.0.0` (đã có sẵn ✅)
   - Port: `server.port=8089`

### Bước 6: Test kết nối từ điện thoại

1. **Mở trình duyệt trên điện thoại** (Chrome, Firefox, etc.)
2. Truy cập: `http://10.0.221.236:8089/api/auth/register`
   - Thay `10.0.221.236` bằng IP máy tính của bạn
3. Nếu thấy lỗi 405 hoặc 400 → **Kết nối thành công!** ✅
4. Nếu không kết nối được → Kiểm tra lại firewall và mạng Wi-Fi

### Bước 7: Rebuild và cài đặt lại app

1. **Trong Android Studio:**
   - Build → Clean Project
   - Build → Rebuild Project

2. **Hoặc chạy lệnh:**
   ```bash
   cd "D:\duancuoikiandroid\Ibanking-Moblie-App\ibanking fe"
   .\gradlew.bat clean
   .\gradlew.bat installDebug
   ```

3. **Cài đặt lại app trên điện thoại**

### Bước 8: Thử lại xác thực khuôn mặt

1. Mở app trên điện thoại
2. Thử đăng ký lại với xác thực khuôn mặt
3. Kiểm tra xem còn lỗi kết nối không

## 🐛 Troubleshooting

### Vẫn không kết nối được?

1. **Kiểm tra Firewall:**
   - Chạy script `fix_face_auth_connection.bat` (đã mở port 8089)
   - Hoặc tắt Windows Firewall tạm thời để test

2. **Kiểm tra Backend:**
   - Backend phải chạy và lắng nghe trên `0.0.0.0:8089`
   - Kiểm tra log trong IntelliJ IDEA

3. **Kiểm tra mạng:**
   - Đảm bảo cả hai cùng mạng Wi-Fi
   - Thử ping từ điện thoại: `ping 10.0.221.236` (dùng app Terminal)

4. **Thử dùng USB tethering:**
   - Kết nối điện thoại qua USB
   - Bật USB tethering trên điện thoại
   - Cập nhật `CONNECTION_MODE = "USB"` trong `ApiClient.java`
   - Chạy: `adb reverse tcp:8089 tcp:8089`

### Lỗi "Khuôn mặt không khớp"

Sau khi fix lỗi kết nối, nếu vẫn báo "Khuôn mặt không khớp":

1. **Đảm bảo ảnh rõ ràng:**
   - Ảnh CCCD phải rõ, không mờ
   - Ảnh selfie phải rõ, đủ ánh sáng
   - Khuôn mặt không bị che (mũ, khẩu trang, kính)

2. **Chụp lại ảnh:**
   - Chụp lại ảnh selfie với điều kiện tốt hơn
   - Đảm bảo khuôn mặt giống với ảnh trên CCCD

3. **Kiểm tra backend:**
   - Backend có thể đang dùng Face++ API
   - Kiểm tra API key trong `application.properties`

## 📞 Liên hệ hỗ trợ

Nếu vẫn gặp vấn đề, cung cấp:
- IP máy tính
- IP điện thoại
- Tên mạng Wi-Fi
- Log từ Android Studio (Logcat)
- Log từ Backend (IntelliJ IDEA console)

