# Home Account Navigation Update

## Tổng quan
Đã cập nhật 2 nút "Tiết kiệm" và "Vay nhanh" trong trang Home để chuyển đến mục Tài khoản với tab tương ứng.

## Thay đổi

### 1. HomeFragment.java
**Vị trí**: `FrontEnd/banking-app/app/src/main/java/com/example/mobilebanking/ui_home/HomeFragment.java`

#### Cập nhật Quick Actions
Thay đổi Intent cho 2 nút:

**Trước đây**:
```java
setupQuickAction(view, R.id.uihome_action_saving, new Intent(requireContext(), ServicesActivity.class));
setupQuickAction(view, R.id.uihome_action_loan, new Intent(requireContext(), ServicesActivity.class));
```

**Sau khi cập nhật**:
```java
// Tiết kiệm - Navigate to Account Activity with Savings tab
Intent savingIntent = new Intent(requireContext(), com.example.mobilebanking.activities.AccountActivity.class);
savingIntent.putExtra("TAB_INDEX", 1); // Tab 1 = Tiết kiệm
setupQuickAction(view, R.id.uihome_action_saving, savingIntent);

// Vay nhanh - Navigate to Account Activity with Loan tab
Intent loanIntent = new Intent(requireContext(), com.example.mobilebanking.activities.AccountActivity.class);
loanIntent.putExtra("TAB_INDEX", 2); // Tab 2 = Tiền vay
setupQuickAction(view, R.id.uihome_action_loan, loanIntent);
```

### 2. AccountActivity.java
**Vị trí**: `FrontEnd/banking-app/app/src/main/java/com/example/mobilebanking/activities/AccountActivity.java`

#### Thêm method handleTabNavigation()
Xử lý navigation đến tab cụ thể từ Intent:

```java
/**
 * Handle navigation to specific tab from Intent
 */
private void handleTabNavigation() {
    Intent intent = getIntent();
    if (intent != null && intent.hasExtra("TAB_INDEX")) {
        int tabIndex = intent.getIntExtra("TAB_INDEX", 0);
        // Validate tab index
        if (tabIndex >= 0 && tabIndex < 3) {
            viewPager.setCurrentItem(tabIndex, false);
        }
    }
}
```

#### Cập nhật onCreate()
Gọi `handleTabNavigation()` sau khi setup tabs:

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_account);
    
    initViews();
    setupToolbar();
    setupViewPager();
    setupTabs();
    
    // Check if we need to navigate to a specific tab
    handleTabNavigation();
}
```

## Tab Index Mapping

| Tab Index | Tab Name | Mô tả |
|-----------|----------|-------|
| 0 | Thanh toán | Tài khoản thanh toán (mặc định) |
| 1 | Tiết kiệm | Tài khoản tiết kiệm |
| 2 | Tiền vay | Tài khoản tiền vay |

## Luồng hoạt động

### Nút "Tiết kiệm"
1. Người dùng click nút "Tiết kiệm" trong trang Home
2. App tạo Intent đến `AccountActivity` với `TAB_INDEX = 1`
3. `AccountActivity` mở và hiển thị tab "Tiết kiệm"

### Nút "Vay nhanh"
1. Người dùng click nút "Vay nhanh" trong trang Home
2. App tạo Intent đến `AccountActivity` với `TAB_INDEX = 2`
3. `AccountActivity` mở và hiển thị tab "Tiền vay"

## Button IDs

### Home Quick Actions
- `@+id/uihome_action_saving` - Nút Tiết kiệm
- `@+id/uihome_action_loan` - Nút Vay nhanh

## Files đã chỉnh sửa
1. `HomeFragment.java` - Cập nhật Intent cho 2 nút
2. `AccountActivity.java` - Thêm xử lý navigation đến tab cụ thể

## Testing

### Test Case 1: Nút Tiết kiệm
1. Vào trang Home
2. Click nút "Tiết kiệm"
3. **Kết quả mong đợi**:
   - Chuyển đến màn hình Tài khoản
   - Tab "Tiết kiệm" được chọn và hiển thị
   - Hiển thị danh sách tài khoản tiết kiệm

### Test Case 2: Nút Vay nhanh
1. Vào trang Home
2. Click nút "Vay nhanh"
3. **Kết quả mong đợi**:
   - Chuyển đến màn hình Tài khoản
   - Tab "Tiền vay" được chọn và hiển thị
   - Hiển thị danh sách khoản vay

### Test Case 3: Navigation từ Balance Card
1. Vào trang Home
2. Click vào Balance Card (số dư)
3. **Kết quả mong đợi**:
   - Chuyển đến màn hình Tài khoản
   - Tab "Thanh toán" được chọn (mặc định)
   - Hiển thị tài khoản thanh toán

## Lưu ý
- `viewPager.setCurrentItem(tabIndex, false)` - Tham số `false` để không có animation khi chuyển tab
- Validate `tabIndex` để đảm bảo nằm trong khoảng 0-2
- Nếu không có `TAB_INDEX` trong Intent, mặc định hiển thị tab đầu tiên (Thanh toán)

## Status
✅ Hoàn thành - Nút Tiết kiệm và Vay nhanh đã được kết nối với AccountActivity
