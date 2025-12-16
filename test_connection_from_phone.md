# 📱 HƯỚNG DẪN TEST KẾT NỐI TỪ ĐIỆN THOẠI - CHI TIẾT

## ⚠️ VẤN ĐỀ: URL thiếu `http://`

Bạn đã nhập: `10.0.221.236:8089` ❌
**Phải nhập:** `http://10.0.221.236:8089` ✅

## ✅ CÁCH TEST ĐÚNG:

### Bước 1: Mở trình duyệt trên điện thoại

1. Mở **Chrome** hoặc **Firefox** trên điện thoại
2. Nhấn vào **thanh địa chỉ** (address bar)

### Bước 2: Nhập URL đúng format

**QUAN TRỌNG:** Phải có `http://` ở đầu!

```
http://10.0.221.236:8089/api/test/jwt
```

**KHÔNG phải:**
- ❌ `10.0.221.236:8089`
- ❌ `10.0.221.236:8089/api/test/jwt`
- ❌ `www.10.0.221.236:8089`

**PHẢI là:**
- ✅ `http://10.0.221.236:8089/api/test/jwt`

### Bước 3: Nhấn Enter và xem kết quả

**Nếu kết nối thành công:**
- Sẽ thấy một trang JSON với nội dung:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "secretKey": "...",
  "algorithm": "HS256",
  "isValid": true,
  "phone": "0912345678",
  ...
}
```
→ **Kết nối thành công!** ✅

**Nếu không kết nối được:**
- Màn hình trắng
- "Không thể kết nối"
- "Connection timeout"
- "ERR_CONNECTION_REFUSED"
→ **Vẫn còn vấn đề mạng/firewall** ❌

## 🔍 CÁC URL TEST KHÁC:

### Test 1: Endpoint đơn giản (GET)
```
http://10.0.221.236:8089/api/test/jwt
```
→ Nên trả về JSON ✅

### Test 2: Endpoint auth (sẽ báo lỗi 405 - đó là tốt!)
```
http://10.0.221.236:8089/api/auth/login
```
→ Nên báo lỗi 405 (Method Not Allowed) hoặc 400 → **Kết nối thành công!** ✅

### Test 3: Endpoint register (sẽ báo lỗi 405 - đó là tốt!)
```
http://10.0.221.236:8089/api/auth/register
```
→ Nên báo lỗi 405 (Method Not Allowed) hoặc 400 → **Kết nối thành công!** ✅

## 🐛 NẾU VẪN KHÔNG KẾT NỐI ĐƯỢC:

### Kiểm tra 1: IP điện thoại
1. Vào **Settings** → **Wi-Fi**
2. Nhấn vào mạng Wi-Fi đang kết nối
3. Xem **IP Address**
4. **Phải là:** `10.0.221.XXX` (cùng subnet với máy tính)

**Nếu IP là `10.0.220.XXX`:**
- Vẫn chưa cùng mạng!
- Ngắt kết nối Wi-Fi và kết nối lại
- Đảm bảo kết nối vào **CÙNG mạng** với máy tính

### Kiểm tra 2: Backend đang chạy?
1. Trên máy tính, mở trình duyệt
2. Truy cập: `http://localhost:8089/api/test/jwt`
3. Nếu thấy JSON → Backend đang chạy ✅
4. Nếu không → Khởi động Backend trong IntelliJ IDEA

### Kiểm tra 3: Firewall
1. Chạy script: `fix_face_auth_connection.bat` (với quyền Admin)
2. Hoặc mở firewall thủ công:
   ```cmd
   netsh advfirewall firewall add rule name="Backend Port 8089" dir=in action=allow protocol=TCP localport=8089
   ```

### Kiểm tra 4: Router có AP Isolation?
- Một số router có tính năng "AP Isolation" → Tắt tính năng này
- Hoặc thử dùng USB tethering thay vì Wi-Fi

## 📸 HÌNH ẢNH MẪU:

**URL đúng trong trình duyệt:**
```
http://10.0.221.236:8089/api/test/jwt
```

**Kết quả mong đợi:**
- Trang JSON với token và thông tin
- Hoặc lỗi 405/400 (nghĩa là kết nối được, chỉ là method không đúng)

## ✅ SAU KHI TEST THÀNH CÔNG:

1. **Rebuild app:**
   ```bash
   cd "D:\duancuoikiandroid\Ibanking-Moblie-App\ibanking fe"
   .\gradlew.bat clean installDebug
   ```

2. **Cài đặt lại app trên điện thoại**

3. **Thử lại xác thực khuôn mặt**

## 📞 THÔNG TIN CẦN CUNG CẤP NẾU VẪN LỖI:

1. IP máy tính: `10.0.221.236`
2. IP điện thoại: `???` (kiểm tra trong Settings → Wi-Fi)
3. Tên mạng Wi-Fi: `???`
4. URL đã test: `???`
5. Kết quả test: `???` (JSON / Lỗi / Màn hình trắng)
6. Log từ Android Studio (Logcat): `???`
