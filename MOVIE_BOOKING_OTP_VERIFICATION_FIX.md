# Thêm Xác Thực OTP Cho Đặt Vé Xem Phim

## Mục Đích
Thêm bước xác thực OTP trước khi thanh toán vé xem phim để tăng cường bảo mật và xác nhận giao dịch.

**LƯU Ý QUAN TRỌNG:** OTP sẽ được gửi đến số điện thoại của tài khoản đang đăng nhập, KHÔNG PHẢI số điện thoại trong form nhập thông tin khách hàng.

## Flow Mới

### Trước Khi Sửa
```
MoviePaymentActivity
  → Click "Thanh toán"
  → Gọi API đặt vé ngay lập tức
  → MovieTicketSuccessActivity (nếu thành công)
```

### Sau Khi Sửa
```
MoviePaymentActivity
  → Click "Thanh toán"
  → Validate thông tin
  → OtpVerificationActivity (activity_otp_verification.xml)
     - Gửi OTP đến số điện thoại khách hàng
     - Nhập OTP
     - Nếu OTP đúng → Gọi API đặt vé
     - Nếu OTP sai → Yêu cầu nhập lại, KHÔNG đặt vé
  → MovieTicketSuccessActivity (nếu đặt vé thành công)
```

## Các Thay Đổi

### 1. MoviePaymentActivity.java

#### a. Thêm Method navigateToOtpVerification()
```java
/**
 * Navigate to OTP verification before booking
 * OTP sẽ được gửi đến số điện thoại của tài khoản đang đăng nhập
 */
private void navigateToOtpVerification() {
    // Lấy thông tin từ form
    String customerName = etCustomerName.getText().toString().trim();
    String customerPhone = etCustomerPhone.getText().toString().trim();
    String customerEmail = etCustomerEmail.getText().toString().trim();
    
    // Lấy số điện thoại của user đang đăng nhập
    DataManager dataManager = DataManager.getInstance(this);
    String userPhone = dataManager.getUserPhone();
    
    if (userPhone == null || userPhone.isEmpty()) {
        Toast.makeText(this, "Không tìm thấy số điện thoại tài khoản. Vui lòng đăng nhập lại.", 
                      Toast.LENGTH_LONG).show();
        return;
    }
    
    Intent intent = new Intent(this, OtpVerificationActivity.class);
    // OTP gửi đến số điện thoại của tài khoản đang đăng nhập
    intent.putExtra("phone", userPhone);
    intent.putExtra("from", "movie_booking");
    
    // Truyền thông tin booking để xử lý sau khi OTP thành công
    intent.putExtra("customer_name", customerName);
    intent.putExtra("customer_phone", customerPhone);  // SĐT khách hàng (có thể khác với SĐT tài khoản)
    intent.putExtra("customer_email", customerEmail);
    intent.putExtra("screening_id", screeningId);
    intent.putExtra("seat_ids", seatIds);
    
    // Truyền thêm thông tin để hiển thị trong success screen
    intent.putExtra("movie_title", getIntent().getStringExtra(EXTRA_MOVIE_TITLE));
    intent.putExtra("cinema_name", getIntent().getStringExtra(EXTRA_CINEMA_NAME));
    intent.putExtra("showtime", getIntent().getStringExtra(EXTRA_SHOWTIME));
    intent.putExtra("seats", getIntent().getStringExtra(EXTRA_SEATS));
    intent.putExtra("total_amount", totalAmount);
    
    startActivity(intent);
    finish(); // Finish activity này để không quay lại được
}
```

**Điểm Quan Trọng:**
- `userPhone = dataManager.getUserPhone()` - Lấy SĐT của tài khoản đang đăng nhập
- `intent.putExtra("phone", userPhone)` - OTP gửi đến SĐT tài khoản
- `intent.putExtra("customer_phone", customerPhone)` - SĐT khách hàng nhận vé (có thể khác)

#### b. Sửa Button Click Handler
```java
// Trước:
btnConfirmPayment.setOnClickListener(v -> {
    if (!isBalanceSufficient()) {
        showInsufficientBalanceWarning();
        return;
    }
    
    if (!validateInput()) {
        return;
    }
    
    // Call booking API ngay
    createBooking();
});

// Sau:
btnConfirmPayment.setOnClickListener(v -> {
    if (!isBalanceSufficient()) {
        showInsufficientBalanceWarning();
        return;
    }
    
    if (!validateInput()) {
        return;
    }
    
    // Chuyển sang OTP verification trước
    navigateToOtpVerification();
});
```

### 2. OtpVerificationActivity.java

**Không cần sửa gì!** Activity này đã có sẵn logic xử lý flow `movie_booking`:

```java
// Trong onCreate()
if ("movie_booking".equals(fromActivity)) {
    // Luồng đặt vé - gửi OTP với Goixe247
    sendOtpWithGoixe();
}

// Trong verifyOtpWithGoixe()
if (otpResponse.isSuccess()) {
    if ("movie_booking".equals(fromActivity)) {
        // Xác thực thành công → Gọi API đặt vé
        processMovieBooking();
    }
}

// Method processMovieBooking() đã có sẵn
private void processMovieBooking() {
    // Lấy thông tin từ Intent
    String customerName = getIntent().getStringExtra("customer_name");
    String customerPhone = getIntent().getStringExtra("customer_phone");
    String customerEmail = getIntent().getStringExtra("customer_email");
    Long screeningId = getIntent().getLongExtra("screening_id", -1);
    long[] seatIds = getIntent().getLongArrayExtra("seat_ids");
    
    // Build request và gọi API
    BookingRequest request = new BookingRequest(...);
    MovieApiService apiService = ApiClient.getMovieApiService();
    Call<BookingResponse> call = apiService.createBooking(request);
    
    // Handle response
    call.enqueue(new Callback<BookingResponse>() {
        @Override
        public void onResponse(...) {
            if (success) {
                navigateToMovieSuccessScreen(data);
            } else {
                // Show error
            }
        }
    });
}
```

## Dữ Liệu Truyền Qua Intent

### MoviePaymentActivity → OtpVerificationActivity
```java
intent.putExtra("phone", userPhone);                  // SĐT tài khoản (nhận OTP) ⭐
intent.putExtra("from", "movie_booking");             // Flow identifier
intent.putExtra("customer_name", customerName);       // Tên khách hàng
intent.putExtra("customer_phone", customerPhone);     // SĐT khách hàng (nhận vé, có thể khác SĐT tài khoản)
intent.putExtra("customer_email", customerEmail);     // Email khách hàng
intent.putExtra("screening_id", screeningId);         // ID suất chiếu
intent.putExtra("seat_ids", seatIds);                 // Mảng ID ghế
intent.putExtra("movie_title", movieTitle);           // Tên phim
intent.putExtra("cinema_name", cinemaName);           // Tên rạp
intent.putExtra("showtime", showtime);                // Giờ chiếu
intent.putExtra("seats", seats);                      // Số ghế (string)
intent.putExtra("total_amount", totalAmount);         // Tổng tiền
```

**⭐ Lưu Ý Quan Trọng:**
- `phone` = Số điện thoại tài khoản đang đăng nhập (nhận OTP)
- `customer_phone` = Số điện thoại khách hàng (nhận thông tin vé, có thể khác với SĐT tài khoản)

### OtpVerificationActivity → MovieTicketSuccessActivity
```java
intent.putExtra(EXTRA_BOOKING_CODE, bookingCode);
intent.putExtra(EXTRA_MOVIE_TITLE, movieTitle);
intent.putExtra(EXTRA_CINEMA_NAME, cinemaName);
intent.putExtra(EXTRA_CINEMA_ADDRESS, cinemaAddress);
intent.putExtra(EXTRA_HALL_NAME, hallName);
intent.putExtra(EXTRA_SCREENING_DATE, screeningDate);
intent.putExtra(EXTRA_START_TIME, startTime);
intent.putExtra(EXTRA_SEAT_COUNT, seatCount);
intent.putExtra(EXTRA_TOTAL_AMOUNT, totalAmount);
intent.putExtra(EXTRA_CUSTOMER_NAME, customerName);
intent.putExtra(EXTRA_QR_CODE, qrCode);
intent.putExtra(EXTRA_SEATS, seats);
```

## API Sử Dụng

### 1. Gửi OTP (Goixe247)
```
POST https://otp.goixe247.com/request_otp.php
Content-Type: application/x-www-form-urlencoded

user_id=13
api_key=328945bfca039d9663890e71f4d9e2203669dd1e49fd3cb9a44fa86a48d915da
recipient_phone=[user_account_phone]  ⭐ SĐT tài khoản đang đăng nhập
```

**⭐ Lưu Ý:** OTP được gửi đến số điện thoại của tài khoản đang đăng nhập, KHÔNG PHẢI số điện thoại trong form.

### 2. Xác Thực OTP (Goixe247)
```
POST https://otp.goixe247.com/verify_otp.php
Content-Type: application/x-www-form-urlencoded

user_id=13
api_key=328945bfca039d9663890e71f4d9e2203669dd1e49fd3cb9a44fa86a48d915da
recipient_phone=[user_account_phone]  ⭐ SĐT tài khoản đang đăng nhập
otp_code=[otp]
```

### 3. Đặt Vé (Backend)
```
POST [Backend]/api/movie/bookings
Content-Type: application/json
Authorization: Bearer [token]

{
  "screeningId": 123,
  "seatIds": [1, 2, 3],
  "customerName": "Nguyen Van A",
  "customerPhone": "0123456789",
  "customerEmail": "email@example.com"
}
```

## Test Cases

### 1. Test Flow Hoàn Chỉnh
1. Mở app → Chọn phim → Chọn suất chiếu → Chọn ghế
2. Màn hình MoviePaymentActivity hiển thị
3. Nhập thông tin:
   - Họ và tên: "Nguyen Van A"
   - Số điện thoại: "0123456789"
   - Email: "test@example.com"
4. Check checkbox "Tôi đồng ý..."
5. Click "Thanh toán"
6. Kiểm tra:
   - ✅ Chuyển sang OtpVerificationActivity
   - ✅ Hiển thị số điện thoại: "Đã gửi đến 0123456789"
   - ✅ Nhận SMS OTP

### 2. Test Nhập OTP Đúng
1. Nhập OTP từ SMS (6 số)
2. Click "Xác nhận"
3. Kiểm tra:
   - ✅ Loading indicator hiển thị
   - ✅ Thông báo "Xác thực OTP thành công!"
   - ✅ Gọi API đặt vé
   - ✅ Chuyển sang MovieTicketSuccessActivity
   - ✅ Hiển thị mã booking, QR code

### 3. Test Nhập OTP Sai
1. Nhập OTP sai (ví dụ: 111111)
2. Click "Xác nhận"
3. Kiểm tra:
   - ✅ Thông báo "Mã OTP không đúng. Vui lòng nhập lại."
   - ✅ Các ô OTP bị xóa
   - ✅ Focus vào ô đầu tiên
   - ✅ KHÔNG gọi API đặt vé
   - ✅ KHÔNG chuyển màn hình
   - ✅ Có thể nhập lại OTP

### 4. Test Gửi Lại OTP
1. Click "Gửi lại OTP"
2. Kiểm tra:
   - ✅ Loading indicator hiển thị
   - ✅ Các ô OTP bị xóa
   - ✅ Timer reset về 60s
   - ✅ Nhận SMS OTP mới
   - ✅ Có thể nhập OTP mới

### 5. Test Số Dư Không Đủ
1. Đảm bảo số dư tài khoản < tổng tiền vé
2. Nhập thông tin và click "Thanh toán"
3. Kiểm tra:
   - ✅ Hiển thị dialog "Số dư không đủ"
   - ✅ KHÔNG chuyển sang OTP verification
   - ✅ Gợi ý nạp thêm tiền

### 6. Test Ghế Đã Được Đặt
1. Nhập OTP đúng
2. Trong lúc xác thực, người khác đặt ghế đó
3. Kiểm tra:
   - ✅ API trả về lỗi 409
   - ✅ Thông báo "Ghế đã được đặt bởi người khác"
   - ✅ KHÔNG chuyển sang success screen

### 7. Test Lỗi Mạng
1. Tắt wifi/data
2. Nhập OTP và click "Xác nhận"
3. Kiểm tra:
   - ✅ Thông báo lỗi kết nối
   - ✅ Có thể thử lại khi có mạng

## Lợi Ích

### 1. Bảo Mật
- ✅ Xác thực số điện thoại khách hàng
- ✅ Ngăn chặn đặt vé giả mạo
- ✅ Xác nhận giao dịch trước khi thanh toán

### 2. Trải Nghiệm Người Dùng
- ✅ Rõ ràng: Người dùng biết họ đang xác thực giao dịch
- ✅ An toàn: Có thể hủy nếu không phải mình đặt
- ✅ Linh hoạt: Có thể gửi lại OTP nếu không nhận được

### 3. Giảm Rủi Ro
- ✅ Giảm booking spam
- ✅ Giảm tranh chấp thanh toán
- ✅ Có bằng chứng xác thực (OTP log)

## Lưu Ý Quan Trọng

### 1. Timeout OTP
- OTP có thời hạn (thường 5-10 phút)
- Nếu hết hạn, cần gửi lại OTP mới

### 2. Rate Limiting
- Giới hạn số lần gửi OTP (ví dụ: 3 lần/số điện thoại/ngày)
- Tránh spam OTP

### 3. Số Điện Thoại
- **OTP gửi đến:** Số điện thoại tài khoản đang đăng nhập
- **Thông tin vé gửi đến:** Số điện thoại trong form (có thể khác)
- Người dùng có thể đặt vé cho người khác (SĐT khác) nhưng phải xác thực bằng SĐT tài khoản của mình

### 4. Ghế Có Thể Bị Đặt
- Trong lúc xác thực OTP, ghế có thể bị người khác đặt
- Backend sẽ check và trả về lỗi 409
- Cần thông báo rõ ràng cho người dùng

### 5. Không Quay Lại
- Sau khi chuyển sang OTP verification, không thể quay lại MoviePaymentActivity
- Nếu muốn hủy, phải bấm Back từ OTP screen

## Debug

### Kiểm Tra Log
```bash
adb logcat | grep -E "OtpVerification|MoviePayment"
```

### Log Mẫu Thành Công
```
D/MoviePayment: Navigating to OTP verification
D/OtpVerification: Sending OTP to: 0123456789
D/OtpVerification: OTP Request Response Code: 200
D/OtpVerification: OTP Response: success=true
D/OtpVerification: Verifying OTP: 123456 for phone: 0123456789
D/OtpVerification: OTP Verify Response Code: 200
D/OtpVerification: OTP Verify Response: success=true
D/OtpVerification: Processing movie booking
D/OtpVerification: Booking successful, navigating to success screen
```

### Log Mẫu Khi OTP Sai
```
D/OtpVerification: Verifying OTP: 111111 for phone: 0123456789
D/OtpVerification: OTP Verify Response Code: 200
D/OtpVerification: OTP Verify Response: success=false, message=Invalid OTP code
```

## Kết Luận
Đã thêm thành công bước xác thực OTP vào flow đặt vé xem phim. Người dùng giờ phải xác thực OTP trước khi thanh toán, tăng cường bảo mật và xác nhận giao dịch.
