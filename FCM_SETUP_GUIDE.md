# Hướng Dẫn Tích Hợp Firebase Cloud Messaging (FCM)

## Tổng Quan
Đã tích hợp Firebase Cloud Messaging để nhận thông báo push từ backend sau khi người dùng đăng nhập.

## Các File Đã Tạo/Cập Nhật

### 1. DTO Classes
- `FcmTokenRequest.java` - Request để gửi FCM token lên server
- `FcmTokenResponse.java` - Response từ server sau khi đăng ký token

### 2. API Service
- `NotificationApiService.java` - Interface định nghĩa API endpoint `/api/notifications/register-token`

### 3. Firebase Service
- `MyFirebaseMessagingService.java` - Service xử lý:
  - Nhận FCM token mới
  - Nhận và hiển thị notification
  - Tự động gửi token lên server

### 4. Utility Class
- `FcmTokenManager.java` - Manager để lấy và đăng ký FCM token

### 5. Cập Nhật LoginActivity
- Tự động gọi `FcmTokenManager.registerFcmToken()` sau khi đăng nhập thành công
- Áp dụng cho cả đăng nhập bằng mật khẩu và vân tay

### 6. Cập Nhật ApiClient
- Thêm `getNotificationApiService()` để lấy NotificationApiService instance

### 7. Cập Nhật build.gradle.kts
- Thêm Firebase dependencies:
  - `firebase-bom:32.7.0`
  - `firebase-messaging`
  - `firebase-analytics`
- Thêm plugin `com.google.gms.google-services`

### 8. Cập Nhật AndroidManifest.xml
- Thêm permission `POST_NOTIFICATIONS` (Android 13+)
- Đăng ký `MyFirebaseMessagingService`

## Các Bước Setup Firebase (Quan Trọng!)

### Bước 1: Tạo Firebase Project
1. Truy cập [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project" hoặc chọn project có sẵn
3. Nhập tên project (ví dụ: "Mobile Banking")
4. Bật/tắt Google Analytics (tùy chọn)
5. Click "Create project"

### Bước 2: Thêm Android App vào Firebase Project
1. Trong Firebase Console, click biểu tượng Android
2. Nhập package name: `com.example.mobilebanking`
3. Nhập app nickname (tùy chọn): "Banking App"
4. Nhập SHA-1 certificate (tùy chọn, cần cho một số tính năng):
   ```bash
   # Debug keystore
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   
   # Windows
   keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
   ```
5. Click "Register app"

### Bước 3: Download google-services.json
1. Sau khi đăng ký app, Firebase sẽ cho phép download file `google-services.json`
2. **QUAN TRỌNG**: Copy file này vào thư mục `FrontEnd/banking-app/app/`
3. Cấu trúc thư mục:
   ```
   FrontEnd/banking-app/
   ├── app/
   │   ├── google-services.json  ← Đặt file ở đây
   │   ├── build.gradle.kts
   │   └── src/
   ```

### Bước 4: Sync Gradle
1. Mở Android Studio
2. Click "Sync Now" hoặc "File > Sync Project with Gradle Files"
3. Đợi Gradle sync hoàn tất

### Bước 5: Test FCM Token
1. Build và chạy app
2. Đăng nhập vào app
3. Kiểm tra Logcat với filter "FCMService" hoặc "FcmTokenManager"
4. Bạn sẽ thấy log:
   ```
   D/FcmTokenManager: FCM Token: [token_string]
   D/FcmTokenManager: Token registered successfully: [message]
   ```

## Luồng Hoạt Động

### 1. Sau Khi Đăng Nhập
```
LoginActivity.performPasswordLogin() 
  → Đăng nhập thành công
  → FcmTokenManager.registerFcmToken()
  → FirebaseMessaging.getInstance().getToken()
  → Gửi POST request đến /api/notifications/register-token
  → Backend lưu token vào database
```

### 2. Khi Nhận Notification
```
Backend gửi notification qua FCM
  → MyFirebaseMessagingService.onMessageReceived()
  → Hiển thị notification trên device
  → Click notification → Mở UiHomeActivity
```

### 3. Khi Token Thay Đổi
```
Firebase tạo token mới
  → MyFirebaseMessagingService.onNewToken()
  → Tự động gửi token mới lên server
```

## API Endpoint

### POST /api/notifications/register-token
**Request:**
```json
{
  "fcmToken": "your_fcm_token_from_firebase"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Token registered successfully"
}
```

**Headers:**
- `Authorization: Bearer {access_token}` (tự động thêm bởi ApiClient)
- `Content-Type: application/json`

## Test Notification

### Từ Firebase Console
1. Vào Firebase Console > Cloud Messaging
2. Click "Send your first message"
3. Nhập notification title và text
4. Click "Send test message"
5. Nhập FCM token (lấy từ Logcat)
6. Click "Test"

### Từ Backend (Recommended)
Backend cần implement endpoint để gửi notification:
```java
// Backend code (Spring Boot example)
@PostMapping("/api/notifications/send")
public void sendNotification(@RequestBody NotificationRequest request) {
    // Lấy FCM token từ database theo userId
    String fcmToken = userService.getFcmToken(request.getUserId());
    
    // Gửi notification qua Firebase Admin SDK
    Message message = Message.builder()
        .setToken(fcmToken)
        .setNotification(Notification.builder()
            .setTitle(request.getTitle())
            .setBody(request.getBody())
            .build())
        .build();
    
    FirebaseMessaging.getInstance().send(message);
}
```

## Notification Icon

Cần tạo icon cho notification:
1. Tạo file `ic_notification.xml` trong `res/drawable/`:
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M12,22c1.1,0 2,-0.9 2,-2h-4c0,1.1 0.9,2 2,2zM18,16v-5c0,-3.07 -1.64,-5.64 -4.5,-6.32V4c0,-0.83 -0.67,-1.5 -1.5,-1.5s-1.5,0.67 -1.5,1.5v0.68C7.63,5.36 6,7.92 6,11v5l-2,2v1h16v-1l-2,-2z"/>
</vector>
```

Hoặc sử dụng icon có sẵn:
```java
.setSmallIcon(android.R.drawable.ic_dialog_info)
```

## Troubleshooting

### Lỗi: "google-services.json not found"
- Đảm bảo file `google-services.json` nằm trong `app/` folder
- Sync Gradle lại

### Lỗi: "Failed to get FCM token"
- Kiểm tra kết nối internet
- Kiểm tra Google Play Services đã cài đặt chưa
- Kiểm tra package name trong `google-services.json` khớp với `applicationId`

### Không nhận được notification
- Kiểm tra app có đang chạy foreground/background không
- Kiểm tra permission `POST_NOTIFICATIONS` đã được cấp chưa (Android 13+)
- Kiểm tra FCM token đã được gửi lên server chưa
- Kiểm tra backend có gửi notification đúng format không

### Token không được gửi lên server
- Kiểm tra user đã đăng nhập chưa (cần access token)
- Kiểm tra API endpoint `/api/notifications/register-token` có hoạt động không
- Kiểm tra Logcat để xem error message

## Lưu Ý Quan Trọng

1. **google-services.json**: File này chứa thông tin nhạy cảm, không nên commit lên Git public repository
2. **Permission**: Android 13+ yêu cầu runtime permission cho POST_NOTIFICATIONS
3. **Token Expiry**: FCM token có thể thay đổi, cần handle `onNewToken()` để cập nhật
4. **Background**: Notification sẽ tự động hiển thị khi app ở background
5. **Foreground**: Cần tự xử lý hiển thị notification khi app ở foreground

## Next Steps

1. Download `google-services.json` từ Firebase Console
2. Copy vào `FrontEnd/banking-app/app/`
3. Sync Gradle
4. Build và test app
5. Implement backend endpoint để gửi notification
6. Test notification từ Firebase Console hoặc backend
