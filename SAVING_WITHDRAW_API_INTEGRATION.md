# Saving Withdraw API Integration - Complete

## Overview
Đã tích hợp API withdraw-confirm để xử lý rút tiền tiết kiệm sau khi xác thực OTP thành công.

## API Endpoint

### Withdraw Confirm API
```
POST /api/saving/{savingBookNumber}/withdraw-confirm
```

**Headers**:
```
Authorization: Bearer {token}
```

**Response**:
```json
{
  "savingBookNumber": "STK-20251222718",
  "principalAmount": 10000000.00,
  "appliedInterestRate": 0.2,
  "interestEarned": 0.00,
  "totalAmount": 10000000.00,
  "checkingAccountNumber": "5967568438",
  "newCheckingBalance": 45442064.00,
  "openedDate": "2025-12-22",
  "closedDate": "2025-12-22",
  "daysHeld": 0,
  "transactionCode": "WDR-1766345216913-A4812493",
  "message": "Tất toán trước hạn thành công. Lãi suất áp dụng: 0.2% (không kỳ hạn)"
}
```

## Implementation Details

### 1. DTO Class
**File**: `api/dto/WithdrawConfirmResponse.java`

**Fields**:
- `savingBookNumber`: Số sổ tiết kiệm
- `principalAmount`: Số tiền gốc
- `appliedInterestRate`: Lãi suất áp dụng
- `interestEarned`: Tiền lãi
- `totalAmount`: Tổng tiền nhận
- `checkingAccountNumber`: Số tài khoản thanh toán nhận tiền
- `newCheckingBalance`: Số dư mới của tài khoản thanh toán
- `openedDate`: Ngày mở sổ
- `closedDate`: Ngày đóng sổ
- `daysHeld`: Số ngày gửi
- `transactionCode`: Mã giao dịch
- `message`: Thông báo (hiển thị màu đỏ)

### 2. API Service
**File**: `api/AccountApiService.java`

**Added Method**:
```java
@POST("saving/{savingBookNumber}/withdraw-confirm")
Call<WithdrawConfirmResponse> confirmWithdraw(@Path("savingBookNumber") String savingBookNumber);
```

### 3. Confirm Activity Update
**File**: `activities/SavingWithdrawConfirmActivity.java`

**Changes**:
1. Updated `setupOtpLauncher()`:
   - Khi OTP thành công → Call `callWithdrawConfirmApi()`
   - Không còn navigate trực tiếp đến success

2. Added `callWithdrawConfirmApi()`:
   - Call API `POST /api/saving/{savingBookNumber}/withdraw-confirm`
   - Show loading state trên button
   - Handle success → Navigate to success với data từ API
   - Handle error → Show toast

3. Updated `navigateToSuccess()`:
   - Nhận `WithdrawConfirmResponse` từ API
   - Pass tất cả fields sang success screen
   - Bao gồm: transactionCode, message, checkingAccountNumber, newCheckingBalance

### 4. Success Activity Update
**File**: `activities/SavingWithdrawSuccessActivity.java`

**Changes**:
1. Added new TextViews:
   - `tvMessage`: Hiển thị message từ API (màu đỏ)
   - `tvCheckingAccount`: Số tài khoản nhận tiền
   - `tvNewBalance`: Số dư mới

2. Updated `displayInfo()`:
   - Nhận thêm fields: message, checkingAccountNumber, newCheckingBalance
   - Hiển thị message nếu có (màu đỏ)
   - Hiển thị checking account info
   - Hiển thị số dư mới (màu xanh)

### 5. Success Layout Update
**File**: `layout/activity_saving_withdraw_success.xml`

**Added Components**:
1. **Tài khoản nhận tiền**:
   - Label: "Tài khoản nhận"
   - Value: Số tài khoản (monospace font)

2. **Số dư mới**:
   - Label: "Số dư mới"
   - Value: Số dư (màu xanh, bold)

3. **Message Card** (màu đỏ):
   - Background: `#FFEBEE` (light red)
   - Icon: ⚠️
   - Text: Message từ API (màu đỏ)
   - Border radius: 12dp
   - Padding: 16dp

## Flow Diagram

```
User clicks "Xác nhận rút tiền"
    ↓
Navigate to OTP Verification
    ↓
User enters OTP
    ↓
OTP verified successfully
    ↓
Return to Confirm Activity (RESULT_OK)
    ↓
Call API: POST /api/saving/{savingBookNumber}/withdraw-confirm
    ↓
API returns WithdrawConfirmResponse
    ↓
Navigate to Success Screen with API data
    ↓
Display: transaction code, message, checking account, new balance
    ↓
User clicks "Hoàn tất" → Home
```

## Data Mapping

### From API Response to Success Screen

| API Field | Success Screen Field | Display Format |
|-----------|---------------------|----------------|
| savingBookNumber | tv_saving_book_number | Plain text |
| totalAmount | tv_total_amount | 10.000.000 VNĐ (large, green) |
| interestEarned | tv_interest_earned | 0 VNĐ (green) |
| closedDate | tv_withdraw_date | dd/MM/yyyy |
| transactionCode | tv_transaction_id | Monospace font |
| checkingAccountNumber | tv_checking_account | Monospace font |
| newCheckingBalance | tv_new_balance | 45.442.064 VNĐ (green, bold) |
| message | tv_message | Red text in red card with ⚠️ icon |

## UI Features

### Message Display (Red Warning)
- **Background**: Light red (#FFEBEE)
- **Text Color**: Red (red_negative)
- **Icon**: ⚠️ warning emoji
- **Border**: Rounded corners (12dp)
- **Visibility**: Only shown if message exists

### Checking Account Info
- **Label**: "Tài khoản nhận"
- **Font**: Monospace for account number
- **Visibility**: Only shown if checkingAccountNumber exists

### New Balance
- **Label**: "Số dư mới"
- **Color**: Green (green_positive)
- **Style**: Bold
- **Visibility**: Only shown if newCheckingBalance > 0

## Testing Guide

### Test Scenario 1: Rút trước hạn
1. Chọn sổ tiết kiệm chưa đáo hạn
2. Click "Rút tiền"
3. Xem preview → Click "Xác nhận"
4. Nhập OTP (123456)
5. Verify API được gọi
6. Verify success screen hiển thị:
   - Message màu đỏ: "Tất toán trước hạn..."
   - Lãi suất thấp (0.2%)
   - Tiền lãi = 0 hoặc rất thấp
   - Tài khoản nhận tiền
   - Số dư mới

### Test Scenario 2: Rút đúng hạn
1. Chọn sổ tiết kiệm đã đáo hạn
2. Click "Rút tiền"
3. Xem preview → Click "Xác nhận"
4. Nhập OTP (123456)
5. Verify API được gọi
6. Verify success screen hiển thị:
   - Message (nếu có)
   - Lãi suất đầy đủ
   - Tiền lãi đầy đủ
   - Tài khoản nhận tiền
   - Số dư mới

### Test Error Cases
1. **API Error**: Verify toast hiển thị lỗi
2. **Network Error**: Verify toast hiển thị lỗi kết nối
3. **OTP Failed**: Verify không call API, hiển thị lỗi OTP

## Files Modified

### New Files
1. `api/dto/WithdrawConfirmResponse.java` - Response DTO

### Modified Files
1. `api/AccountApiService.java` - Added confirmWithdraw endpoint
2. `activities/SavingWithdrawConfirmActivity.java` - Added API call after OTP
3. `activities/SavingWithdrawSuccessActivity.java` - Display API data
4. `layout/activity_saving_withdraw_success.xml` - Added message card and account info

## Status
✅ **COMPLETE** - API integration hoàn tất

## Next Steps
- Test với backend API thực tế
- Verify transaction code format
- Test với các trường hợp rút trước hạn và đúng hạn
- Verify số dư tài khoản checking được cập nhật
