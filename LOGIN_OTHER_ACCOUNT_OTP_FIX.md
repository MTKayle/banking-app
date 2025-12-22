# Sửa Lỗi: Đăng Nhập Lần Đầu Phải Verify OTP

## Vấn Đề
Trước đây, khi người dùng mới tải app lần đầu và đăng nhập, app cho phép đăng nhập thẳng vào Dashboard mà không cần xác thực OTP. Điều này không an toàn.

## Giải Pháp
Đã sửa lại flow đăng nhập trong `LoginActivity.java`:

### Flow Mới

#### 1. Lần Đầu Tải App (chưa có lastUsername)
- Người dùng nhập số điện thoại và mật khẩu
- **Gọi API verify thông tin đăng nhập**
- Nếu đúng → Chuyển sang OTP verification
- Nếu sai → Hiển thị lỗi

#### 2. Đăng Nhập Lại Tài Khoản Cuối Cùng
- Người dùng nhập mật khẩu (số điện thoại đã tự động điền)
- **Đăng nhập thẳng vào Dashboard (không cần OTP)**

#### 3. Đăng Nhập Bằng Tài Khoản Khác
- Người dùng chọn "Đăng nhập bằng tài khoản khác"
- Nhập số điện thoại và mật khẩu
- **Gọi API verify thông tin đăng nhập**
- Nếu đúng → Chuyển sang OTP verification
- Nếu sai → Hiển thị lỗi

### Code Thay Đổi

#### Cập Nhật Logic trong `handleLogin()`
```java
// Kiểm tra xem có phải tài khoản cuối cùng không
String lastUsername = dataManager.getLastUsername();
final String finalPhone = phone;
final String finalPassword = password;

// Chỉ khi đăng nhập lại ĐÚNG tài khoản cuối cùng thì mới không cần OTP
if (lastUsername != null && !lastUsername.isEmpty() && finalPhone.equals(lastUsername)) {
    // Đăng nhập lại tài khoản cuối cùng → Đăng nhập bình thường (không cần OTP)
    performPasswordLogin(finalPhone, finalPassword);
    return;
}

// Các trường hợp khác → Yêu cầu OTP:
// 1. Lần đầu tải app (lastUsername = null)
// 2. Đăng nhập bằng tài khoản khác (finalPhone != lastUsername)
verifyLoginAndRequestOtp(finalPhone, finalPassword);
```

#### Method `verifyLoginAndRequestOtp()`
```java
/**
 * Xác thực thông tin đăng nhập trước khi yêu cầu OTP
 * Dùng cho:
 * - Lần đầu tải app
 * - Đăng nhập bằng tài khoản khác
 */
private void verifyLoginAndRequestOtp(String phone, String password) {
    // Disable login button
    btnLogin.setEnabled(false);
    btnLogin.setText("Đang kiểm tra...");

    // Call API để verify thông tin đăng nhập
    LoginRequest loginRequest = new LoginRequest(phone, password);
    AuthApiService authApiService = ApiClient.getAuthApiService();
    
    Call<AuthResponse> call = authApiService.login(loginRequest);
    call.enqueue(new Callback<AuthResponse>() {
        @Override
        public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
            btnLogin.setEnabled(true);
            btnLogin.setText("Đăng nhập");
            
            if (response.isSuccessful() && response.body() != null) {
                // Đăng nhập thành công → Chuyển sang OTP verification
                new AlertDialog.Builder(LoginActivity.this)
                    .setTitle("Xác Thực OTP")
                    .setMessage("Thông tin đăng nhập chính xác. Vui lòng xác thực OTP để tiếp tục.")
                    .setPositiveButton("Xác Thực", (dialog, which) -> {
                        Intent intent = new Intent(LoginActivity.this, OtpVerificationActivity.class);
                        intent.putExtra("flow", "login_verification");
                        intent.putExtra("phone", phone);
                        intent.putExtra("password", password);
                        startActivity(intent);
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
            } else {
                // Hiển thị lỗi
                String errorMessage = "Số điện thoại hoặc mật khẩu không chính xác";
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onFailure(Call<AuthResponse> call, Throwable t) {
            // Xử lý lỗi kết nối...
        }
    });
}
```

## Hướng Dẫn Test

### Test Case 1: Lần Đầu Tải App - Thông Tin Đúng
1. Xóa data app hoặc cài mới
2. Mở app → Nhập số điện thoại và mật khẩu (tài khoản đã có trong hệ thống)
3. Nhấn "Đăng nhập"

**Kết quả mong đợi:**
- Hiển thị loading "Đang kiểm tra..."
- API verify thành công → Hiển thị dialog "Thông tin đăng nhập chính xác. Vui lòng xác thực OTP để tiếp tục."
- Nhấn "Xác Thực" → Chuyển sang trang OTP verification
- Nhập OTP đúng → Đăng nhập thành công vào Dashboard

### Test Case 2: Lần Đầu Tải App - Thông Tin Sai
1. Xóa data app hoặc cài mới
2. Mở app → Nhập số điện thoại và **mật khẩu sai**
3. Nhấn "Đăng nhập"

**Kết quả mong đợi:**
- Hiển thị loading "Đang kiểm tra..."
- API verify thất bại → Hiển thị Toast "Số điện thoại hoặc mật khẩu không chính xác"
- **KHÔNG** chuyển sang trang OTP

### Test Case 3: Đăng Nhập Lại Tài Khoản Cuối Cùng
1. Đăng nhập thành công với tài khoản A (ví dụ: 0123456789)
2. Đăng xuất
3. Mở app lại → Nhập mật khẩu (số điện thoại đã tự động điền)
4. Nhấn "Đăng nhập"

**Kết quả mong đợi:**
- Đăng nhập bình thường (không yêu cầu OTP)
- Chuyển thẳng vào Dashboard

### Test Case 4: Đăng Nhập Bằng Tài Khoản Khác - Thông Tin Đúng
1. Đăng nhập thành công với tài khoản A
2. Đăng xuất
3. Mở app lại → Chọn "Đăng nhập bằng tài khoản khác"
4. Nhập tài khoản B (ví dụ: 0987654321) với mật khẩu đúng
5. Nhấn "Đăng nhập"

**Kết quả mong đợi:**
- Hiển thị loading "Đang kiểm tra..."
- API verify thành công → Hiển thị dialog xác nhận
- Nhấn "Xác Thực" → Chuyển sang trang OTP verification

### Test Case 5: Đăng Nhập Bằng Tài Khoản Khác - Thông Tin Sai
1. Đăng nhập thành công với tài khoản A
2. Đăng xuất
3. Mở app lại → Chọn "Đăng nhập bằng tài khoản khác"
4. Nhập tài khoản B với **mật khẩu sai**
5. Nhấn "Đăng nhập"

**Kết quả mong đợi:**
- Hiển thị loading "Đang kiểm tra..."
- API verify thất bại → Hiển thị Toast lỗi
- **KHÔNG** chuyển sang trang OTP

## Tóm Tắt Logic

| Trường Hợp | Cần OTP? | Lý Do |
|------------|----------|-------|
| Lần đầu tải app | ✅ CÓ | Bảo mật cao hơn cho lần đăng nhập đầu tiên |
| Đăng nhập lại tài khoản cuối | ❌ KHÔNG | Trải nghiệm người dùng tốt hơn |
| Đăng nhập tài khoản khác | ✅ CÓ | Bảo mật khi chuyển đổi tài khoản |

## Lợi Ích
✅ Bảo mật cao hơn: Lần đầu đăng nhập phải verify OTP  
✅ Trải nghiệm tốt: Đăng nhập lại tài khoản cũ không cần OTP  
✅ Linh hoạt: Hỗ trợ nhiều tài khoản với xác thực OTP  

## File Đã Thay Đổi
- `FrontEnd/banking-app/app/src/main/java/com/example/mobilebanking/activities/LoginActivity.java`

## API Sử Dụng
- `POST /auth/login` - Verify số điện thoại và mật khẩu
