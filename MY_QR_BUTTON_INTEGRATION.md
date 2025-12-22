# My QR Button Integration

## Thay đổi
Đã kết nối nút "QR của tôi" trong trang home với MyQRActivity.

## Chi tiết

### File đã chỉnh sửa
- `FrontEnd/banking-app/app/src/main/java/com/example/mobilebanking/ui_home/HomeFragment.java`

### Thay đổi
Thêm dòng code sau vào phần setup quick actions trong method `onViewCreated()`:

```java
setupQuickAction(view, R.id.uihome_action_my_qr, new Intent(requireContext(), com.example.mobilebanking.activities.MyQRActivity.class));
```

### Vị trí nút
- **Layout**: `ui_home_view_quick_actions.xml`
- **ID**: `uihome_action_my_qr`
- **Text**: "QR của tôi"
- **Icon**: `ic_qr_scanner`

### Chức năng
Khi người dùng click vào nút "QR của tôi" trong trang home:
1. Ứng dụng sẽ chuyển đến `MyQRActivity`
2. Hiển thị mã QR của tài khoản người dùng
3. Người dùng có thể thêm số tiền và nội dung vào mã QR

## Các nút khác trong home
- **Chuyển tiền** → `TransferActivity`
- **QR của tôi** → `MyQRActivity` ✅ (mới thêm)
- **Vay nhanh** → `ServicesActivity`
- **Tiết kiệm** → `ServicesActivity`

## Bottom Navigation
- **Home** → Hiện tại
- **QR** → `QrScanPaymentActivity` (quét mã QR)
- **Promo** → `ServicesActivity`
- **More** → `SettingsActivity`

## Status
✅ Hoàn thành - Nút "QR của tôi" đã được kết nối với MyQRActivity
