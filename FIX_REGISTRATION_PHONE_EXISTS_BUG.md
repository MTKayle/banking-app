# Sửa Lỗi: Số Điện Thoại Đã Tồn Tại Vẫn Chuyển Sang Trang OTP

## Mô Tả Lỗi
Trong bước 1 của đăng ký, khi người dùng nhập số điện thoại đã tồn tại:
- ❌ Hệ thống hiển thị lỗi "Số điện thoại này đã được đăng ký"
- ❌ Nhưng VẪN chuyển sang trang OTP verification
- ❌ Điều này không đúng với yêu cầu

## Yêu Cầu Đúng
Khi số điện thoại đã tồn tại:
- ✅ Hiển thị lỗi
- ✅ KHÔNG chuyển sang trang OTP
- ✅ Cho phép người dùng nhập lại số điện thoại khác

## Nguyên Nhân
Trong file `Step1BasicInfoFragment.java`, method `checkPhoneExistsAndContinue()` có logic sai:

```java
} else {
    // Lỗi từ server
    if (response.code() == 404) {
        // BUG: Vẫn gọi saveDataAndContinue() khi lỗi 404
        saveDataAndContinue(phone, email, password, confirmPassword);
    } else {
        Toast.makeText(getContext(), "Không thể kiểm tra số điện thoại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
    }
}
```

**Vấn đề**: Khi API trả về lỗi 404 (hoặc bất kỳ lỗi nào), code vẫn gọi `saveDataAndContinue()`, dẫn đến việc mở trang OTP.

## Giải Pháp

### File: Step1BasicInfoFragment.java
**Đường dẫn**: `FrontEnd/banking-app/app/src/main/java/com/example/mobilebanking/fragments/Step1BasicInfoFragment.java`

### Thay đổi trong `checkPhoneExistsAndContinue()`:

**TRƯỚC** (Code lỗi):
```java
@Override
public void onResponse(Call<FeatureStatusResponse> call, Response<FeatureStatusResponse> response) {
    if (progressDialog != null && progressDialog.isShowing()) {
        progressDialog.dismiss();
    }
    
    if (response.isSuccessful() && response.body() != null) {
        FeatureStatusResponse result = response.body();
        
        if (result.isEnabled()) {
            tilPhone.setError("Số điện thoại này đã được đăng ký");
            Toast.makeText(getContext(), "Số điện thoại đã tồn tại. Vui lòng sử dụng số khác.", Toast.LENGTH_LONG).show();
        } else {
            saveDataAndContinue(phone, email, password, confirmPassword);
        }
    } else {
        // Lỗi từ server
        Log.e(TAG, "Check phone exists failed: " + response.code());
        
        if (response.code() == 404) {
            // BUG: Vẫn cho phép tiếp tục
            saveDataAndContinue(phone, email, password, confirmPassword);
        } else {
            Toast.makeText(getContext(), "Không thể kiểm tra số điện thoại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
        }
    }
}
```

**SAU** (Code đã sửa):
```java
@Override
public void onResponse(Call<FeatureStatusResponse> call, Response<FeatureStatusResponse> response) {
    if (progressDialog != null && progressDialog.isShowing()) {
        progressDialog.dismiss();
    }
    
    if (response.isSuccessful() && response.body() != null) {
        FeatureStatusResponse result = response.body();
        
        // Nếu enabled = true → Số điện thoại đã tồn tại → KHÔNG cho phép tiếp tục
        if (result.isEnabled()) {
            tilPhone.setError("Số điện thoại này đã được đăng ký");
            Toast.makeText(getContext(), "Số điện thoại đã tồn tại. Vui lòng sử dụng số khác.", Toast.LENGTH_LONG).show();
            // KHÔNG gọi saveDataAndContinue() - dừng lại ở đây
        } else {
            // Số điện thoại chưa tồn tại (enabled = false) → OK, tiếp tục
            saveDataAndContinue(phone, email, password, confirmPassword);
        }
    } else {
        // Lỗi từ server - KHÔNG cho phép tiếp tục
        Log.e(TAG, "Check phone exists failed: " + response.code());
        Toast.makeText(getContext(), "Không thể kiểm tra số điện thoại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
    }
}
```

## Thay Đổi Chính

### 1. Xóa logic xử lý 404
- **Trước**: Khi API trả về 404, vẫn gọi `saveDataAndContinue()`
- **Sau**: Mọi lỗi API đều hiển thị thông báo lỗi và KHÔNG tiếp tục

### 2. Chỉ tiếp tục khi enabled = false
- **Điều kiện duy nhất để tiếp tục**: `response.isSuccessful() && response.body() != null && !result.isEnabled()`
- **Mọi trường hợp khác**: Dừng lại và yêu cầu người dùng thử lại

## Logic API

### API: check-phone-exists
- **Endpoint**: `GET /api/auth/check-phone-exists?phone={phone}`
- **Response**:
  ```json
  {
    "enabled": true,  // true = đã tồn tại, false = chưa tồn tại
    "message": "Phone exists"
  }
  ```

### Logic Xử Lý
| Trường hợp | enabled | Hành động |
|------------|---------|-----------|
| Số điện thoại đã tồn tại | `true` | ❌ Hiển thị lỗi, KHÔNG tiếp tục |
| Số điện thoại chưa tồn tại | `false` | ✅ Chuyển sang OTP verification |
| API lỗi (404, 500, etc.) | N/A | ❌ Hiển thị lỗi, KHÔNG tiếp tục |
| Lỗi kết nối | N/A | ❌ Hiển thị lỗi, KHÔNG tiếp tục |

## Luồng Hoạt Động Sau Khi Sửa

```
1. Người dùng nhập thông tin Step 1
2. Click "Tiếp tục"
3. Validate dữ liệu (phone, email, password)
4. Call API check-phone-exists
   │
   ├─ Response thành công (200 OK)
   │  │
   │  ├─ enabled = true (Số điện thoại ĐÃ tồn tại)
   │  │  ├─ Hiển thị lỗi: "Số điện thoại này đã được đăng ký"
   │  │  ├─ Toast: "Số điện thoại đã tồn tại. Vui lòng sử dụng số khác."
   │  │  └─ DỪNG LẠI - Người dùng vẫn ở Step 1
   │  │
   │  └─ enabled = false (Số điện thoại CHƯA tồn tại)
   │     ├─ Lưu dữ liệu vào RegistrationData
   │     ├─ Mở OtpVerificationActivity
   │     └─ Gửi OTP qua eSMS
   │
   └─ Response lỗi (404, 500, etc.)
      ├─ Toast: "Không thể kiểm tra số điện thoại. Vui lòng thử lại."
      └─ DỪNG LẠI - Người dùng vẫn ở Step 1
```

## Test Cases

### Test Case 1: Số điện thoại đã tồn tại ✅
**Bước thực hiện**:
1. Mở app → Click "Đăng ký"
2. Nhập số điện thoại đã tồn tại: `0123456789`
3. Nhập email: `test@example.com`
4. Nhập mật khẩu: `123456`
5. Nhập xác nhận mật khẩu: `123456`
6. Click "Tiếp tục"

**Kết quả mong đợi**:
- ✅ Hiển thị loading "Đang kiểm tra số điện thoại..."
- ✅ Hiển thị lỗi dưới ô số điện thoại: "Số điện thoại này đã được đăng ký"
- ✅ Toast: "Số điện thoại đã tồn tại. Vui lòng sử dụng số khác."
- ✅ KHÔNG mở trang OTP
- ✅ KHÔNG chuyển sang Step 2
- ✅ Người dùng vẫn ở Step 1

### Test Case 2: Số điện thoại chưa tồn tại ✅
**Bước thực hiện**:
1. Mở app → Click "Đăng ký"
2. Nhập số điện thoại chưa tồn tại: `0987654321`
3. Nhập email: `newuser@example.com`
4. Nhập mật khẩu: `123456`
5. Nhập xác nhận mật khẩu: `123456`
6. Click "Tiếp tục"

**Kết quả mong đợi**:
- ✅ Hiển thị loading "Đang kiểm tra số điện thoại..."
- ✅ Mở trang OTP verification
- ✅ Gửi OTP qua eSMS
- ✅ Hiển thị "Đã gửi đến 0987654321"

### Test Case 3: Lỗi API (404, 500) ✅
**Bước thực hiện**:
1. Mở app → Click "Đăng ký"
2. Nhập thông tin hợp lệ
3. Click "Tiếp tục"
4. (Giả sử API trả về lỗi 404 hoặc 500)

**Kết quả mong đợi**:
- ✅ Hiển thị loading "Đang kiểm tra số điện thoại..."
- ✅ Toast: "Không thể kiểm tra số điện thoại. Vui lòng thử lại."
- ✅ KHÔNG mở trang OTP
- ✅ Người dùng vẫn ở Step 1

### Test Case 4: Lỗi kết nối ✅
**Bước thực hiện**:
1. Tắt mạng hoặc ngắt kết nối
2. Mở app → Click "Đăng ký"
3. Nhập thông tin hợp lệ
4. Click "Tiếp tục"

**Kết quả mong đợi**:
- ✅ Hiển thị loading "Đang kiểm tra số điện thoại..."
- ✅ Toast: "Lỗi kết nối. Vui lòng kiểm tra mạng và thử lại."
- ✅ KHÔNG mở trang OTP
- ✅ Người dùng vẫn ở Step 1

## So Sánh Trước và Sau

| Tình huống | Trước (Lỗi) | Sau (Đã sửa) |
|------------|-------------|--------------|
| Số điện thoại đã tồn tại | ❌ Hiển thị lỗi NHƯNG vẫn mở OTP | ✅ Hiển thị lỗi, KHÔNG mở OTP |
| Số điện thoại chưa tồn tại | ✅ Mở OTP | ✅ Mở OTP |
| API lỗi 404 | ❌ Vẫn mở OTP | ✅ Hiển thị lỗi, KHÔNG mở OTP |
| API lỗi 500 | ✅ Hiển thị lỗi | ✅ Hiển thị lỗi |
| Lỗi kết nối | ✅ Hiển thị lỗi | ✅ Hiển thị lỗi |

## Tóm Tắt
- ✅ Đã sửa lỗi: Số điện thoại đã tồn tại vẫn chuyển sang trang OTP
- ✅ Đã xóa logic xử lý 404 cho phép tiếp tục
- ✅ Chỉ cho phép tiếp tục khi `enabled = false` (số điện thoại chưa tồn tại)
- ✅ Mọi lỗi API đều KHÔNG cho phép tiếp tục
- ✅ Trải nghiệm người dùng được cải thiện
