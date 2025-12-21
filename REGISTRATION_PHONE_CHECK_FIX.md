# Thêm Kiểm Tra Số Điện Thoại Đã Tồn Tại Trong Đăng Ký

## Mục Đích
Kiểm tra số điện thoại đã được đăng ký chưa trước khi cho phép người dùng tiếp tục đăng ký và gửi OTP.

## Vấn Đề Trước Đây
- Người dùng nhập số điện thoại đã tồn tại
- Hệ thống vẫn cho phép tiếp tục và gửi OTP
- Chỉ phát hiện lỗi ở bước cuối cùng khi gọi API register
- Lãng phí thời gian và OTP của người dùng

## Giải Pháp
Thêm API call `GET /api/auth/check-phone-exists?phone={phone}` ngay ở Step 1 trước khi chuyển sang bước tiếp theo.

## Các Thay Đổi

### 1. AuthApiService.java
Thêm API endpoint mới:

```java
/**
 * Kiểm tra số điện thoại đã tồn tại chưa
 * Dùng trong registration để validate phone trước khi gửi OTP
 */
@GET("auth/check-phone-exists")
Call<FeatureStatusResponse> checkPhoneExists(@Query("phone") String phone);
```

**Response Format:**
```json
{
  "enabled": true,    // true = số điện thoại đã tồn tại
  "message": "Phone number already exists"
}
```

hoặc

```json
{
  "enabled": false,   // false = số điện thoại chưa tồn tại (OK)
  "message": "Phone number available"
}
```

### 2. Step1BasicInfoFragment.java

#### a. Thêm Import và Biến
```java
import android.app.ProgressDialog;
import android.util.Log;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.AuthApiService;
import com.example.mobilebanking.api.dto.FeatureStatusResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

private static final String TAG = "Step1BasicInfo";
private ProgressDialog progressDialog;
```

#### b. Sửa Logic validateAndContinue()
```java
// Trước:
if (isValid) {
    // Lưu data và chuyển sang bước tiếp theo ngay
    registrationData.setPhoneNumber(phone);
    registrationData.setEmail(email);
    registrationData.setPassword(password);
    registrationData.setConfirmPassword(confirmPassword);
    
    ((MainRegistrationActivity) getActivity()).goToNextStep();
}

// Sau:
if (isValid) {
    // Kiểm tra số điện thoại đã tồn tại chưa
    checkPhoneExistsAndContinue(phone, email, password, confirmPassword);
}
```

#### c. Thêm Method checkPhoneExistsAndContinue()
```java
/**
 * Kiểm tra số điện thoại đã tồn tại chưa trước khi tiếp tục
 */
private void checkPhoneExistsAndContinue(String phone, String email, String password, String confirmPassword) {
    // Show loading
    progressDialog = new ProgressDialog(getContext());
    progressDialog.setMessage("Đang kiểm tra số điện thoại...");
    progressDialog.setCancelable(false);
    progressDialog.show();
    
    AuthApiService authApiService = ApiClient.getAuthApiService();
    Call<FeatureStatusResponse> call = authApiService.checkPhoneExists(phone);
    
    call.enqueue(new Callback<FeatureStatusResponse>() {
        @Override
        public void onResponse(Call<FeatureStatusResponse> call, Response<FeatureStatusResponse> response) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            
            if (response.isSuccessful() && response.body() != null) {
                FeatureStatusResponse result = response.body();
                
                // Nếu enabled = true → Số điện thoại đã tồn tại
                if (result.isEnabled()) {
                    tilPhone.setError("Số điện thoại này đã được đăng ký");
                    Toast.makeText(getContext(), "Số điện thoại đã tồn tại. Vui lòng sử dụng số khác.", 
                                 Toast.LENGTH_LONG).show();
                } else {
                    // Số điện thoại chưa tồn tại → OK, tiếp tục
                    saveDataAndContinue(phone, email, password, confirmPassword);
                }
            } else {
                // Lỗi từ server
                Log.e(TAG, "Check phone exists failed: " + response.code());
                
                // Nếu API trả về 404, có thể nghĩa là số điện thoại chưa tồn tại
                if (response.code() == 404) {
                    saveDataAndContinue(phone, email, password, confirmPassword);
                } else {
                    Toast.makeText(getContext(), "Không thể kiểm tra số điện thoại. Vui lòng thử lại.", 
                                 Toast.LENGTH_SHORT).show();
                }
            }
        }
        
        @Override
        public void onFailure(Call<FeatureStatusResponse> call, Throwable t) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            
            Log.e(TAG, "Check phone exists error", t);
            Toast.makeText(getContext(), "Lỗi kết nối. Vui lòng kiểm tra mạng và thử lại.", 
                         Toast.LENGTH_SHORT).show();
        }
    });
}
```

#### d. Thêm Method saveDataAndContinue()
```java
/**
 * Lưu dữ liệu và chuyển sang bước tiếp theo
 */
private void saveDataAndContinue(String phone, String email, String password, String confirmPassword) {
    // Ensure registrationData is not null
    if (registrationData == null) {
        if (getActivity() instanceof MainRegistrationActivity) {
            registrationData = ((MainRegistrationActivity) getActivity()).getRegistrationData();
        }
        if (registrationData == null) {
            registrationData = new RegistrationData();
        }
    }
    
    // Save data
    registrationData.setPhoneNumber(phone);
    registrationData.setEmail(email);
    registrationData.setPassword(password);
    registrationData.setConfirmPassword(confirmPassword);
    
    // Navigate to next step
    if (getActivity() instanceof MainRegistrationActivity) {
        ((MainRegistrationActivity) getActivity()).goToNextStep();
    }
}
```

#### e. Thêm Cleanup trong onDestroy()
```java
@Override
public void onDestroy() {
    super.onDestroy();
    
    // Dismiss progress dialog if showing
    if (progressDialog != null && progressDialog.isShowing()) {
        progressDialog.dismiss();
    }
}
```

## Flow Mới

### Trước Khi Sửa
```
Step 1: Nhập thông tin
  → Validate format
  → Lưu data
  → Chuyển sang Step 2 (OTP)
  → Gửi OTP
  → Nhập OTP
  → ...
  → Step cuối: Gọi API register
  → ❌ Lỗi: Số điện thoại đã tồn tại
```

### Sau Khi Sửa
```
Step 1: Nhập thông tin
  → Validate format
  → Call API check-phone-exists
  → Nếu đã tồn tại: ❌ Hiển thị lỗi ngay, không cho tiếp tục
  → Nếu chưa tồn tại: ✅ Lưu data và chuyển sang Step 2
  → Gửi OTP
  → ...
```

## API Backend

### Endpoint
```
GET /api/auth/check-phone-exists?phone={phone}
```

### Request
```
GET /api/auth/check-phone-exists?phone=0123456789
```

### Response - Số Điện Thoại Đã Tồn Tại
```json
{
  "enabled": true,
  "message": "Phone number already exists"
}
```

### Response - Số Điện Thoại Chưa Tồn Tại
```json
{
  "enabled": false,
  "message": "Phone number available"
}
```

### Response - Lỗi
```
404 Not Found (có thể nghĩa là chưa tồn tại)
500 Internal Server Error
```

## Test Cases

### Test Case 1: Số Điện Thoại Chưa Tồn Tại
**Các bước:**
1. Mở app → Click "Đăng ký"
2. Nhập số điện thoại chưa đăng ký: `0999999999`
3. Nhập email, password, confirm password
4. Click "Tiếp tục"

**Kết quả mong đợi:**
- ✅ Hiển thị loading "Đang kiểm tra số điện thoại..."
- ✅ API trả về `enabled: false`
- ✅ Chuyển sang Step 2 (OTP verification)

### Test Case 2: Số Điện Thoại Đã Tồn Tại
**Các bước:**
1. Mở app → Click "Đăng ký"
2. Nhập số điện thoại đã đăng ký: `0123456789`
3. Nhập email, password, confirm password
4. Click "Tiếp tục"

**Kết quả mong đợi:**
- ✅ Hiển thị loading "Đang kiểm tra số điện thoại..."
- ✅ API trả về `enabled: true`
- ✅ Hiển thị lỗi ở ô phone: "Số điện thoại này đã được đăng ký"
- ✅ Toast: "Số điện thoại đã tồn tại. Vui lòng sử dụng số khác."
- ✅ KHÔNG chuyển sang Step 2
- ✅ Người dùng có thể sửa số điện thoại và thử lại

### Test Case 3: Lỗi Mạng
**Các bước:**
1. Tắt wifi/data
2. Nhập thông tin và click "Tiếp tục"

**Kết quả mong đợi:**
- ✅ Hiển thị loading
- ✅ Toast: "Lỗi kết nối. Vui lòng kiểm tra mạng và thử lại."
- ✅ KHÔNG chuyển sang Step 2
- ✅ Có thể thử lại khi có mạng

### Test Case 4: Server Error
**Các bước:**
1. Backend trả về lỗi 500
2. Nhập thông tin và click "Tiếp tục"

**Kết quả mong đợi:**
- ✅ Hiển thị loading
- ✅ Toast: "Không thể kiểm tra số điện thoại. Vui lòng thử lại."
- ✅ KHÔNG chuyển sang Step 2

### Test Case 5: API Trả Về 404
**Các bước:**
1. Backend trả về 404 (có thể nghĩa là số điện thoại chưa tồn tại)
2. Nhập thông tin và click "Tiếp tục"

**Kết quả mong đợi:**
- ✅ Hiển thị loading
- ✅ Coi như số điện thoại chưa tồn tại
- ✅ Chuyển sang Step 2

## Lợi Ích

### 1. UX Tốt Hơn
- ✅ Phát hiện lỗi sớm, ngay ở bước đầu tiên
- ✅ Không lãng phí thời gian người dùng
- ✅ Không lãng phí OTP

### 2. Giảm Tải Server
- ✅ Không gửi OTP cho số điện thoại đã tồn tại
- ✅ Không xử lý các bước tiếp theo nếu số điện thoại không hợp lệ

### 3. Bảo Mật
- ✅ Ngăn chặn spam registration với số điện thoại đã tồn tại
- ✅ Giảm số lượng OTP gửi đi

## Lưu Ý

### 1. Privacy
API này có thể bị lợi dụng để kiểm tra xem một số điện thoại có đăng ký hay không. Backend nên:
- Rate limiting (giới hạn số lần gọi)
- CAPTCHA nếu cần
- Log để phát hiện abuse

### 2. Error Handling
- Nếu API lỗi, có thể cho phép tiếp tục (fallback)
- Hoặc yêu cầu thử lại (strict)
- Tùy theo yêu cầu bảo mật

### 3. Loading State
- Disable button "Tiếp tục" khi đang check
- Hiển thị progress dialog rõ ràng
- Cho phép cancel nếu cần

## Kết Luận
Đã thêm thành công kiểm tra số điện thoại đã tồn tại ở Step 1 của đăng ký. Người dùng sẽ biết ngay nếu số điện thoại đã được đăng ký, không cần phải đi qua tất cả các bước.
