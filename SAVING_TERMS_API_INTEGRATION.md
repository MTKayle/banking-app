# Saving Terms API Integration

## Tổng quan
Đã tích hợp API lấy danh sách kỳ hạn tiết kiệm vào trang "Mở tài khoản tiết kiệm". Khi người dùng click nút "Mở tài khoản" trong tab Tiết kiệm, app sẽ gọi API để lấy danh sách các kỳ hạn và lãi suất.

## API Endpoint

### Get Saving Terms
```
GET http://localhost:8089/api/saving/terms
```

**Response**:
```json
{
  "total": 11,
  "data": [
    {
      "termId": 1,
      "termType": "NON_TERM",
      "months": 0,
      "displayName": "Không kỳ hạn",
      "interestRate": 0.2000,
      "updatedBy": null,
      "updatedAt": "2025-12-21T16:14:57.115572Z"
    },
    {
      "termId": 2,
      "termType": "ONE_MONTH",
      "months": 1,
      "displayName": "1 tháng",
      "interestRate": 3.2000,
      "updatedBy": null,
      "updatedAt": "2025-12-21T16:14:57.115572Z"
    },
    ...
  ],
  "success": true,
  "message": "Lấy danh sách kỳ hạn thành công"
}
```

## Thay đổi

### 1. SavingTermDTO.java
**Vị trí**: `FrontEnd/banking-app/app/src/main/java/com/example/mobilebanking/api/dto/SavingTermDTO.java`

#### Thêm fields mới
- `months` (Integer): Số tháng kỳ hạn từ API
- `displayName` (String): Tên hiển thị từ API (ví dụ: "1 tháng", "6 tháng")

#### Cập nhật method getTermMonths()
```java
/**
 * Lấy số tháng (sử dụng field months từ API)
 */
public int getTermMonths() {
    return months != null ? months : 0;
}
```

Trước đây method này dùng switch-case để map từ `termType`, bây giờ lấy trực tiếp từ field `months`.

### 2. SavingTermsResponse.java (Mới)
**Vị trí**: `FrontEnd/banking-app/app/src/main/java/com/example/mobilebanking/api/dto/SavingTermsResponse.java`

Wrapper DTO cho API response:
```java
public class SavingTermsResponse {
    private Integer total;
    private List<SavingTermDTO> data;
    private Boolean success;
    private String message;
    // getters and setters
}
```

### 3. AccountApiService.java
**Vị trí**: `FrontEnd/banking-app/app/src/main/java/com/example/mobilebanking/api/AccountApiService.java`

#### Cập nhật return type
```java
@GET("saving/terms")
Call<SavingTermsResponse> getSavingTerms();
```

Trước: `Call<List<SavingTermDTO>>`
Sau: `Call<SavingTermsResponse>`

### 4. SavingTermListActivity.java
**Vị trí**: `FrontEnd/banking-app/app/src/main/java/com/example/mobilebanking/activities/SavingTermListActivity.java`

#### Cập nhật loadTerms()
```java
private void loadTerms() {
    AccountApiService apiService = ApiClient.getAccountApiService();
    
    apiService.getSavingTerms().enqueue(new Callback<SavingTermsResponse>() {
        @Override
        public void onResponse(Call<SavingTermsResponse> call, Response<SavingTermsResponse> response) {
            if (response.isSuccessful() && response.body() != null) {
                SavingTermsResponse termsResponse = response.body();
                
                if (termsResponse.getSuccess() && termsResponse.getData() != null) {
                    termList.clear();
                    // Lọc bỏ NON_TERM và sắp xếp theo số tháng
                    for (SavingTermDTO term : termsResponse.getData()) {
                        if (!"NON_TERM".equals(term.getTermType())) {
                            termList.add(term);
                        }
                    }
                    // Sắp xếp theo số tháng tăng dần
                    Collections.sort(termList, (t1, t2) -> 
                            Integer.compare(t1.getTermMonths(), t2.getTermMonths()));
                    adapter.updateData(termList);
                }
            }
        }
        
        @Override
        public void onFailure(Call<SavingTermsResponse> call, Throwable t) {
            Toast.makeText(SavingTermListActivity.this, 
                    "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
        }
    });
}
```

## Field Mapping

| API Field | DTO Field | Mô tả |
|-----------|-----------|-------|
| termId | termId | ID kỳ hạn |
| termType | termType | Loại kỳ hạn (ONE_MONTH, SIX_MONTHS, ...) |
| months | months | Số tháng kỳ hạn |
| displayName | displayName | Tên hiển thị ("1 tháng", "6 tháng", ...) |
| interestRate | interestRate | Lãi suất (%/năm) |
| updatedBy | updatedBy | Người cập nhật |
| updatedAt | updatedAt | Thời gian cập nhật |

## Luồng hoạt động

1. **Người dùng vào tab Tiết kiệm**:
   - Fragment hiển thị danh sách sổ tiết kiệm hiện có
   - Hiển thị nút "Mở tài khoản"

2. **Click nút "Mở tài khoản"** (`btn_create_saving`):
   - Chuyển đến `SavingTermListActivity`
   - Gọi API `GET /api/saving/terms`
   - Nhận danh sách 11 kỳ hạn

3. **Xử lý response**:
   - Lọc bỏ kỳ hạn "NON_TERM" (không kỳ hạn)
   - Sắp xếp theo số tháng tăng dần (1, 2, 3, 6, 9, 12, 15, 18, 24, 36)
   - Hiển thị trong RecyclerView

4. **Người dùng chọn kỳ hạn**:
   - Click vào một kỳ hạn
   - Chuyển đến `SavingDepositActivity` với thông tin:
     - `termType`: Loại kỳ hạn
     - `termMonths`: Số tháng
     - `interestRate`: Lãi suất

## Danh sách kỳ hạn

| Term ID | Term Type | Months | Display Name | Interest Rate |
|---------|-----------|--------|--------------|---------------|
| 1 | NON_TERM | 0 | Không kỳ hạn | 0.20% |
| 2 | ONE_MONTH | 1 | 1 tháng | 3.20% |
| 3 | TWO_MONTHS | 2 | 2 tháng | 3.40% |
| 4 | THREE_MONTHS | 3 | 3 tháng | 3.60% |
| 5 | SIX_MONTHS | 6 | 6 tháng | 4.80% |
| 6 | NINE_MONTHS | 9 | 9 tháng | 5.00% |
| 7 | TWELVE_MONTHS | 12 | 12 tháng | 5.50% |
| 8 | FIFTEEN_MONTHS | 15 | 15 tháng | 5.80% |
| 9 | EIGHTEEN_MONTHS | 18 | 18 tháng | 6.00% |
| 10 | TWENTY_FOUR_MONTHS | 24 | 24 tháng | 6.40% |
| 11 | THIRTY_SIX_MONTHS | 36 | 36 tháng | 6.80% |

**Lưu ý**: Kỳ hạn "NON_TERM" (không kỳ hạn) bị lọc bỏ và không hiển thị trong danh sách.

## Files đã chỉnh sửa/tạo mới

### Đã chỉnh sửa
1. `SavingTermDTO.java` - Thêm fields `months` và `displayName`
2. `AccountApiService.java` - Cập nhật return type
3. `SavingTermListActivity.java` - Cập nhật xử lý response

### Đã tạo mới
1. `SavingTermsResponse.java` - Wrapper DTO cho API response

## Testing

### Test Case 1: Load Terms List
1. Vào tab "Tiết kiệm" trong màn hình Tài khoản
2. Click nút "Mở tài khoản"
3. **Kết quả mong đợi**:
   - Hiển thị danh sách 10 kỳ hạn (không có "Không kỳ hạn")
   - Sắp xếp từ 1 tháng đến 36 tháng
   - Mỗi item hiển thị: tên kỳ hạn và lãi suất

### Test Case 2: Select Term
1. Trong danh sách kỳ hạn
2. Click vào một kỳ hạn (ví dụ: "6 tháng - 4.8%")
3. **Kết quả mong đợi**:
   - Chuyển đến màn hình gửi tiết kiệm
   - Hiển thị thông tin kỳ hạn đã chọn
   - Cho phép nhập số tiền gửi

### Test Case 3: API Error Handling
1. Tắt backend server
2. Click nút "Mở tài khoản"
3. **Kết quả mong đợi**:
   - Hiển thị toast "Lỗi kết nối: ..."
   - Không crash app

## Status
✅ Hoàn thành - API lấy danh sách kỳ hạn tiết kiệm đã được tích hợp
