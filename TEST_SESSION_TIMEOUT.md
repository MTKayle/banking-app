# Hướng Dẫn Test Session Timeout

## Chuẩn Bị
1. Build và cài đặt app lên thiết bị/emulator
2. Đảm bảo backend đang chạy (nếu cần)

## Test Case 1: Tắt App và Mở Lại

### Mục đích
Kiểm tra khi người dùng tắt app và mở lại, hiển thị popup yêu cầu đăng nhập lại

### Các bước thực hiện
1. **Mở app và đăng nhập**
   - Mở app Mobile Banking
   - Đăng nhập bằng tài khoản: `0901234567` / `123456`
   - Xác nhận đã vào màn hình Dashboard (màn hình xanh lá)

2. **Tắt app (không force close)**
   - Nhấn nút Home trên điện thoại
   - Hoặc nhấn nút Recent Apps và vuốt app ra ngoài
   - **KHÔNG** force close app từ Settings

3. **Mở lại app**
   - Mở lại app từ launcher hoặc Recent Apps
   
4. **Kiểm tra kết quả**
   - ✅ **PASS:** Popup "Phiên Làm Việc Hết Hạn" hiển thị ngay lập tức
   - ✅ **PASS:** Nội dung: "Phiên làm việc của bạn đã hết hạn vì lý do bảo mật. Vui lòng đăng nhập lại để tiếp tục sử dụng."
   - ✅ **PASS:** Có nút "Đăng Nhập Lại"
   - ✅ **PASS:** Không thể đóng popup bằng cách nhấn ngoài hoặc nút Back
   - ❌ **FAIL:** App vẫn ở màn hình Dashboard mà không hiển thị popup

5. **Thử thao tác trước khi nhấn nút**
   - Thử chạm vào màn hình phía sau popup
   - ✅ **PASS:** Không thể thao tác, popup vẫn hiển thị
   - ✅ **PASS:** Popup hiện lại nếu bị đóng (không nên xảy ra)

6. **Nhấn "Đăng Nhập Lại"**
   - Nhấn nút "Đăng Nhập Lại"
   - ✅ **PASS:** App quay về màn hình LoginActivity (activity_login_quick)
   - ✅ **PASS:** Hiển thị tên người dùng đã đăng nhập trước đó
   - ✅ **PASS:** Chỉ cần nhập mật khẩu để đăng nhập lại

### Ghi chú
- Nếu FAIL, kiểm tra Activity có kế thừa BaseActivity chưa
- Kiểm tra log trong Logcat để debug

---

## Test Case 2: Timeout 5 Phút (Không Thao Tác)

### Mục đích
Kiểm tra khi người dùng không thao tác quá 5 phút, hiển thị popup yêu cầu đăng nhập lại

### Các bước thực hiện
1. **Mở app và đăng nhập**
   - Mở app Mobile Banking
   - Đăng nhập bằng tài khoản: `0901234567` / `123456`
   - Xác nhận đã vào màn hình Dashboard

2. **Để app không thao tác**
   - Đặt điện thoại xuống
   - **KHÔNG** chạm vào màn hình
   - **KHÔNG** nhấn bất kỳ nút nào
   - Đợi 5 phút (300 giây)

3. **Kiểm tra kết quả sau 5 phút**
   - ✅ **PASS:** Session được đánh dấu hết hạn (không hiển thị popup ngay)
   - ✅ **PASS:** App vẫn hiển thị màn hình Dashboard

4. **Thử thao tác sau 5 phút**
   - Chạm vào màn hình hoặc nhấn nút bất kỳ
   - ✅ **PASS:** Popup "Phiên Làm Việc Hết Hạn" hiển thị ngay lập tức
   - ✅ **PASS:** Không thể thao tác, mọi touch event bị chặn
   - ❌ **FAIL:** App vẫn hoạt động bình thường

5. **Nhấn "Đăng Nhập Lại"**
   - Nhấn nút "Đăng Nhập Lại"
   - ✅ **PASS:** App quay về màn hình LoginActivity

### Ghi chú
- Để test nhanh hơn, có thể giảm `SESSION_TIMEOUT_MS` trong SessionManager.java xuống 30 giây
- Ví dụ: `private static final long SESSION_TIMEOUT_MS = 30 * 1000;` (30 giây)

---

## Test Case 3: Reset Timeout Khi Có Tương Tác

### Mục đích
Kiểm tra timeout được reset khi người dùng có tương tác với app

### Các bước thực hiện
1. **Mở app và đăng nhập**
   - Mở app Mobile Banking
   - Đăng nhập bằng tài khoản: `0901234567` / `123456`
   - Xác nhận đã vào màn hình Dashboard

2. **Đợi 4 phút**
   - Đặt điện thoại xuống
   - Đợi 4 phút (chưa đến 5 phút)

3. **Thao tác với app**
   - Chạm vào màn hình (scroll, click button, etc.)
   - Hoặc chuyển sang màn hình khác

4. **Đợi thêm 4 phút nữa**
   - Đặt điện thoại xuống
   - Đợi thêm 4 phút

5. **Kiểm tra kết quả**
   - ✅ **PASS:** App vẫn hoạt động bình thường (timeout đã được reset)
   - ❌ **FAIL:** App logout sau 5 phút kể từ lần đăng nhập

### Ghi chú
- Timeout được reset mỗi khi có tương tác (touch event)
- Tổng thời gian: 4 phút + 4 phút = 8 phút, nhưng app vẫn hoạt động vì đã reset ở phút thứ 4

---

## Test Case 4: Chuyển Màn Hình

### Mục đích
Kiểm tra popup hiển thị đúng khi chuyển giữa các màn hình sau khi session hết hạn

### Các bước thực hiện
1. **Mở app và đăng nhập**
   - Mở app Mobile Banking
   - Đăng nhập bằng tài khoản: `0901234567` / `123456`

2. **Chuyển qua các màn hình**
   - Dashboard → Transfer → Dashboard
   - Dashboard → Settings → Dashboard
   - Dashboard → Profile → Dashboard

3. **Đợi 5 phút ở bất kỳ màn hình nào**
   - Ví dụ: Ở màn hình Transfer
   - Không thao tác trong 5 phút

4. **Thử thao tác sau 5 phút**
   - Chạm vào màn hình hoặc nhấn nút
   - ✅ **PASS:** Popup "Phiên Làm Việc Hết Hạn" hiển thị
   - ✅ **PASS:** Không thể thao tác
   - ❌ **FAIL:** App vẫn hoạt động bình thường

5. **Nhấn "Đăng Nhập Lại"**
   - ✅ **PASS:** App quay về LoginActivity

---

## Test Case 5: Đăng Nhập Lại Sau Timeout

### Mục đích
Kiểm tra có thể đăng nhập lại bình thường sau khi bị timeout

### Các bước thực hiện
1. **Để app timeout**
   - Thực hiện Test Case 1 hoặc 2 để app bị logout

2. **Đăng nhập lại**
   - Nhập mật khẩu: `123456`
   - Nhấn "Đăng nhập"

3. **Kiểm tra kết quả**
   - ✅ **PASS:** Đăng nhập thành công và vào Dashboard
   - ✅ **PASS:** Session được reset, timeout timer bắt đầu lại
   - ❌ **FAIL:** Không thể đăng nhập hoặc có lỗi

---

## Test Case 6: Đăng Nhập Bằng Vân Tay Sau Timeout

### Mục đích
Kiểm tra đăng nhập bằng vân tay vẫn hoạt động sau khi bị timeout

### Điều kiện tiên quyết
- Đã bật chức năng đăng nhập bằng vân tay
- Thiết bị hỗ trợ vân tay

### Các bước thực hiện
1. **Để app timeout**
   - Thực hiện Test Case 1 hoặc 2 để app bị logout

2. **Đăng nhập bằng vân tay**
   - Nhấn icon vân tay trên màn hình login
   - Quét vân tay

3. **Kiểm tra kết quả**
   - ✅ **PASS:** Đăng nhập thành công bằng vân tay
   - ✅ **PASS:** Vào Dashboard bình thường
   - ❌ **FAIL:** Không thể đăng nhập bằng vân tay

---

## Debug Tips

### Xem Log trong Logcat
```
adb logcat | grep -i "session"
```

### Kiểm tra SharedPreferences
```
adb shell
run-as com.example.mobilebanking
cd shared_prefs
cat SessionPrefs.xml
```

### Thay đổi timeout để test nhanh
Trong `SessionManager.java`:
```java
// Đổi từ 5 phút thành 30 giây
private static final long SESSION_TIMEOUT_MS = 30 * 1000;
```

### Force logout để test
Thêm button test trong Dashboard:
```java
Button btnTestLogout = findViewById(R.id.btn_test_logout);
btnTestLogout.setOnClickListener(v -> {
    SessionManager.getInstance(this).logout(this);
});
```

---

## Kết Quả Mong Đợi

### Tất cả test case PASS
- ✅ App logout khi tắt và mở lại
- ✅ App logout sau 5 phút không thao tác
- ✅ Timeout được reset khi có tương tác
- ✅ Timeout hoạt động đúng trên mọi màn hình
- ✅ Có thể đăng nhập lại bình thường
- ✅ Đăng nhập bằng vân tay vẫn hoạt động

### Nếu có test case FAIL
1. Kiểm tra Activity có kế thừa BaseActivity chưa
2. Kiểm tra `shouldCheckSession()` có return đúng không
3. Xem log trong Logcat để debug
4. Kiểm tra SessionManager có được khởi tạo đúng không

---

## Checklist Hoàn Thành

- [ ] Test Case 1: Tắt app và mở lại - PASS
- [ ] Test Case 2: Timeout 5 phút - PASS
- [ ] Test Case 3: Reset timeout khi có tương tác - PASS
- [ ] Test Case 4: Chuyển màn hình - PASS
- [ ] Test Case 5: Đăng nhập lại sau timeout - PASS
- [ ] Test Case 6: Đăng nhập bằng vân tay sau timeout - PASS

Khi tất cả test case PASS, tính năng session timeout đã hoạt động đúng!


---

## Test Case 7: Thử Đóng Popup và Thao Tác

### Mục đích
Kiểm tra popup không thể đóng và chặn mọi tương tác khi session hết hạn

### Các bước thực hiện
1. **Để session hết hạn**
   - Thực hiện Test Case 1 hoặc 2 để popup hiển thị

2. **Thử đóng popup bằng nút Back**
   - Nhấn nút Back trên điện thoại
   - ✅ **PASS:** Popup không đóng, vẫn hiển thị
   - ❌ **FAIL:** Popup đóng

3. **Thử đóng popup bằng cách nhấn ngoài**
   - Chạm vào vùng tối phía sau popup
   - ✅ **PASS:** Popup không đóng, vẫn hiển thị
   - ❌ **FAIL:** Popup đóng

4. **Thử thao tác với app**
   - Cố gắng chạm vào các nút/element phía sau popup
   - ✅ **PASS:** Không thể thao tác, popup chặn mọi tương tác
   - ✅ **PASS:** Popup vẫn hiển thị
   - ❌ **FAIL:** Có thể thao tác với app

5. **Chỉ có thể đóng bằng nút "Đăng Nhập Lại"**
   - Nhấn nút "Đăng Nhập Lại"
   - ✅ **PASS:** Popup đóng và quay về LoginActivity
   - ❌ **FAIL:** Popup không đóng hoặc có lỗi

---

## Test Case 8: Popup Hiện Lại Khi Cố Thao Tác

### Mục đích
Kiểm tra popup hiện lại mỗi khi người dùng cố thao tác sau khi session hết hạn

### Điều kiện
Giả sử có cách đóng popup (không nên xảy ra, nhưng test để chắc chắn)

### Các bước thực hiện
1. **Để session hết hạn**
   - Thực hiện Test Case 2 để session hết hạn
   - Popup hiển thị

2. **Giả sử popup bị đóng (không nên xảy ra)**
   - Nếu có cách đóng popup (bug)

3. **Thử thao tác với app**
   - Chạm vào màn hình
   - Nhấn bất kỳ nút nào
   - Scroll màn hình

4. **Kiểm tra kết quả**
   - ✅ **PASS:** Popup hiện lại ngay lập tức
   - ✅ **PASS:** Không thể thao tác, event bị chặn
   - ❌ **FAIL:** App hoạt động bình thường mà không hiển thị popup

---

## Test Case 9: Nhiều Lần Thao Tác Sau Khi Hết Hạn

### Mục đích
Kiểm tra popup hoạt động ổn định khi người dùng thao tác nhiều lần

### Các bước thực hiện
1. **Để session hết hạn**
   - Thực hiện Test Case 2

2. **Thao tác nhiều lần**
   - Chạm vào màn hình 10 lần liên tiếp
   - Nhấn các nút khác nhau
   - Scroll màn hình

3. **Kiểm tra kết quả**
   - ✅ **PASS:** Popup vẫn hiển thị ổn định
   - ✅ **PASS:** Không bị crash hoặc lỗi
   - ✅ **PASS:** Mọi tương tác đều bị chặn
   - ❌ **FAIL:** App crash hoặc có lỗi

4. **Nhấn "Đăng Nhập Lại"**
   - ✅ **PASS:** Logout bình thường

---

## Kết Quả Mong Đợi (Cập Nhật)

### Tất cả test case PASS
- ✅ App hiển thị popup khi tắt và mở lại
- ✅ App hiển thị popup sau 5 phút không thao tác (khi có tương tác)
- ✅ Timeout được reset khi có tương tác (trước khi hết hạn)
- ✅ Popup hiển thị đúng trên mọi màn hình
- ✅ Có thể đăng nhập lại bình thường
- ✅ Đăng nhập bằng vân tay vẫn hoạt động
- ✅ **Popup không thể đóng bằng Back hoặc nhấn ngoài**
- ✅ **Popup chặn mọi tương tác với app**
- ✅ **Popup hiện lại khi người dùng cố thao tác**
- ✅ **Chỉ logout khi người dùng nhấn "Đăng Nhập Lại"**

---

## Checklist Hoàn Thành (Cập Nhật)

- [ ] Test Case 1: Tắt app và mở lại - Popup hiển thị - PASS
- [ ] Test Case 2: Timeout 5 phút - Popup hiển thị khi thao tác - PASS
- [ ] Test Case 3: Reset timeout khi có tương tác - PASS
- [ ] Test Case 4: Chuyển màn hình - Popup hiển thị - PASS
- [ ] Test Case 5: Đăng nhập lại sau timeout - PASS
- [ ] Test Case 6: Đăng nhập bằng vân tay sau timeout - PASS
- [ ] Test Case 7: Thử đóng popup và thao tác - Không thể đóng - PASS
- [ ] Test Case 8: Popup hiện lại khi cố thao tác - PASS
- [ ] Test Case 9: Nhiều lần thao tác sau khi hết hạn - Ổn định - PASS

Khi tất cả test case PASS, tính năng session timeout với popup đã hoạt động đúng!
