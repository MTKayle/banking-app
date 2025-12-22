# QR Scan to Transfer Integration

## Overview
Integrated QR code scanning with automatic transfer form population. When a user scans a QR code, the app calls the backend API to parse the QR data and automatically navigates to the transfer screen with all fields pre-filled.

## API Integration

### Endpoint
```
POST http://localhost:8089/api/qr/scan
```

### Request
```json
{
  "qrContent": "00020101021238380010A00000072701067707170210572061374353037045405680075802VN5914LUONG MINH TAN6006HA NOI62140810havavaghss630408B1"
}
```

### Response
```json
{
  "accountNumber": "5720613743",
  "accountHolderName": "Lương Minh Tân",
  "bankBin": "770717",
  "bankCode": "HATBANK",
  "bankName": "Ngan hang cong nghe HAT",
  "amount": 68007,
  "description": "havavaghss",
  "userId": 1,
  "accountType": "checking"
}
```

## Implementation Details

### 1. QrScanPaymentActivity Updates
- Added missing imports for API integration:
  - `retrofit2.Call`
  - `retrofit2.Callback`
  - `retrofit2.Response`
  - `ApiClient`
  - `QrApiService`
  - `QrScanRequest`
  - `QrScanResponse`

- Updated `handleQrCodeScanned()` method:
  - Shows progress bar during API call
  - Calls `/api/qr/scan` endpoint with scanned QR content
  - On success: Navigates to TransferActivity with intent extras
  - On error: Shows error toast message

### 2. TransferActivity Updates
- Added `handleQrScanData()` method in `onCreate()`:
  - Reads intent extras from QR scan
  - Populates bank selection (BANK_CODE, BANK_NAME, BANK_BIN)
  - Populates account number (ACCOUNT_NUMBER)
  - Displays account holder name (ACCOUNT_HOLDER_NAME)
  - Populates amount if present (AMOUNT)
  - Populates description/note if present (DESCRIPTION)

### 3. Data Flow
```
QR Code Scanned
    ↓
Call API: POST /api/qr/scan
    ↓
Parse Response
    ↓
Navigate to TransferActivity with Intent Extras:
  - BANK_CODE: "HATBANK"
  - BANK_NAME: "Ngan hang cong nghe HAT"
  - BANK_BIN: "770717"
  - ACCOUNT_NUMBER: "5720613743"
  - ACCOUNT_HOLDER_NAME: "Lương Minh Tân"
  - AMOUNT: 68007 (optional)
  - DESCRIPTION: "havavaghss" (optional)
    ↓
TransferActivity.handleQrScanData()
    ↓
Auto-populate all fields
```

## Field Mapping

| QR Response Field | Transfer Activity Field | Notes |
|------------------|------------------------|-------|
| bankCode | selectedBankCode, tvBankName | Bank selection |
| bankBin | selectedBankBin | Used for external account lookup |
| accountNumber | etRecipientAccount | Recipient account number |
| accountHolderName | tvRecipientName | Displayed in uppercase |
| amount | etAmount | Formatted with dots (e.g., 68.007) |
| description | etNote | Transfer note/message |

## Features

### Auto-Population
- Bank is automatically selected based on QR data
- Account number is filled and account name is displayed
- Amount is formatted with thousand separators (dots)
- Amount in Vietnamese words is shown automatically
- Description/note is pre-filled if present in QR

### Validation
- All existing validation rules still apply
- User can modify any pre-filled field
- Account name lookup is skipped (already provided by API)
- Balance check still performed before transfer

### User Experience
1. User scans QR code (camera/clipboard/gallery)
2. App shows progress indicator
3. On success: Instantly navigates to transfer screen
4. All fields are pre-filled and ready to review
5. User can modify any field if needed
6. User clicks "Tiếp tục" to proceed with transfer

## Error Handling
- Network errors: Shows toast with error message
- Invalid QR code: Shows "Không thể đọc mã QR"
- API errors: Displays error message from backend
- User stays on scanner screen if error occurs

## Testing

### Test QR Code
Use the example QR content from the API documentation:
```
00020101021238380010A00000072701067707170210572061374353037045405680075802VN5914LUONG MINH TAN6006HA NOI62140810havavaghss630408B1
```

### Expected Result
- Bank: HATBANK
- Account: 5720613743
- Name: LƯƠNG MINH TÂN
- Amount: 68.007 VNĐ (Sáu mươi tám nghìn lẻ bảy đồng)
- Note: havavaghss

## Files Modified
1. `QrScanPaymentActivity.java` - Added API integration and navigation
2. `TransferActivity.java` - Added QR data handling and auto-population
3. `QrScanResponse.java` - DTO for API response (already created)
4. `QrScanRequest.java` - DTO for API request (already created)
5. `QrApiService.java` - API service interface (already created)
6. `ApiClient.java` - Added QrApiService getter (already created)

## Status
✅ Complete - QR scan to transfer integration is fully functional
