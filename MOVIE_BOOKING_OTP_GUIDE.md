# Hướng Dẫn Xác Thực OTP Cho Đặt Vé Xem Phim

## Tổng Quan
Đã thêm xác thực OTP cho luồng đặt vé xem phim. Người dùng phải nhập đúng mã OTP được gửi qua số điện thoại trước khi thanh toán và đặt vé thành công.

## Luồng Hoạt Động

### Luồng Cũ (Trước khi cập nhật):
```
MovieListActivity → MovieDetailActivity → SelectShowtimeActivity 
→ SeatSelectionActivity → MoviePaymentActivity
→ Nhập thông tin → Nhấn "Đặt vé"
→ Gọi API đặt vé ngay lập tức
→ MovieTicketSuccessActivity
```

### Luồng Mới (Sau khi cập nhật):
```
MovieListActivity → MovieDetailActivity → SelectShowtimeActivity 
→ SeatSelectionActivity → MoviePaymentActivity
→ Nhập thông tin → Nhấn "Đặt vé"
→ OtpVerificationActivity (from="movie_booking")
  - Gửi OTP qua Goixe247 API
  - User nhập 6 số OTP
  - Xác thực OTP với Goixe247 API
  - Nếu đúng: Gọi API đặt vé
  - Nếu sai: Yêu cầu nhập lại
→ MovieTicketSuccessActivity
```

## Các File Đã Thay Đổi

### 1. MoviePaymentActivity.java

#### Thay đổi chính:
- **Đổi `createBooking()` từ `private` thành `public`**: Để có thể gọi từ OtpVerificationActivity
- **Thêm `navigateToOtpVerification()`**: Chuyển sang màn hình OTP thay vì gọi API ngay
- **Cập nhật `btnConfirmPayment.setOnClickListener()`**: Gọi `navigateToOtpVerification()` thay vì `createBooking()`

#### Code quan trọng:
```java
// Khi nhấn nút "Đặt vé"
btnConfirmPayment.setOnClickListener(v -> {
    // Check balance
    if (!isBalanceSufficient()) {
        showInsufficientBalanceWarning();
        return;
    }
    
    // Validate input
    if (!validateInput()) {
        return;
    }
    
    // Chuyển sang màn hình OTP thay vì đặt vé ngay
    navigateToOtpVerification();
});

// Chuyển sang OTP với tất cả thông tin cần thiết
private void navigateToOtpVerification() {
    String customerPhone = etCustomerPhone.getText().toString().trim();
    
    Intent intent = new Intent(this, OtpVerificationActivity.class);
    intent.putExtra("phone", customerPhone);
    intent.putExtra("from", "movie_booking");
    
    // Truyền thông tin booking
    intent.putExtra("customer_name", etCustomerName.getText().toString().trim());
    intent.putExtra("customer_phone", customerPhone);
    intent.putExtra("customer_email", etCustomerEmail.getText().toString().trim());
    intent.putExtra("screening_id", screeningId);
    intent.putExtra("seat_ids", seatIds);
    
    // Truyền thông tin hiển thị
    intent.putExtra(EXTRA_MOVIE_TITLE, getIntent().getStringExtra(EXTRA_MOVIE_TITLE));
    // ... các extra khác
    
    startActivity(intent);
    finish();
}

// Method này giờ là public để OtpVerificationActivity có thể gọi
public void createBooking() {
    // ... logic đặt vé như cũ
}
```

### 2. OtpVerificationActivity.java

#### Thêm hỗ trợ luồng movie_booking:

**1. Trong `onCreate()`:**
```java
if ("forgot_password".equals(fromActivity)) {
    // Đã gửi OTP từ ForgotPasswordActivity
    Toast.makeText(this, "Mã OTP đã được gửi đến " + phoneNumber, Toast.LENGTH_SHORT).show();
} else if ("movie_booking".equals(fromActivity)) {
    // Luồng đặt vé - gửi OTP với Goixe247
    sendOtpWithGoixe();
} else {
    // Luồng đăng ký - dùng eSMS
    if (esmsConfig.isConfigured()) {
        sendOtp();
    } else {
        showApiKeyConfigDialog();
    }
}
```

**2. Thêm `sendOtpWithGoixe()`:**
```java
private void sendOtpWithGoixe() {
    if (progressBar != null) {
        progressBar.setVisibility(android.view.View.VISIBLE);
    }
    btnResend.setEnabled(false);
    
    Call<OtpResponse> call = otpApiService.requestOtp(
            GOIXE_USER_ID,
            GOIXE_API_KEY,
            phoneNumber
    );
    
    call.enqueue(new Callback<OtpResponse>() {
        @Override
        public void onResponse(Call<OtpResponse> call, Response<OtpResponse> response) {
            // Xử lý response
            if (response.isSuccessful() && response.body() != null) {
                OtpResponse otpResponse = response.body();
                if (otpResponse.isSuccess()) {
                    Toast.makeText(OtpVerificationActivity.this, 
                            "Mã OTP đã được gửi đến " + phoneNumber, Toast.LENGTH_SHORT).show();
                }
            }
        }
        
        @Override
        public void onFailure(Call<OtpResponse> call, Throwable t) {
            Toast.makeText(OtpVerificationActivity.this, 
                    "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
        }
    });
}
```

**3. Cập nhật `verifyOtpWithGoixe()`:**
```java
if (otpResponse.isSuccess()) {
    Toast.makeText(OtpVerificationActivity.this, 
            "Xác thực OTP thành công!", Toast.LENGTH_SHORT).show();
    
    if ("forgot_password".equals(fromActivity)) {
        // Chuyển sang màn hình đặt lại mật khẩu
        Intent intent = new Intent(OtpVerificationActivity.this, ResetPasswordActivity.class);
        intent.putExtra("phone", phoneNumber);
        startActivity(intent);
        finish();
    } else if ("movie_booking".equals(fromActivity)) {
        // Xác thực thành công → Gọi API đặt vé
        processMovieBooking();
    }
}
```

**4. Thêm `processMovieBooking()`:**
```java
private void processMovieBooking() {
    // Show loading
    if (progressBar != null) {
        progressBar.setVisibility(android.view.View.VISIBLE);
    }
    btnVerify.setEnabled(false);
    
    // Lấy thông tin booking từ Intent
    String customerName = getIntent().getStringExtra("customer_name");
    String customerPhone = getIntent().getStringExtra("customer_phone");
    String customerEmail = getIntent().getStringExtra("customer_email");
    Long screeningId = getIntent().getLongExtra("screening_id", -1);
    long[] seatIds = getIntent().getLongArrayExtra("seat_ids");
    
    // Convert seatIds to List
    List<Long> seatIdList = new ArrayList<>();
    for (long id : seatIds) {
        seatIdList.add(id);
    }
    
    // Build request
    BookingRequest request = new BookingRequest(
            screeningId,
            seatIdList,
            customerName,
            customerPhone,
            customerEmail
    );
    
    // Call API
    MovieApiService apiService = ApiClient.getMovieApiService();
    Call<BookingResponse> call = apiService.createBooking(request);
    
    call.enqueue(new Callback<BookingResponse>() {
        @Override
        public void onResponse(Call<BookingResponse> call, Response<BookingResponse> response) {
            if (response.isSuccessful() && response.body() != null) {
                BookingResponse bookingResponse = response.body();
                
                if (bookingResponse.getSuccess() != null && bookingResponse.getSuccess()) {
                    // Success - navigate to success screen
                    navigateToMovieSuccessScreen(bookingResponse.getData());
                } else {
                    // Error
                    String errorMsg = bookingResponse.getMessage() != null 
                            ? bookingResponse.getMessage() 
                            : "Đặt vé thất bại. Vui lòng thử lại.";
                    Toast.makeText(OtpVerificationActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }
        }
        
        @Override
        public void onFailure(Call<BookingResponse> call, Throwable t) {
            Toast.makeText(OtpVerificationActivity.this, 
                    "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
        }
    });
}
```

**5. Thêm `navigateToMovieSuccessScreen()`:**
```java
private void navigateToMovieSuccessScreen(BookingResponse.BookingData data) {
    Intent intent = new Intent(this, MovieTicketSuccessActivity.class);
    
    if (data != null) {
        intent.putExtra(MovieTicketSuccessActivity.EXTRA_BOOKING_CODE, data.getBookingCode());
        intent.putExtra(MovieTicketSuccessActivity.EXTRA_MOVIE_TITLE, data.getMovieTitle());
        // ... các extra khác
    }
    
    startActivity(intent);
    finish();
}
```

**6. Cập nhật `resendOtp()`:**
```java
private void resendOtp() {
    if ("forgot_password".equals(fromActivity) || "movie_booking".equals(fromActivity)) {
        resendOtpWithGoixe();
    } else {
        if (esmsConfig.isConfigured()) {
            sendOtp();
        }
    }
    startTimer();
}
```

**7. Cập nhật `handleOtpVerification()`:**
```java
private void handleOtpVerification() {
    String otp = collectOtp();
    
    if ("forgot_password".equals(fromActivity) || "movie_booking".equals(fromActivity)) {
        verifyOtpWithGoixe(otp);
    } else {
        verifyOtpWithESms(otp);
    }
}
```

## So Sánh 3 Luồng

| Tính năng | Đăng ký | Quên mật khẩu | Đặt vé xem phim |
|-----------|---------|---------------|-----------------|
| from | register | forgot_password | movie_booking |
| API gửi OTP | eSMS | Goixe247 | Goixe247 |
| Gửi OTP | Tự động khi vào màn hình | Đã gửi từ ForgotPasswordActivity | Tự động khi vào màn hình |
| Xác thực OTP | eSMS hoặc fake "123456" | Goixe247 API | Goixe247 API |
| Gửi lại OTP | eSMS | Goixe247 API | Goixe247 API |
| Sau khi thành công | LoginActivity | ResetPasswordActivity | Gọi API đặt vé → MovieTicketSuccessActivity |

## Test

### Test Case 1: Đặt vé - Gửi OTP
1. Mở app → Chọn phim → Chọn suất chiếu → Chọn ghế
2. Nhập thông tin:
   - Họ và tên: `Nguyễn Văn A`
   - Số điện thoại: `0901234567`
   - Email: `test@example.com`
3. Check "Tôi đồng ý với điều khoản"
4. Nhấn "Đặt vé"
5. **Kết quả mong đợi:**
   - Chuyển đến OtpVerificationActivity
   - Gọi Goixe247 API gửi OTP
   - Hiển thị "Mã OTP đã được gửi đến 0901234567"

### Test Case 2: Đặt vé - Xác thực OTP đúng
1. Tiếp tục từ Test Case 1
2. Nhập mã OTP nhận được từ SMS
3. **Kết quả mong đợi:**
   - Gọi Goixe247 verify API
   - Xác thực thành công
   - Gọi API đặt vé
   - Chuyển đến MovieTicketSuccessActivity
   - Hiển thị thông tin vé

### Test Case 3: Đặt vé - Xác thực OTP sai
1. Tiếp tục từ Test Case 1
2. Nhập mã OTP sai (ví dụ: `111111`)
3. **Kết quả mong đợi:**
   - Gọi Goixe247 verify API
   - Xác thực thất bại
   - Hiển thị "Mã OTP không đúng. Vui lòng nhập lại."
   - Xóa tất cả các ô OTP
   - Focus vào ô đầu tiên

### Test Case 4: Đặt vé - Gửi lại OTP
1. Tiếp tục từ Test Case 1
2. Đợi 60 giây
3. Nhấn "Gửi lại OTP"
4. **Kết quả mong đợi:**
   - Gọi Goixe247 API lại
   - Hiển thị "Đã gửi lại mã OTP thành công!"
   - Đếm ngược 60 giây lại

### Test Case 5: Đặt vé - Số dư không đủ
1. Mở app → Chọn phim → Chọn suất chiếu → Chọn ghế
2. Nhập thông tin
3. Nhấn "Đặt vé"
4. Nhập OTP đúng
5. **Kết quả mong đợi:**
   - Gọi API đặt vé
   - Backend trả về lỗi 402 (Insufficient funds)
   - Hiển thị "Số dư tài khoản không đủ để thanh toán."

### Test Case 6: Đặt vé - Ghế đã được đặt
1. Mở app → Chọn phim → Chọn suất chiếu → Chọn ghế
2. Nhập thông tin
3. Nhấn "Đặt vé"
4. Nhập OTP đúng
5. **Kết quả mong đợi:**
   - Gọi API đặt vé
   - Backend trả về lỗi 409 (Conflict)
   - Hiển thị "Ghế đã được đặt bởi người khác. Vui lòng chọn ghế khác."

## Lưu Ý

### 1. Bảo mật
- OTP được gửi qua SMS, không lưu trong app
- Xác thực OTP qua Goixe247 API
- Chỉ khi OTP đúng mới gọi API đặt vé

### 2. UX
- User phải nhập OTP trước khi thanh toán
- Nếu OTP sai, có thể nhập lại hoặc gửi lại OTP
- Nếu đặt vé thất bại (số dư không đủ, ghế đã đặt), hiển thị lỗi rõ ràng

### 3. Timeout
- OTP có thời gian hết hạn (thường 5-10 phút)
- Nếu hết hạn, user phải gửi lại OTP

### 4. Rate limiting
- Nên giới hạn số lần gửi OTP (ví dụ: 3 lần/ngày)
- Nên giới hạn số lần nhập sai OTP (ví dụ: 5 lần)

## Troubleshooting

### Vấn đề: Không nhận được OTP
- Kiểm tra số điện thoại có đúng format không
- Kiểm tra API key Goixe247 có đúng không
- Kiểm tra kết nối internet
- Xem log trong Logcat

### Vấn đề: OTP luôn báo sai
- Kiểm tra OTP có đúng 6 số không
- Kiểm tra OTP có hết hạn không
- Kiểm tra API verify có hoạt động không
- Xem response từ Goixe247 API

### Vấn đề: Đặt vé thất bại sau khi OTP đúng
- Kiểm tra số dư tài khoản
- Kiểm tra ghế có còn trống không
- Kiểm tra API đặt vé có hoạt động không
- Xem response từ backend

### Vấn đề: Không chuyển được sang MovieTicketSuccessActivity
- Kiểm tra MovieTicketSuccessActivity có tồn tại không
- Kiểm tra AndroidManifest.xml đã khai báo chưa
- Kiểm tra intent có đúng không
- Kiểm tra data từ API có đầy đủ không

## Kết Luận

Đã thêm thành công xác thực OTP cho luồng đặt vé xem phim:
- ✅ Gửi OTP qua Goixe247 API khi nhấn "Đặt vé"
- ✅ Xác thực OTP trước khi gọi API đặt vé
- ✅ Chỉ đặt vé khi OTP đúng
- ✅ Hiển thị lỗi rõ ràng khi OTP sai hoặc đặt vé thất bại
- ✅ Tái sử dụng OtpVerificationActivity cho nhiều luồng
- ✅ Dễ bảo trì và mở rộng

Luồng đặt vé giờ đây an toàn hơn với xác thực OTP 2 lớp!
