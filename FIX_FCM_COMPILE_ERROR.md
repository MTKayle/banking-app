# Fix Lỗi Compile FCM và Thông Báo Chuyển Tiền

## Vấn Đề

### 1. Lỗi Compile
```
Cannot resolve symbol 'FirebaseMessaging'
Cannot resolve method 'isSuccessful()'
Cannot resolve method 'getException()'
Cannot resolve symbol 'messaging'
Cannot resolve method 'getResult()'
```

### 2. Không Gửi Được Thông Báo Sau Khi Chuyển Tiền
- Giao dịch chuyển tiền thành công (status: SUCCESS)
- Nhưng không có thông báo push notification được gửi đến người nhận

## Giải Pháp

### 1. Fix Lỗi Compile Firebase

#### Bước 1: Sync Gradle
1. Mở Android Studio
2. Click **File** → **Sync Project with Gradle Files**
3. Hoặc click icon **Sync Now** ở góc trên bên phải
4. Đợi Gradle sync xong

#### Bước 2: Invalidate Caches (nếu vẫn lỗi)
1. Click **File** → **Invalidate Caches / Restart**
2. Chọn **Invalidate and Restart**
3. Đợi Android Studio khởi động lại

#### Bước 3: Clean và Rebuild Project
```bash
# Trong terminal của Android Studio
./gradlew clean
./gradlew build
```

Hoặc:
1. Click **Build** → **Clean Project**
2. Sau đó click **Build** → **Rebuild Project**

#### Bước 4: Kiểm Tra Import
Đảm bảo file `FcmTokenManager.java` có đầy đủ import:

```java
package com.example.mobilebanking.utils;

import android.content.Context;
import android.util.Log;

import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.NotificationApiService;
import com.example.mobilebanking.api.dto.FcmTokenRequest;
import com.example.mobilebanking.api.dto.FcmTokenResponse;
import com.google.firebase.messaging.FirebaseMessaging;  // ← Quan trọng

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
```

#### Bước 5: Kiểm Tra google-services.json
Đảm bảo file `google-services.json` tồn tại trong thư mục `app/`:
```
FrontEnd/banking-app/app/google-services.json
```

Nếu chưa có, tải từ Firebase Console:
1. Vào https://console.firebase.google.com/
2. Chọn project
3. Project Settings → General
4. Scroll xuống phần "Your apps"
5. Click **Download google-services.json**
6. Copy vào thư mục `app/`

### 2. Fix Không Gửi Thông Báo Sau Khi Chuyển Tiền

**Vấn đề:** Backend không gửi notification sau khi confirm transfer thành công.

#### Backend Cần Làm:

##### Bước 1: Thêm Logic Gửi Notification Trong Transfer Confirm API

**File Backend:** `TransferController.java` hoặc `TransferService.java`

```java
@PostMapping("/transfer/confirm")
public ResponseEntity<TransferConfirmResponse> confirmTransfer(
        @RequestBody TransferConfirmRequest request,
        @RequestHeader("Authorization") String authHeader) {
    
    // ... existing code to process transfer ...
    
    // Sau khi transfer thành công
    if (transferResponse.getStatus().equals("SUCCESS")) {
        // Lấy thông tin người nhận
        User receiver = userRepository.findByAccountNumber(request.getReceiverAccountNumber());
        
        if (receiver != null && receiver.getFcmToken() != null) {
            // Gửi notification cho người nhận
            String title = "Bạn nhận được tiền";
            String body = String.format(
                "Bạn vừa nhận được %s từ %s. Nội dung: %s",
                formatCurrency(request.getAmount()),
                senderName,
                request.getDescription()
            );
            
            notificationService.sendNotification(
                receiver.getFcmToken(),
                title,
                body,
                createTransferData(transferResponse)
            );
        }
        
        // Gửi notification cho người gửi (optional)
        User sender = getCurrentUser(authHeader);
        if (sender != null && sender.getFcmToken() != null) {
            String title = "Chuyển tiền thành công";
            String body = String.format(
                "Bạn đã chuyển %s đến %s",
                formatCurrency(request.getAmount()),
                receiverName
            );
            
            notificationService.sendNotification(
                sender.getFcmToken(),
                title,
                body,
                createTransferData(transferResponse)
            );
        }
    }
    
    return ResponseEntity.ok(transferResponse);
}
```

##### Bước 2: Tạo NotificationService (nếu chưa có)

**File Backend:** `NotificationService.java`

```java
@Service
public class NotificationService {
    private static final String FCM_API_URL = "https://fcm.googleapis.com/fcm/send";
    private static final String SERVER_KEY = "YOUR_FIREBASE_SERVER_KEY";
    
    public void sendNotification(String fcmToken, String title, String body, Map<String, String> data) {
        try {
            // Build FCM message
            JSONObject notification = new JSONObject();
            notification.put("title", title);
            notification.put("body", body);
            
            JSONObject message = new JSONObject();
            message.put("to", fcmToken);
            message.put("notification", notification);
            
            if (data != null && !data.isEmpty()) {
                JSONObject dataObj = new JSONObject(data);
                message.put("data", dataObj);
            }
            
            // Send to FCM
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(FCM_API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "key=" + SERVER_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(message.toString()))
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                log.info("Notification sent successfully to: " + fcmToken);
            } else {
                log.error("Failed to send notification: " + response.body());
            }
        } catch (Exception e) {
            log.error("Error sending notification", e);
        }
    }
    
    private Map<String, String> createTransferData(TransferConfirmResponse transfer) {
        Map<String, String> data = new HashMap<>();
        data.put("type", "TRANSFER");
        data.put("transactionId", String.valueOf(transfer.getTransactionId()));
        data.put("amount", String.valueOf(transfer.getAmount()));
        data.put("status", transfer.getStatus());
        return data;
    }
}
```

##### Bước 3: Lấy Firebase Server Key

1. Vào Firebase Console: https://console.firebase.google.com/
2. Chọn project
3. Click icon ⚙️ → **Project Settings**
4. Tab **Cloud Messaging**
5. Copy **Server key** (hoặc tạo mới nếu chưa có)
6. Paste vào `SERVER_KEY` trong `NotificationService.java`

##### Bước 4: Test Backend

**Test API gửi notification:**
```bash
curl -X POST http://localhost:8089/api/notifications/send-test \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Notification",
    "body": "This is a test message",
    "fcmToken": "d65KcUD0RR6DB4Lz4qrEUn:APA91b..."
  }'
```

## Test Flow Hoàn Chỉnh

### Test Case: Chuyển Tiền và Nhận Thông Báo

#### Bước 1: Chuẩn Bị
1. Đăng nhập 2 tài khoản trên 2 thiết bị khác nhau:
   - Device A: Tài khoản người gửi (0839256305)
   - Device B: Tài khoản người nhận (0987654321)

2. Kiểm tra FCM token đã được đăng ký:
   - Xem log khi đăng nhập: "FCM Token: ..."
   - Xem log: "Token registered successfully"

#### Bước 2: Thực Hiện Chuyển Tiền
1. Trên Device A:
   - Vào màn hình chuyển tiền
   - Nhập số tài khoản người nhận
   - Nhập số tiền: 134,884 VNĐ
   - Nhập nội dung: "TRƯƠNG DƯƠNG HƯNG chuyen tien"
   - Xác nhận OTP
   - Chuyển tiền thành công

#### Bước 3: Kiểm Tra Thông Báo
1. Trên Device B (người nhận):
   - **Nên nhận được notification:**
     - Title: "Bạn nhận được tiền"
     - Body: "Bạn vừa nhận được 134,884 VNĐ từ TRƯƠNG DƯƠNG HƯNG. Nội dung: TRƯƠNG DƯƠNG HƯNG chuyen tien"

2. Trên Device A (người gửi):
   - **Có thể nhận được notification:**
     - Title: "Chuyển tiền thành công"
     - Body: "Bạn đã chuyển 134,884 VNĐ đến [Tên người nhận]"

#### Bước 4: Kiểm Tra Log Backend
```
[INFO] Notification sent successfully to: d65KcUD0RR6DB4Lz4qrEUn:APA91b...
```

## Troubleshooting

### Vẫn Không Nhận Được Thông Báo?

#### 1. Kiểm Tra FCM Token
```bash
# Xem log khi đăng nhập
adb logcat | grep "FCM Token"
```

Kết quả mong đợi:
```
D/FcmTokenManager: FCM Token: d65KcUD0RR6DB4Lz4qrEUn:APA91b...
D/FcmTokenManager: Token registered successfully: Đăng ký FCM token thành công
```

#### 2. Kiểm Tra Backend Log
```bash
# Xem log backend khi chuyển tiền
tail -f backend.log | grep "Notification"
```

Kết quả mong đợi:
```
[INFO] Sending notification to user: 5
[INFO] FCM Token: d65KcUD0RR6DB4Lz4qrEUn:APA91b...
[INFO] Notification sent successfully
```

#### 3. Test Gửi Notification Trực Tiếp Từ Firebase Console
1. Vào Firebase Console → Cloud Messaging
2. Click **Send your first message**
3. Nhập title và body
4. Click **Send test message**
5. Paste FCM token
6. Click **Test**

Nếu nhận được → Backend có vấn đề  
Nếu không nhận được → Frontend/Firebase config có vấn đề

#### 4. Kiểm Tra Quyền Notification
```java
// Trong AndroidManifest.xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

Và request permission trong code (Android 13+):
```java
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
    }
}
```

## Tóm Tắt

### Frontend (Android):
✅ FCM token được đăng ký khi đăng nhập  
✅ MyFirebaseMessagingService xử lý notification  
✅ Hiển thị notification khi nhận được  

### Backend (Spring Boot):
❌ **Cần thêm:** Gửi notification sau khi transfer thành công  
❌ **Cần thêm:** NotificationService để gửi FCM message  
❌ **Cần thêm:** Firebase Server Key configuration  

### Ưu Tiên:
1. **Sync Gradle** để fix lỗi compile
2. **Thêm logic gửi notification trong backend** (quan trọng nhất)
3. **Test với Firebase Console** để verify FCM token hoạt động
4. **Test flow hoàn chỉnh** chuyển tiền và nhận notification
