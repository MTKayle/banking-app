# Hướng dẫn mở port 8089 trong Windows Firewall

## Cách 1: Qua Command Prompt (Admin)

1. Mở Command Prompt hoặc PowerShell **với quyền Administrator**
2. Chạy lệnh:

```powershell
netsh advfirewall firewall add rule name="Backend Port 8089" dir=in action=allow protocol=TCP localport=8089
```

## Cách 2: Qua Windows Defender Firewall GUI

1. Mở **Windows Defender Firewall with Advanced Security**
   - Nhấn `Win + R`
   - Gõ: `wf.msc`
   - Nhấn Enter

2. Chọn **Inbound Rules** ở bên trái

3. Nhấn **New Rule...** ở bên phải

4. Chọn **Port** → Next

5. Chọn **TCP** và **Specific local ports**: `8089` → Next

6. Chọn **Allow the connection** → Next

7. Chọn tất cả (Domain, Private, Public) → Next

8. Đặt tên: `Backend Port 8089` → Finish

## Cách 3: Tạm thời tắt Firewall (chỉ để test)

1. Mở **Windows Defender Firewall**
2. Chọn **Turn Windows Defender Firewall on or off**
3. Tắt cho **Private network** (tạm thời)
4. Test lại app
5. **Nhớ bật lại sau khi test!**

## Kiểm tra Firewall đã mở chưa

Chạy lệnh:
```powershell
netsh advfirewall firewall show rule name="Backend Port 8089"
```

