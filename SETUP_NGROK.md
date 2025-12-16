# Hướng dẫn dùng ngrok để tạo public URL

## Bước 1: Tải ngrok
1. Truy cập: https://ngrok.com/download
2. Tải bản Windows
3. Giải nén file `ngrok.exe`

## Bước 2: Đăng ký tài khoản (miễn phí)
1. Truy cập: https://dashboard.ngrok.com/signup
2. Đăng ký tài khoản
3. Lấy **authtoken** từ dashboard

## Bước 3: Cấu hình ngrok
```powershell
ngrok config add-authtoken YOUR_AUTH_TOKEN
```

## Bước 4: Chạy ngrok
```powershell
ngrok http 8089
```

## Bước 5: Lấy URL public
Ngrok sẽ hiển thị URL dạng:
```
Forwarding: https://xxxx-xxxx-xxxx.ngrok-free.app -> http://localhost:8089
```

## Bước 6: Cập nhật ApiClient.java
Thay BASE_URL thành URL ngrok:
```java
private static final String BASE_URL = "https://xxxx-xxxx-xxxx.ngrok-free.app/api/";
```

## Lưu ý:
- URL ngrok thay đổi mỗi lần chạy (trừ khi dùng plan trả phí)
- Cần chạy ngrok mỗi khi test
- Có thể dùng ngrok với domain cố định (plan trả phí)

