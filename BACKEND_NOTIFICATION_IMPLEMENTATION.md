# Backend: Hướng Dẫn Gửi Notification Sau Khi Chuyển Tiền

## Vấn Đề Hiện Tại

**Frontend:** ✅ Đã sẵn sàng nhận notification
- FCM token đã được đăng ký khi đăng nhập
- MyFirebaseMessagingService đã được implement
- Có thể nhận và hiển thị notification

**Backend:** ❌ Chưa gửi notification
- API `/api/payment/transfer/confirm` trả về SUCCESS
- Nhưng không có code gửi notification cho người nhận
- Người nhận không biết mình vừa nhận được tiền

## Giải Pháp: Thêm Code Gửi Notification Trong Backend

### Bước 1: Thêm Dependency Firebase Admin SDK

**File:** `pom.xml` (Maven) hoặc `build.gradle` (Gradle)

#### Maven:
```xml
<dependency>
    <groupId>com.google.firebase</groupId>
    <artifactId>firebase-admin</artifactId>
    <version>9.2.0</version>
</dependency>
```

#### Gradle:
```gradle
implementation 'com.google.firebase:firebase-admin:9.2.0'
```

### Bước 2: Download Service Account Key

1. Vào Firebase Console: https://console.firebase.google.com/
2. Chọn project của bạn
3. Click icon ⚙️ → **Project Settings**
4. Tab **Service Accounts**
5. Click **Generate new private key**
6. Download file JSON (ví dụ: `serviceAccountKey.json`)
7. Copy file vào thư mục `src/main/resources/` của backend project

### Bước 3: Khởi Tạo Firebase Admin SDK

**File:** `FirebaseConfig.java`

```java
package com.example.ibanking.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        try {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(
                            new ClassPathResource("serviceAccountKey.json").getInputStream()))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase Admin SDK initialized successfully");
            }
        } catch (IOException e) {
            System.err.println("Failed to initialize Firebase Admin SDK: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
```

### Bước 4: Tạo NotificationService

**File:** `NotificationService.java`

```java
package com.example.ibanking.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    /**
     * Gửi notification đến một FCM token
     */
    public void sendNotification(String fcmToken, String title, String body, Map<String, String> data) {
        if (fcmToken == null || fcmToken.isEmpty()) {
            log.warn("FCM token is null or empty, cannot send notification");
            return;
        }

        try {
            // Build notification
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            // Build message
            Message.Builder messageBuilder = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(notification);

            // Add data if provided
            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            Message message = messageBuilder.build();

            // Send message
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent notification to token: {} - Response: {}", 
                    fcmToken.substring(0, Math.min(20, fcmToken.length())) + "...", response);

        } catch (Exception e) {
            log.error("Failed to send notification to token: {}", fcmToken, e);
        }
    }

    /**
     * Gửi notification khi nhận được tiền
     */
    public void sendMoneyReceivedNotification(
            String receiverFcmToken,
            String senderName,
            double amount,
            String description,
            Long transactionId) {

        String title = "Bạn nhận được tiền";
        String body = String.format(
                "Bạn vừa nhận được %s từ %s. Nội dung: %s",
                formatCurrency(amount),
                senderName,
                description
        );

        Map<String, String> data = new HashMap<>();
        data.put("type", "MONEY_RECEIVED");
        data.put("transactionId", String.valueOf(transactionId));
        data.put("amount", String.valueOf(amount));
        data.put("senderName", senderName);

        sendNotification(receiverFcmToken, title, body, data);
    }

    /**
     * Gửi notification khi chuyển tiền thành công
     */
    public void sendMoneySentNotification(
            String senderFcmToken,
            String receiverName,
            double amount,
            Long transactionId) {

        String title = "Chuyển tiền thành công";
        String body = String.format(
                "Bạn đã chuyển %s đến %s",
                formatCurrency(amount),
                receiverName
        );

        Map<String, String> data = new HashMap<>();
        data.put("type", "MONEY_SENT");
        data.put("transactionId", String.valueOf(transactionId));
        data.put("amount", String.valueOf(amount));
        data.put("receiverName", receiverName);

        sendNotification(senderFcmToken, title, body, data);
    }

    /**
     * Format số tiền theo định dạng Việt Nam
     */
    private String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(amount);
    }
}
```

### Bước 5: Cập Nhật TransferController/Service

**File:** `TransferController.java` hoặc `TransferService.java`

```java
package com.example.ibanking.controller;

import com.example.ibanking.dto.TransferConfirmRequest;
import com.example.ibanking.dto.TransferConfirmResponse;
import com.example.ibanking.entity.User;
import com.example.ibanking.repository.UserRepository;
import com.example.ibanking.service.NotificationService;
import com.example.ibanking.service.TransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class TransferController {

    @Autowired
    private TransferService transferService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/transfer/confirm")
    public ResponseEntity<TransferConfirmResponse> confirmTransfer(
            @RequestBody TransferConfirmRequest request,
            Authentication authentication) {

        // Xử lý transfer
        TransferConfirmResponse response = transferService.confirmTransfer(request, authentication);

        // Nếu transfer thành công, gửi notification
        if ("SUCCESS".equals(response.getStatus())) {
            try {
                // Lấy thông tin người gửi
                String senderPhone = authentication.getName();
                User sender = userRepository.findByPhone(senderPhone)
                        .orElse(null);

                // Lấy thông tin người nhận
                User receiver = userRepository.findByAccountNumber(response.getReceiverAccountNumber())
                        .orElse(null);

                if (receiver != null && receiver.getFcmToken() != null && !receiver.getFcmToken().isEmpty()) {
                    // Gửi notification cho người nhận
                    String senderName = sender != null ? sender.getFullName() : "Người gửi";
                    notificationService.sendMoneyReceivedNotification(
                            receiver.getFcmToken(),
                            senderName,
                            response.getAmount(),
                            response.getDescription(),
                            response.getTransactionId()
                    );
                }

                // Optional: Gửi notification cho người gửi
                if (sender != null && sender.getFcmToken() != null && !sender.getFcmToken().isEmpty()) {
                    String receiverName = receiver != null ? receiver.getFullName() : "Người nhận";
                    notificationService.sendMoneySentNotification(
                            sender.getFcmToken(),
                            receiverName,
                            response.getAmount(),
                            response.getTransactionId()
                    );
                }

            } catch (Exception e) {
                // Log lỗi nhưng không fail transaction
                System.err.println("Failed to send notification: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return ResponseEntity.ok(response);
    }
}
```

### Bước 6: Thêm Field fcmToken Vào User Entity

**File:** `User.java`

```java
package com.example.ibanking.entity;

import javax.persistence.*;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String phone;
    private String fullName;
    private String email;
    private String accountNumber;
    
    // ⭐ Thêm field này
    @Column(name = "fcm_token", length = 500)
    private String fcmToken;
    
    // Getters and Setters
    public String getFcmToken() {
        return fcmToken;
    }
    
    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
    
    // ... other getters and setters
}
```

### Bước 7: Cập Nhật Database Schema

**SQL Migration:**

```sql
-- Thêm column fcm_token vào table users
ALTER TABLE users ADD COLUMN fcm_token VARCHAR(500);

-- Index để tìm kiếm nhanh
CREATE INDEX idx_users_fcm_token ON users(fcm_token);
```

### Bước 8: Cập Nhật API Register FCM Token

**File:** `NotificationController.java`

```java
package com.example.ibanking.controller;

import com.example.ibanking.dto.FcmTokenRequest;
import com.example.ibanking.dto.FcmTokenResponse;
import com.example.ibanking.entity.User;
import com.example.ibanking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register-token")
    public ResponseEntity<FcmTokenResponse> registerFcmToken(
            @RequestBody FcmTokenRequest request,
            Authentication authentication) {

        try {
            String phone = authentication.getName();
            User user = userRepository.findByPhone(phone)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Lưu FCM token
            user.setFcmToken(request.getFcmToken());
            userRepository.save(user);

            System.out.println("FCM token registered for user: " + phone);

            return ResponseEntity.ok(new FcmTokenResponse(true, "Đăng ký FCM token thành công"));

        } catch (Exception e) {
            System.err.println("Failed to register FCM token: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new FcmTokenResponse(false, "Đăng ký FCM token thất bại: " + e.getMessage()));
        }
    }
}
```

**DTO Classes:**

```java
// FcmTokenRequest.java
package com.example.ibanking.dto;

public class FcmTokenRequest {
    private String fcmToken;
    
    public String getFcmToken() {
        return fcmToken;
    }
    
    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}

// FcmTokenResponse.java
package com.example.ibanking.dto;

public class FcmTokenResponse {
    private boolean success;
    private String message;
    
    public FcmTokenResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
```

## Test Backend

### Test 1: Kiểm Tra Firebase Admin SDK Đã Khởi Tạo

Khi start backend, xem log:
```
Firebase Admin SDK initialized successfully
```

### Test 2: Test API Register FCM Token

```bash
curl -X POST http://localhost:8089/api/notifications/register-token \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fcmToken": "d65KcUD0RR6DB4Lz4qrEUn:APA91bEmkjaoNTW59dia5ADROH3lUPkl-6CZW2QWwfOI1_3Rr-XjLUuspvMywzYaPd6V6HU3yPfTnDKh-vIQSjhRAQ9vN03ii89StDGLHINpLi9Z6pJw-CI"
  }'
```

Kết quả mong đợi:
```json
{
  "success": true,
  "message": "Đăng ký FCM token thành công"
}
```

### Test 3: Test Gửi Notification Khi Chuyển Tiền

1. Đăng nhập 2 tài khoản trên 2 thiết bị
2. Chuyển tiền từ A sang B
3. Xem log backend:

```
Successfully sent notification to token: d65KcUD0RR6DB4Lz4q... - Response: projects/your-project/messages/0:1234567890
```

4. Kiểm tra điện thoại B nhận được notification:
   - Title: "Bạn nhận được tiền"
   - Body: "Bạn vừa nhận được 1.234.844 ₫ từ TRƯƠNG DƯƠNG HƯNG. Nội dung: TRƯƠNG DƯƠNG HƯNG chuyen tien"

## Troubleshooting

### Lỗi: "Failed to initialize Firebase Admin SDK"

**Nguyên nhân:** File `serviceAccountKey.json` không tồn tại hoặc sai đường dẫn

**Giải pháp:**
1. Kiểm tra file có trong `src/main/resources/`
2. Kiểm tra tên file đúng chưa
3. Rebuild project

### Lỗi: "Failed to send notification"

**Nguyên nhân:** FCM token không hợp lệ hoặc đã hết hạn

**Giải pháp:**
1. Kiểm tra FCM token trong database
2. Đăng nhập lại trên app để lấy token mới
3. Kiểm tra Firebase project settings

### Không Nhận Được Notification

**Checklist:**
- ✅ Backend đã khởi tạo Firebase Admin SDK
- ✅ FCM token đã được lưu vào database
- ✅ Backend log hiển thị "Successfully sent notification"
- ✅ App đang chạy hoặc ở background
- ✅ Notification permission đã được cấp

## Tóm Tắt

### Backend Cần Làm:
1. ✅ Thêm Firebase Admin SDK dependency
2. ✅ Download service account key
3. ✅ Khởi tạo Firebase Admin SDK
4. ✅ Tạo NotificationService
5. ✅ Thêm field fcmToken vào User entity
6. ✅ Cập nhật database schema
7. ✅ Implement API register FCM token
8. ✅ Gửi notification sau khi transfer thành công

### Frontend Đã Có:
- ✅ FCM token registration
- ✅ MyFirebaseMessagingService
- ✅ Notification display

### Kết Quả:
- Người nhận sẽ nhận được notification ngay khi có tiền chuyển vào
- Người gửi sẽ nhận được xác nhận chuyển tiền thành công
- Tất cả đều realtime, không cần refresh app
