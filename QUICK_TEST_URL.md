# 🚀 URL TEST NHANH TỪ ĐIỆN THOẠI

## ⚠️ QUAN TRỌNG: Phải có `http://` ở đầu URL!

## ✅ URL TEST (Copy và paste vào trình duyệt điện thoại):

### Test 1: Endpoint Register (sẽ báo lỗi - đó là tốt!)
```
http://10.0.221.236:8089/api/auth/register
```
**Kết quả mong đợi:**
- Lỗi 405 (Method Not Allowed) → ✅ **Kết nối thành công!**
- Lỗi 400 (Bad Request) → ✅ **Kết nối thành công!**
- Màn hình trắng / Timeout → ❌ Vẫn còn vấn đề

### Test 2: Endpoint Login (sẽ báo lỗi - đó là tốt!)
```
http://10.0.221.236:8089/api/auth/login
```
**Kết quả mong đợi:**
- Lỗi 405 (Method Not Allowed) → ✅ **Kết nối thành công!**
- Lỗi 400 (Bad Request) → ✅ **Kết nối thành công!**
- Màn hình trắng / Timeout → ❌ Vẫn còn vấn đề

## 📱 CÁCH TEST:

1. **Mở trình duyệt trên điện thoại** (Chrome, Firefox, etc.)
2. **Copy URL ở trên** (có `http://` ở đầu!)
3. **Paste vào thanh địa chỉ**
4. **Nhấn Enter**
5. **Xem kết quả**

## ✅ NẾU THẤY LỖI 405 HOẶC 400:

→ **Kết nối thành công!** Backend đang chạy và nhận được request!

**Lý do:** Endpoint này cần POST method, nhưng trình duyệt dùng GET → Lỗi 405 là bình thường!

## ❌ NẾU THẤY MÀN HÌNH TRẮNG HOẶC TIMEOUT:

→ Vẫn còn vấn đề mạng/firewall

**Kiểm tra:**
1. IP điện thoại phải là `10.0.221.XXX` (cùng subnet với máy tính)
2. Chạy script: `fix_face_auth_connection.bat` (với quyền Admin)
3. Đảm bảo Backend đang chạy trong IntelliJ IDEA

## 🔄 SAU KHI TEST THÀNH CÔNG:

1. Rebuild app:
   ```bash
   .\gradlew.bat clean installDebug
   ```

2. Thử lại xác thực khuôn mặt trong app

