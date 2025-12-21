# DTO Classes Reference

## 📌 Các DTO Classes Mới Đã Tạo

---

## 1. SmartFlagsRequest.java

**Location:** `app/src/main/java/com/example/mobilebanking/api/dto/SmartFlagsRequest.java`

```java
package com.example.mobilebanking.api.dto;

public class SmartFlagsRequest {
    private Boolean smartEkycEnabled;
    private Boolean faceRecognitionEnabled;
    private Boolean fingerprintLoginEnabled;

    public SmartFlagsRequest() {
    }

    public SmartFlagsRequest(Boolean fingerprintLoginEnabled) {
        this.fingerprintLoginEnabled = fingerprintLoginEnabled;
    }

    public Boolean getSmartEkycEnabled() {
        return smartEkycEnabled;
    }

    public void setSmartEkycEnabled(Boolean smartEkycEnabled) {
        this.smartEkycEnabled = smartEkycEnabled;
    }

    public Boolean getFaceRecognitionEnabled() {
        return faceRecognitionEnabled;
    }

    public void setFaceRecognitionEnabled(Boolean faceRecognitionEnabled) {
        this.faceRecognitionEnabled = faceRecognitionEnabled;
    }

    public Boolean getFingerprintLoginEnabled() {
        return fingerprintLoginEnabled;
    }

    public void setFingerprintLoginEnabled(Boolean fingerprintLoginEnabled) {
        this.fingerprintLoginEnabled = fingerprintLoginEnabled;
    }
}
```

---

## 2. UserResponse.java

**Location:** `app/src/main/java/com/example/mobilebanking/api/dto/UserResponse.java`

```java
package com.example.mobilebanking.api.dto;

public class UserResponse {
    private Long userId;
    private String phone;
    private String email;
    private String fullName;
    private String cccdNumber;
    private Boolean smartEkycEnabled;
    private Boolean faceRecognitionEnabled;
    private Boolean fingerprintLoginEnabled;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCccdNumber() {
        return cccdNumber;
    }

    public void setCccdNumber(String cccdNumber) {
        this.cccdNumber = cccdNumber;
    }

    public Boolean getSmartEkycEnabled() {
        return smartEkycEnabled;
    }

    public void setSmartEkycEnabled(Boolean smartEkycEnabled) {
        this.smartEkycEnabled = smartEkycEnabled;
    }

    public Boolean getFaceRecognitionEnabled() {
        return faceRecognitionEnabled;
    }

    public void setFaceRecognitionEnabled(Boolean faceRecognitionEnabled) {
        this.faceRecognitionEnabled = faceRecognitionEnabled;
    }

    public Boolean getFingerprintLoginEnabled() {
        return fingerprintLoginEnabled;
    }

    public void setFingerprintLoginEnabled(Boolean fingerprintLoginEnabled) {
        this.fingerprintLoginEnabled = fingerprintLoginEnabled;
    }
}
```

---

## 3. UserApiService.java

**Location:** `app/src/main/java/com/example/mobilebanking/api/UserApiService.java`

```java
package com.example.mobilebanking.api;

import com.example.mobilebanking.api.dto.SmartFlagsRequest;
import com.example.mobilebanking.api.dto.UserResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PATCH;
import retrofit2.http.Path;

/**
 * User Management API Service
 */
public interface UserApiService {
    
    /**
     * Update user smart flags (fingerprint, face recognition, etc.)
     * Endpoint: PATCH /users/{userId}/settings
     */
    @PATCH("users/{userId}/settings")
    Call<UserResponse> updateSmartFlags(
            @Path("userId") Long userId,
            @Body SmartFlagsRequest request
    );
}
```

---

## 📝 Cách Sử Dụng

### Bật Fingerprint:
```java
SmartFlagsRequest request = new SmartFlagsRequest();
request.setFingerprintLoginEnabled(true);

UserApiService apiService = ApiClient.getUserApiService();
Call<UserResponse> call = apiService.updateSmartFlags(userId, request);

call.enqueue(new Callback<UserResponse>() {
    @Override
    public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
        if (response.isSuccessful() && response.body() != null) {
            // Success
        }
    }
    
    @Override
    public void onFailure(Call<UserResponse> call, Throwable t) {
        // Error
    }
});
```

### Tắt Fingerprint:
```java
SmartFlagsRequest request = new SmartFlagsRequest();
request.setFingerprintLoginEnabled(false);

// Same as above
```

---

## 🔗 Backend API

### Endpoint:
```
PATCH /users/{userId}/settings
```

### Headers:
```
Authorization: Bearer {access_token}
Content-Type: application/json
```

### Request Body:
```json
{
  "fingerprintLoginEnabled": true,
  "faceRecognitionEnabled": false,
  "smartEkycEnabled": false
}
```

### Response:
```json
{
  "userId": 123,
  "phone": "0901234567",
  "email": "user@example.com",
  "fullName": "Nguyen Van A",
  "cccdNumber": "001234567890",
  "smartEkycEnabled": false,
  "faceRecognitionEnabled": false,
  "fingerprintLoginEnabled": true
}
```

---

## ⚠️ Lưu Ý

1. **userId** là bắt buộc - Lấy từ `DataManager.getInstance(context).getUserId()`
2. **Authorization header** tự động được thêm bởi `ApiClient`
3. Chỉ cần set field cần update, các field khác có thể null
4. Backend sẽ chỉ update các field không null
