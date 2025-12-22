# Hướng Dẫn Test Thanh Toán Hóa Đơn

## Chuẩn Bị

1. Đảm bảo backend đang chạy tại `http://localhost:8089`
2. Đảm bảo app đã cấu hình đúng IP trong `ApiClient.java`
3. Đăng nhập vào app

## Các Bước Test

### Test 1: Kiểm Tra Load Danh Sách Loại Hóa Đơn

1. Từ màn hình Home, chọn "Thanh toán hóa đơn"
2. Kiểm tra dropdown "Loại hóa đơn" hiển thị:
   - ✅ Tiền điện (với icon bóng đèn màu cam)
   - ✅ Tiền nước (với icon giọt nước màu xanh)
   - ❌ KHÔNG có "Internet"

### Test 2: Tìm Kiếm Hóa Đơn Thành Công

1. Chọn loại hóa đơn: **Tiền điện**
2. Nhập mã hóa đơn: **EVN202411001**
3. Nhấn nút **"Tiếp tục"**
4. Kiểm tra:
   - ✅ Chuyển sang màn hình "Xác nhận thanh toán"
   - ✅ Hiển thị đúng thông tin:
     - Loại hóa đơn: Tiền điện
     - Nhà cung cấp: Tổng Công ty Điện lực TP.HCM
     - Mã hóa đơn: EVN202411001
     - Kỳ thanh toán: 2024-11
     - Hạn thanh toán: 2024-12-20
     - Tổng tiền: 687,500 VND

### Test 3: Tìm Kiếm Hóa Đơn Không Tồn Tại

1. Chọn loại hóa đơn: **Tiền điện**
2. Nhập mã hóa đơn: **INVALID123**
3. Nhấn nút **"Tiếp tục"**
4. Kiểm tra:
   - ✅ Hiển thị thông báo lỗi
   - ✅ Không chuyển màn hình
   - ✅ Người dùng có thể nhập lại

### Test 4: Validation - Không Chọn Loại Hóa Đơn

1. Không chọn loại hóa đơn (hoặc để mặc định)
2. Để trống mã hóa đơn
3. Nhấn nút **"Tiếp tục"**
4. Kiểm tra:
   - ✅ Hiển thị: "Vui lòng nhập mã hóa đơn"

### Test 5: Validation - Không Nhập Mã Hóa Đơn

1. Chọn loại hóa đơn: **Tiền nước**
2. Để trống mã hóa đơn
3. Nhấn nút **"Tiếp tục"**
4. Kiểm tra:
   - ✅ Hiển thị: "Vui lòng nhập mã hóa đơn"
   - ✅ Focus vào ô nhập mã hóa đơn

### Test 6: Lỗi Loại Hóa Đơn Không Hợp Lệ

Nếu backend trả về lỗi loại hóa đơn không hợp lệ:
1. Kiểm tra:
   - ✅ Hiển thị message từ API
   - ✅ Ví dụ: "Loại hóa đơn không hợp lệ: ELECTRICTY. Các loại hợp lệ: ELECTRICITY, WATER, INTERNET, PHONE"

### Test 7: Lỗi Kết Nối

1. Tắt backend hoặc ngắt kết nối mạng
2. Chọn loại hóa đơn và nhập mã
3. Nhấn **"Tiếp tục"**
4. Kiểm tra:
   - ✅ Hiển thị: "Lỗi kết nối: [error message]"
   - ✅ Button "Tiếp tục" được enable lại

### Test 8: Loading State

1. Chọn loại hóa đơn: **Tiền điện**
2. Nhập mã hóa đơn: **EVN202411001**
3. Nhấn **"Tiếp tục"**
4. Kiểm tra trong lúc đang gọi API:
   - ✅ Button hiển thị: "Đang tìm kiếm..."
   - ✅ Button bị disable (không thể nhấn)
   - ✅ Sau khi có kết quả, button trở về: "Tiếp tục" và enable lại

## Dữ Liệu Test

### Mã Hóa Đơn Hợp Lệ
- **Tiền điện:** EVN202411001
- **Tiền nước:** (cần kiểm tra với backend)

### Mã Hóa Đơn Không Hợp Lệ
- INVALID123
- TEST001
- (bất kỳ mã nào không có trong database)

## Kết Quả Mong Đợi

✅ **Tất cả test cases phải pass**

### Checklist Tổng Quan
- [ ] Load danh sách loại hóa đơn thành công
- [ ] Lọc bỏ "Internet" khỏi danh sách
- [ ] Tìm kiếm hóa đơn thành công
- [ ] Hiển thị lỗi khi không tìm thấy hóa đơn
- [ ] Validation đầy đủ
- [ ] Xử lý lỗi kết nối
- [ ] Loading state hoạt động đúng
- [ ] Chuyển màn hình xác nhận đúng
- [ ] Hiển thị thông tin hóa đơn chính xác

## Lưu Ý

1. **Backend phải chạy:** Đảm bảo backend đang chạy và có dữ liệu test
2. **IP Configuration:** Kiểm tra `ApiClient.java` đã cấu hình đúng IP
3. **Network Permission:** App phải có quyền truy cập internet
4. **Token:** Phải đăng nhập trước khi test (để có JWT token)

## Troubleshooting

### Lỗi: "Lỗi kết nối"
- Kiểm tra backend có đang chạy không
- Kiểm tra IP trong `ApiClient.java`
- Kiểm tra firewall/antivirus
- Kiểm tra điện thoại và máy tính cùng mạng

### Lỗi: "Không thể tải danh sách loại hóa đơn"
- Kiểm tra API `/api/utility-bills/bill-types` có hoạt động không
- Kiểm tra response format có đúng không

### Lỗi: Không chuyển màn hình
- Kiểm tra log để xem lỗi chi tiết
- Kiểm tra API `/api/utility-bills/search` có trả về đúng format không
