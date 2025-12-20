# Hướng Dẫn Triển Khai Session Timeout

## Tổng Quan
Đã triển khai hệ thống quản lý session timeout với 2 tính năng chính:
1. **Khi tắt app và mở lại** → Hiển thị popup yêu cầu đăng nhập lại
2. **Không thao tác quá 5 phút** → Hiển thị popup yêu cầu đăng nhập lại

## Đặc Điểm Popup

### Thông Báo
- **Tiêu đề:** "Phiên Làm Việc Hết Hạn"
- **Nội dung:** "Phiên làm việc của bạn đã hết hạn vì lý do bảo mật. Vui lòng đăng nhập lại để tiếp tục sử dụng."
- **Không nói cụ thể:** Không đề cập đến "5 phút" hay lý do cụ thể

### Hành Vi
- ✅ **Có nút xác nhận:** "Đăng Nhập Lại"
- ✅ **Không thể đóng:** Không cho phép đóng bằng cách nhấn ngoài dialog hoặc nút Back
- ✅ **Chặn tương tác:** Khi popup hiển thị, mọi tương tác với app đều bị chặn
- ✅ **Hiện lại khi click:** Nếu người dùng cố gắng thao tác, popup sẽ hiện lại
- ✅ **Chỉ logout khi xác nhận:** Chỉ khi người dùng nhấn "Đăng Nhập Lại" mới logout

## Các File Đã Tạo

### 1. SessionManager.java
**Đường dẫn:** `app/src/main/java/com/example/mobilebanking/utils/SessionManager.java`

**Chức năng:**
- Theo dõi thời gian activity cuối cùng
- Kiểm tra session có hết hạn không (5 phút timeout)
- Tự động logout khi timeout
- Đánh dấu app đang foreground/background

**Các method quan trọng:**
- `isSessionExpired()` - Kiểm tra session hết hạn
- `updateLastActivityTime()` - Cập nhật thời gian activity
- `startTimeoutTimer(Activity)` - Bắt đầu đếm ngược 5 phút
- `stopTimeoutTimer()` - Dừng timer
- `logout(Activity)` - Logout và quay về LoginActivity
- `onLoginSuccess()` - Reset session khi đăng nhập thành công
- `markSessionExpired()` - Đánh dấu session đã hết hạn
- `isSessionExpiredDialogShowing()` - Kiểm tra dialog có đang hiển thị không
- `setSessionExpiredDialogShowing(boolean)` - Set trạng thái dialog

### 2. BaseActivity.java
**Đường dẫn:** `app/src/main/java/com/example/mobilebanking/activities/BaseActivity.java`

**Chức năng:**
- Activity cơ sở cho tất cả các Activity trong app
- Tự động kiểm tra session timeout trong `onResume()`
- Theo dõi user interaction trong `dispatchTouchEvent()`
- Hiển thị popup khi session hết hạn
- Chặn mọi tương tác khi session hết hạn

**Lifecycle:**
```
onCreate() → Khởi tạo SessionManager
onResume() → Kiểm tra session + Hiển thị popup nếu hết hạn + Start timeout timer
onPause() → Đánh dấu background + Stop timer + Đóng dialog
dispatchTouchEvent() → 
  - Nếu session hết hạn: Hiển thị popup + Chặn event
  - Nếu session còn: Reset timeout timer
onDestroy() → Đóng dialog nếu đang hiển thị
```

## Các Activity Đã Cập Nhật

### ✅ Đã cập nhật kế thừa BaseActivity:
1. **LoginActivity** - Override `shouldCheckSession()` return false (không kiểm tra session)
2. **CustomerDashboardActivity** - Kế thừa BaseActivity
3. **TransferActivity** - Kế thừa BaseActivity
4. **OfficerDashboardActivity** - Kế thừa BaseActivity
5. **ProfileActivity** - Kế thừa BaseActivity
6. **SettingsActivity** - Kế thừa BaseActivity

### ⚠️ Cần cập nhật các Activity còn lại:

Tất cả các Activity sau cần thay đổi từ:
```java
public class XxxActivity extends AppCompatActivity {
```

Thành:
```java
public class XxxActivity extends BaseActivity {
```

**Danh sách Activity cần cập nhật:**

#### Activity quan trọng (nên cập nhật):
- [ ] TransactionConfirmationActivity
- [ ] TransferSuccessActivity
- [ ] TransactionHistoryActivity
- [ ] AccountDetailActivity
- [ ] BillPaymentActivity
- [ ] MobileTopUpActivity
- [ ] ServicesActivity
- [ ] BranchLocatorActivity
- [ ] QrScannerActivity
- [ ] TicketBookingActivity
- [ ] MovieListActivity
- [ ] MovieDetailActivity
- [ ] SelectShowtimeActivity
- [ ] SeatSelectionActivity
- [ ] MoviePaymentActivity
- [ ] MovieTicketSuccessActivity
- [ ] MyTicketsActivity
- [ ] TicketDetailActivity

#### Activity đăng ký/xác thực (có thể bỏ qua kiểm tra session):
- [ ] RegisterActivity - Override `shouldCheckSession()` return false
- [ ] MainRegistrationActivity - Override `shouldCheckSession()` return false
- [ ] OtpVerificationActivity - Override `shouldCheckSession()` return false
- [ ] ForgotPasswordActivity - Override `shouldCheckSession()` return false
- [ ] ForgotOtpVerificationActivity - Override `shouldCheckSession()` return false
- [ ] ResetPasswordActivity - Override `shouldCheckSession()` return false
- [ ] BiometricAuthActivity - Override `shouldCheckSession()` return false
- [ ] WelcomeBannerActivity - Override `shouldCheckSession()` return false

#### Activity camera/scanner (có thể bỏ qua):
- [ ] CccdAutoScannerActivity
- [ ] CccdBackScannerActivity
- [ ] CccdCaptureActivity
- [ ] DataPreviewActivity

## Cách Cập Nhật Activity

### Bước 1: Thay đổi extends
```java
// Trước
public class YourActivity extends AppCompatActivity {

// Sau
public class YourActivity extends BaseActivity {
```

### Bước 2: (Tùy chọn) Bỏ qua kiểm tra session
Nếu Activity không cần kiểm tra session (như màn hình đăng ký), thêm method:
```java
@Override
protected boolean shouldCheckSession() {
    return false;
}
```

## Cách Test

### Test 1: Tắt app và mở lại
1. Đăng nhập vào app
2. Vào màn hình Dashboard
3. Nhấn nút Home để thoát app (không force close)
4. Mở lại app
5. **Kết quả mong đợi:** App tự động logout và quay về màn hình LoginActivity (activity_login_quick)

### Test 2: Timeout 5 phút
1. Đăng nhập vào app
2. Vào màn hình Dashboard
3. Không chạm vào màn hình trong 5 phút
4. **Kết quả mong đợi:** App tự động logout và quay về màn hình LoginActivity

### Test 3: Reset timeout khi có tương tác
1. Đăng nhập vào app
2. Vào màn hình Dashboard
3. Đợi 4 phút
4. Chạm vào màn hình (scroll, click button, etc.)
5. Đợi thêm 4 phút nữa
6. **Kết quả mong đợi:** App vẫn hoạt động bình thường (timeout đã được reset)

## Lưu Ý Quan Trọng

1. **LoginActivity không kiểm tra session** - Đã override `shouldCheckSession()` return false
2. **Session được reset khi đăng nhập thành công** - Gọi `sessionManager.onLoginSuccess()` trong LoginActivity
3. **Mọi tương tác đều reset timeout** - `dispatchTouchEvent()` trong BaseActivity tự động xử lý
4. **App background = session hết hạn** - Khi mở lại app từ background, phải đăng nhập lại

## Troubleshooting

### Vấn đề: App không logout khi tắt và mở lại
- Kiểm tra Activity có kế thừa BaseActivity chưa
- Kiểm tra `shouldCheckSession()` có return true không

### Vấn đề: App logout quá nhanh
- Kiểm tra `SESSION_TIMEOUT_MS` trong SessionManager.java
- Hiện tại đang set 5 phút (300,000 ms)

### Vấn đề: App không logout sau 5 phút
- Kiểm tra `startTimeoutTimer()` có được gọi trong `onResume()` không
- Kiểm tra `dispatchTouchEvent()` có reset timer đúng không

## Tùy Chỉnh Timeout

Để thay đổi thời gian timeout, sửa trong `SessionManager.java`:
```java
// Timeout: 5 phút = 300,000 milliseconds
private static final long SESSION_TIMEOUT_MS = 5 * 60 * 1000;

// Ví dụ: Đổi thành 10 phút
private static final long SESSION_TIMEOUT_MS = 10 * 60 * 1000;
```

## Kết Luận

Hệ thống session timeout đã được triển khai thành công với:
- ✅ SessionManager để quản lý session
- ✅ BaseActivity để tự động kiểm tra và xử lý timeout
- ✅ LoginActivity đã tích hợp reset session
- ✅ Một số Activity quan trọng đã cập nhật

Cần cập nhật các Activity còn lại để hoàn thiện hệ thống.


## Cách Hoạt Động Chi Tiết

### 1. Khi tắt app và mở lại
```
User tắt app → onPause() → Đánh dấu KEY_APP_IN_BACKGROUND = true
User mở lại app → onResume() → Kiểm tra isSessionExpired()
→ KEY_APP_IN_BACKGROUND = true → markSessionExpired()
→ Hiển thị popup "Phiên Làm Việc Hết Hạn"
→ Chặn mọi tương tác (dispatchTouchEvent return true)
User nhấn "Đăng Nhập Lại" → logout() → Quay về LoginActivity
```

### 2. Không thao tác quá 5 phút
```
User đăng nhập → startTimeoutTimer() → Đếm ngược 5 phút
5 phút trôi qua → timeoutRunnable chạy → markSessionExpired()
User chạm màn hình → dispatchTouchEvent() → Kiểm tra isSessionExpired()
→ Session đã hết hạn → Hiển thị popup
→ Chặn event (return true)
User nhấn "Đăng Nhập Lại" → logout() → Quay về LoginActivity
```

### 3. Khi có tương tác (session còn)
```
User chạm màn hình → dispatchTouchEvent()
→ Kiểm tra isSessionExpired() → false (session còn)
→ updateLastActivityTime() → Reset thời gian
→ startTimeoutTimer() → Đếm lại từ đầu (5 phút mới)
→ super.dispatchTouchEvent() → Xử lý event bình thường
```

### 4. Khi người dùng cố thao tác sau khi session hết hạn
```
Session đã hết hạn → Popup đang hiển thị
User nhấn nút Back → Không đóng được (setCancelable(false))
User nhấn ngoài dialog → Không đóng được (setCancelable(false))
User chạm vào màn hình → dispatchTouchEvent()
→ Kiểm tra isSessionExpired() → true
→ showSessionExpiredDialog() → Hiển thị lại popup (nếu chưa hiển thị)
→ return true → Chặn event, không cho tương tác
```

### 5. Luồng đăng nhập lại
```
User nhấn "Đăng Nhập Lại" trong popup
→ setSessionExpiredDialogShowing(false)
→ logout() → Clear session data
→ Intent → LoginActivity (FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK)
→ finish() → Đóng Activity hiện tại
→ LoginActivity hiển thị activity_login_quick với tên người dùng
User nhập mật khẩu → Đăng nhập thành công
→ onLoginSuccess() → Reset session
→ clearSessionExpired() → Xóa flag hết hạn
→ Vào Dashboard bình thường
```
