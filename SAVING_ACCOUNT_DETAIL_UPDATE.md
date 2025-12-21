# Saving Account Detail Update - Complete

## Overview
Cập nhật adapter để call API detail khi click vào item, hiển thị đúng dữ liệu và ẩn nút rút tiền cho tài khoản đã đóng.

## Changes Made

### 1. MySavingAccountAdapter Updates

**File**: `adapters/MySavingAccountAdapter.java`

#### A. Initial Display (onBindViewHolder)
**Changes**:
1. Hiển thị 0 cho null values:
   ```java
   Double estimatedInterest = account.getEstimatedInterestAtMaturity();
   holder.tvEstimatedInterest.setText(formatCurrency(estimatedInterest != null ? estimatedInterest : 0.0) + " VNĐ");
   
   Double totalAtMaturity = account.getEstimatedTotalAtMaturity();
   holder.tvTotalAtMaturity.setText(formatCurrency(totalAtMaturity != null ? totalAtMaturity : 0.0) + " VNĐ");
   
   Integer daysUntil = account.getDaysUntilMaturity();
   holder.tvDaysUntilMaturity.setText("Còn " + (daysUntil != null ? daysUntil : 0) + " ngày đến ngày đáo hạn");
   ```

2. Ẩn nút rút tiền nếu status = "CLOSED":
   ```java
   if ("CLOSED".equals(account.getStatus())) {
       holder.btnWithdraw.setVisibility(View.GONE);
   } else {
       holder.btnWithdraw.setVisibility(View.VISIBLE);
   }
   ```

#### B. Detail API Call (loadDetailAndUpdate)
**Changes**:
1. Hiển thị 0 cho null values từ API
2. Cập nhật status từ API
3. Cập nhật balance từ API
4. Ẩn/hiện nút rút tiền dựa trên status mới:
   ```java
   holder.tvStatus.setText(formatStatus(detail.getStatus()));
   if ("CLOSED".equals(detail.getStatus())) {
       holder.btnWithdraw.setVisibility(View.GONE);
   } else {
       holder.btnWithdraw.setVisibility(View.VISIBLE);
   }
   ```

## API Integration

### Detail API
```
GET /api/saving/{savingBookNumber}
```

**Response Example (CLOSED account)**:
```json
{
  "savingId": 21,
  "savingBookNumber": "STK-20251222718",
  "accountNumber": "SAV2069848784",
  "balance": 0.00,
  "term": "12 tháng",
  "termMonths": 12,
  "interestRate": 5.5000,
  "openedDate": "22/12/2025",
  "maturityDate": "22/12/2026",
  "status": "CLOSED",
  "userId": 5,
  "userFullName": "Trương Dương Hưng",
  "estimatedInterestAtMaturity": null,
  "estimatedTotalAtMaturity": null,
  "daysUntilMaturity": 0,
  "totalDaysOfTerm": 0
}
```

## Data Mapping

| API Field | TextView ID | Display Logic |
|-----------|------------|---------------|
| daysUntilMaturity | tv_days_until_maturity | "Còn {value} ngày đến ngày đáo hạn" (0 if null) |
| estimatedInterestAtMaturity | tv_estimated_interest | "{value} VNĐ" (0 if null) |
| estimatedTotalAtMaturity | tv_total_at_maturity | "{value} VNĐ" (0 if null) |
| status | tv_status | "Đang hoạt động" or "Đã đóng" |
| balance | tv_balance | "{value} VNĐ" |

## UI Behavior

### Status-Based Display

#### ACTIVE Account
- Status badge: "Đang hoạt động" (green)
- Withdraw button: **VISIBLE**
- All fields displayed normally

#### CLOSED Account
- Status badge: "Đã đóng" (gray/red)
- Withdraw button: **HIDDEN**
- Balance: 0.00 VNĐ
- Estimated interest: 0 VNĐ (null → 0)
- Estimated total: 0 VNĐ (null → 0)
- Days until maturity: 0

### Click Behavior

**When user clicks on item**:
1. Call API `GET /api/saving/{savingBookNumber}`
2. Update all fields with fresh data
3. Update status and balance
4. Show/hide withdraw button based on status
5. Display 0 for null values

**When user clicks withdraw button**:
1. Only available if status != "CLOSED"
2. Navigate to `SavingWithdrawConfirmActivity`

## Testing Guide

### Test Case 1: ACTIVE Account
1. Login và vào Tài khoản → Tiết kiệm
2. Xem danh sách sổ tiết kiệm
3. Click vào sổ có status = "ACTIVE"
4. Verify:
   - API detail được gọi
   - Thông tin được cập nhật
   - Nút "Rút tiền" hiển thị
   - Estimated values hiển thị đúng

### Test Case 2: CLOSED Account
1. Login và vào Tài khoản → Tiết kiệm
2. Xem danh sách sổ tiết kiệm
3. Click vào sổ có status = "CLOSED"
4. Verify:
   - API detail được gọi
   - Status hiển thị "Đã đóng"
   - Balance = 0.00 VNĐ
   - Estimated interest = 0 VNĐ
   - Estimated total = 0 VNĐ
   - Days until maturity = 0
   - Nút "Rút tiền" **BỊ ẨN**

### Test Case 3: Null Values
1. Click vào sổ có estimatedInterestAtMaturity = null
2. Verify hiển thị "0 VNĐ" thay vì null hoặc trống

### Test Case 4: After Withdrawal
1. Rút tiền thành công từ 1 sổ
2. Quay lại danh sách
3. Click vào sổ vừa rút
4. Verify:
   - Status = "CLOSED"
   - Balance = 0
   - Nút rút tiền bị ẩn

## Edge Cases Handled

1. **Null estimatedInterestAtMaturity**: Display 0 VNĐ
2. **Null estimatedTotalAtMaturity**: Display 0 VNĐ
3. **Null daysUntilMaturity**: Display 0 ngày
4. **CLOSED status**: Hide withdraw button
5. **API failure**: Log error, don't crash, keep existing data

## Visual States

### Before Click (Initial Load)
```
┌─────────────────────────────────┐
│ STK-20251222718    [Đang hoạt động] │
│ 10.000.000 VNĐ                  │
│ 12 tháng | 5.5%/năm             │
│ Ngày mở: 22/12/2025             │
│ Đáo hạn: 22/12/2026             │
│ Lãi dự kiến: 0 VNĐ              │
│ Tổng nhận: 0 VNĐ                │
│ Còn 0 ngày đến ngày đáo hạn     │
│           [Rút tiền]            │
└─────────────────────────────────┘
```

### After Click - ACTIVE
```
┌─────────────────────────────────┐
│ STK-20251222718    [Đang hoạt động] │
│ 10.000.000 VNĐ                  │
│ 12 tháng | 5.5%/năm             │
│ Ngày mở: 22/12/2025             │
│ Đáo hạn: 22/12/2026             │
│ Lãi dự kiến: 550.000 VNĐ        │
│ Tổng nhận: 10.550.000 VNĐ       │
│ Còn 365 ngày đến ngày đáo hạn   │
│           [Rút tiền]            │
└─────────────────────────────────┘
```

### After Click - CLOSED
```
┌─────────────────────────────────┐
│ STK-20251222718      [Đã đóng]  │
│ 0 VNĐ                           │
│ 12 tháng | 5.5%/năm             │
│ Ngày mở: 22/12/2025             │
│ Đáo hạn: 22/12/2026             │
│ Lãi dự kiến: 0 VNĐ              │
│ Tổng nhận: 0 VNĐ                │
│ Còn 0 ngày đến ngày đáo hạn     │
│        (no button)              │
└─────────────────────────────────┘
```

## Files Modified

1. `adapters/MySavingAccountAdapter.java`
   - Updated `onBindViewHolder()` to handle null values and CLOSED status
   - Updated `loadDetailAndUpdate()` to update status and hide button

## Status
✅ **COMPLETE** - All features implemented

## Next Steps
- Test với backend API thực tế
- Verify CLOSED accounts không hiển thị nút rút
- Test null values hiển thị 0
- Verify refresh sau khi rút tiền
