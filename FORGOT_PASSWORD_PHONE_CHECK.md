# Thêm Kiểm Tra Số Điện Thoại Trong Quên Mật Khẩu

## Vấn Đề
Trong chức năng quên mật khẩu, khi người dùng nhập số điện thoại, hệ thống gửi OTP ngay lập tức mà không kiểm tra số điện thoại có tồn tại trong hệ thống hay không. Điều này gây lãng phí tài nguyên và trải nghiệm người dùng không tốt.

## Yêu Cầu
Khi người dùng nhập số điện thoại trong màn hình quên mật khẩu:
1. Kiểm tra số điện thoại có tồn tại trong hệ thống không (API: `api/auth/check-phone-exists?phone=`)
2. **Nếu tồn tại**: Gửi OTP và chuyển sang màn hình xác thực OTP
3. **Nếu không tồn tại**: Hiển thị lỗi, KHÔNG gửi OTP

## Thay Đổi

### File: ForgotPasswordActivity.java
**Đường dẫn**: `FrontEnd/banking-app/app/src/main/java/com/example/mobilebanking/activities/ForgotPasswordActivity.java`

#### 1. Thêm import
```java
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.AuthApiService;
import com.example.mobilebanking.api.dto.FeatureStatusResponse;
```

#### 2. Sửa setupListeners()
**Trước**:
```java
private void setupListeners() {
    btnSendOtp.setOnClickListener(v -> {
        String phone = etPhone.getText() != null
                ? etPhone.getText().toString().trim()
                : "";

        if (!isValidPhone(phone)) {
            showError(getString(R.string.invalid_phone_format));
            return;
        }

        hideError();
        requestOtp(phone); // Gửi OTP trực tiếp
    });
}
```

**Sau**:
```java
private void setupListeners() {
    btnSendOtp.setOnClickListener(v -> {
        String phone = etPhone.getText() != null
                ? etPhone.getText().toString().trim()
                : "";

        if (!isValidPhone(phone)) {
            showError(getString(R.string.invalid_phone_format));
            return;
        }

        hideError();
        checkPhoneExistsAndSendOtp(phone); // Kiểm tra trước khi gửi OTP
    });
}
```

#### 3. Thêm method checkPhoneExistsAndSendOtp()
```java
/**
 * Kiểm tra số điện thoại có tồn tại trong hệ thống không
 * Nếu tồn tại → Gửi OTP
 * Nếu không tồn tại → Hiển thị lỗi
 */
private void checkPhoneExistsAndSendOtp(String phone) {
    setLoading(true);

    AuthApiService authApiService = ApiClient.getAuthApiService();
    Call<FeatureStatusResponse> call = authApiService.checkPhoneExists(phone);

    call.enqueue(new Callback<FeatureStatusResponse>() {
        @Override
        public void onResponse(Call<FeatureStatusResponse> call, Response<FeatureStatusResponse> response) {
            if (response.isSuccessful() && response.body() != null) {
                FeatureStatusResponse result = response.body();

                // Nếu enabled = true → Số điện thoại đã tồn tại → OK, gửi OTP
                if (result.isEnabled()) {
                    requestOtp(phone);
                } else {
                    // Số điện thoại không tồn tại
                    setLoading(false);
                    showError("Số điện thoại này chưa được đăng ký. Vui lòng kiểm tra lại.");
                    Toast.makeText(ForgotPasswordActivity.this,
                            "Số điện thoại không tồn tại trong hệ thống",
                            Toast.LENGTH_LONG).show();
                }
            } else {
                setLoading(false);
                
                if (response.code() == 404) {
                    showError("Số điện thoại này chưa được đăng ký. Vui lòng kiểm tra lại.");
                } else {
                    Toast.makeText(ForgotPasswordActivity.this,
                            "Không thể kiểm tra số điện thoại. Vui lòng thử lại.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onFailure(Call<FeatureStatusResponse> call, Throwable t) {
            setLoading(false);
            Toast.makeText(ForgotPasswordActivity.this,
                    "Lỗi kết nối. Vui lòng kiểm tra mạng và thử lại.",
                    Toast.LENGTH_SHORT).show();
        }
    });
}
```

## Luồng Hoạt Động

### Luồng Cũ (Trước khi sửa)
```
1. Người dùng nhập số điện thoại
2. Click "Gửi OTP"
3. Gửi OTP ngay lập tức (không kiểm tra)
4. Chuyển sang màn hình xác thực OTP
```

### Luồng Mới (Sau khi sửa)
```
1. Người dùng nhập số điện thoại
2. Click "Gửi OTP"
3. Kiểm tra số điện thoại có tồn tại không
   ├─ Nếu TỒN TẠI:
   │  ├─ Gửi OTP qua Goixe247 API
   │  └─ Chuyển sang màn hình xác thực OTP
   └─ Nếu KHÔNG TỒN TẠI:
      ├─ Hiển thị lỗi: "Số điện thoại này chưa được đăng ký"
      └─ KHÔNG gửi OTP, KHÔNG chuyển màn hình
```

## API Sử Dụng

### 1. Check Phone Exists API
- **Endpoint**: `GET /api/auth/check-phone-exists?phone={phone}`
- **Response**:
  ```json
  {
    "enabled": true,  // true = tồn tại, false = không tồn tại
    "message": "Phone exists"
  }
  ```

### 2. Goixe247 OTP API
- **Endpoint**: `POST https://otp.goixe247.com/request_otp.php`
- **Parameters**:
  - `user_id`: "13"
  - `api_key`: "328945bfca039d9663890e71f4d9e2203669dd1e49fd3cb9a44fa86a48d915da"
  - `recipient_phone`: số điện thoại

## Hướng Dẫn Test

### Test Case 1: Số điện thoại không tồn tại
1. Mở app → Click "Quên mật khẩu"
2. Nhập số điện thoại chưa đăng ký (ví dụ: 0999999999)
3. Click "Gửi OTP"
4. **Kết quả mong đợi**:
   - Hiển thị lỗi: "Số điện thoại này chưa được đăng ký. Vui lòng kiểm tra lại."
   - Toast: "Số điện thoại không tồn tại trong hệ thống"
   - KHÔNG gửi OTP
   - KHÔNG chuyển sang màn hình xác thực OTP

### Test Case 2: Số điện thoại tồn tại
1. Mở app → Click "Quên mật khẩu"
2. Nhập số điện thoại đã đăng ký (ví dụ: 0123456789)
3. Click "Gửi OTP"
4. **Kết quả mong đợi**:
   - Hiển thị loading
   - Gửi OTP qua Goixe247 API
   - Toast: "Đã gửi mã OTP thành công"
   - Chuyển sang màn hình xác thực OTP

### Test Case 3: Lỗi kết nối
1. Mở app → Click "Quên mật khẩu"
2. Tắt mạng hoặc ngắt kết nối
3. Nhập số điện thoại bất kỳ
4. Click "Gửi OTP"
5. **Kết quả mong đợi**:
   - Toast: "Lỗi kết nối. Vui lòng kiểm tra mạng và thử lại."
   - KHÔNG chuyển màn hình

### Test Case 4: Số điện thoại không hợp lệ
1. Mở app → Click "Quên mật khẩu"
2. Nhập số điện thoại không hợp lệ (ví dụ: 123)
3. Click "Gửi OTP"
4. **Kết quả mong đợi**:
   - Hiển thị lỗi: "Định dạng số điện thoại không hợp lệ"
   - KHÔNG gọi API kiểm tra
   - KHÔNG gửi OTP

## So Sánh Với Đăng Ký

| Tính năng | Đăng ký | Quên mật khẩu |
|-----------|---------|---------------|
| Kiểm tra số điện thoại | ✅ Phải CHƯA tồn tại | ✅ Phải ĐÃ tồn tại |
| API kiểm tra | `check-phone-exists` | `check-phone-exists` |
| Logic | `enabled = false` → OK | `enabled = true` → OK |
| OTP API | eSMS | Goixe247 |
| Sau OTP đúng | Chuyển sang Step 2 | Chuyển sang đặt lại mật khẩu |

## Lợi Ích
1. ✅ Tránh gửi OTP cho số điện thoại không tồn tại (tiết kiệm chi phí SMS)
2. ✅ Trải nghiệm người dùng tốt hơn (thông báo rõ ràng)
3. ✅ Bảo mật tốt hơn (không tiết lộ thông tin số điện thoại nào đã đăng ký)
4. ✅ Giảm tải cho hệ thống OTP

## Ghi Chú
- API `check-phone-exists` trả về `enabled = true` khi số điện thoại TỒN TẠI
- Trong đăng ký: `enabled = false` → OK (chưa tồn tại)
- Trong quên mật khẩu: `enabled = true` → OK (đã tồn tại)
- OTP được gửi qua Goixe247 API (không phải eSMS)
- Mã OTP test: Nhận từ SMS thực tế hoặc kiểm tra log
