# Hướng Dẫn Thanh Toán Hóa Đơn (Utility Bill Payment)

## Tổng Quan

Tính năng thanh toán hóa đơn cho phép người dùng thanh toán các loại hóa đơn như tiền điện, tiền nước thông qua ứng dụng mobile banking.

## Các API Được Sử Dụng

### 1. Lấy Danh Sách Loại Hóa Đơn

**Endpoint:** `GET /api/utility-bills/bill-types`

**Response:**
```json
{
  "data": [
    {
      "displayName": "Tiền điện",
      "value": "ELECTRICITY"
    },
    {
      "displayName": "Tiền nước",
      "value": "WATER"
    },
    {
      "displayName": "Internet",
      "value": "INTERNET"
    }
  ],
  "success": true,
  "message": "Lấy danh sách loại hóa đơn thành công"
}
```

**Lưu ý:** App sẽ lọc bỏ loại "INTERNET" và chỉ hiển thị "Tiền điện" và "Tiền nước".

### 2. Tìm Kiếm Hóa Đơn

**Endpoint:** `GET /api/utility-bills/search?billCode=EVN202411001&billType=ELECTRICITY`

**Parameters:**
- `billCode`: Mã hóa đơn (ví dụ: EVN202411001)
- `billType`: Loại hóa đơn (ELECTRICITY hoặc WATER)

**Response Thành Công:**
```json
{
  "data": {
    "billId": 14,
    "billCode": "EVN202411001",
    "billType": "ELECTRICITY",
    "billTypeDisplay": "Tiền điện",
    "customerName": "Nguyễn Văn A",
    "customerAddress": "123 Nguyễn Huệ, Quận 1, TP.HCM",
    "customerPhone": "0901234567",
    "period": "2024-11",
    "usageAmount": 250,
    "oldIndex": 1000,
    "newIndex": 1250,
    "unitPrice": 2500.00,
    "amount": 625000.00,
    "vat": 62500.00,
    "totalAmount": 687500.00,
    "issueDate": "2024-11-25",
    "dueDate": "2024-12-20",
    "status": "PAID",
    "statusDisplay": "Đã thanh toán",
    "providerName": "Tổng Công ty Điện lực TP.HCM",
    "providerCode": "EVNHCMC",
    "notes": "Hóa đơn tiền điện tháng 11/2024",
    "overdue": false
  },
  "success": true,
  "message": "Tìm thấy hóa đơn"
}
```

**Response Lỗi:**
```json
{
  "success": false,
  "message": "Loại hóa đơn không hợp lệ: ELECTRICTY. Các loại hợp lệ: ELECTRICITY, WATER, INTERNET, PHONE"
}
```

## Luồng Xử Lý

### 1. Màn Hình Thanh Toán (BillPaymentActivity)

**File:** `BillPaymentActivity.java`

**Chức năng:**
1. Load danh sách loại hóa đơn từ API
2. Lọc bỏ loại "INTERNET"
3. Hiển thị dropdown với các loại hóa đơn còn lại
4. Cho phép người dùng nhập mã hóa đơn
5. Khi nhấn "Tiếp tục", gọi API tìm kiếm hóa đơn
6. Nếu tìm thấy → chuyển sang màn hình xác nhận
7. Nếu không tìm thấy → hiển thị thông báo lỗi

**Các bước xử lý:**

```java
// 1. Load bill types khi activity khởi tạo
private void loadBillTypes() {
    utilityBillApiService.getBillTypes().enqueue(new Callback<BillTypesResponse>() {
        @Override
        public void onResponse(Call<BillTypesResponse> call, Response<BillTypesResponse> response) {
            if (response.isSuccessful() && response.body() != null) {
                // Filter out INTERNET
                for (BillTypesResponse.BillType type : response.body().getData()) {
                    if (!"INTERNET".equals(type.getValue())) {
                        billTypes.add(type);
                    }
                }
                buildBillTypeOptions();
            }
        }
    });
}

// 2. Search bill khi user nhấn "Tiếp tục"
private void searchBill(String billCode, String billType) {
    utilityBillApiService.searchBill(billCode, billType).enqueue(new Callback<BillSearchResponse>() {
        @Override
        public void onResponse(Call<BillSearchResponse> call, Response<BillSearchResponse> response) {
            if (response.isSuccessful() && response.body() != null) {
                if (response.body().getSuccess()) {
                    // Navigate to confirmation
                    navigateToConfirmationScreen(response.body().getData());
                } else {
                    // Show error message
                    Toast.makeText(this, response.body().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
    });
}
```

### 2. Màn Hình Xác Nhận (BillPaymentConfirmationActivity)

**File:** `BillPaymentConfirmationActivity.java`

**Chức năng:**
1. Hiển thị thông tin hóa đơn chi tiết
2. Cho phép người dùng xem lại trước khi thanh toán
3. Khi nhấn "Xác nhận thanh toán" → chuyển sang màn hình thành công

**Dữ liệu nhận từ BillPaymentActivity:**
- Provider name (Tên nhà cung cấp)
- Bill type display (Loại hóa đơn)
- Bill code (Mã hóa đơn)
- Billing period (Kỳ thanh toán)
- Due date (Hạn thanh toán)
- Total amount (Tổng tiền)
- Account number (Số tài khoản)

## Các File Đã Tạo/Cập Nhật

### 1. API Service
- **File:** `UtilityBillApiService.java`
- **Mô tả:** Interface định nghĩa các API endpoint cho utility bills

### 2. DTO Classes
- **File:** `BillTypesResponse.java`
- **Mô tả:** Response DTO cho API lấy danh sách loại hóa đơn

- **File:** `BillSearchResponse.java`
- **Mô tả:** Response DTO cho API tìm kiếm hóa đơn

### 3. Activity
- **File:** `BillPaymentActivity.java`
- **Mô tả:** Cập nhật để gọi API thực thay vì dùng mock data

- **File:** `BillPaymentConfirmationActivity.java`
- **Mô tả:** Cập nhật để nhận và hiển thị due date từ API

### 4. API Client
- **File:** `ApiClient.java`
- **Mô tả:** Thêm method `getUtilityBillApiService()` để lấy instance của UtilityBillApiService

## Xử Lý Lỗi

### 1. Lỗi Kết Nối
```java
@Override
public void onFailure(Call<BillSearchResponse> call, Throwable t) {
    Toast.makeText(this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
}
```

### 2. Lỗi API (Không Tìm Thấy Hóa Đơn)
```java
if (!response.body().getSuccess()) {
    Toast.makeText(this, response.body().getMessage(), Toast.LENGTH_LONG).show();
}
```

### 3. Lỗi Loại Hóa Đơn Không Hợp Lệ
API sẽ trả về message: "Loại hóa đơn không hợp lệ: ELECTRICTY. Các loại hợp lệ: ELECTRICITY, WATER, INTERNET, PHONE"

App sẽ hiển thị message này cho người dùng.

## Test Cases

### 1. Test Load Bill Types
- Mở màn hình thanh toán
- Kiểm tra dropdown hiển thị đúng 2 loại: "Tiền điện" và "Tiền nước"
- Kiểm tra không có "Internet" trong danh sách

### 2. Test Search Bill - Thành Công
- Chọn loại hóa đơn: "Tiền điện"
- Nhập mã hóa đơn: "EVN202411001"
- Nhấn "Tiếp tục"
- Kiểm tra chuyển sang màn hình xác nhận với đúng thông tin

### 3. Test Search Bill - Lỗi
- Chọn loại hóa đơn: "Tiền điện"
- Nhập mã hóa đơn không tồn tại: "INVALID123"
- Nhấn "Tiếp tục"
- Kiểm tra hiển thị thông báo lỗi

### 4. Test Validation
- Không chọn loại hóa đơn → hiển thị "Vui lòng chọn loại hóa đơn"
- Không nhập mã hóa đơn → hiển thị "Vui lòng nhập mã hóa đơn"

## Lưu Ý Quan Trọng

1. **Lọc Internet:** App phải lọc bỏ loại "INTERNET" từ danh sách bill types
2. **Validation:** Phải validate cả loại hóa đơn và mã hóa đơn trước khi gọi API
3. **Error Handling:** Phải xử lý đầy đủ các trường hợp lỗi (network, API error, validation)
4. **Loading State:** Hiển thị trạng thái loading khi đang gọi API
5. **Bill Type Value:** Phải dùng `value` (ELECTRICITY, WATER) để gọi API, không phải `displayName`

## Ví Dụ Mã Hóa Đơn Test

- **Tiền điện:** EVN202411001
- **Tiền nước:** (cần kiểm tra với backend)

## Kết Luận

Tính năng thanh toán hóa đơn đã được implement đầy đủ với:
- ✅ Gọi API lấy danh sách loại hóa đơn
- ✅ Lọc bỏ loại "INTERNET"
- ✅ Gọi API tìm kiếm hóa đơn
- ✅ Xử lý lỗi đầy đủ
- ✅ Chuyển sang màn hình xác nhận khi tìm thấy hóa đơn
- ✅ Hiển thị thông báo lỗi khi không tìm thấy hoặc có lỗi
