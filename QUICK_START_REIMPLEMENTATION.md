# Quick Start - Implement Lại Nhanh

## 🚀 Hướng Dẫn Implement Lại Từ Đầu

Nếu code bị mất, làm theo thứ tự sau:

---

## Bước 1: Tạo DTO Classes (5 phút)

### 1.1 Tạo SmartFlagsRequest.java
- Location: `app/src/main/java/com/example/mobilebanking/api/dto/`
- Copy code từ: `DTO_CLASSES_REFERENCE.md` → Section 1

### 1.2 Tạo UserResponse.java
- Location: `app/src/main/java/com/example/mobilebanking/api/dto/`
- Copy code từ: `DTO_CLASSES_REFERENCE.md` → Section 2

### 1.3 Tạo UserApiService.java
- Location: `app/src/main/java/com/example/mobilebanking/api/`
- Copy code từ: `DTO_CLASSES_REFERENCE.md` → Section 3

---

## Bước 2: Update ApiClient.java (2 phút)

### 2.1 Thêm field
```java
private static UserApiService userApiService;
```

### 2.2 Thêm method
Copy từ: `CODE_BACKUP_IMPORTANT_CHANGES.md` → Section 5

### 2.3 Update reset()
Thêm dòng: `userApiService = null;`

---

## Bước 3: Update SettingsActivity.java (10 phút)

### 3.1 Thêm import
```java
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.UserApiService;
import com.example.mobilebanking.api.dto.SmartFlagsRequest;
import com.example.mobilebanking.api.dto.UserResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
```

### 3.2 Thêm field
```java
private UserApiService userApiService;
```

### 3.3 Trong onCreate()
```java
userApiService = ApiClient.getUserApiService();
```

### 3.4 Thêm 2 methods
Copy từ: `CODE_BACKUP_IMPORTANT_CHANGES.md` → Section 4
- `enableFingerprintOnBackend()`
- `disableFingerprintOnBackend()`

### 3.5 Update toggleBiometric()
Thay đổi:
- Khi bật: Gọi `enableFingerprintOnBackend()` trong callback `onSuccess()`
- Khi tắt: Gọi `disableFingerprintOnBackend()` thay vì `biometricManager.disableBiometric()`

---

## Bước 4: Update LoginActivity.java (15 phút)

### 4.1 Update handleLogin()
Copy từ: `CODE_BACKUP_IMPORTANT_CHANGES.md` → Section 1
- Thêm logic kiểm tra tài khoản cuối cùng
- Hiển thị dialog OTP
- Tạo method `performPasswordLogin()`

### 4.2 Update phần lưu token
Copy từ: `CODE_BACKUP_IMPORTANT_CHANGES.md` → Section 2
- Trong `performPasswordLogin()`, sau khi login thành công
- Luôn gọi `saveRefreshTokenWithoutAuth()`

### 4.3 Update startBiometricFlow()
Copy từ: `CODE_BACKUP_IMPORTANT_CHANGES.md` → Section 3
- Trong callback của `refreshToken()`
- Lưu userId, phone, fullName, email

---

## Bước 5: Update OtpVerificationActivity.java (20 phút)

### 5.1 Thêm field
```java
private String password; // For login_verification flow
```

### 5.2 Update onCreate()
Copy từ: `CODE_BACKUP_IMPORTANT_CHANGES.md` → Section 6
- Lấy flow từ intent
- Lấy password từ intent
- Xử lý flow `login_verification`

### 5.3 Update handleOtpVerification()
Thêm `login_verification` vào điều kiện:
```java
if ("forgot_password".equals(fromActivity) || "movie_booking".equals(fromActivity) || "login_verification".equals(fromActivity)) {
    verifyOtpWithGoixe(otp);
}
```

### 5.4 Update verifyOtpWithGoixe()
Thêm xử lý:
```java
else if ("login_verification".equals(fromActivity)) {
    performLogin();
}
```

### 5.5 Thêm method performLogin()
Copy từ: `CODE_BACKUP_IMPORTANT_CHANGES.md` → Section 6

### 5.6 Update resendOtp()
Thêm `login_verification` vào điều kiện

---

## Bước 6: Test (10 phút)

### 6.1 Test Fingerprint
1. Đăng nhập → Vào Settings → Bật fingerprint
2. Đăng xuất → Đăng nhập bằng vân tay
3. Vào Settings → Tắt fingerprint

### 6.2 Test OTP Login
1. Đăng nhập phone A → Đăng xuất
2. Đăng nhập phone B → Xác thực OTP
3. Đăng nhập thành công

---

## ⏱️ Tổng Thời Gian: ~60 phút

---

## 📋 Checklist

- [ ] Tạo SmartFlagsRequest.java
- [ ] Tạo UserResponse.java
- [ ] Tạo UserApiService.java
- [ ] Update ApiClient.java
- [ ] Update SettingsActivity.java
- [ ] Update LoginActivity.java (handleLogin)
- [ ] Update LoginActivity.java (lưu token)
- [ ] Update LoginActivity.java (startBiometricFlow)
- [ ] Update OtpVerificationActivity.java (field)
- [ ] Update OtpVerificationActivity.java (onCreate)
- [ ] Update OtpVerificationActivity.java (handleOtpVerification)
- [ ] Update OtpVerificationActivity.java (verifyOtpWithGoixe)
- [ ] Update OtpVerificationActivity.java (performLogin)
- [ ] Update OtpVerificationActivity.java (resendOtp)
- [ ] Test fingerprint
- [ ] Test OTP login

---

## 🆘 Nếu Gặp Lỗi

### Lỗi Compile:
1. Kiểm tra import statements
2. Kiểm tra package names
3. Rebuild project

### Lỗi Runtime:
1. Kiểm tra userId có được lưu không
2. Kiểm tra backend API có hoạt động không
3. Xem logs trong Logcat

### Lỗi API:
1. Kiểm tra endpoint URL
2. Kiểm tra Authorization header
3. Kiểm tra request body format

---

## 📚 Tài Liệu Tham Khảo

1. **IMPLEMENTATION_CHECKLIST.md** - Danh sách tất cả thay đổi
2. **CODE_BACKUP_IMPORTANT_CHANGES.md** - Code backup chi tiết
3. **DTO_CLASSES_REFERENCE.md** - DTO classes reference
4. **FINGERPRINT_ALL_FIXES_SUMMARY.md** - Tổng hợp fingerprint fixes
5. **OTP_LOGIN_VERIFICATION_GUIDE.md** - Hướng dẫn OTP login

---

## 💡 Tips

- Làm từng bước một, test sau mỗi bước
- Commit code sau mỗi feature hoàn thành
- Backup code thường xuyên
- Đọc documentation trước khi implement
