# Triển khai Màn hình Lịch Thanh Toán Chi Tiết

## Tổng quan
Đã triển khai màn hình lịch thanh toán chi tiết với danh sách các kỳ thanh toán và thống kê.

## Các thay đổi đã thực hiện

### 1. Tạo item_payment_schedule.xml
Layout cho mỗi kỳ thanh toán gồm:
- **Status Indicator**: Thanh màu bên trái (xanh lá = đã trả, xanh dương = kỳ hiện tại, đỏ = quá hạn, xám = chờ thanh toán)
- **Header**: Số kỳ và badge trạng thái
- **Due Date**: Ngày đến hạn
- **Total Amount**: Tổng thanh toán (màu xanh primary, bold)
- **Penalty Warning**: Cảnh báo lãi phạt (chỉ hiển thị khi quá hạn)
- **Details Grid**: Gốc, Lãi, Dư nợ còn lại

### 2. Tạo activity_payment_schedule.xml
Layout màn hình chính gồm:
- **Toolbar**: Tiêu đề "Lịch thanh toán" với nút back
- **Summary Card** (màu xanh primary):
  - Số tiền cần thanh toán
  - Danh sách kỳ quá hạn/hiện tại
  - Thống kê: Đã trả, Quá hạn, Còn lại
- **Legend**: Chú thích màu sắc (Đã trả, Kỳ hiện tại, Quá hạn)
- **RecyclerView**: Danh sách các kỳ thanh toán
- **Action Buttons**:
  - "Thanh toán kỳ hiện tại" (màu xanh, có icon)
  - "TẤT TOÁN KHOẢN VAY" (outline)

### 3. Tạo PaymentScheduleAdapter.java
Adapter xử lý:
- Hiển thị thông tin mỗi kỳ thanh toán
- Styling theo trạng thái:
  - **PAID**: Màu xanh lá, background trắng
  - **Overdue**: Màu đỏ, background xám nhạt, hiển thị lãi phạt
  - **Current Period**: Màu xanh dương, background xanh nhạt
  - **Pending**: Màu xám, background trắng
- Format số tiền và ngày tháng

### 4. Tạo PaymentScheduleActivity.java
Activity xử lý:
- Nhận `mortgageId` từ Intent
- Call API `GET /api/mortgage/{mortgageId}`
- Lấy danh sách `paymentSchedules` từ response
- Tính toán thống kê:
  - Đếm số kỳ đã trả, quá hạn, còn lại
  - Tính tổng số tiền cần thanh toán (bao gồm cả lãi phạt)
  - Tạo danh sách các kỳ quá hạn/hiện tại
- Hiển thị/ẩn nút "Thanh toán kỳ hiện tại" dựa vào trạng thái
- Xử lý click buttons (hiện tại chỉ show toast)

### 5. Cập nhật MortgageDetailActivity.java
- Thay đổi click listener của nút "XEM CHI TIẾT LỊCH THANH TOÁN"
- Navigate đến PaymentScheduleActivity
- Truyền `mortgageId` qua Intent

### 6. Cập nhật AndroidManifest.xml
Thêm PaymentScheduleActivity với parent là MortgageDetailActivity

## Tính năng

### 1. Summary Card
- Hiển thị tổng số tiền cần thanh toán (các kỳ quá hạn + kỳ hiện tại)
- Liệt kê các kỳ quá hạn và kỳ hiện tại
- Thống kê số lượng: Đã trả, Quá hạn, Còn lại

### 2. Danh sách kỳ thanh toán
Mỗi item hiển thị:
- Số kỳ và badge trạng thái
- Ngày đến hạn
- Tổng thanh toán
- Lãi phạt (nếu quá hạn)
- Chi tiết: Gốc, Lãi, Dư nợ còn lại

### 3. Styling theo trạng thái
- **Đã trả**: Thanh xanh lá, badge "ĐÃ TRẢ" xanh dương
- **Quá hạn**: Thanh đỏ, badge "QUÁ HẠN" đỏ, background xám, hiển thị lãi phạt
- **Kỳ hiện tại**: Thanh xanh dương, badge "KỲ HIỆN TẠI" xanh lá, background xanh nhạt
- **Chờ thanh toán**: Thanh xám, badge "CHỜ THANH TOÁN" cam

### 4. Action Buttons
- **Thanh toán kỳ hiện tại**: Chỉ hiển thị khi có kỳ hiện tại hoặc kỳ quá hạn
- **Tất toán khoản vay**: Luôn hiển thị

## API Endpoint
```
GET http://localhost:8089/api/mortgage/{mortgageId}
```

Sử dụng lại API chi tiết khoản vay, lấy field `paymentSchedules` từ response.

## Mapping Status
- `PAID` → "ĐÃ TRẢ" (Xanh lá)
- `PENDING` + `overdue = true` → "QUÁ HẠN" (Đỏ)
- `PENDING` + `currentPeriod = true` → "KỲ HIỆN TẠI" (Xanh dương)
- `PENDING` → "CHỜ THANH TOÁN" (Cam)

## Tính toán
### Tổng số tiền cần thanh toán
```
totalRemaining = 0
for each schedule:
    if overdue or currentPeriod:
        totalRemaining += totalAmount
        if penaltyAmount exists:
            totalRemaining += penaltyAmount
```

### Danh sách kỳ quá hạn/hiện tại
```
overduePeriods = []
for each schedule:
    if overdue:
        overduePeriods.add("Kỳ X (Quá hạn)")
    else if currentPeriod:
        overduePeriods.add("Kỳ X")
```

## Hướng dẫn test

### 1. Test navigation
1. Vào màn hình chi tiết khoản vay
2. Click nút "XEM CHI TIẾT LỊCH THANH TOÁN"
3. Kiểm tra chuyển đến màn hình lịch thanh toán

### 2. Test summary card
1. Kiểm tra tổng số tiền cần thanh toán đúng
2. Kiểm tra danh sách kỳ quá hạn/hiện tại
3. Kiểm tra thống kê đếm đúng

### 3. Test danh sách kỳ
1. Kiểm tra tất cả kỳ hiển thị đúng thứ tự
2. Kiểm tra màu sắc và badge theo trạng thái
3. Kiểm tra lãi phạt chỉ hiển thị khi quá hạn
4. Kiểm tra format số tiền và ngày tháng

### 4. Test với các trường hợp
- Khoản vay không có kỳ quá hạn
- Khoản vay có nhiều kỳ quá hạn
- Khoản vay đã thanh toán hết
- Khoản vay mới chưa thanh toán kỳ nào

### 5. Test buttons
1. Click "Thanh toán kỳ hiện tại" → Hiện toast
2. Click "TẤT TOÁN KHOẢN VAY" → Hiện toast
3. Kiểm tra nút "Thanh toán kỳ hiện tại" ẩn khi không có kỳ cần thanh toán

## Files đã tạo/cập nhật
1. ✅ `item_payment_schedule.xml` - Tạo mới
2. ✅ `activity_payment_schedule.xml` - Tạo mới
3. ✅ `PaymentScheduleAdapter.java` - Tạo mới
4. ✅ `PaymentScheduleActivity.java` - Tạo mới
5. ✅ `MortgageDetailActivity.java` - Cập nhật (navigation)
6. ✅ `AndroidManifest.xml` - Cập nhật (thêm PaymentScheduleActivity)

## Hoàn thành
✅ Màn hình lịch thanh toán chi tiết
✅ Summary card với thống kê
✅ Danh sách kỳ thanh toán với styling theo trạng thái
✅ Hiển thị lãi phạt khi quá hạn
✅ Legend chú thích màu sắc
✅ Action buttons
✅ Navigation từ màn hình chi tiết
✅ Tính toán tổng số tiền cần thanh toán
✅ UI/UX đẹp và trực quan giống hình mẫu
