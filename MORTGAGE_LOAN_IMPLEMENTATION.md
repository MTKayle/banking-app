# Triển khai Tính năng Tiền Vay (Mortgage Loan)

## Tổng quan
Đã triển khai giao diện mới cho tính năng "Tiền vay" với 4 tab header để lọc khoản vay theo trạng thái và tích hợp API.

## Các thay đổi đã thực hiện

### 1. Cập nhật DTO (MortgageAccountDTO.java)
- Cập nhật các field để map đúng với response API mới
- Các field chính:
  - `mortgageId`: ID khoản vay
  - `accountNumber`: Số tài khoản vay (MTG...)
  - `customerName`: Tên khách hàng
  - `customerPhone`: Số điện thoại
  - `principalAmount`: Số tiền vay
  - `interestRate`: Lãi suất
  - `termMonths`: Kỳ hạn (tháng)
  - `status`: Trạng thái (PENDING_APPRAISAL, ACTIVE, REJECTED, COMPLETED)
  - `collateralType`: Loại tài sản thế chấp (HOUSE, CAR, LAND)
  - `collateralDescription`: Mô tả tài sản
  - `rejectionReason`: Lý do từ chối (nếu có)
  - `createdDate`: Ngày tạo
  - `remainingBalance`: Số dư còn lại

### 2. Layout mới (fragment_mortgage_account.xml)
- **Search Bar**: Tìm kiếm theo tên, số tài khoản
- **TabLayout**: 5 tab để filter
  - Tất cả
  - Chờ duyệt (PENDING_APPRAISAL)
  - Đang vay (ACTIVE)
  - Từ chối (REJECTED)
  - Hoàn thành (COMPLETED)
- **RecyclerView**: Hiển thị danh sách khoản vay
- **Empty State**: Hiển thị khi không có dữ liệu

### 3. Item Layout (item_mortgage_account.xml)
Mỗi item hiển thị:
- Số tài khoản vay (MTG...)
- Badge trạng thái (màu sắc khác nhau theo status)
- Ngày tạo
- Thông tin khách hàng (tên, số điện thoại)
- Số tiền vay
- Loại tài sản thế chấp
- Lý do từ chối (chỉ hiển thị khi status = REJECTED)

### 4. Status Badge Drawables
Tạo 4 drawable cho các trạng thái:
- `bg_status_pending.xml`: Màu cam (#FFA726) - Chờ duyệt
- `bg_status_active.xml`: Màu xanh lá (#4CAF50) - Đang vay
- `bg_status_rejected.xml`: Màu đỏ (#F44336) - Từ chối
- `bg_status_completed.xml`: Màu xanh dương (#2196F3) - Hoàn thành

### 5. Adapter (MortgageAccountAdapter.java)
- Hiển thị thông tin khoản vay
- Format số tiền theo định dạng Việt Nam
- Format ngày tháng (dd/MM/yyyy)
- Hiển thị badge trạng thái với màu sắc phù hợp
- Hiển thị lý do từ chối (nếu có)
- Method `updateList()` để cập nhật danh sách khi filter

### 6. Fragment (MortgageAccountFragment.java)
- Setup TabLayout với 5 tab
- Xử lý filter theo status khi chọn tab
- Xử lý tìm kiếm theo tên, số tài khoản, số điện thoại
- Call API: `GET /api/mortgage/user/{userId}`
- Hiển thị empty state khi không có dữ liệu

### 7. API Service (AccountApiService.java)
- Thêm method `getMortgagesByUserId(Long userId)`
- Endpoint: `http://localhost:8089/api/mortgage/user/{userId}`

## API Response Format
```json
[
  {
    "mortgageId": 35,
    "accountNumber": "MTG202512229569",
    "customerName": "Trương Dương Hưng",
    "customerPhone": "0839256305",
    "principalAmount": 0.00,
    "interestRate": 0.0000,
    "termMonths": null,
    "startDate": null,
    "status": "PENDING_APPRAISAL",
    "collateralType": "HOUSE",
    "collateralDescription": "Nhà 3 tầng tại Hà Nội, diện tích 150m2",
    "rejectionReason": null,
    "createdDate": "2025-12-22",
    "remainingBalance": 0,
    "earlySettlementAmount": 0
  }
]
```

## Mapping Status
- `PENDING_APPRAISAL` → "Chờ duyệt" (Màu cam)
- `ACTIVE` → "Đang vay" (Màu xanh lá)
- `REJECTED` → "Từ chối" (Màu đỏ)
- `COMPLETED` → "Hoàn thành" (Màu xanh dương)

## Mapping Collateral Type
- `HOUSE` → "Nhà ở"
- `CAR` → "Xe"
- `LAND` → "Đất"

## Hướng dẫn test

### 1. Chuẩn bị
- Đảm bảo backend đang chạy tại `http://localhost:8089`
- Đăng nhập với tài khoản có userId = 5

### 2. Test các tính năng

#### Test hiển thị danh sách
1. Vào màn hình "Tài khoản"
2. Chọn tab "Tiền vay"
3. Kiểm tra danh sách khoản vay hiển thị đúng

#### Test filter theo status
1. Click vào tab "Chờ duyệt" → Chỉ hiển thị khoản vay có status = PENDING_APPRAISAL
2. Click vào tab "Đang vay" → Chỉ hiển thị khoản vay có status = ACTIVE
3. Click vào tab "Từ chối" → Chỉ hiển thị khoản vay có status = REJECTED (có hiển thị lý do từ chối)
4. Click vào tab "Hoàn thành" → Chỉ hiển thị khoản vay có status = COMPLETED
5. Click vào tab "Tất cả" → Hiển thị tất cả khoản vay

#### Test tìm kiếm
1. Nhập số tài khoản (VD: MTG202512229569) → Hiển thị khoản vay tương ứng
2. Nhập tên khách hàng (VD: Trương) → Hiển thị các khoản vay của khách hàng đó
3. Nhập số điện thoại (VD: 0839) → Hiển thị các khoản vay có số điện thoại chứa chuỗi đó

#### Test empty state
1. Chọn tab không có dữ liệu → Hiển thị "Chưa có khoản vay"
2. Tìm kiếm với từ khóa không tồn tại → Hiển thị empty state

### 3. Kiểm tra UI
- Badge trạng thái hiển thị đúng màu sắc
- Số tiền format đúng (VD: 200.000.000 đ)
- Ngày tháng format đúng (VD: 22/12/2025)
- Lý do từ chối chỉ hiển thị khi status = REJECTED
- Hiển thị "Chờ thỏa thuận" khi principalAmount = 0

## Lưu ý
- API endpoint: `http://localhost:8089/api/mortgage/user/{userId}`
- Cần có token trong header để call API
- userId được lấy từ DataManager sau khi đăng nhập
- Nếu không có khoản vay nào, hiển thị empty state

## Files đã tạo/cập nhật
1. `MortgageAccountDTO.java` - Cập nhật
2. `fragment_mortgage_account.xml` - Tạo mới
3. `item_mortgage_account.xml` - Tạo mới
4. `bg_status_pending.xml` - Tạo mới
5. `bg_status_active.xml` - Tạo mới
6. `bg_status_rejected.xml` - Tạo mới
7. `bg_status_completed.xml` - Tạo mới
8. `circle_background.xml` - Tạo mới
9. `styles.xml` - Cập nhật (thêm TabTextStyle)
10. `MortgageAccountAdapter.java` - Cập nhật
11. `MortgageAccountFragment.java` - Cập nhật
12. `AccountApiService.java` - Cập nhật

## Hoàn thành
✅ Giao diện mới với TabLayout
✅ Filter theo 4 trạng thái
✅ Tìm kiếm theo tên, số tài khoản, số điện thoại
✅ Hiển thị thông tin chi tiết khoản vay
✅ Badge trạng thái với màu sắc phù hợp
✅ Hiển thị lý do từ chối (nếu có)
✅ Tích hợp API mới
✅ Empty state khi không có dữ liệu
