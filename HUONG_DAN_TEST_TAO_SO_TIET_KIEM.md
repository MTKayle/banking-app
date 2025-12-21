# Hướng dẫn Test Tạo Sổ Tiết Kiệm

## Chuẩn bị
1. Đảm bảo backend đang chạy tại `http://localhost:8089`
2. Đã đăng nhập vào app
3. Tài khoản có đủ số dư

## Bước test

### Test 1: Tạo sổ tiết kiệm < 10 triệu (chỉ OTP)

1. **Vào trang Tài khoản**
   - Từ trang chủ → Click "Tài khoản" ở bottom navigation
   - Hoặc click nút "Tiết kiệm" ở trang chủ

2. **Chọn tab Tiết kiệm**
   - Click tab "Tiết kiệm"
   - Click nút "Mở tài khoản"

3. **Chọn kỳ hạn**
   - Danh sách kỳ hạn được load từ API
   - Chọn một kỳ hạn (ví dụ: 12 tháng - 5.5%)

4. **Nhập số tiền**
   - Nhập: `5000000`
   - Tự động format thành: `5.000.000`
   - Hiển thị chữ: "Năm triệu đồng"
   - Click "Tiếp tục"

5. **Xác nhận thông tin**
   - Kiểm tra thông tin hiển thị:
     - Tài khoản nguồn
     - Số tiền: 5.000.000 VND
     - Kỳ hạn: 12 Tháng
     - Lãi suất: 5.5%/năm
     - Lãi dự kiến
   - Click "Xác nhận"

6. **Xác thực OTP** (bỏ qua Face vì < 10 triệu)
   - Nhập mã OTP nhận được
   - Click "Xác nhận"

7. **Kiểm tra kết quả**
   - Màn hình thành công hiển thị:
     - ✅ Giao dịch thành công
     - Số tiền: 5.000.000 VND
     - Số sổ tiết kiệm (từ API)
     - Kỳ hạn: 12 Tháng
     - Lãi suất: 5.5%/năm
     - Mã tham chiếu (savingId)
   - Click "Hoàn tất" → Về trang chủ

### Test 2: Tạo sổ tiết kiệm >= 10 triệu (Face + OTP)

1. **Vào trang Tài khoản → Tiết kiệm**
   - Click "Mở tài khoản"

2. **Chọn kỳ hạn**
   - Chọn kỳ hạn 12 tháng

3. **Nhập số tiền**
   - Nhập: `10000000`
   - Format: `10.000.000`
   - Chữ: "Mười triệu đồng"
   - Click "Tiếp tục"

4. **Xác nhận thông tin**
   - Kiểm tra thông tin
   - Click "Xác nhận"

5. **Xác thực khuôn mặt** (vì >= 10 triệu)
   - Màn hình Face Verification xuất hiện
   - Quét khuôn mặt
   - Nếu thành công → Chuyển sang OTP

6. **Xác thực OTP**
   - Nhập mã OTP
   - Click "Xác nhận"

7. **Kiểm tra kết quả**
   - Màn hình thành công với đầy đủ thông tin từ API

### Test 3: Xác thực thất bại

#### Test 3a: Face thất bại
1. Nhập số tiền >= 10 triệu
2. Xác nhận
3. Face verification → Click "Hủy" hoặc thất bại
4. **Kết quả**: Hiển thị "Xác thực khuôn mặt thất bại"
5. Không chuyển sang OTP

#### Test 3b: OTP sai
1. Nhập số tiền bất kỳ
2. Xác nhận
3. [Face nếu >= 10 triệu]
4. OTP → Nhập sai mã
5. **Kết quả**: Hiển thị "Xác thực OTP thất bại"
6. Không gọi API

### Test 4: API lỗi

#### Test 4a: Số dư không đủ
1. Nhập số tiền lớn hơn số dư
2. Hoàn thành xác thực
3. **Kết quả**: API trả về lỗi, hiển thị thông báo lỗi

#### Test 4b: Backend không chạy
1. Tắt backend
2. Hoàn thành xác thực
3. **Kết quả**: "Lỗi kết nối: ..."

## Kiểm tra API Request/Response

### Mở Logcat trong Android Studio
```
Filter: SavingConfirm
```

### Logs quan trọng
```
Creating saving: account=5967568438, amount=10000000.0, term=TWELVE_MONTHS
Response code: 200
```

### Nếu có lỗi
```
Error: {"message": "Insufficient balance", ...}
```

## API Endpoint

### Request
```
POST http://localhost:8089/api/saving/create
Content-Type: application/json
Authorization: Bearer {token}

{
  "senderAccountNumber": "5967568438",
  "amount": 10000000,
  "term": "TWELVE_MONTHS"
}
```

### Response thành công (200)
```json
{
  "savingId": 21,
  "savingBookNumber": "STK-20251222718",
  "accountNumber": "SAV2069848784",
  "balance": 10000000,
  "term": "12 tháng",
  "termMonths": 12,
  "interestRate": 5.5000,
  "openedDate": "2025-12-22",
  "maturityDate": "2026-12-22",
  "status": "ACTIVE",
  "userId": 5,
  "userFullName": "Trương Dương Hưng"
}
```

## Các trường hợp cần test

- ✅ Số tiền < 10 triệu → Chỉ OTP
- ✅ Số tiền >= 10 triệu → Face + OTP
- ✅ Face thất bại → Dừng lại
- ✅ OTP thất bại → Dừng lại
- ✅ API thành công → Hiển thị kết quả
- ✅ API lỗi → Hiển thị thông báo
- ✅ Lỗi kết nối → Hiển thị thông báo
- ✅ Click nhiều lần → Chỉ gọi API 1 lần (prevent double submission)

## Lưu ý

1. **Token hết hạn**: Nếu API trả về 401, cần đăng nhập lại
2. **Số dư**: Đảm bảo tài khoản có đủ số dư
3. **Kỳ hạn**: Chỉ hiển thị các kỳ hạn có trong API (đã lọc bỏ NON_TERM)
4. **Format số**: Tự động thêm dấu chấm (10.000.000)
5. **Back stack**: Sau khi thành công, nhấn Back sẽ về trang chủ (không quay lại confirm)

## Troubleshooting

### Không hiển thị danh sách kỳ hạn
- Kiểm tra API: `GET http://localhost:8089/api/saving/terms`
- Xem Logcat có lỗi gì không

### Không gọi API sau OTP
- Kiểm tra Logcat: "Creating saving: ..."
- Đảm bảo OTP verification trả về RESULT_OK

### API trả về lỗi
- Kiểm tra request body trong Logcat
- Kiểm tra token còn hạn không
- Kiểm tra số dư tài khoản

### Face verification không hoạt động
- Kiểm tra camera permission
- Kiểm tra FaceVerificationTransactionActivity có hoạt động không
