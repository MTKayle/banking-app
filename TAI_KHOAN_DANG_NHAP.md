# 📋 TÀI KHOẢN ĐĂNG NHẬP MẪU

## 🔐 Thông tin đăng nhập

### 👤 Tài khoản Khách hàng (CUSTOMER)

#### Tài khoản 1
- **Username**: `customer1`
- **Password**: `123456`
- **Họ tên**: Nguyen Van A
- **Email**: nguyenvana@email.com
- **Số điện thoại**: 0901234567
- **CMND/CCCD**: 001234567890
- **Vai trò**: CUSTOMER

#### Tài khoản 2
- **Username**: `customer2`
- **Password**: `123456`
- **Họ tên**: Le Thi C
- **Email**: lethic@email.com
- **Số điện thoại**: 0901111111
- **CMND/CCCD**: 001111111111
- **Vai trò**: CUSTOMER

---

### 👔 Tài khoản Nhân viên (OFFICER)

#### Tài khoản 1
- **Username**: `officer1`
- **Password**: `123456`
- **Họ tên**: Tran Thi B
- **Email**: tranthib@bank.com
- **Số điện thoại**: 0907654321
- **CMND/CCCD**: 009876543210
- **Vai trò**: OFFICER

#### Tài khoản 2
- **Username**: `officer2`
- **Password**: `123456`
- **Họ tên**: Pham Van D
- **Email**: phamvand@bank.com
- **Số điện thoại**: 0902222222
- **CMND/CCCD**: 002222222222
- **Vai trò**: OFFICER

---

## 📊 Tóm tắt nhanh

| Vai trò | Username | Password | Màn hình sau đăng nhập |
|---------|----------|----------|------------------------|
| Khách hàng | `customer1` | `123456` | Customer Dashboard |
| Khách hàng | `customer2` | `123456` | Customer Dashboard |
| Nhân viên | `officer1` | `123456` | Officer Dashboard |
| Nhân viên | `officer2` | `123456` | Officer Dashboard |

---

## 💰 Thông tin tài khoản ngân hàng (Mock Data)

Mỗi user sẽ có 3 loại tài khoản:

### 1. Tài khoản Thanh toán (Checking Account)
- **Số dư**: ₫50,000,000
- **Loại**: Tài khoản giao dịch thông thường

### 2. Tài khoản Tiết kiệm (Savings Account)
- **Số dư**: ₫100,000,000
- **Lãi suất**: 6.5% / năm
- **Lợi nhuận hàng tháng**: ₫541,666.67

### 3. Tài khoản Vay (Mortgage Account)
- **Số dư**: -₫500,000,000 (nợ)
- **Số tiền vay**: ₫500,000,000
- **Thanh toán hàng tháng**: ₫15,000,000
- **Số tháng còn lại**: 36 tháng

---

## 🧪 Hướng dẫn Test

### Test 1: Đăng nhập Khách hàng
1. Mở app
2. Nhập Username: `customer1`
3. Nhập Password: `123456`
4. Bấm "Đăng nhập"
5. ✅ Kết quả: Vào Customer Dashboard

### Test 2: Đăng nhập Nhân viên
1. Mở app
2. Nhập Username: `officer1`
3. Nhập Password: `123456`
4. Bấm "Đăng nhập"
5. ✅ Kết quả: Vào Officer Dashboard

### Test 3: Đăng nhập sai mật khẩu
1. Mở app
2. Nhập Username: `customer1`
3. Nhập Password: `sai_mat_khau`
4. Bấm "Đăng nhập"
5. ✅ Kết quả: Hiển thị thông báo lỗi "Tên đăng nhập hoặc mật khẩu không đúng"

### Test 4: Đăng nhập bằng vân tay
**Lưu ý**: Cần bật chức năng vân tay trước
1. Đăng nhập bằng mật khẩu với `customer1` / `123456`
2. Vào Profile → Bật Switch "Đăng nhập bằng vân tay"
3. Quét vân tay để xác thực
4. Thoát app (kill app, không logout)
5. Mở lại app
6. Bấm nút "Đăng nhập bằng vân tay"
7. Quét vân tay
8. ✅ Kết quả: Đăng nhập thành công

---

## 🔒 Lưu ý bảo mật

- ⚠️ Đây là dữ liệu mẫu chỉ dùng cho môi trường phát triển và test
- ⚠️ Không sử dụng mật khẩu này trong môi trường production
- ⚠️ Tất cả mật khẩu đều là `123456` để dễ test

---

## 📝 Ghi chú

- Tất cả tài khoản đều có cùng mật khẩu: `123456`
- Dữ liệu được lưu trong `DataManager.java`
- Trong production, dữ liệu sẽ được lấy từ backend API
- Refresh token có thời hạn 7 ngày

---

**Tạo bởi**: Mobile Banking App  
**Ngày cập nhật**: 2025-12-15

