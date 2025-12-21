# Hướng Dẫn Test Tính Năng Thêm Số Tiền Vào QR Code

## Tính Năng Đã Hoàn Thành

### 1. Bottom Sheet Thêm Số Tiền
- ✅ Nút "+ Thêm số tiền" chỉ hiển thị chữ (không có viền, không có bo tròn)
- ✅ Bottom sheet chiếm 2/3 chiều cao màn hình
- ✅ Bo tròn 2 góc phía trên
- ✅ Nền trắng với overlay mờ phía sau
- ✅ Click ra ngoài để đóng
- ✅ Animation trượt từ dưới lên

### 2. Nội Dung Bottom Sheet
- ✅ Ô nhập số tiền (bắt buộc)
  - ✅ Tự động format với dấu chấm (1000000 → 1.000.000)
  - ✅ Hiển thị chữ số bằng chữ bên dưới (Một triệu đồng)
- ✅ Ô nhập nội dung chuyển khoản (tùy chọn)
- ✅ Nút "Xoá tất cả" (màu xám, solid)
- ✅ Nút "Lưu" (màu xanh lá, solid)

### 3. Hiển Thị Sau Khi Lưu
- ✅ Số tiền hiển thị với dấu chấm (1.000.000 VND)
- ✅ Số tiền có gạch chân màu xanh lá
- ✅ Nội dung hiển thị bên dưới (nếu có)
- ✅ Click vào để chỉnh sửa lại

### 4. Chức Năng
- ✅ Validation: Số tiền bắt buộc phải nhập
- ✅ Validation: Số tiền phải lớn hơn 0
- ✅ Nút "Xoá tất cả" chỉ xóa input, không đóng dialog
- ✅ Nút "Lưu" validate và đóng dialog
- ✅ QR code tự động regenerate với format: `accountNumber|amount|message`

## Các Bước Test

### Bước 1: Mở Màn Hình QR Code
1. Đăng nhập vào app
2. Vào trang Home
3. Click vào "Nhận tiền" hoặc "Mã QR của tôi"
4. Màn hình QR code hiển thị với nút "+ Thêm số tiền"

### Bước 2: Test Bottom Sheet với Format Số Tiền
1. Click vào nút "+ Thêm số tiền"
2. **Kiểm tra:**
   - Bottom sheet xuất hiện với animation trượt từ dưới lên
   - Chiếm khoảng 2/3 màn hình
   - Bo tròn 2 góc trên
   - Có overlay mờ phía sau
   - Có handle bar ở trên cùng

### Bước 3: Test Format Số Tiền Tự Động
1. Nhập số tiền: 1000000
   - **Kết quả:** Tự động format thành "1.000.000"
   - Hiển thị chữ bên dưới: "Một triệu đồng"

2. Nhập số tiền: 500000
   - **Kết quả:** Format thành "500.000"
   - Hiển thị: "Năm trăm nghìn đồng"

3. Nhập số tiền: 123456789
   - **Kết quả:** Format thành "123.456.789"
   - Hiển thị: "Một trăm hai mươi ba triệu bốn trăm năm mươi sáu nghìn bảy trăm tám mươi chín đồng"

### Bước 4: Test Validation
1. Để trống ô số tiền, nhập nội dung, click "Lưu"
   - **Kết quả:** Toast "Vui lòng nhập số tiền"
   - Dialog không đóng

2. Nhập số tiền = 0, click "Lưu"
   - **Kết quả:** Toast "Số tiền phải lớn hơn 0"
   - Dialog không đóng

### Bước 5: Test Nút "Xoá tất cả"
1. Nhập số tiền: 500000 (hiển thị "500.000" và "Năm trăm nghìn đồng")
2. Nhập nội dung: "Chuyển tiền test"
3. Click "Xoá tất cả"
   - **Kết quả:** 
     - Cả 2 ô input bị xóa trắng
     - Chữ số bằng chữ biến mất
     - Dialog vẫn mở
     - Có thể nhập lại

### Bước 6: Test Lưu Thành Công
1. Nhập số tiền: 1000000 (hiển thị "1.000.000" và "Một triệu đồng")
2. Nhập nội dung: "Thanh toán hóa đơn"
3. Click "Lưu"
   - **Kết quả:**
     - Toast "Đã cập nhật thông tin QR"
     - Dialog đóng
     - Nút "+ Thêm số tiền" biến mất
     - Hiển thị thông tin:
       - Số tiền: "1.000.000 VND" (có dấu chấm và gạch chân màu xanh lá)
       - Nội dung: "Thanh toán hóa đơn"
     - QR code tự động regenerate

### Bước 7: Test Gạch Chân Số Tiền
1. Sau khi lưu, kiểm tra số tiền hiển thị dưới QR code
   - **Kết quả:**
     - Số tiền có gạch chân màu xanh lá (#1B5E20)
     - Format với dấu chấm: "1.000.000 VND"
     - Màu chữ xanh lá đậm

### Bước 8: Test Chỉnh Sửa
1. Click vào thông tin đã hiển thị (số tiền + nội dung)
   - **Kết quả:**
     - Bottom sheet mở lại
     - Số tiền đã được format với dấu chấm
     - Chữ số bằng chữ hiển thị ngay
     - Nội dung đã được điền sẵn
     - Có thể chỉnh sửa và lưu lại

### Bước 9: Test Chỉ Nhập Số Tiền (Không Nhập Nội Dung)
1. Click "+ Thêm số tiền"
2. Nhập số tiền: 2500000 (hiển thị "2.500.000" và "Hai triệu năm trăm nghìn đồng")
3. Để trống nội dung
4. Click "Lưu"
   - **Kết quả:**
     - Hiển thị số tiền: "2.500.000 VND" với gạch chân
     - Không hiển thị dòng nội dung (visibility = GONE)
     - QR code format: `accountNumber|2500000|`

### Bước 10: Test QR Code Data
1. Sau khi lưu số tiền và nội dung
2. Dùng app quét QR khác để quét mã
   - **Kết quả:** Data format: `accountNumber|amount|message`
   - Ví dụ: `0839256305|1000000|Thanh toán hóa đơn`

## Test Cases Chi Tiết

| Test Case | Input | Expected Result | Status |
|-----------|-------|-----------------|--------|
| TC1 | Nhập 1000000 | Hiển thị "1.000.000" và "Một triệu đồng" | ✅ |
| TC2 | Nhập 500000 | Hiển thị "500.000" và "Năm trăm nghìn đồng" | ✅ |
| TC3 | Nhập 0 | Hiển thị "0", không có chữ | ✅ |
| TC4 | Không nhập gì, click Lưu | Toast "Vui lòng nhập số tiền" | ✅ |
| TC5 | Số tiền = 0, click Lưu | Toast "Số tiền phải lớn hơn 0" | ✅ |
| TC6 | Click Xoá tất cả | Input xóa trắng, chữ biến mất, dialog không đóng | ✅ |
| TC7 | Lưu số tiền | Hiển thị với dấu chấm và gạch chân xanh lá | ✅ |
| TC8 | Click vào info đã lưu | Bottom sheet mở với data đã format | ✅ |

## Lưu Ý Khi Test

1. **Format Số Tiền:**
   - Trong bottom sheet: Dấu chấm ngăn cách hàng nghìn (1.000.000)
   - Sau khi lưu: Cũng dùng dấu chấm (1.000.000 VND)
   - Có gạch chân màu xanh lá (#1B5E20)

2. **Chữ Số Bằng Chữ:**
   - Hiển thị màu xanh lá (#1B5E20)
   - Font chữ nghiêng (italic)
   - Tự động cập nhật khi nhập số
   - Ẩn khi số = 0 hoặc trống

3. **QR Code Format:**
   - Có số tiền + nội dung: `accountNumber|amount|message`
   - Chỉ có số tiền: `accountNumber|amount|`
   - Không có gì: `accountNumber`
   - Amount lưu dạng số nguyên không có dấu chấm

4. **UI Behavior:**
   - Khi chưa có thông tin: Hiển thị nút "+ Thêm số tiền"
   - Khi đã có thông tin: Ẩn nút, hiển thị info layout với gạch chân
   - Info layout có thể click để chỉnh sửa

## Các File Đã Cập Nhật

- `MyQRActivity.java` - Thêm logic format số tiền và chuyển đổi sang chữ
- `activity_my_qr.xml` - Thêm background gạch chân cho số tiền
- `bottom_sheet_add_amount.xml` - Thêm TextView hiển thị chữ số bằng chữ
- `bg_underline_green.xml` - Drawable gạch chân màu xanh lá

## Kết Quả Mong Đợi

✅ Số tiền tự động format với dấu chấm khi nhập
✅ Hiển thị chữ số bằng chữ tiếng Việt
✅ Số tiền hiển thị có gạch chân màu xanh lá
✅ UI đẹp, mượt mà, giống trang chuyển tiền
✅ Validation chặt chẽ
✅ QR code regenerate tự động

