# Hướng dẫn dùng Mobile Hotspot

## Bước 1: Bật Mobile Hotspot trên điện thoại

### Android:
1. Settings → Connections → Mobile Hotspot and Tethering
2. Mobile Hotspot → Bật ON
3. Ghi nhớ:
   - Tên mạng (SSID)
   - Mật khẩu

### iPhone:
1. Settings → Personal Hotspot
2. Bật "Allow Others to Join"
3. Ghi nhớ mật khẩu

## Bước 2: Kết nối máy tính vào Hotspot

1. Trên máy tính: Settings → Network & Internet → Wi-Fi
2. Tìm tên mạng hotspot của điện thoại
3. Kết nối và nhập mật khẩu

## Bước 3: Tìm IP mới của máy tính

Sau khi kết nối, chạy:
```powershell
ipconfig
```

Tìm IP trong phần "Wireless LAN adapter Wi-Fi" hoặc "Wireless LAN adapter Local Area Connection*"

IP thường sẽ là:
- `192.168.43.x` (Android hotspot)
- `192.168.137.x` (một số Android)
- `172.20.10.x` (iPhone hotspot)

## Bước 4: Cập nhật IP trong ApiClient.java

Mở file: `app/src/main/java/com/example/mobilebanking/api/ApiClient.java`

Tìm dòng:
```java
private static final String IP_MÁY_TÍNH_CỦA_BẠN = "10.0.221.236";
```

Đổi thành IP mới (từ bước 3), ví dụ:
```java
private static final String IP_MÁY_TÍNH_CỦA_BẠN = "192.168.43.100";
```

## Bước 5: Rebuild app

```powershell
.\gradlew.bat installDebug
```

## Bước 6: Test

Mở app trên điện thoại và test lại.

## Lưu ý:

⚠️ **Quan trọng:**
- IP máy tính sẽ thay đổi mỗi khi kết nối lại hotspot
- Cần cập nhật IP trong code mỗi lần
- Tốn data di động
- Có thể chậm hơn Wi-Fi

✅ **Ưu điểm:**
- Không cần router Wi-Fi
- Có thể dùng ở bất cứ đâu
- Dễ setup

❌ **Nhược điểm:**
- Tốn data
- IP thay đổi mỗi lần
- Có thể chậm

## Giải pháp tốt hơn:

Nếu IP thay đổi thường xuyên, nên dùng:
- **USB Tethering + ADB Reverse** (ổn định hơn)
- **ngrok** (không cần cùng mạng)

