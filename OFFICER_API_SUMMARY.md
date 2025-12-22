# Tổng hợp API cho trang Officer

Base URL: `http://localhost:8089/api` (hoặc ngrok URL)

**Lưu ý:** Tất cả API đều yêu cầu JWT token trong header:
```
Authorization: Bearer {access_token}
```

---

## 1. Quản lý Người dùng (User Management)

### 1.1. Lấy danh sách tất cả người dùng
- **Endpoint:** `GET /api/users`
- **Quyền:** `OFFICER`
- **Response:**
```json
[
  {
    "userId": 1,
    "phone": "0912345678",
    "email": "user@example.com",
    "fullName": "Nguyen Van A",
    "role": "CUSTOMER",
    "isLocked": false,
    ...
  }
]
```

### 1.2. Lấy thông tin người dùng theo ID
- **Endpoint:** `GET /api/users/{userId}`
- **Quyền:** `OFFICER`
- **Response:** `UserResponse`

### 1.3. Lấy thông tin người dùng theo số điện thoại
- **Endpoint:** `GET /api/users/by-phone/{phone}`
- **Quyền:** `OFFICER`
- **Response:** `UserResponse`

### 1.4. Lấy thông tin người dùng theo số CCCD
- **Endpoint:** `GET /api/users/by-cccd/{cccdNumber}`
- **Quyền:** `OFFICER`
- **Response:** `UserResponse`

### 1.5. Cập nhật thông tin người dùng
- **Endpoint:** `PUT /api/users/{userId}`
- **Quyền:** `OFFICER`
- **Body:**
```json
{
  "email": "newemail@example.com",
  "fullName": "Nguyen Van B",
  "permanentAddress": "123 ABC Street",
  "temporaryAddress": "456 XYZ Street"
}
```

### 1.6. Khóa/Mở khóa tài khoản người dùng
- **Endpoint:** `PATCH /api/users/{userId}/lock`
- **Quyền:** `OFFICER`
- **Body:**
```json
{
  "isLocked": true,
  "lockReason": "Vi phạm quy định"
}
```

### 1.7. Cập nhật số điện thoại
- **Endpoint:** `PATCH /api/users/{userId}/phone`
- **Quyền:** `OFFICER`
- **Body:**
```json
{
  "phone": "0987654321"
}
```

### 1.8. Cập nhật số CCCD
- **Endpoint:** `PATCH /api/users/{userId}/cccd`
- **Quyền:** `OFFICER`
- **Body:**
```json
{
  "cccdNumber": "001234567890"
}
```

### 1.9. Cập nhật ảnh đại diện người dùng
- **Endpoint:** `POST /api/users/{userId}/update-photo`
- **Quyền:** `OFFICER`
- **Content-Type:** `multipart/form-data`
- **Body:** `photo` (file)

### 1.10. Kiểm tra tính năng của người dùng
- **Endpoint:** `GET /api/users/{userId}/features/face-recognition`
- **Endpoint:** `GET /api/users/{userId}/features/smart-ekyc`
- **Endpoint:** `GET /api/users/{userId}/features/fingerprint-login`
- **Quyền:** `OFFICER` hoặc chính user đó
- **Response:**
```json
{
  "enabled": true
}
```

### 1.11. Cập nhật cài đặt tính năng
- **Endpoint:** `PATCH /api/users/{userId}/settings`
- **Quyền:** `OFFICER` hoặc chính user đó
- **Body:**
```json
{
  "smartEkycEnabled": true,
  "faceRecognitionEnabled": true
}
```

### 1.12. Cập nhật Smart OTP
- **Endpoint:** `PATCH /api/users/{userId}/smart-otp`
- **Quyền:** `OFFICER` hoặc chính user đó
- **Body:**
```json
{
  "smartOtpEnabled": true
}
```

---

## 2. Quản lý Vay thế chấp (Mortgage Management)

### 2.1. Tạo tài khoản vay thế chấp mới
- **Endpoint:** `POST /api/mortgage/create`
- **Quyền:** `OFFICER`
- **Content-Type:** `multipart/form-data`
- **Body:**
  - `request` (JSON string):
  ```json
  {
    "phoneNumber": "0912345678",
    "collateralType": "NHA",
    "collateralDescription": "Nhà 3 tầng tại Hà Nội",
    "paymentFrequency": "MONTHLY"
  }
  ```
  - `cccdFront` (File, optional)
  - `cccdBack` (File, optional)
  - `collateralDocuments` (File[], optional)

### 2.2. Phê duyệt khoản vay
- **Endpoint:** `POST /api/mortgage/approve`
- **Quyền:** `OFFICER`
- **Body:**
```json
{
  "mortgageId": 1,
  "approvedAmount": 1000000000,
  "interestRate": 8.5,
  "loanTermMonths": 60
}
```

### 2.3. Từ chối khoản vay
- **Endpoint:** `POST /api/mortgage/reject`
- **Quyền:** `OFFICER`
- **Body:**
```json
{
  "mortgageId": 1,
  "rejectionReason": "Không đủ điều kiện"
}
```

### 2.4. Lấy danh sách vay theo trạng thái
- **Endpoint:** `GET /api/mortgage/status/{status}`
- **Quyền:** `OFFICER`
- **Status:** `PENDING_APPRAISAL`, `APPROVED`, `ACTIVE`, `COMPLETED`, `REJECTED`
- **Response:** `List<MortgageAccountResponse>`

### 2.5. Lấy danh sách vay chờ thẩm định
- **Endpoint:** `GET /api/mortgage/pending`
- **Quyền:** `OFFICER`
- **Response:** `List<MortgageAccountResponse>`

### 2.6. Tìm kiếm vay theo trạng thái và số điện thoại
- **Endpoint:** `GET /api/mortgage/status/{status}/search?phone={phoneNumber}`
- **Quyền:** `OFFICER`
- **Response:** `List<MortgageAccountResponse>`

### 2.7. Lấy chi tiết khoản vay
- **Endpoint:** `GET /api/mortgage/{mortgageId}`
- **Quyền:** `CUSTOMER` hoặc `OFFICER`
- **Response:** `MortgageAccountResponse`

### 2.8. Lấy danh sách vay của một user
- **Endpoint:** `GET /api/mortgage/user/{userId}`
- **Quyền:** `CUSTOMER` hoặc `OFFICER`
- **Response:** `List<MortgageAccountResponse>`

### 2.9. Thanh toán khoản vay (Tất toán)
- **Endpoint:** `POST /api/mortgage/payment`
- **Quyền:** `CUSTOMER` hoặc `BANKING_OFFICER`
- **Body:**
```json
{
  "mortgageId": 1,
  "amount": 1000000000
}
```

### 2.10. Thanh toán kỳ hiện tại
- **Endpoint:** `POST /api/mortgage/payment/current`
- **Quyền:** `CUSTOMER` hoặc `OFFICER`
- **Body:**
```json
{
  "mortgageId": 1,
  "amount": 20000000
}
```

### 2.11. Lấy danh sách loại tài sản thế chấp
- **Endpoint:** `GET /api/mortgage/collateral-types`
- **Quyền:** Public
- **Response:**
```json
{
  "success": true,
  "data": [
    {
      "value": "NHA",
      "displayName": "Nhà ở"
    },
    ...
  ]
}
```

### 2.12. Lấy danh sách lãi suất vay
- **Endpoint:** `GET /api/mortgage/interest-rates`
- **Quyền:** Public
- **Response:**
```json
{
  "success": true,
  "data": [
    {
      "termMonths": 12,
      "interestRate": 8.5,
      ...
    }
  ]
}
```

---

## 3. Quản lý Tiết kiệm (Saving Management)

### 3.1. Cập nhật lãi suất kỳ hạn tiết kiệm
- **Endpoint:** `PUT /api/saving/terms/update-rate`
- **Quyền:** `OFFICER`
- **Body:**
```json
{
  "termType": "SIX_MONTHS",
  "interestRate": 6.5
}
```

### 3.2. Lấy danh sách kỳ hạn tiết kiệm
- **Endpoint:** `GET /api/saving/terms`
- **Quyền:** Public
- **Response:**
```json
{
  "success": true,
  "data": [
    {
      "termId": 1,
      "termType": "SIX_MONTHS",
      "months": 6,
      "displayName": "6 tháng",
      "interestRate": 6.5,
      ...
    }
  ]
}
```

### 3.3. Lấy danh sách tài khoản tiết kiệm
- **Endpoint:** `GET /api/saving/my-accounts`
- **Quyền:** `CUSTOMER` hoặc `OFFICER`
- **Response:** `List<SavingAccountResponse>`

### 3.4. Lấy chi tiết sổ tiết kiệm
- **Endpoint:** `GET /api/saving/{savingBookNumber}`
- **Quyền:** `CUSTOMER` hoặc `OFFICER`
- **Response:** `SavingAccountDetailResponse`

### 3.5. Tạo tài khoản tiết kiệm
- **Endpoint:** `POST /api/saving/create`
- **Quyền:** `CUSTOMER` hoặc `OFFICER`
- **Body:**
```json
{
  "senderAccountNumber": "CHK001",
  "amount": 10000000,
  "term": "SIX_MONTHS"
}
```

---

## 4. Quản lý Thanh toán (Payment Management)

### 4.1. Nạp tiền vào tài khoản checking
- **Endpoint:** `POST /api/payment/checking/deposit`
- **Quyền:** `OFFICER` hoặc `ADMIN`
- **Body:**
```json
{
  "accountNumber": "CHK001",
  "amount": 5000000,
  "description": "Nạp tiền mặt"
}
```

### 4.2. Rút tiền từ tài khoản checking
- **Endpoint:** `POST /api/payment/checking/withdraw`
- **Quyền:** `OFFICER` hoặc `ADMIN`
- **Body:**
```json
{
  "accountNumber": "CHK001",
  "amount": 2000000,
  "description": "Rút tiền mặt"
}
```

### 4.3. Chuyển tiền
- **Endpoint:** `POST /api/payment/transfer`
- **Quyền:** `CUSTOMER`, `OFFICER`, hoặc `ADMIN`
- **Body:**
```json
{
  "fromAccountNumber": "CHK001",
  "toAccountNumber": "CHK002",
  "amount": 1000000,
  "description": "Chuyển tiền"
}
```

### 4.4. Khởi tạo chuyển tiền với OTP
- **Endpoint:** `POST /api/payment/transfer/initiate`
- **Quyền:** `CUSTOMER`, `OFFICER`, hoặc `ADMIN`
- **Body:** `TransferRequest`
- **Response:** `OtpResponse` (chứa OTP và transaction code)

### 4.5. Xác nhận chuyển tiền với OTP
- **Endpoint:** `POST /api/payment/transfer/confirm`
- **Quyền:** `CUSTOMER`, `OFFICER`, hoặc `ADMIN`
- **Body:**
```json
{
  "transactionCode": "TXN123456",
  "otp": "123456"
}
```

---

## 5. Quản lý Giao dịch (Transaction Management)

### 5.1. Lấy tất cả giao dịch của một user
- **Endpoint:** `GET /api/transactions/user/{userId}`
- **Quyền:** `OFFICER`
- **Response:**
```json
{
  "success": true,
  "message": "Lấy lịch sử giao dịch thành công",
  "data": [
    {
      "transactionId": 1,
      "type": "TRANSFER",
      "amount": 1000000,
      "status": "SUCCESS",
      "createdAt": "2024-01-01T10:00:00",
      ...
    }
  ],
  "total": 10
}
```

### 5.2. Lấy giao dịch của user hiện tại
- **Endpoint:** `GET /api/transactions/my-transactions`
- **Quyền:** Authenticated
- **Response:** Tương tự như trên

### 5.3. Lấy chi tiết giao dịch nội bộ
- **Endpoint:** `GET /api/transactions/internal/{transactionId}`
- **Quyền:** Authenticated
- **Response:** `TransactionHistoryDTO`

### 5.4. Lấy chi tiết giao dịch ngoài ngân hàng
- **Endpoint:** `GET /api/transactions/external/{externalTransferId}`
- **Quyền:** Authenticated
- **Response:** `TransactionHistoryDTO`

### 5.5. Lấy giao dịch tiền vào
- **Endpoint:** `GET /api/transactions/incoming`
- **Quyền:** Authenticated
- **Response:** `List<TransactionHistoryDTO>`

### 5.6. Lấy giao dịch tiền ra
- **Endpoint:** `GET /api/transactions/outgoing`
- **Quyền:** Authenticated
- **Response:** `List<TransactionHistoryDTO>`

---

## 6. Quản lý Tài khoản (Account Management)

### 6.1. Lấy thông tin tài khoản checking
- **Endpoint:** `GET /api/accounts/{userId}/checking`
- **Quyền:** `OFFICER` hoặc chính user đó
- **Response:** `CheckingAccountInfoResponse`

### 6.2. Lấy thông tin tài khoản theo số tài khoản
- **Endpoint:** `GET /api/accounts/info/{accountNumber}`
- **Quyền:** `CUSTOMER`, `OFFICER`, hoặc `ADMIN`
- **Response:** `AccountInfoResponse`

### 6.3. Tạo mã QR VietQR
- **Endpoint:** `POST /api/accounts/checking/qr-code`
- **Quyền:** `CUSTOMER`, `OFFICER`, hoặc `ADMIN`
- **Body (optional):**
```json
{
  "amount": 100000,
  "description": "Thanh toan"
}
```
- **Response:** PNG image

---

## 7. API chung (Có thể dùng bởi OFFICER)

### 7.1. Đăng nhập
- **Endpoint:** `POST /api/auth/login`
- **Quyền:** Public
- **Body:**
```json
{
  "phone": "0912345678",
  "password": "password123"
}
```

### 7.2. Refresh token
- **Endpoint:** `POST /api/auth/refresh-token`
- **Quyền:** Authenticated
- **Body:**
```json
{
  "refreshToken": "refresh_token_here"
}
```

### 7.3. Kiểm tra fingerprint enabled
- **Endpoint:** `GET /api/auth/check-fingerprint-enabled?phone={phone}`
- **Quyền:** Public

---

## Tóm tắt theo chức năng Officer Dashboard

### Quick Actions:
1. **Chuyển tiền** → `POST /api/payment/transfer` hoặc `/api/payment/transfer/initiate`
2. **QR của tôi** → `POST /api/accounts/checking/qr-code`
3. **Vay nhanh** → `GET /api/mortgage/pending`, `POST /api/mortgage/create`
4. **Tìm Kiếm** → `GET /api/users`, `GET /api/users/by-phone/{phone}`, `GET /api/users/by-cccd/{cccd}`

### User Management:
- `GET /api/users` - Danh sách users
- `GET /api/users/{userId}` - Chi tiết user
- `PUT /api/users/{userId}` - Cập nhật user
- `PATCH /api/users/{userId}/lock` - Khóa/Mở khóa
- `POST /api/users/{userId}/update-photo` - Cập nhật ảnh

### Mortgage Management:
- `GET /api/mortgage/pending` - Danh sách chờ duyệt
- `GET /api/mortgage/status/{status}` - Lọc theo trạng thái
- `POST /api/mortgage/create` - Tạo khoản vay
- `POST /api/mortgage/approve` - Phê duyệt
- `POST /api/mortgage/reject` - Từ chối

### Saving Management:
- `PUT /api/saving/terms/update-rate` - Cập nhật lãi suất
- `GET /api/saving/terms` - Danh sách kỳ hạn

### Payment Management:
- `POST /api/payment/checking/deposit` - Nạp tiền
- `POST /api/payment/checking/withdraw` - Rút tiền

### Transaction Management:
- `GET /api/transactions/user/{userId}` - Xem giao dịch của user

---

## Lưu ý quan trọng:

1. **Authentication:** Tất cả API (trừ public) đều cần JWT token trong header
2. **Role-based access:** Một số API chỉ dành cho OFFICER, một số cho cả CUSTOMER và OFFICER
3. **Error handling:** Tất cả API trả về error với format:
```json
{
  "success": false,
  "message": "Error message"
}
```
4. **Success response:** Thường có format:
```json
{
  "success": true,
  "message": "Success message",
  "data": {...}
}
```


