# ✅ Fix Lỗi Compile - OtpApiService

## 🐛 Lỗi
```
error: cannot find symbol
import com.example.mobilebanking.api.OtpApiService;
^
symbol:   class OtpApiService
location: package com.example.mobilebanking.api
```

## 🔧 Nguyên Nhân
- File `OtpApiService.java` tồn tại ở package SAI: `com.example.mobilebanking.utils`
- File `OtpResponse.java` cũng ở package SAI: `com.example.mobilebanking.utils`
- `OtpVerificationActivity` import từ package `com.example.mobilebanking.api`

## ✅ Giải Pháp

### 1. Tạo OtpApiService.java ở đúng package
**File:** `app/src/main/java/com/example/mobilebanking/api/OtpApiService.java`

```java
package com.example.mobilebanking.api;

import com.example.mobilebanking.api.dto.OtpResponse;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface OtpApiService {
    
    @FormUrlEncoded
    @POST("api/otp/request")
    Call<OtpResponse> requestOtp(
            @Field("user_id") String userId,
            @Field("api_key") String apiKey,
            @Field("phone") String phone
    );

    @FormUrlEncoded
    @POST("api/otp/verify")
    Call<OtpResponse> verifyOtp(
            @Field("user_id") String userId,
            @Field("api_key") String apiKey,
            @Field("phone") String phone,
            @Field("otp") String otp
    );
}
```

### 2. Tạo OtpResponse.java ở đúng package
**File:** `app/src/main/java/com/example/mobilebanking/api/dto/OtpResponse.java`

```java
package com.example.mobilebanking.api.dto;

import com.google.gson.annotations.SerializedName;

public class OtpResponse {
    @SerializedName("success")
    private Boolean success;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("status")
    private String status;

    public Boolean getSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }

    public boolean isSuccess() {
        if (success != null) {
            return success;
        }
        if (status != null) {
            return "success".equalsIgnoreCase(status);
        }
        return false;
    }
}
```

## 📝 Thay Đổi So Với File Cũ

### OtpApiService:
| Cũ | Mới |
|----|-----|
| Package: `utils` | Package: `api` |
| Endpoint: `request_otp.php` | Endpoint: `api/otp/request` |
| Endpoint: `verify_otp.php` | Endpoint: `api/otp/verify` |
| Field: `recipient_phone` | Field: `phone` |
| Field: `otp_code` | Field: `otp` |

### OtpResponse:
| Cũ | Mới |
|----|-----|
| Package: `utils` | Package: `dto` |
| Chỉ có `status` và `message` | Có cả `success`, `status`, `message` |
| Không có annotation | Có `@SerializedName` |

## 🎯 Goixe247 API

### Base URL:
```
https://otp.goixe247.com/
```

### Gửi OTP:
```
POST /api/otp/request
Body: {
  "user_id": "13",
  "api_key": "328945bfca039d9663890e71f4d9e2203669dd1e49fd3cb9a44fa86a48d915da",
  "phone": "0901234567"
}

Response: {
  "success": true,
  "message": "OTP sent successfully"
}
```

### Xác Thực OTP:
```
POST /api/otp/verify
Body: {
  "user_id": "13",
  "api_key": "328945bfca039d9663890e71f4d9e2203669dd1e49fd3cb9a44fa86a48d915da",
  "phone": "0901234567",
  "otp": "123456"
}

Response: {
  "success": true,
  "message": "OTP verified successfully"
}
```

## ✅ Kết Quả

Sau khi tạo 2 files:
- ✅ `OtpApiService.java` ở package `api`
- ✅ `OtpResponse.java` ở package `dto`

**Lỗi compile đã được fix!**

## 🧪 Kiểm Tra

```bash
# Build project
Build → Rebuild Project

# Hoặc
./gradlew clean build
```

**Kết quả:** ✅ No diagnostics found

## 📚 Files Liên Quan

1. `OtpApiService.java` - Interface cho Goixe247 API
2. `OtpResponse.java` - DTO cho response
3. `OtpVerificationActivity.java` - Activity sử dụng OTP API
4. `LoginActivity.java` - Gọi OTP verification cho tài khoản khác
5. `ForgotPasswordActivity.java` - Gọi OTP cho quên mật khẩu
6. `MoviePaymentActivity.java` - Gọi OTP cho đặt vé

## 🎉 Hoàn Thành

Tất cả các file đã được tạo đúng package và không còn lỗi compile!

