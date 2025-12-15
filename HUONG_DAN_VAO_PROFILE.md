# 📱 HƯỚNG DẪN VÀO PROFILE ĐỂ BẬT VÂN TAY

## 🎯 Cách vào Profile

### Bước 1: Đăng nhập vào app
1. Mở app Mobile Banking
2. Nhập Username: `customer1`
3. Nhập Password: `123456`
4. Bấm "Đăng nhập"
5. ✅ Vào Dashboard (Trang Chủ)

### Bước 2: Mở menu (3 chấm)
1. Ở góc trên bên phải của màn hình Dashboard, bạn sẽ thấy **icon 3 chấm dọc** (⋮)
2. Bấm vào **icon 3 chấm** này

### Bước 3: Chọn "Hồ Sơ" (Profile)
1. Menu sẽ hiển thị 2 mục:
   - **Hồ Sơ** (Profile) - có icon người
   - **Đăng Xuất** (Logout) - có icon X
2. Bấm vào **"Hồ Sơ"** (Profile)

### Bước 4: Vào màn hình Profile
- ✅ Màn hình Profile sẽ hiển thị với:
  - Thông tin cá nhân (Họ tên, Username, Email, Số điện thoại, CMND/CCCD)
  - Phần **"Cài đặt bảo mật"** ở dưới
  - Switch **"Đăng nhập bằng vân tay"**

---

## 🔐 Bật chức năng vân tay

### Trong màn hình Profile:

1. **Cuộn xuống** để tìm phần **"Cài đặt bảo mật"**
2. Bạn sẽ thấy:
   - Tiêu đề: **"Cài đặt bảo mật"**
   - Dòng 1: **"Đăng nhập bằng vân tay"** (chữ đậm)
   - Dòng 2: "Sử dụng vân tay để đăng nhập nhanh chóng và an toàn" (chữ nhỏ, màu xám)
   - **Switch** (nút bật/tắt) ở bên phải

3. **Bấm vào Switch** để bật
4. Android sẽ hiển thị popup yêu cầu quét vân tay
5. **Quét vân tay** để xác thực
6. ✅ Thấy Toast: **"Đã bật đăng nhập bằng vân tay"**
7. ✅ Thấy Toast: **"Đã lưu thông tin đăng nhập"**

---

## 📸 Hình ảnh minh họa vị trí

```
┌─────────────────────────────────┐
│  Trang Chủ            [⋮]  ← Bấm vào đây
├─────────────────────────────────┤
│  Xin chào, customer1            │
│  Tổng Số Dư: 150.000.000 ₫     │
│                                 │
│  [Chuyển Tiền] [Thanh Toán]    │
│                                 │
│  ...                            │
└─────────────────────────────────┘

Sau khi bấm [⋮]:
┌─────────────────────────────────┐
│  ┌───────────────────────────┐  │
│  │ 👤 Hồ Sơ                  │  │ ← Bấm vào đây
│  ├───────────────────────────┤  │
│  │ ✕ Đăng Xuất              │  │
│  └───────────────────────────┘  │
└─────────────────────────────────┘
```

---

## ⚠️ Lưu ý

1. **Nếu không thấy icon 3 chấm:**
   - Kiểm tra xem Toolbar có hiển thị không
   - Thử build lại app: `./gradlew clean build`

2. **Nếu bấm vào 3 chấm nhưng không thấy menu:**
   - Kiểm tra xem `onCreateOptionsMenu` có được gọi không
   - Kiểm tra file `dashboard_menu.xml` có đúng không

3. **Nếu không thấy phần "Cài đặt bảo mật":**
   - Cuộn xuống trong màn hình Profile
   - Kiểm tra file `activity_profile.xml` có phần biometric settings không

---

## ✅ Checklist

- [ ] Đăng nhập thành công
- [ ] Thấy icon 3 chấm (⋮) ở góc trên bên phải
- [ ] Bấm vào 3 chấm → Thấy menu
- [ ] Thấy mục "Hồ Sơ" trong menu
- [ ] Bấm "Hồ Sơ" → Vào màn hình Profile
- [ ] Thấy phần "Cài đặt bảo mật"
- [ ] Thấy Switch "Đăng nhập bằng vân tay"
- [ ] Bật Switch thành công
- [ ] Quét vân tay thành công
- [ ] Thấy Toast "Đã bật đăng nhập bằng vân tay"

---

**Tạo bởi**: Mobile Banking App  
**Ngày**: 2025-12-15

