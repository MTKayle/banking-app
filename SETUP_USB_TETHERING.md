# Hướng dẫn dùng USB Tethering + ADB Reverse

## Bước 1: Kết nối điện thoại qua USB
1. Cắm cáp USB vào máy tính
2. Trên điện thoại: Settings → Developer options → USB debugging (Bật)
3. Cho phép USB debugging khi có popup

## Bước 2: Kiểm tra kết nối
```powershell
adb devices
```
Phải thấy thiết bị của bạn trong danh sách

## Bước 3: Forward port
```powershell
adb reverse tcp:8089 tcp:8089
```

## Bước 4: Cập nhật ApiClient.java
Giữ nguyên localhost:
```java
private static final String BASE_URL = "http://localhost:8089/api/";
```

## Lưu ý:
- Cần chạy lại lệnh `adb reverse` mỗi khi ngắt kết nối USB
- Có thể tạo file batch để tự động chạy

