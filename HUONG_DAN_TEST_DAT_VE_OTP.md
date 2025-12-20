# Hướng Dẫn Test Đặt Vé Xem Phim Với OTP

## Chuẩn Bị
1. Đảm bảo backend đang chạy
2. Đảm bảo app đã được build và cài đặt trên thiết bị
3. **Có tài khoản đã đăng nhập với số dư đủ**
4. **Số điện thoại tài khoản phải là số thật để nhận SMS OTP**

## ⭐ LƯU Ý QUAN TRỌNG
**OTP sẽ được gửi đến số điện thoại của tài khoản đang đăng nhập, KHÔNG PHẢI số điện thoại trong form nhập thông tin khách hàng.**

Ví dụ:
- Tài khoản đăng nhập: 0987654321 → **OTP gửi đến số này**
- Số điện thoại trong form: 0123456789 → Chỉ dùng để nhận thông tin vé

## Flow Đặt Vé Mới (Có OTP)

```
Chọn phim → Chọn suất chiếu → Chọn ghế 
  → Trang thanh toán (nhập thông tin)
  → Xác thực OTP
  → Đặt vé thành công
```

## Các Bước Test Chi Tiết

### Bước 1: Chọn Phim và Ghế
1. Mở app và đăng nhập
2. Vào mục "Đặt vé xem phim"
3. Chọn một bộ phim
4. Chọn rạp chiếu
5. Chọn suất chiếu
6. Chọn ghế (ví dụ: H12)
7. Click "Tiếp tục"

**Kiểm tra:**
- ✅ Chuyển sang màn hình thanh toán
- ✅ Hiển thị đầy đủ thông tin: phim, rạp, ghế, giá

### Bước 2: Nhập Thông Tin Khách Hàng
1. Màn hình "Thanh toán vé xem phim" hiển thị
2. Nhập thông tin:
   - **Họ và Tên:** Nguyen Van A
   - **Số điện thoại:** 0123456789 (có thể khác với SĐT tài khoản)
   - **Email:** test@example.com (tùy chọn)
3. Kiểm tra thông tin phim, ghế, giá tiền
4. Check vào checkbox "Tôi đồng ý với điều khoản..."

**⭐ Lưu Ý:**
- Số điện thoại ở đây là SĐT nhận thông tin vé, KHÔNG PHẢI SĐT nhận OTP
- OTP sẽ được gửi đến SĐT tài khoản đang đăng nhập

**Kiểm tra:**
- ✅ Nút "Thanh toán" được enable khi check checkbox
- ✅ Tổng tiền hiển thị đúng
- ✅ Thông tin phim, ghế hiển thị đúng

### Bước 3: Click Thanh Toán
1. Click nút "Thanh toán"
2. Chờ một chút

**Kiểm tra:**
- ✅ Chuyển sang màn hình "Xác Thực OTP"
- ✅ Hiển thị: "Đã gửi đến [SĐT tài khoản]" (VD: 0987654321)
- ✅ Có 6 ô nhập OTP
- ✅ Có nút "Xác nhận" và "Gửi lại OTP"
- ✅ Có timer đếm ngược (60 giây)
- ✅ **Nhận được SMS OTP trên điện thoại tài khoản (KHÔNG PHẢI SĐT trong form)**

**⭐ Quan Trọng:**
- Kiểm tra SMS OTP được gửi đến đúng số điện thoại tài khoản đang đăng nhập
- Nếu SĐT trong form khác với SĐT tài khoản, SMS OTP vẫn gửi đến SĐT tài khoản

### Bước 4: Nhập OTP Đúng
1. Kiểm tra SMS để lấy mã OTP (6 số)
2. Nhập từng số vào 6 ô
3. Click "Xác nhận"

**Kiểm tra:**
- ✅ Loading indicator hiển thị
- ✅ Thông báo "Xác thực OTP thành công!"
- ✅ Loading "Đang xử lý đặt vé..."
- ✅ Chuyển sang màn hình "Đặt vé thành công"
- ✅ Hiển thị:
  - Mã booking
  - QR code
  - Thông tin phim
  - Thông tin ghế
  - Thông tin rạp
  - Tổng tiền đã thanh toán

### Bước 5: Kiểm Tra Vé
1. Ở màn hình thành công, kiểm tra thông tin
2. Chụp màn hình hoặc lưu QR code
3. Click "Về trang chủ"

**Kiểm tra:**
- ✅ Tất cả thông tin hiển thị đúng
- ✅ QR code hiển thị rõ ràng
- ✅ Có thể quay về trang chủ

## Test Cases Đặc Biệt

### Test Case 1: Nhập OTP Sai
**Mục đích:** Kiểm tra hệ thống không cho đặt vé khi OTP sai

**Các bước:**
1. Làm theo Bước 1-3 ở trên
2. Ở màn hình OTP, nhập OTP sai (ví dụ: 111111)
3. Click "Xác nhận"

**Kết quả mong đợi:**
- ✅ Thông báo "Mã OTP không đúng. Vui lòng nhập lại."
- ✅ Tất cả 6 ô OTP bị xóa
- ✅ Focus vào ô đầu tiên
- ✅ KHÔNG gọi API đặt vé
- ✅ KHÔNG chuyển sang màn hình thành công
- ✅ Vẫn ở màn hình OTP, có thể nhập lại

**Tiếp tục test:**
4. Nhập OTP đúng từ SMS
5. Click "Xác nhận"
6. Kiểm tra đặt vé thành công

### Test Case 2: Gửi Lại OTP
**Mục đích:** Kiểm tra chức năng gửi lại OTP

**Các bước:**
1. Làm theo Bước 1-3 ở trên
2. Ở màn hình OTP, đợi 60 giây (hoặc nhập sai OTP)
3. Click "Gửi lại OTP"

**Kết quả mong đợi:**
- ✅ Loading indicator hiển thị
- ✅ Thông báo "Mã OTP đã được gửi đến [số điện thoại]"
- ✅ Tất cả 6 ô OTP bị xóa
- ✅ Timer reset về 60 giây
- ✅ Focus vào ô đầu tiên
- ✅ Nhận được SMS OTP mới

**Tiếp tục test:**
4. Nhập OTP mới từ SMS
5. Click "Xác nhận"
6. Kiểm tra đặt vé thành công

### Test Case 3: Số Dư Không Đủ
**Mục đích:** Kiểm tra hệ thống không cho đặt vé khi số dư không đủ

**Chuẩn bị:**
- Đảm bảo số dư tài khoản < giá vé

**Các bước:**
1. Làm theo Bước 1-2 ở trên
2. Check checkbox
3. Click "Thanh toán"

**Kết quả mong đợi:**
- ✅ Hiển thị dialog "Số dư không đủ"
- ✅ Thông báo: "Số dư tài khoản của bạn ([số dư]) không đủ để thanh toán [giá vé]"
- ✅ Gợi ý nạp thêm tiền
- ✅ KHÔNG chuyển sang màn hình OTP
- ✅ Click "Đóng" sẽ quay lại trang trước

### Test Case 4: Ghế Đã Được Đặt
**Mục đích:** Kiểm tra xử lý khi ghế bị người khác đặt trong lúc xác thực OTP

**Chuẩn bị:**
- Cần 2 thiết bị hoặc 2 tài khoản

**Các bước:**
1. Thiết bị A: Chọn phim, ghế H12, đến màn hình OTP
2. Thiết bị B: Nhanh chóng chọn cùng phim, cùng suất, ghế H12
3. Thiết bị B: Nhập OTP và đặt vé thành công
4. Thiết bị A: Nhập OTP và click "Xác nhận"

**Kết quả mong đợi (Thiết bị A):**
- ✅ Thông báo "Ghế đã được đặt bởi người khác. Vui lòng chọn ghế khác."
- ✅ KHÔNG chuyển sang màn hình thành công
- ✅ Có thể quay lại chọn ghế khác

### Test Case 5: Lỗi Mạng
**Mục đích:** Kiểm tra xử lý khi mất kết nối

**Các bước:**
1. Làm theo Bước 1-3 ở trên
2. Tắt wifi/data trên điện thoại
3. Nhập OTP và click "Xác nhận"

**Kết quả mong đợi:**
- ✅ Thông báo "Lỗi kết nối: [chi tiết lỗi]"
- ✅ KHÔNG chuyển màn hình
- ✅ Có thể thử lại khi bật lại mạng

**Tiếp tục test:**
4. Bật lại wifi/data
5. Click "Gửi lại OTP"
6. Nhập OTP mới và xác nhận
7. Kiểm tra đặt vé thành công

### Test Case 6: OTP Hết Hạn
**Mục đích:** Kiểm tra xử lý khi OTP hết hạn

**Các bước:**
1. Làm theo Bước 1-3 ở trên
2. Đợi 10 phút (OTP thường hết hạn sau 5-10 phút)
3. Nhập OTP cũ và click "Xác nhận"

**Kết quả mong đợi:**
- ✅ Thông báo "Mã OTP đã hết hạn" hoặc "Mã OTP không đúng"
- ✅ Có thể click "Gửi lại OTP"
- ✅ Nhận OTP mới và đặt vé thành công

### Test Case 7: Nhập Thông Tin Không Hợp Lệ
**Mục đích:** Kiểm tra validation

**Các bước:**
1. Làm theo Bước 1 ở trên
2. Ở màn hình thanh toán:
   - Để trống "Họ và Tên"
   - Click "Thanh toán"

**Kết quả mong đợi:**
- ✅ Hiển thị lỗi "Vui lòng nhập họ và tên"
- ✅ Focus vào trường "Họ và Tên"
- ✅ KHÔNG chuyển màn hình

**Tiếp tục test:**
3. Nhập tên, để trống "Số điện thoại"
4. Click "Thanh toán"
5. Kiểm tra: ✅ Hiển thị lỗi "Vui lòng nhập số điện thoại"

6. Nhập số điện thoại ngắn (ví dụ: 123)
7. Click "Thanh toán"
8. Kiểm tra: ✅ Hiển thị lỗi "Số điện thoại không hợp lệ"

**⭐ Lưu Ý:** Validation này chỉ kiểm tra SĐT khách hàng trong form, không ảnh hưởng đến việc gửi OTP (OTP vẫn gửi đến SĐT tài khoản)

### Test Case 8: Auto-Focus Giữa Các Ô OTP
**Mục đích:** Kiểm tra UX khi nhập OTP

**Các bước:**
1. Làm theo Bước 1-3 ở trên
2. Ở màn hình OTP, nhập số vào ô đầu tiên

**Kết quả mong đợi:**
- ✅ Tự động chuyển sang ô thứ 2
- ✅ Tiếp tục nhập, tự động chuyển sang ô thứ 3, 4, 5, 6
- ✅ Mỗi ô chỉ chấp nhận 1 số
- ✅ Không thể nhập chữ cái

### Test Case 9: Đặt Vé Cho Người Khác (SĐT Khác)
**Mục đích:** Kiểm tra tính năng đặt vé cho người khác

**Chuẩn bị:**
- Tài khoản đăng nhập: 0987654321
- Muốn đặt vé cho người khác: 0123456789

**Các bước:**
1. Đăng nhập với tài khoản 0987654321
2. Chọn phim, suất chiếu, ghế
3. Ở màn hình thanh toán, nhập:
   - Họ và Tên: Nguyen Van B (tên người nhận vé)
   - Số điện thoại: 0123456789 (SĐT người nhận vé)
   - Email: person_b@example.com
4. Check checkbox và click "Thanh toán"

**Kết quả mong đợi:**
- ✅ Chuyển sang màn hình OTP
- ✅ Hiển thị: "Đã gửi đến 0987654321" (SĐT tài khoản)
- ✅ **SMS OTP gửi đến 0987654321** (SĐT tài khoản, KHÔNG PHẢI 0123456789)
- ✅ Nhập OTP từ điện thoại 0987654321
- ✅ Đặt vé thành công
- ✅ Thông tin vé hiển thị SĐT 0123456789 (người nhận vé)

**⭐ Điểm Quan Trọng:**
- Xác thực OTP bằng SĐT tài khoản (0987654321)
- Thông tin vé gửi đến SĐT khách hàng (0123456789)
- Người dùng có thể đặt vé cho người khác nhưng phải xác thực bằng SĐT của mình

## Kiểm Tra Logcat

### Xem Log
```bash
adb logcat | grep -E "OtpVerification|MoviePayment"
```

### Log Mẫu Khi Thành Công
```
D/MoviePayment: User phone: 0987654321
D/MoviePayment: Navigating to OTP verification
D/OtpVerification: Sending OTP to: 0987654321  ⭐ SĐT tài khoản
D/OtpVerification: OTP Request Response Code: 200
D/OtpVerification: OTP Response: success=true, message=OTP sent successfully
D/OtpVerification: Verifying OTP: 123456 for phone: 0987654321
D/OtpVerification: OTP Verify Response Code: 200
D/OtpVerification: OTP Verify Response: success=true, message=OTP verified
D/OtpVerification: Processing movie booking
D/OtpVerification: Customer phone: 0123456789  ⭐ SĐT khách hàng (có thể khác)
D/OtpVerification: Booking successful
```

### Log Mẫu Khi OTP Sai
```
D/OtpVerification: Verifying OTP: 111111 for phone: 0987654321
D/OtpVerification: OTP Verify Response Code: 200
D/OtpVerification: OTP Verify Response: success=false, message=Invalid OTP code
```

### Log Mẫu Khi Đặt Vé Cho Người Khác
```
D/MoviePayment: User phone: 0987654321  ⭐ SĐT tài khoản
D/MoviePayment: Customer phone: 0123456789  ⭐ SĐT khách hàng (khác)
D/MoviePayment: Navigating to OTP verification
D/OtpVerification: Sending OTP to: 0987654321  ⭐ OTP gửi đến SĐT tài khoản
D/OtpVerification: Customer phone for booking: 0123456789  ⭐ Vé gửi đến SĐT khách hàng
```

## Checklist Tổng Hợp

### Chức Năng Cơ Bản
- [ ] Chọn phim, suất chiếu, ghế thành công
- [ ] Nhập thông tin khách hàng thành công
- [ ] Chuyển sang màn hình OTP khi click "Thanh toán"
- [ ] Nhận được SMS OTP
- [ ] Nhập OTP đúng → Đặt vé thành công
- [ ] Hiển thị màn hình thành công với đầy đủ thông tin

### Xử Lý Lỗi
- [ ] Nhập OTP sai → Không cho đặt vé, yêu cầu nhập lại
- [ ] Gửi lại OTP hoạt động đúng
- [ ] Số dư không đủ → Không cho đặt vé
- [ ] Ghế đã được đặt → Thông báo lỗi rõ ràng
- [ ] Lỗi mạng → Thông báo và có thể thử lại
- [ ] OTP hết hạn → Có thể gửi lại OTP mới

### UX/UI
- [ ] Auto-focus giữa các ô OTP
- [ ] Timer đếm ngược hoạt động đúng
- [ ] Loading indicator hiển thị khi cần
- [ ] Thông báo lỗi rõ ràng, dễ hiểu
- [ ] Có thể quay lại màn hình trước
- [ ] Không bị crash trong mọi trường hợp

## Lưu Ý Quan Trọng

1. **⭐ OTP gửi đến SĐT tài khoản:** OTP được gửi đến số điện thoại của tài khoản đang đăng nhập, KHÔNG PHẢI số điện thoại trong form
2. **SĐT khách hàng khác SĐT tài khoản:** Người dùng có thể đặt vé cho người khác (nhập SĐT khác trong form) nhưng phải xác thực bằng SĐT tài khoản của mình
3. **OTP có thời hạn** (thường 5-10 phút)
4. **Ghế có thể bị đặt** trong lúc xác thực OTP
5. **Số dư phải đủ** để thanh toán
6. **Không thể quay lại** sau khi chuyển sang OTP verification

## Kết Quả Mong Đợi

Sau khi test tất cả các case trên, hệ thống phải:
- ✅ Bảo mật: Chỉ đặt vé khi OTP đúng
- ✅ Ổn định: Không crash trong mọi trường hợp
- ✅ Rõ ràng: Thông báo lỗi dễ hiểu
- ✅ Linh hoạt: Có thể gửi lại OTP, thử lại khi lỗi
- ✅ Nhanh chóng: Xử lý trong vài giây
