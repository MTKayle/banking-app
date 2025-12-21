# Tích hợp API Chi tiết Tài khoản Tiết kiệm

## Tổng quan
Đã tích hợp API `/api/saving/{savingBookNumber}` để hiển thị thông tin chi tiết của sổ tiết kiệm bao gồm lãi dự kiến, tổng tiền nhận, và số ngày còn lại.

## API Endpoint

### GET /api/saving/{savingBookNumber}
**Headers**: `Authorization: Bearer {token}`

**Example**: `GET /api/saving/STK-20251222718`

**Response**:
```json
{
  "savingId": 21,
  "savingBookNumber": "STK-20251222718",
  "accountNumber": "SAV2069848784",
  "balance": 10000000.00,
  "term": "12 tháng",
  "termMonths": 12,
  "interestRate": 5.5000,
  "openedDate": "22/12/2025",
  "maturityDate": "22/12/2026",
  "status": "ACTIVE",
  "userId": 5,
  "userFullName": "Trương Dương Hưng",
  "estimatedInterestAtMaturity": 550000.00,
  "estimatedTotalAtMaturity": 10550000.00,
  "daysUntilMaturity": 365,
  "totalDaysOfTerm": 365
}
```

## Thông tin hiển thị bổ sung

### 1. Lãi dự kiến (estimatedInterestAtMaturity)
- Số tiền lãi dự kiến nhận được khi đáo hạn
- Hiển thị: "550.000 VNĐ"
- Màu: Vàng accent (#FFB900)
- Vị trí: Khung màu vàng nhạt bên trái

### 2. Tổng tiền nhận (estimatedTotalAtMaturity)
- Tổng tiền gốc + lãi nhận được khi đáo hạn
- Hiển thị: "10.550.000 VNĐ"
- Màu: Xanh lá (#388E3C)
- Vị trí: Khung màu vàng nhạt bên phải

### 3. Số ngày còn lại (daysUntilMaturity)
- Số ngày còn lại đến ngày đáo hạn
- Hiển thị: "Còn 365 ngày đến ngày đáo hạn"
- Icon: Calendar
- Màu: Xám (#757575)

### 4. Nút rút tiền
- Màu: Đỏ (#D32F2F)
- Text: "Rút tiền"
- Vị trí: Dưới cùng của card
- Chức năng: Hiện tại hiển thị toast "Tính năng đang phát triển"

## Cách hoạt động

### Load danh sách ban đầu
1. Fragment gọi API `GET /api/saving/my-accounts`
2. Hiển thị danh sách với thông tin cơ bản
3. Các trường chi tiết (lãi dự kiến, tổng tiền, số ngày) có thể null

### Load chi tiết khi click
1. User click vào một item trong danh sách
2. Adapter gọi API `GET /api/saving/{savingBookNumber}`
3. Cập nhật UI với thông tin chi tiết
4. Cập nhật object trong list để cache

## Files đã cập nhật

### 1. MySavingAccountDTO.java
Thêm 4 fields mới:
```java
private Double estimatedInterestAtMaturity;
private Double estimatedTotalAtMaturity;
private Integer daysUntilMaturity;
private Integer totalDaysOfTerm;
```

### 2. AccountApiService.java
Thêm endpoint mới:
```java
@GET("saving/{savingBookNumber}")
Call<MySavingAccountDTO> getSavingDetail(@Path("savingBookNumber") String savingBookNumber);
```

### 3. item_my_saving_account.xml
Thêm UI elements:
- Khung màu vàng nhạt (#FFF3E0) cho lãi dự kiến và tổng tiền
- TextView cho số ngày còn lại với icon calendar
- Button "Rút tiền" màu đỏ

### 4. MySavingAccountAdapter.java
- Thêm method `loadDetailAndUpdate()` để gọi API chi tiết
- Bind thông tin chi tiết vào ViewHolder
- Xử lý click vào item để load detail
- Xử lý click nút "Rút tiền"

### 5. ic_calendar.xml
Icon calendar cho hiển thị số ngày còn lại

## Layout Structure

```
CardView (bo tròn 16dp)
├── Header (Số sổ + Trạng thái)
├── Số dư (lớn, màu primary)
├── Divider
├── Thông tin cơ bản (2 cột)
│   ├── Kỳ hạn + Ngày mở
│   └── Lãi suất + Ngày đáo hạn
├── Khung lãi dự kiến (màu vàng nhạt)
│   ├── Lãi dự kiến
│   └── Tổng tiền nhận
├── Số ngày còn lại (với icon)
└── Nút rút tiền (màu đỏ)
```

## Format dữ liệu

### Số tiền
```java
DecimalFormat numberFormatter = new DecimalFormat("#,###");
// 550000 → "550.000"
// 10550000 → "10.550.000"
```

### Số ngày
```java
"Còn " + daysUntilMaturity + " ngày đến ngày đáo hạn"
// 365 → "Còn 365 ngày đến ngày đáo hạn"
```

### Ngày tháng
```java
// API có thể trả về 2 format:
// 1. ISO: "2025-12-22" → Parse và format thành "22/12/2025"
// 2. Đã format: "22/12/2025" → Giữ nguyên
```

## Xử lý lỗi

### API chi tiết thất bại
```java
@Override
public void onFailure(Call<MySavingAccountDTO> call, Throwable t) {
    // Không hiển thị lỗi cho user
    // Chỉ log để debug
    android.util.Log.e("MySavingAdapter", "Failed to load detail: " + t.getMessage());
}
```

Lý do: Thông tin cơ bản đã hiển thị, thông tin chi tiết là bonus. Không cần làm phiền user nếu API chi tiết lỗi.

## Test

### Test case 1: Danh sách ban đầu
1. Vào tab "Tiết kiệm"
2. **Kết quả**: Hiển thị danh sách với thông tin cơ bản
3. Các trường chi tiết có thể trống hoặc hiển thị giá trị mặc định

### Test case 2: Click vào item
1. Click vào một sổ tiết kiệm
2. **Kết quả**: 
   - API được gọi
   - Thông tin chi tiết được cập nhật
   - Hiển thị lãi dự kiến, tổng tiền, số ngày còn lại

### Test case 3: Click lại item đã load
1. Click vào item đã load chi tiết
2. **Kết quả**: API được gọi lại để refresh data

### Test case 4: Click nút rút tiền
1. Click nút "Rút tiền"
2. **Kết quả**: Toast "Tính năng rút tiền đang phát triển"

### Test case 5: API chi tiết lỗi
1. Tắt backend hoặc sai endpoint
2. Click vào item
3. **Kết quả**: 
   - Không hiển thị lỗi cho user
   - Thông tin cơ bản vẫn hiển thị bình thường
   - Log lỗi trong Logcat

## Logs để debug

```
Filter: MySavingAdapter
```

Logs:
```
Failed to load detail: Connection refused
Failed to load detail: timeout
```

## Màu sắc sử dụng

- **Lãi dự kiến**: `#FFB900` (bidv_accent)
- **Tổng tiền nhận**: `#388E3C` (green_positive)
- **Khung nền**: `#FFF3E0` (vàng nhạt)
- **Nút rút**: `#D32F2F` (red_negative)
- **Text xám**: `#757575` (bidv_text_gray)

## Hoàn thành ✅

- ✅ Thêm 4 fields mới vào DTO
- ✅ Thêm API endpoint chi tiết
- ✅ Cập nhật layout với thông tin bổ sung
- ✅ Tạo icon calendar
- ✅ Cập nhật adapter để load và hiển thị chi tiết
- ✅ Xử lý click vào item
- ✅ Thêm nút "Rút tiền"
- ✅ Format số tiền và ngày tháng
- ✅ Xử lý lỗi gracefully
