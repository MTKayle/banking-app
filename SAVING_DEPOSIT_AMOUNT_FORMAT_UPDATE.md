# Saving Deposit Amount Format Update

## Tổng quan
Đã cập nhật trang gửi tiết kiệm (`activity_saving_deposit.xml`) với:
- Bo tròn ô nhập số tiền
- Format số tiền với dấu chấm (10.000)
- Hiển thị số tiền bằng chữ tiếng Việt màu xanh

## Thay đổi

### 1. Layout (activity_saving_deposit.xml)

#### Cập nhật CardView ô nhập số tiền
**Trước**:
- `cardCornerRadius="12dp"`
- `cardElevation="0dp"`
- `strokeColor="#E0E0E0"` với `strokeWidth="1dp"`
- Text "VND" màu xám

**Sau**:
- `cardCornerRadius="16dp"` - Bo tròn hơn
- `cardElevation="2dp"` - Có bóng đổ nhẹ
- Không có stroke (viền)
- Text "VNĐ" màu xanh `#1B5E20` và bold

#### Thêm TextView hiển thị số bằng chữ
```xml
<TextView
    android:id="@+id/tv_amount_in_words"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_marginTop="8dp"
    android:layout_marginStart="4dp"
    android:text=""
    android:textColor="#1B5E20"
    android:textSize="14sp"
    android:visibility="gone" />
```

### 2. Activity (SavingDepositActivity.java)

#### Thêm field
```java
private TextView tvAmountInWords;
```

#### Cập nhật TextWatcher
Thêm logic format số với dấu chấm và hiển thị chữ số:
```java
etAmount.addTextChangedListener(new TextWatcher() {
    @Override
    public void afterTextChanged(Editable s) {
        // Remove non-digits
        String raw = s.toString().replaceAll("[^0-9]", "");
        
        // Format with dots
        String formatted = formatWithDots(raw);
        etAmount.setText(formatted);
        
        // Show amount in Vietnamese words
        showAmountInWordsIfNeeded();
    }
});
```

#### Thêm helper methods
1. **formatWithDots()** - Format số với dấu chấm
   ```java
   10000 → "10.000"
   1000000 → "1.000.000"
   ```

2. **showAmountInWordsIfNeeded()** - Hiển thị số bằng chữ
   ```java
   10000 → "Mười nghìn đồng"
   1000000 → "Một triệu đồng"
   ```

3. **numberToVietnameseWords()** - Chuyển đổi số sang chữ tiếng Việt
4. **readThreeDigits()** - Đọc 3 chữ số
5. **readUnit()** - Đọc đơn vị (mốt, lăm, etc.)

#### Cập nhật validation
```java
// Remove dots before parsing
String cleaned = amountStr.replace(".", "");
double amount = Double.parseDouble(cleaned);

if (amount < 1000000) {
    showValidationError("Số tiền gửi tối thiểu là 1.000.000 VNĐ");
    return false;
}
```

## Ví dụ

### Input: 10000
- **Hiển thị**: `10.000 VNĐ`
- **Chữ**: `Mười nghìn đồng` (màu xanh #1B5E20)

### Input: 1000000
- **Hiển thị**: `1.000.000 VNĐ`
- **Chữ**: `Một triệu đồng` (màu xanh #1B5E20)

### Input: 5500000
- **Hiển thị**: `5.500.000 VNĐ`
- **Chữ**: `Năm triệu năm trăm nghìn đồng` (màu xanh #1B5E20)

## UI Changes

### CardView Amount Input
```
┌─────────────────────────────────┐
│  10.000.000          VNĐ        │  ← Bo tròn 16dp, có bóng
└─────────────────────────────────┘
  Mười triệu đồng                    ← Màu xanh #1B5E20
```

### Trước vs Sau

**Trước**:
- Bo tròn 12dp
- Viền xám
- Không có bóng
- VND màu xám
- Không có chữ số

**Sau**:
- Bo tròn 16dp
- Không có viền
- Có bóng nhẹ (elevation 2dp)
- VNĐ màu xanh bold
- Có chữ số màu xanh

## Validation

### Số tiền tối thiểu
- **Giá trị**: 1.000.000 VNĐ
- **Thông báo**: "Số tiền gửi tối thiểu là 1.000.000 VNĐ"

### Số dư không đủ
- **Kiểm tra**: `amount > accountBalance`
- **Thông báo**: "Số dư tài khoản không đủ"

### Số tiền không hợp lệ
- **Kiểm tra**: NumberFormatException
- **Thông báo**: "Số tiền không hợp lệ"

## Files đã chỉnh sửa
1. `activity_saving_deposit.xml` - Cập nhật UI ô nhập số tiền
2. `SavingDepositActivity.java` - Thêm logic format và chuyển đổi sang chữ

## Testing

### Test Case 1: Format Number
1. Nhập "10000"
2. **Kết quả mong đợi**:
   - Hiển thị "10.000"
   - Chữ: "Mười nghìn đồng" (màu xanh)

### Test Case 2: Large Number
1. Nhập "5500000"
2. **Kết quả mong đợi**:
   - Hiển thị "5.500.000"
   - Chữ: "Năm triệu năm trăm nghìn đồng" (màu xanh)

### Test Case 3: Minimum Amount Validation
1. Nhập "500000"
2. Click "Tiếp tục"
3. **Kết quả mong đợi**:
   - Hiển thị lỗi: "Số tiền gửi tối thiểu là 1.000.000 VNĐ"

### Test Case 4: Insufficient Balance
1. Nhập số tiền lớn hơn số dư
2. Click "Tiếp tục"
3. **Kết quả mong đợi**:
   - Hiển thị lỗi: "Số dư tài khoản không đủ"

## Status
✅ Hoàn thành - Ô nhập số tiền đã được cập nhật với format dấu chấm và hiển thị chữ số tiếng Việt
