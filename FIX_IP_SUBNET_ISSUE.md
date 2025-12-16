# 🔧 SỬA LỖI IP KHÁC SUBNET

## ❌ VẤN ĐỀ:

- **IP máy tính:** `10.0.221.236` (subnet 10.0.221.x)
- **IP điện thoại:** `10.0.220.70` (subnet 10.0.220.x)
- **Kết quả:** Không kết nối được vì khác subnet!

## ✅ GIẢI PHÁP 1: Dùng USB Tethering (KHUYẾN NGHỊ - Dễ nhất!)

### Bước 1: Kết nối điện thoại qua USB

1. Cắm cáp USB vào máy tính
2. Trên điện thoại: **Settings** → **Developer Options** → Bật **USB Debugging**
3. Cho phép USB Debugging khi có popup

### Bước 2: Chạy script setup

1. Mở CMD (không cần Admin)
2. Chạy:
   ```bash
   cd "D:\duancuoikiandroid\Ibanking-Moblie-App\ibanking fe"
   use_usb_solution.bat
   ```

### Bước 3: Code đã được cập nhật

File `ApiClient.java` đã được đổi `CONNECTION_MODE = "USB"` ✅

### Bước 4: Rebuild app

```bash
.\gradlew.bat clean installDebug
```

### Bước 5: Test

App sẽ kết nối qua USB, không cần IP!

**Lưu ý:** Giữ terminal mở trong khi test (port forwarding sẽ dừng khi đóng terminal)

---

## ✅ GIẢI PHÁP 2: Kiểm tra Router Settings

Nếu muốn tiếp tục dùng Wi-Fi:

### Bước 1: Kiểm tra AP Isolation

1. Đăng nhập vào router (thường là `192.168.1.1` hoặc `10.0.0.1`)
2. Tìm mục **Wireless Settings** hoặc **Advanced Settings**
3. Tìm **AP Isolation** hoặc **Client Isolation**
4. **TẮT** tính năng này
5. Lưu và khởi động lại router

### Bước 2: Kết nối lại Wi-Fi

1. Ngắt kết nối Wi-Fi trên cả máy tính và điện thoại
2. Kết nối lại
3. Kiểm tra IP lại - phải cùng subnet

### Bước 3: Cập nhật code

Đổi lại `CONNECTION_MODE = "WIFI"` trong `ApiClient.java`

---

## ✅ GIẢI PHÁP 3: Dùng Mobile Hotspot

1. Trên điện thoại: **Settings** → **Hotspot & Tethering** → Bật **Mobile Hotspot**
2. Trên máy tính: Kết nối vào hotspot của điện thoại
3. Cả hai sẽ cùng subnet
4. Cập nhật IP trong `ApiClient.java` nếu cần

---

## 🎯 KHUYẾN NGHỊ:

**Dùng USB Tethering** - Ổn định nhất, không phụ thuộc router!

