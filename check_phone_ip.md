# Kiểm tra IP điện thoại

## Bước 1: Kiểm tra IP điện thoại

1. Trên điện thoại: **Settings** → **Wi-Fi**
2. Nhấn vào mạng Wi-Fi đang kết nối
3. Xem **IP Address**
4. **Ghi lại IP này:** `???`

## Bước 2: So sánh với IP máy tính

- **IP máy tính:** `10.0.221.236`
- **IP điện thoại:** `???`

**Nếu IP điện thoại là:**
- `10.0.221.XXX` → ✅ Cùng subnet, OK!
- `10.0.220.XXX` → ❌ Khác subnet, không cùng mạng!
- `192.168.XXX.XXX` → ❌ Khác mạng hoàn toàn!

## Bước 3: Nếu IP khác subnet

→ Điện thoại và máy tính **KHÔNG cùng mạng Wi-Fi**!

**Giải pháp:**
1. Ngắt kết nối Wi-Fi trên điện thoại
2. Kết nối lại vào **CÙNG mạng** với máy tính
3. Kiểm tra lại IP

