# Tích hợp API Danh sách Tài khoản Tiết kiệm

## Tổng quan
Đã tích hợp API `/api/saving/my-accounts` để hiển thị danh sách sổ tiết kiệm của người dùng.

## API Endpoint

### GET /api/saving/my-accounts
**Headers**: `Authorization: Bearer {token}`

**Response**:
```json
[
  {
    "savingId": 21,
    "savingBookNumber": "STK-20251222718",
    "accountNumber": "SAV2069848784",
    "balance": 10000000.00,
    "term": "12 tháng",
    "termMonths": 12,
    "interestRate": 5.5000,
    "openedDate": "2025-12-22",
    "maturityDate": "2026-12-22",
    "status": "ACTIVE",
    "userId": 5,
    "userFullName": "Trương Dương Hưng"
  },
  {
    "savingId": 22,
    "savingBookNumber": "STK-20251222011",
    "accountNumber": "SAV0673712014",
    "balance": 13457577.00,
    "term": "1 tháng",
    "termMonths": 1,
    "interestRate": 3.2000,
    "openedDate": "2025-12-22",
    "maturityDate": "2026-01-22",
    "status": "ACTIVE",
    "userId": 5,
    "userFullName": "Trương Dương Hưng"
  }
]
```

## UI Logic

### Khi KHÔNG có tài khoản (danh sách rỗng):
- ✅ Hiển thị banner "Tiết kiệm Online" với nút "Mở tài khoản"
- ✅ Hiển thị empty state "Chưa có sổ tiết kiệm"
- ❌ Ẩn RecyclerView
- ❌ Ẩn nút "Mở tài khoản" ở dưới

### Khi CÓ tài khoản:
- ❌ Ẩn banner "Tiết kiệm Online"
- ❌ Ẩn empty state
- ✅ Hiển thị RecyclerView với danh sách tài khoản
- ✅ Hiển thị nút "Mở tài khoản" ở dưới cùng

## Files đã tạo/cập nhật

### 1. DTO mới
**File**: `MySavingAccountDTO.java`
- Mapping với response từ API `/api/saving/my-accounts`
- Các fields: savingId, savingBookNumber, accountNumber, balance, term, termMonths, interestRate, openedDate, maturityDate, status, userId, userFullName

### 2. API Service
**File**: `AccountApiService.java`
```java
@GET("saving/my-accounts")
Call<List<MySavingAccountDTO>> getMySavingAccounts();
```

### 3. Fragment
**File**: `SavingAccountFragment.java`
- Gọi API `getMySavingAccounts()` khi load
- Logic hiển thị/ẩn banner và nút dựa trên số lượng tài khoản
- 2 nút "Mở tài khoản": trong banner và ở dưới cùng

### 4. Adapter
**File**: `MySavingAccountAdapter.java`
- Hiển thị thông tin từ `MySavingAccountDTO`
- Format số tiền với dấu chấm (10.000.000)
- Format ngày từ ISO (YYYY-MM-DD) sang dd/MM/yyyy
- Format trạng thái: ACTIVE → "Đang hoạt động"

### 5. Layout
**File**: `fragment_saving_account.xml`
- Banner với id `banner_create_saving` (có thể ẩn)
- RecyclerView với id `rv_saving_accounts`
- Empty state với id `empty_state`
- Nút dưới với id `btn_create_saving_bottom` (có thể ẩn)

**File**: `item_my_saving_account.xml`
- CardView bo tròn 16dp
- Hiển thị: Số sổ, trạng thái, số dư, kỳ hạn, lãi suất, ngày mở, ngày đáo hạn

### 6. Drawable
**File**: `bg_status_active.xml`
- Background cho badge trạng thái
- Màu xanh nhạt (#E8F5E9) với bo góc 12dp

## Hiển thị thông tin

### Mỗi tài khoản hiển thị:
1. **Số sổ tiết kiệm**: STK-20251222718
2. **Trạng thái**: Đang hoạt động (màu xanh)
3. **Số dư**: 10.000.000 VNĐ (màu primary, size lớn)
4. **Kỳ hạn**: 12 tháng
5. **Lãi suất**: 5.5%/năm (màu accent)
6. **Ngày mở**: 22/12/2025
7. **Ngày đáo hạn**: 22/12/2026

## Format dữ liệu

### Số tiền
```java
DecimalFormat numberFormatter = new DecimalFormat("#,###");
String formatted = numberFormatter.format(10000000); // "10.000.000"
```

### Ngày tháng
```java
// Input: "2025-12-22" (ISO format)
// Output: "22/12/2025"
SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
```

### Trạng thái
```java
"ACTIVE" → "Đang hoạt động" (màu xanh)
"CLOSED" → "Đã đóng" (màu xám)
```

## Test

### Test case 1: Không có tài khoản
1. Đăng nhập với user chưa có sổ tiết kiệm
2. Vào tab "Tiết kiệm"
3. **Kết quả**:
   - Hiển thị banner "Tiết kiệm Online"
   - Hiển thị empty state
   - Không hiển thị danh sách
   - Không hiển thị nút dưới

### Test case 2: Có tài khoản
1. Đăng nhập với user đã có sổ tiết kiệm
2. Vào tab "Tiết kiệm"
3. **Kết quả**:
   - Ẩn banner
   - Hiển thị danh sách tài khoản (mỗi tài khoản trong 1 CardView bo tròn)
   - Hiển thị nút "Mở tài khoản" ở dưới cùng

### Test case 3: Tạo sổ mới
1. Vào tab "Tiết kiệm" (chưa có tài khoản)
2. Click "Mở tài khoản" trong banner
3. Hoàn thành tạo sổ
4. Quay lại tab "Tiết kiệm"
5. **Kết quả**:
   - Banner biến mất
   - Hiển thị sổ vừa tạo
   - Hiển thị nút "Mở tài khoản" ở dưới

### Test case 4: Tạo sổ thứ 2
1. Vào tab "Tiết kiệm" (đã có 1 sổ)
2. Click "Mở tài khoản" ở dưới
3. Hoàn thành tạo sổ
4. Quay lại tab "Tiết kiệm"
5. **Kết quả**:
   - Hiển thị 2 sổ tiết kiệm
   - Nút "Mở tài khoản" vẫn ở dưới

## Xử lý lỗi

### API lỗi
```java
@Override
public void onFailure(Call<List<MySavingAccountDTO>> call, Throwable t) {
    Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
    showEmptyState();
}
```

### Response không thành công
```java
if (response.isSuccessful() && response.body() != null) {
    // Success
} else {
    Toast.makeText(requireContext(), "Không thể tải danh sách tiết kiệm", Toast.LENGTH_SHORT).show();
    showEmptyState();
}
```

## Refresh data

Để refresh danh sách sau khi tạo sổ mới, có thể:

### Option 1: Override onResume
```java
@Override
public void onResume() {
    super.onResume();
    fetchSavingAccounts(); // Reload data mỗi khi quay lại tab
}
```

### Option 2: Sử dụng ActivityResult
Trong `SavingSuccessActivity`, set result trước khi finish:
```java
setResult(RESULT_OK);
finish();
```

Trong `SavingAccountFragment`, listen result và refresh.

## Hoàn thành ✅

- ✅ Tích hợp API `/api/saving/my-accounts`
- ✅ Hiển thị danh sách tài khoản trong CardView bo tròn
- ✅ Ẩn banner khi có tài khoản
- ✅ Hiển thị nút "Mở tài khoản" ở dưới khi có tài khoản
- ✅ Format số tiền, ngày tháng, trạng thái
- ✅ Xử lý empty state
- ✅ Xử lý lỗi API
