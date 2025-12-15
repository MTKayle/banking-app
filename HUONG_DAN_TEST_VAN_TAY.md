# 🔐 HƯỚNG DẪN TEST CHỨC NĂNG ĐĂNG NHẬP BẰNG VÂN TAY

## 📋 Yêu cầu trước khi test

1. ✅ Thiết bị/Emulator phải hỗ trợ vân tay (Fingerprint)
2. ✅ Đã cài đặt vân tay trên thiết bị/emulator
3. ✅ App đã được build và cài đặt thành công

---

## 🚀 BƯỚC 1: Đăng nhập vào app

### 1.1. Mở app
- Mở ứng dụng Mobile Banking trên thiết bị/emulator

### 1.2. Đăng nhập bằng mật khẩu
- **Username**: `customer1`
- **Password**: `123456`
- Bấm nút **"Đăng nhập"**

### 1.3. Kiểm tra
- ✅ Đăng nhập thành công
- ✅ Vào màn hình Dashboard (Trang Chủ)
- ✅ Thấy thông tin: "Xin chào, customer1"

---

## ⚙️ BƯỚC 2: Bật chức năng đăng nhập bằng vân tay

### 2.1. Vào màn hình Profile
- Từ Dashboard, bấm vào **menu** (3 chấm ở góc trên bên phải)
- Chọn **"Profile"** hoặc **"Cài đặt"**

### 2.2. Tìm phần "Cài đặt bảo mật"
- Cuộn xuống để tìm phần **"Cài đặt bảo mật"**
- Bạn sẽ thấy:
  - Tiêu đề: "Đăng nhập bằng vân tay"
  - Mô tả: "Sử dụng vân tay để đăng nhập nhanh chóng và an toàn"
  - **Switch** (nút bật/tắt) ở bên phải

### 2.3. Bật Switch
- Bấm vào **Switch** để bật chức năng vân tay
- Android sẽ hiển thị popup **BiometricPrompt** yêu cầu quét vân tay

### 2.4. Quét vân tay
- **Tiêu đề**: "Xác thực dấu vân tay để bật tính năng đăng nhập sinh trắc học"
- **Mô tả**: "Sử dụng vân tay của bạn để xác thực"
- Quét vân tay của bạn trên cảm biến vân tay

### 2.5. Kết quả
- ✅ Nếu quét thành công:
  - Switch sẽ chuyển sang trạng thái **BẬT** (màu xanh)
  - Hiển thị Toast: **"Đã bật đăng nhập bằng vân tay"**
  - Sau đó sẽ tự động yêu cầu quét vân tay lại để lưu refresh token
  - Hiển thị Toast: **"Đã lưu thông tin đăng nhập"**

- ❌ Nếu quét thất bại:
  - Switch sẽ quay về trạng thái **TẮT**
  - Hiển thị thông báo lỗi
  - Thử lại từ bước 2.3

---

## 🚪 BƯỚC 3: Thoát app (QUAN TRỌNG)

### ⚠️ LƯU Ý QUAN TRỌNG:
Có 2 cách thoát app, mỗi cách có kết quả khác nhau:

### 3.1. Cách 1: Kill app (Đóng app, KHÔNG logout)
- **Cách làm**: 
  - Bấm nút **Home** (nút tròn ở giữa) để về màn hình chính
  - Hoặc vuốt app ra khỏi danh sách app gần đây
  - **KHÔNG** bấm nút Logout trong app

- **Kết quả**: 
  - ✅ Refresh token vẫn được lưu trong Keystore
  - ✅ Có thể đăng nhập bằng vân tay lần sau

### 3.2. Cách 2: Logout (Đăng xuất)
- **Cách làm**:
  - Vào Profile → Bấm nút **"Đăng Xuất"**
  - Xác nhận đăng xuất

- **Kết quả**:
  - ❌ Tất cả token bị xóa
  - ❌ Phải đăng nhập lại bằng mật khẩu lần sau

### 📝 Khuyến nghị cho test:
**Dùng Cách 1 (Kill app)** để test chức năng đăng nhập bằng vân tay.

---

## 🔓 BƯỚC 4: Test đăng nhập bằng vân tay

### 4.1. Mở lại app
- Mở ứng dụng Mobile Banking
- Bạn sẽ thấy màn hình **Login**

### 4.2. Kiểm tra nút "Đăng nhập bằng vân tay"
- Tìm nút **"Đăng nhập bằng vân tay"** (có icon vân tay)
- Nút này nằm dưới nút "Đăng nhập" chính
- ✅ Nếu thấy nút → Thiết bị hỗ trợ vân tay
- ❌ Nếu không thấy nút → Thiết bị không hỗ trợ vân tay

### 4.3. Bấm nút "Đăng nhập bằng vân tay"
- Bấm vào nút **"Đăng nhập bằng vân tay"**

### 4.4. Kiểm tra các trường hợp

#### ✅ Trường hợp 1: Đã bật vân tay và có refresh token
- Android sẽ hiển thị popup **BiometricPrompt**
- **Tiêu đề**: "Xác thực dấu vân tay để đăng nhập"
- **Mô tả**: "Sử dụng vân tay của bạn để xác thực"
- Quét vân tay
- ✅ **Kết quả**: 
  - Đăng nhập thành công
  - Vào Dashboard
  - Hiển thị Toast: **"Đăng nhập bằng vân tay thành công!"**
  - Sau đó sẽ yêu cầu quét vân tay lại để lưu refresh token mới

#### ❌ Trường hợp 2: Chưa bật chức năng vân tay
- Hiển thị Dialog:
  - **Tiêu đề**: "Chưa bật đăng nhập bằng vân tay"
  - **Nội dung**: "Bạn chưa bật chức năng đăng nhập bằng vân tay. Vui lòng vào Cài đặt để bật tính năng này, hoặc đăng nhập bằng mật khẩu."
  - **Nút**: "Đăng nhập bằng mật khẩu" hoặc "Hủy"
- ✅ **Giải pháp**: Quay lại Bước 2 để bật chức năng

#### ❌ Trường hợp 3: Chưa có refresh token (chưa đăng nhập lần nào)
- Hiển thị Dialog:
  - **Tiêu đề**: "Chưa có thông tin đăng nhập"
  - **Nội dung**: "Bạn chưa đăng nhập trên thiết bị này. Vui lòng đăng nhập bằng mật khẩu lần đầu."
  - **Nút**: "Đăng nhập bằng mật khẩu" hoặc "Hủy"
- ✅ **Giải pháp**: Đăng nhập bằng mật khẩu trước (Bước 1)

#### ❌ Trường hợp 4: Refresh token đã hết hạn (sau 7 ngày)
- Hiển thị Dialog:
  - **Tiêu đề**: "Token đã hết hạn"
  - **Nội dung**: "Token đã hết hạn. Vui lòng đăng nhập bằng mật khẩu."
- ✅ **Giải pháp**: Đăng nhập lại bằng mật khẩu

#### ❌ Trường hợp 5: Quét vân tay thất bại
- Hiển thị Toast: **"Vân tay không khớp"** hoặc **"Xác thực thất bại"**
- ✅ **Giải pháp**: Thử quét lại

---

## 🧪 TEST CASE ĐẦY ĐỦ

### Test Case 1: Luồng hoàn chỉnh
1. ✅ Đăng nhập bằng mật khẩu: `customer1` / `123456`
2. ✅ Vào Profile → Bật chức năng vân tay
3. ✅ Quét vân tay để xác thực
4. ✅ Kill app (không logout)
5. ✅ Mở lại app
6. ✅ Bấm "Đăng nhập bằng vân tay"
7. ✅ Quét vân tay
8. ✅ Đăng nhập thành công

### Test Case 2: Test khi chưa bật vân tay
1. ✅ Đăng nhập bằng mật khẩu
2. ✅ Kill app (không bật vân tay)
3. ✅ Mở lại app
4. ✅ Bấm "Đăng nhập bằng vân tay"
5. ✅ Hiển thị thông báo "Chưa bật đăng nhập bằng vân tay"

### Test Case 3: Test khi logout
1. ✅ Đăng nhập bằng mật khẩu
2. ✅ Bật chức năng vân tay
3. ✅ Logout (đăng xuất)
4. ✅ Mở lại app
5. ✅ Bấm "Đăng nhập bằng vân tay"
6. ✅ Hiển thị thông báo "Chưa có thông tin đăng nhập"

### Test Case 4: Test tắt chức năng vân tay
1. ✅ Đăng nhập bằng mật khẩu
2. ✅ Bật chức năng vân tay
3. ✅ Vào Profile → Tắt Switch vân tay
4. ✅ Xác nhận tắt
5. ✅ Kill app
6. ✅ Mở lại app
7. ✅ Bấm "Đăng nhập bằng vân tay"
8. ✅ Hiển thị thông báo "Chưa bật đăng nhập bằng vân tay"

---

## 🐛 Xử lý lỗi thường gặp

### Lỗi 1: "Thiết bị không hỗ trợ vân tay"
- **Nguyên nhân**: Emulator/thiết bị không có cảm biến vân tay
- **Giải pháp**: 
  - Dùng thiết bị thật có vân tay
  - Hoặc cấu hình emulator có vân tay (Settings → Security → Fingerprint)

### Lỗi 2: "Không thể khởi tạo bảo mật vân tay"
- **Nguyên nhân**: Lỗi khi tạo key trong Android Keystore
- **Giải pháp**: 
  - Xóa app và cài lại
  - Hoặc xóa dữ liệu app (Settings → Apps → Mobile Banking → Clear Data)

### Lỗi 3: "Vân tay không khớp"
- **Nguyên nhân**: Vân tay quét không đúng
- **Giải pháp**: 
  - Thử quét lại
  - Đảm bảo ngón tay sạch và khô
  - Đặt ngón tay đúng vị trí cảm biến

### Lỗi 4: Switch không bật được
- **Nguyên nhân**: Chưa cài đặt vân tay trên thiết bị
- **Giải pháp**: 
  - Vào Settings → Security → Fingerprint
  - Thêm vân tay mới
  - Quay lại app và thử lại

---

## 📝 Checklist Test

- [ ] Đăng nhập bằng mật khẩu thành công
- [ ] Vào Profile thành công
- [ ] Thấy phần "Cài đặt bảo mật" với Switch
- [ ] Bật Switch thành công
- [ ] Quét vân tay để xác thực thành công
- [ ] Thấy Toast "Đã bật đăng nhập bằng vân tay"
- [ ] Thấy Toast "Đã lưu thông tin đăng nhập"
- [ ] Kill app (không logout)
- [ ] Mở lại app
- [ ] Thấy nút "Đăng nhập bằng vân tay"
- [ ] Bấm nút "Đăng nhập bằng vân tay"
- [ ] Hiển thị BiometricPrompt
- [ ] Quét vân tay thành công
- [ ] Đăng nhập thành công
- [ ] Vào Dashboard
- [ ] Thấy Toast "Đăng nhập bằng vân tay thành công!"

---

## 🎯 Kết quả mong đợi

Sau khi hoàn thành tất cả các bước, bạn sẽ có thể:
- ✅ Bật/tắt chức năng đăng nhập bằng vân tay
- ✅ Đăng nhập bằng vân tay thay vì mật khẩu
- ✅ Refresh token được lưu an toàn trong Android Keystore
- ✅ Chỉ giải mã được token khi quét vân tay đúng
- ✅ Token tự động hết hạn sau 7 ngày

---

**Tạo bởi**: Mobile Banking App  
**Ngày cập nhật**: 2025-12-15  
**Phiên bản**: 1.0

