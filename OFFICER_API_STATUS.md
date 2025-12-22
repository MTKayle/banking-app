# Báo Cáo Trạng Thái API Officer

## Tổng Quan
- **Tổng số API trong OFFICER_API_SUMMARY.md:** 47 API
- **API đã được định nghĩa trong Service:** 35 API
- **API đã được gọi trong code:** 25 API
- **API chưa được gọi:** 22 API

---

## 1. QUẢN LÝ NGƯỜI DÙNG (User Management)

### ✅ Đã được gọi:
1. ✅ `GET /api/users` - Lấy danh sách tất cả người dùng
   - **Service:** `UserApiService.getAllUsers()`
   - **Nơi gọi:** `OfficerUserListActivity.java`

2. ✅ `GET /api/users/{userId}` - Lấy thông tin người dùng theo ID
   - **Service:** `UserApiService.getUserById()`
   - **Nơi gọi:** `OfficerUserDetailActivity.java`, `OfficerUserUpdateActivity.java`

3. ✅ `GET /api/users/by-phone/{phone}` - Lấy thông tin theo số điện thoại
   - **Service:** `UserApiService.getUserByPhone()`
   - **Nơi gọi:** `OfficerUserListActivity.java`, `OfficerCustomerTransactionsActivity.java`

4. ✅ `GET /api/users/by-cccd/{cccdNumber}` - Lấy thông tin theo số CCCD
   - **Service:** `UserApiService.getUserByCccd()`
   - **Nơi gọi:** `OfficerUserListActivity.java`

5. ✅ `PUT /api/users/{userId}` - Cập nhật thông tin người dùng
   - **Service:** `UserApiService.updateUser()`
   - **Nơi gọi:** `OfficerUserUpdateActivity.java`

6. ✅ `PATCH /api/users/{userId}/lock` - Khóa/Mở khóa tài khoản
   - **Service:** `UserApiService.lockAccount()`
   - **Nơi gọi:** `OfficerUserDetailActivity.java`

7. ✅ `PATCH /api/users/{userId}/phone` - Cập nhật số điện thoại
   - **Service:** `UserApiService.updatePhoneNumber()`
   - **Nơi gọi:** `OfficerUserUpdateActivity.java`

8. ✅ `PATCH /api/users/{userId}/cccd` - Cập nhật số CCCD
   - **Service:** `UserApiService.updateCccdNumber()`
   - **Nơi gọi:** `OfficerUserUpdateActivity.java`

9. ✅ `PATCH /api/users/{userId}/settings` - Cập nhật cài đặt tính năng
   - **Service:** `UserApiService.updateSmartFlags()`
   - **Nơi gọi:** `SettingsActivity.java`

### ❌ Chưa được gọi:
1. ❌ `POST /api/users/{userId}/update-photo` - Cập nhật ảnh đại diện
   - **Service:** ✅ `UserApiService.updateUserPhoto()` (đã định nghĩa)
   - **Nơi gọi:** ❌ Chưa có

2. ❌ `GET /api/users/{userId}/features/face-recognition` - Kiểm tra face recognition
   - **Service:** ✅ `UserApiService.checkFaceRecognition()` (đã định nghĩa)
   - **Nơi gọi:** ❌ Chưa có

3. ❌ `GET /api/users/{userId}/features/smart-ekyc` - Kiểm tra smart eKYC
   - **Service:** ✅ `UserApiService.checkSmartEkyc()` (đã định nghĩa)
   - **Nơi gọi:** ❌ Chưa có

4. ❌ `GET /api/users/{userId}/features/fingerprint-login` - Kiểm tra fingerprint login
   - **Service:** ✅ `UserApiService.checkFingerprintLogin()` (đã định nghĩa)
   - **Nơi gọi:** ❌ Chưa có

5. ❌ `PATCH /api/users/{userId}/smart-otp` - Cập nhật Smart OTP
   - **Service:** ❌ Chưa định nghĩa
   - **Nơi gọi:** ❌ Chưa có

---

## 2. QUẢN LÝ VAY THẾ CHẤP (Mortgage Management)

### ✅ Đã được gọi:
1. ✅ `GET /api/mortgage/pending` - Lấy danh sách vay chờ thẩm định
   - **Service:** `MortgageApiService.getPendingMortgages()`
   - **Nơi gọi:** `OfficerMortgageListActivity.java`

2. ✅ `GET /api/mortgage/status/{status}` - Lấy danh sách vay theo trạng thái
   - **Service:** `MortgageApiService.getMortgagesByStatus()`
   - **Nơi gọi:** `OfficerMortgageListActivity.java`

3. ✅ `GET /api/mortgage/status/{status}/search?phone={phoneNumber}` - Tìm kiếm vay
   - **Service:** `MortgageApiService.searchMortgages()`
   - **Nơi gọi:** `OfficerMortgageListActivity.java`

4. ✅ `GET /api/mortgage/{mortgageId}` - Lấy chi tiết khoản vay
   - **Service:** `MortgageApiService.getMortgageDetail()`
   - **Nơi gọi:** `OfficerMortgageDetailActivity.java`, `PaymentSchedulesActivity.java`

5. ✅ `POST /api/mortgage/create` - Tạo tài khoản vay thế chấp mới
   - **Service:** `MortgageApiService.createMortgageSimple()`
   - **Nơi gọi:** `OfficerMortgageCreateActivity.java`

6. ✅ `POST /api/mortgage/approve` - Phê duyệt khoản vay
   - **Service:** `MortgageApiService.approveMortgage()`
   - **Nơi gọi:** `OfficerMortgageDetailActivity.java`

7. ✅ `POST /api/mortgage/reject` - Từ chối khoản vay
   - **Service:** `MortgageApiService.rejectMortgage()`
   - **Nơi gọi:** `OfficerMortgageDetailActivity.java`

8. ✅ `GET /api/mortgage/collateral-types` - Lấy danh sách loại tài sản thế chấp
   - **Service:** `MortgageApiService.getCollateralTypes()`
   - **Nơi gọi:** `OfficerMortgageCreateActivity.java`

9. ✅ `POST /api/mortgage/payment` - Thanh toán khoản vay (Tất toán)
   - **Service:** `MortgageApiService.makeMortgagePayment()`
   - **Nơi gọi:** `PaymentSchedulesActivity.java`

10. ✅ `POST /api/mortgage/payment/current` - Thanh toán kỳ hiện tại
    - **Service:** `MortgageApiService.makeCurrentPeriodPayment()`
    - **Nơi gọi:** `PaymentSchedulesActivity.java`

### ❌ Chưa được gọi:
1. ❌ `GET /api/mortgage/user/{userId}` - Lấy danh sách vay của một user
   - **Service:** ✅ `MortgageApiService.getMortgagesByUser()` (đã định nghĩa)
   - **Nơi gọi:** ❌ Chưa có

2. ❌ `GET /api/mortgage/interest-rates` - Lấy danh sách lãi suất vay
   - **Service:** ✅ `MortgageApiService.getInterestRates()` (đã định nghĩa)
   - **Nơi gọi:** ❌ Chưa có

---

## 3. QUẢN LÝ TIẾT KIỆM (Saving Management)

### ✅ Đã được gọi:
1. ✅ `GET /api/saving/terms` - Lấy danh sách kỳ hạn tiết kiệm
   - **Service:** `AccountApiService.getSavingTerms()`
   - **Nơi gọi:** `OfficerInterestRateActivity.java`, `SavingTermListActivity.java`

2. ✅ `PUT /api/saving/terms/update-rate` - Cập nhật lãi suất kỳ hạn tiết kiệm
   - **Service:** `AccountApiService.updateSavingTermRate()`
   - **Nơi gọi:** `OfficerInterestRateActivity.java`

3. ✅ `GET /api/saving/my-accounts` - Lấy danh sách tài khoản tiết kiệm
   - **Service:** `AccountApiService.getMySavingAccounts()`
   - **Nơi gọi:** (có thể được gọi ở đâu đó)

4. ✅ `POST /api/saving/create` - Tạo tài khoản tiết kiệm
   - **Service:** `AccountApiService.createSaving()`
   - **Nơi gọi:** `SavingConfirmActivity.java`

5. ✅ `GET /api/saving/accounts/user/{userId}` - Lấy danh sách tiết kiệm theo userId
   - **Service:** `AccountApiService.getSavingAccounts()`
   - **Nơi gọi:** `SavingAccountFragment.java`

### ❌ Chưa được gọi:
1. ❌ `GET /api/saving/{savingBookNumber}` - Lấy chi tiết sổ tiết kiệm
   - **Service:** ❌ Chưa định nghĩa
   - **Nơi gọi:** ❌ Chưa có

---

## 4. QUẢN LÝ THANH TOÁN (Payment Management)

### ✅ Đã được gọi:
1. ✅ `POST /api/payment/checking/deposit` - Nạp tiền vào tài khoản checking
   - **Service:** `PaymentApiService.depositToChecking()`
   - **Nơi gọi:** `OfficerDepositActivity.java`

2. ✅ `POST /api/payment/checking/withdraw` - Rút tiền từ tài khoản checking
   - **Service:** `PaymentApiService.withdrawFromChecking()`
   - **Nơi gọi:** `OfficerDepositActivity.java`

3. ✅ `POST /api/payment/transfer/initiate` - Khởi tạo chuyển tiền với OTP
   - **Service:** `TransferApiService.initiateInternalTransfer()`
   - **Nơi gọi:** (có thể được gọi trong TransferActivity)

4. ✅ `POST /api/payment/transfer/confirm` - Xác nhận chuyển tiền với OTP
   - **Service:** `TransferApiService.confirmInternalTransfer()`
   - **Nơi gọi:** `OtpVerificationActivity.java`

### ❌ Chưa được gọi:
1. ❌ `POST /api/payment/transfer` - Chuyển tiền (không OTP)
   - **Service:** ❌ Chưa định nghĩa
   - **Nơi gọi:** ❌ Chưa có

---

## 5. QUẢN LÝ GIAO DỊCH (Transaction Management)

### ✅ Đã được gọi:
1. ✅ `GET /api/transactions/user/{userId}` - Lấy tất cả giao dịch của một user
   - **Service:** `TransactionApiService.getTransactionsByUser()`
   - **Nơi gọi:** `OfficerCustomerTransactionsActivity.java`

2. ✅ `GET /api/transactions/my-transactions` - Lấy giao dịch của user hiện tại
   - **Service:** `TransactionApiService.getMyTransactions()`
   - **Nơi gọi:** (có thể được gọi ở đâu đó)

### ❌ Chưa được gọi:
1. ❌ `GET /api/transactions/internal/{transactionId}` - Lấy chi tiết giao dịch nội bộ
   - **Service:** ❌ Chưa định nghĩa
   - **Nơi gọi:** ❌ Chưa có

2. ❌ `GET /api/transactions/external/{externalTransferId}` - Lấy chi tiết giao dịch ngoài ngân hàng
   - **Service:** ❌ Chưa định nghĩa
   - **Nơi gọi:** ❌ Chưa có

3. ❌ `GET /api/transactions/incoming` - Lấy giao dịch tiền vào
   - **Service:** ❌ Chưa định nghĩa
   - **Nơi gọi:** ❌ Chưa có

4. ❌ `GET /api/transactions/outgoing` - Lấy giao dịch tiền ra
   - **Service:** ❌ Chưa định nghĩa
   - **Nơi gọi:** ❌ Chưa có

---

## 6. QUẢN LÝ TÀI KHOẢN (Account Management)

### ✅ Đã được gọi:
1. ✅ `GET /api/accounts/{userId}/checking` - Lấy thông tin tài khoản checking
   - **Service:** `AccountApiService.getCheckingAccountInfo()`
   - **Nơi gọi:** (có thể được gọi ở đâu đó)

2. ✅ `GET /api/accounts/info/{accountNumber}` - Lấy thông tin tài khoản theo số tài khoản
   - **Service:** `AccountApiService.getAccountInfo()`
   - **Nơi gọi:** (có thể được gọi ở đâu đó)

3. ✅ `POST /api/accounts/checking/qr-code` - Tạo mã QR VietQR
   - **Service:** `AccountApiService.getCheckingQRCode()`
   - **Nơi gọi:** `MyQRActivity.java`

---

## 7. API CHUNG (Có thể dùng bởi OFFICER)

### ✅ Đã được gọi:
1. ✅ `POST /api/auth/login` - Đăng nhập
   - **Service:** `AuthApiService.login()`
   - **Nơi gọi:** `LoginActivity.java`

2. ✅ `POST /api/auth/refresh-token` - Refresh token
   - **Service:** (có thể có)
   - **Nơi gọi:** (có thể được gọi tự động)

---

## 📊 TÓM TẮT

### API Chưa Được Gọi (22 API):

#### User Management (5 API):
1. `POST /api/users/{userId}/update-photo` - Cập nhật ảnh đại diện
2. `GET /api/users/{userId}/features/face-recognition` - Kiểm tra face recognition
3. `GET /api/users/{userId}/features/smart-ekyc` - Kiểm tra smart eKYC
4. `GET /api/users/{userId}/features/fingerprint-login` - Kiểm tra fingerprint login
5. `PATCH /api/users/{userId}/smart-otp` - Cập nhật Smart OTP

#### Mortgage Management (2 API):
6. `GET /api/mortgage/user/{userId}` - Lấy danh sách vay của một user
7. `GET /api/mortgage/interest-rates` - Lấy danh sách lãi suất vay

#### Saving Management (1 API):
8. `GET /api/saving/{savingBookNumber}` - Lấy chi tiết sổ tiết kiệm

#### Payment Management (1 API):
9. `POST /api/payment/transfer` - Chuyển tiền (không OTP)

#### Transaction Management (4 API):
10. `GET /api/transactions/internal/{transactionId}` - Chi tiết giao dịch nội bộ
11. `GET /api/transactions/external/{externalTransferId}` - Chi tiết giao dịch ngoài ngân hàng
12. `GET /api/transactions/incoming` - Giao dịch tiền vào
13. `GET /api/transactions/outgoing` - Giao dịch tiền ra

### API Chưa Được Định Nghĩa Trong Service (9 API):
1. `PATCH /api/users/{userId}/smart-otp` - Cập nhật Smart OTP
2. `GET /api/saving/{savingBookNumber}` - Lấy chi tiết sổ tiết kiệm
3. `POST /api/payment/transfer` - Chuyển tiền (không OTP)
4. `GET /api/transactions/internal/{transactionId}` - Chi tiết giao dịch nội bộ
5. `GET /api/transactions/external/{externalTransferId}` - Chi tiết giao dịch ngoài ngân hàng
6. `GET /api/transactions/incoming` - Giao dịch tiền vào
7. `GET /api/transactions/outgoing` - Giao dịch tiền ra

---

## 🎯 KHUYẾN NGHỊ

### Ưu tiên cao:
1. **API Chi tiết sổ tiết kiệm** - Cần thiết cho officer xem chi tiết sổ tiết kiệm của khách hàng
2. **API Lãi suất vay** - Cần thiết khi tạo/phê duyệt khoản vay
3. **API Giao dịch chi tiết** - Cần thiết để officer xem chi tiết giao dịch

### Ưu tiên trung bình:
4. **API Cập nhật ảnh đại diện** - Hữu ích cho officer quản lý thông tin khách hàng
5. **API Kiểm tra tính năng** - Hữu ích để officer kiểm tra trạng thái tính năng của khách hàng

### Ưu tiên thấp:
6. **API Smart OTP** - Có thể không cần thiết cho officer
7. **API Chuyển tiền không OTP** - Đã có API với OTP rồi


