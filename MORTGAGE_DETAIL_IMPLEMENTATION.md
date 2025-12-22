# Triển khai Màn hình Chi tiết Khoản Vay

## Tổng quan
Đã triển khai màn hình chi tiết khoản vay với đầy đủ thông tin và lịch thanh toán.

## Các thay đổi đã thực hiện

### 1. Tạo PaymentScheduleDTO.java
DTO cho lịch thanh toán với các field:
- `scheduleId`: ID lịch thanh toán
- `periodNumber`: Kỳ thứ mấy
- `dueDate`: Ngày đến hạn
- `principalAmount`: Tiền gốc
- `interestAmount`: Tiền lãi
- `totalAmount`: Tổng tiền phải trả
- `penaltyAmount`: Tiền phạt
- `remainingBalance`: Số dư còn lại
- `status`: Trạng thái (PENDING, PAID, OVERDUE)
- `paidDate`: Ngày đã trả
- `paidAmount`: Số tiền đã trả
- `overdueDays`: Số ngày quá hạn
- `currentPeriod`: Kỳ hiện tại
- `overdue`: Có quá hạn không

### 2. Cập nhật MortgageAccountDTO.java
- Thêm field `paymentSchedules`: List<PaymentScheduleDTO>
- Import java.util.List

### 3. Tạo activity_mortgage_detail.xml
Layout gồm các phần:
- **Toolbar**: Tiêu đề "Chi tiết khoản vay" với nút back
- **Header Card**: Số tài khoản, badge trạng thái, số tiền vay
- **Customer Info Card**: Thông tin khách hàng (tên, số điện thoại)
- **Loan Details Card**: Lãi suất, kỳ hạn, ngày tạo
- **Collateral Card**: Loại tài sản thế chấp và mô tả
- **Payment Schedule Card**: Tổng quan lịch thanh toán
  - Số kỳ thanh toán
  - Thống kê: Đã trả, Quá hạn, Còn lại
  - Nút "XEM CHI TIẾT LỊCH THANH TOÁN"
- **Remaining Balance Card**: Dư nợ còn lại (chỉ hiển thị khi status = ACTIVE)

### 4. Tạo MortgageDetailActivity.java
Activity xử lý:
- Nhận `mortgageId` từ Intent
- Call API `GET /api/mortgage/{mortgageId}`
- Hiển thị đầy đủ thông tin khoản vay
- Tính toán thống kê lịch thanh toán:
  - Đếm số kỳ đã trả (PAID)
  - Đếm số kỳ quá hạn (overdue = true)
  - Đếm số kỳ còn lại (PENDING)
- Hiển thị/ẩn các card theo status:
  - Payment Schedule: Hiển thị khi có lịch thanh toán
  - Remaining Balance: Chỉ hiển thị khi status = ACTIVE

### 5. Cập nhật AccountApiService.java
Thêm method:
```java
@GET("mortgage/{mortgageId}")
Call<MortgageAccountDTO> getMortgageDetail(@Path("mortgageId") Long mortgageId);
```

### 6. Cập nhật MortgageAccountAdapter.java
- Thêm interface `OnItemClickListener`
- Thêm method `setOnItemClickListener()`
- Set click listener cho itemView trong `onBindViewHolder()`

### 7. Cập nhật MortgageAccountFragment.java
- Import `MortgageDetailActivity`
- Set click listener cho adapter
- Navigate đến MortgageDetailActivity khi click item
- Truyền `mortgageId` qua Intent

### 8. Cập nhật AndroidManifest.xml
Thêm MortgageDetailActivity với parent là AccountActivity

### 9. Tạo bg_button_outline_green.xml
Drawable cho button outline màu xanh

## API Endpoint
```
GET http://localhost:8089/api/mortgage/{mortgageId}
```

## Response Format
```json
{
  "mortgageId": 40,
  "accountNumber": "MTG202512224112",
  "customerName": "Trương Dương Hưng",
  "customerPhone": "0839256305",
  "principalAmount": 200000000.00,
  "interestRate": 0.6667,
  "termMonths": 14,
  "startDate": "2025-12-22",
  "status": "ACTIVE",
  "collateralType": "HOUSE",
  "collateralDescription": "Nhà 3 tầng tại Hà Nội, diện tích 150m2",
  "paymentFrequency": "MONTHLY",
  "createdDate": "2025-12-22",
  "approvalDate": "2025-12-22",
  "remainingBalance": 200000000.00,
  "earlySettlementAmount": 210143944.27,
  "paymentSchedules": [
    {
      "scheduleId": 1,
      "periodNumber": 1,
      "dueDate": "2026-01-22",
      "principalAmount": 13676948.39,
      "interestAmount": 1333333.34,
      "totalAmount": 15010281.73,
      "status": "PENDING",
      "remainingBalance": 186323051.61,
      "currentPeriod": true,
      "overdue": false
    }
  ]
}
```

## Tính năng

### 1. Hiển thị thông tin cơ bản
- Số tài khoản vay
- Badge trạng thái (màu sắc theo status)
- Số tiền vay (hoặc "Từ chối" nếu REJECTED)
- Thông tin khách hàng
- Lãi suất, kỳ hạn, ngày tạo
- Tài sản thế chấp

### 2. Thống kê lịch thanh toán
- Tổng số kỳ
- Số kỳ đã trả (màu xanh)
- Số kỳ quá hạn (màu đỏ)
- Số kỳ còn lại (màu xanh primary)

### 3. Dư nợ còn lại
- Chỉ hiển thị khi status = ACTIVE
- Hiển thị trong card màu cam cảnh báo

### 4. Navigation
- Click vào item trong danh sách → Chuyển đến màn hình chi tiết
- Nút back trên toolbar → Quay lại danh sách

## Hướng dẫn test

### 1. Test navigation
1. Vào màn hình "Tài khoản" → Tab "Tiền vay"
2. Click vào bất kỳ khoản vay nào
3. Kiểm tra chuyển đến màn hình chi tiết

### 2. Test hiển thị thông tin
1. Kiểm tra tất cả thông tin hiển thị đúng
2. Kiểm tra badge trạng thái đúng màu
3. Kiểm tra số tiền format đúng
4. Kiểm tra ngày tháng format đúng (dd/MM/yyyy)

### 3. Test lịch thanh toán
1. Kiểm tra thống kê đếm đúng:
   - Đã trả: Số kỳ có status = PAID
   - Quá hạn: Số kỳ có overdue = true
   - Còn lại: Số kỳ có status = PENDING
2. Click nút "XEM CHI TIẾT LỊCH THANH TOÁN" → Hiện toast "Chức năng đang phát triển"

### 4. Test theo status
- **PENDING_APPRAISAL**: Không hiển thị payment schedule và remaining balance
- **ACTIVE**: Hiển thị đầy đủ payment schedule và remaining balance
- **REJECTED**: Số tiền vay hiển thị "Từ chối" màu đỏ, không hiển thị payment schedule
- **COMPLETED**: Hiển thị payment schedule, không hiển thị remaining balance

### 5. Test với các khoản vay khác nhau
- Khoản vay có lịch thanh toán
- Khoản vay không có lịch thanh toán
- Khoản vay bị từ chối
- Khoản vay đã hoàn thành

## Files đã tạo/cập nhật
1. ✅ `PaymentScheduleDTO.java` - Tạo mới
2. ✅ `MortgageAccountDTO.java` - Cập nhật (thêm paymentSchedules)
3. ✅ `activity_mortgage_detail.xml` - Tạo mới
4. ✅ `MortgageDetailActivity.java` - Tạo mới
5. ✅ `bg_button_outline_green.xml` - Tạo mới
6. ✅ `AccountApiService.java` - Cập nhật (thêm getMortgageDetail)
7. ✅ `MortgageAccountAdapter.java` - Cập nhật (thêm click listener)
8. ✅ `MortgageAccountFragment.java` - Cập nhật (xử lý navigation)
9. ✅ `AndroidManifest.xml` - Cập nhật (thêm MortgageDetailActivity)

## Hoàn thành
✅ Màn hình chi tiết khoản vay
✅ Hiển thị đầy đủ thông tin
✅ Thống kê lịch thanh toán
✅ Click navigation từ danh sách
✅ Tích hợp API chi tiết
✅ Xử lý các trạng thái khác nhau
✅ UI/UX đẹp và trực quan
