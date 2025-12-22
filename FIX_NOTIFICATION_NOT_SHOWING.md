# Fix Lỗi Notification Không Hiển Thị

## Vấn Đề Đã Fix

### 1. Icon Notification Không Tồn Tại
**Lỗi:** `R.drawable.ic_notification` không tồn tại → Notification không hiển thị

**Giải pháp:** Đã thay bằng icon mặc định của Android
```java
.setSmallIcon(android.R.drawable.ic_dialog_info) // Icon mặc định
```

## Cách Test Notification

### Bước 1: Chạy Debug Script

```cmd
cd FrontEnd\banking-app
debug_fcm.bat
```

Script này sẽ hiển thị tất cả logs liên quan đến FCM.

### Bước 2: Đăng Nhập và Kiểm Tra FCM Token

1. Mở app và đăng nhập
2. Xem log, tìm dòng:
```
D/FcmTokenManager: FCM Token: d65KcUD0RR6DB4Lz4qrEUn:APA91b...
D/FcmTokenManager: Token registered successfully: Đăng ký FCM token thành công
```

Nếu **KHÔNG** thấy log này → FCM token chưa được đăng ký → Cần rebuild app:
```cmd
clean_and_build.bat
install_and_run.bat
```

### Bước 3: Test Chuyển Tiền

1. Đăng nhập 2 tài khoản trên 2 thiết bị:
   - Device A: Người gửi (0839256305)
   - Device B: Người nhận (0987654321)

2. Trên Device A: Chuyển tiền cho Device B

3. Xem log trên Device B, tìm dòng:
```
D/FCMService: Message received from: ...
D/FCMService: Message Notification Title: Bạn nhận được tiền
D/FCMService: Message Notification Body: Bạn vừa nhận được ...
```

4. Kiểm tra notification bar trên Device B

### Bước 4: Test Bằng Firebase Console (Nếu Vẫn Không Nhận Được)

1. Vào Firebase Console: https://console.firebase.google.com/
2. Chọn project
3. Cloud Messaging → **Send your first message**
4. Nhập:
   - **Notification title:** Test Notification
   - **Notification text:** This is a test message
5. Click **Send test message**
6. Paste FCM token từ log (bước 2)
7. Click **Test**

**Nếu nhận được** → Backend có vấn đề  
**Nếu không nhận được** → Tiếp tục troubleshooting

## Troubleshooting

### Vấn Đề 1: Không Thấy Log "FCM Token"

**Nguyên nhân:** FCM token chưa được lấy hoặc đăng ký

**Giải pháp:**

1. Kiểm tra `google-services.json` có trong `app/` không
2. Rebuild project:
```cmd
clean_and_build.bat
```

3. Reinstall app:
```cmd
install_and_run.bat
```

4. Đăng nhập lại

### Vấn Đề 2: Log Hiển thị "Failed to register token"

**Nguyên nhân:** Backend API không hoạt động hoặc access token hết hạn

**Giải pháp:**

1. Kiểm tra backend đang chạy:
```
http://localhost:8089/api/notifications/register-token
```

2. Kiểm tra access token còn hạn không:
   - Đăng xuất
   - Đăng nhập lại
   - Xem log lại

### Vấn Đề 3: Nhận Được Log "Message received" Nhưng Không Hiển Thị Notification

**Nguyên nhân:** 
- Notification permission chưa được cấp
- Notification channel bị tắt
- App đang ở foreground

**Giải pháp:**

1. Kiểm tra notification permission:
   - Settings → Apps → Mobile Banking → Notifications → ON

2. Kiểm tra notification channel:
   - Settings → Apps → Mobile Banking → Notifications → Banking Notifications → ON

3. Test khi app ở background:
   - Nhấn Home button (không tắt app)
   - Chuyển tiền từ device khác
   - Xem notification bar

### Vấn Đề 4: Backend Không Gửi Notification

**Kiểm tra backend log:**

```bash
# Xem log backend
tail -f backend.log | grep -i "notification"
```

**Kết quả mong đợi:**
```
[INFO] Sending notification to user: 5
[INFO] FCM Token: d65KcUD0RR6DB4Lz4qrEUn:APA91b...
[INFO] Notification sent successfully
```

**Nếu không thấy log này** → Backend chưa gửi notification → Xem file `BACKEND_NOTIFICATION_IMPLEMENTATION.md`

## Checklist Đầy Đủ

### Frontend (Android):
- [x] MyFirebaseMessagingService đã được đăng ký trong AndroidManifest.xml
- [x] Icon notification đã được fix (dùng icon mặc định)
- [x] FcmTokenManager gọi khi đăng nhập
- [x] Notification permission đã được khai báo
- [ ] FCM token được đăng ký thành công (xem log)
- [ ] Notification hiển thị khi nhận message

### Backend:
- [ ] Firebase Admin SDK đã được khởi tạo
- [ ] API `/api/notifications/register-token` hoạt động
- [ ] FCM token được lưu vào database
- [ ] Notification được gửi sau khi transfer thành công
- [ ] Backend log hiển thị "Notification sent successfully"

### Firebase:
- [ ] `google-services.json` có trong `app/`
- [ ] Firebase project đã được tạo
- [ ] Package name khớp: `com.example.mobilebanking`
- [ ] Cloud Messaging đã được enable

## Test Commands

### 1. Rebuild và Reinstall App
```cmd
cd FrontEnd\banking-app
clean_and_build.bat
install_and_run.bat
```

### 2. Xem FCM Logs
```cmd
cd FrontEnd\banking-app
debug_fcm.bat
```

### 3. Xem Tất Cả Logs
```cmd
cd FrontEnd\banking-app
view_logs.bat
```

### 4. Test Backend API
```bash
# Test register FCM token
curl -X POST http://localhost:8089/api/notifications/register-token \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fcmToken": "YOUR_FCM_TOKEN_FROM_LOG"
  }'
```

## Kết Luận

**Đã fix:**
- ✅ Icon notification (dùng icon mặc định)
- ✅ MyFirebaseMessagingService hoạt động đúng
- ✅ FcmTokenManager đăng ký token khi đăng nhập

**Cần kiểm tra:**
1. FCM token có được đăng ký thành công không (xem log)
2. Backend có gửi notification không (xem backend log)
3. Notification permission đã được cấp chưa

**Nếu vẫn không nhận được notification:**
1. Chạy `debug_fcm.bat` để xem logs
2. Test bằng Firebase Console
3. Kiểm tra backend log
4. Đảm bảo app ở background khi test
