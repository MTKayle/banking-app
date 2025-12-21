# Hướng Dẫn Sử Dụng Màn Hình Quét QR Thanh Toán

## Tổng Quan

Đã tạo màn hình quét QR code mới với giao diện hiện đại, tối ưu cho thanh toán. Giao diện tương tự MoMo/ZaloPay với:
- Nền tối (dark theme)
- Khung quét bo tròn với góc chỉ dẫn
- 2 nút chức năng: Dán mã QR và Chọn QR từ ảnh
- Logo ngân hàng (BIDV, VNPAY, VietQR)

## Files Đã Tạo

### 1. Layout XML
**File:** `activity_qr_scan_payment.xml`

**Cấu trúc:**
- Camera preview full screen
- Dark overlay (60% opacity)
- Top bar: Back button, Title, Flash button
- Instructions text
- Bank logos row
- Scanning frame (280x280dp) với góc bo tròn
- Bottom buttons: "Dán mã QR" | "Chọn QR từ ảnh"

### 2. Activity Java
**File:** `QrScanPaymentActivity.java`

**Chức năng:**
- Quét QR code từ camera (real-time)
- Dán QR code từ clipboard
- Chọn ảnh từ thư viện và quét QR
- Bật/tắt đèn flash
- Xử lý permissions

### 3. Drawable Resources
- `bg_scan_frame_rounded.xml` - Khung quét bo tròn
- `bg_bottom_buttons_rounded.xml` - Background nút dưới
- `ic_paste.xml` - Icon dán
- `ic_image.xml` - Icon chọn ảnh

## Cách Sử Dụng

### 1. Mở Màn Hình Quét QR

```java
Intent intent = new Intent(this, QrScanPaymentActivity.class);
startActivityForResult(intent, REQUEST_CODE_SCAN_QR);
```

### 2. Nhận Kết Quả

```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    
    if (requestCode == REQUEST_CODE_SCAN_QR && resultCode == RESULT_OK) {
        String qrData = data.getStringExtra("qr_data");
        // Xử lý dữ liệu QR
        processQrData(qrData);
    }
}
```

## Tính Năng

### 1. Quét QR Từ Camera
- Tự động quét khi phát hiện QR code
- Hiển thị khung quét với góc chỉ dẫn
- Hỗ trợ bật/tắt đèn flash (nếu có)

### 2. Dán Mã QR
- Click nút "Dán mã QR"
- Đọc dữ liệu từ clipboard
- Xử lý ngay lập tức

### 3. Chọn QR Từ Ảnh
- Click nút "Chọn QR từ ảnh"
- Mở thư viện ảnh
- Quét QR code từ ảnh đã chọn
- Hiển thị progress bar khi đang xử lý

### 4. Bật/Tắt Flash
- Icon flash ở góc trên bên phải
- Chỉ hiển thị nếu thiết bị có đèn flash
- Toggle on/off

## Giao Diện

### Màu Sắc
- Background: `#000000` (đen)
- Overlay: `#99000000` (đen 60% opacity)
- Text: `@android:color/white`
- Khung quét: White border, 2dp
- Bottom buttons background: `#333333`

### Kích Thước
- Khung quét: 280x280dp
- Góc bo tròn: 24dp
- Góc chỉ dẫn: 40dp x 4dp
- Bottom buttons: Full width với padding 32dp

### Font & Text
- Title: 20sp, bold
- Instructions: 14sp, line spacing 4dp
- Button text: 14sp

## Permissions

Cần thêm vào `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />

<activity
    android:name=".activities.QrScanPaymentActivity"
    android:screenOrientation="portrait"
    android:theme="@style/Theme.AppCompat.NoActionBar" />
```

## Dependencies

Đã có sẵn trong project:
- CameraX
- ML Kit Barcode Scanning
- Guava

## Test Cases

### TC1: Quét QR Từ Camera
**Bước:**
1. Mở màn hình quét QR
2. Cho phép quyền camera
3. Đưa QR code vào khung quét

**Kết quả:**
- Camera hiển thị
- Khung quét hiển thị với góc chỉ dẫn
- Tự động quét và trả về kết quả

### TC2: Dán Mã QR
**Bước:**
1. Copy text QR vào clipboard
2. Mở màn hình quét QR
3. Click "Dán mã QR"

**Kết quả:**
- Đọc dữ liệu từ clipboard
- Trả về kết quả ngay lập tức

### TC3: Chọn QR Từ Ảnh
**Bước:**
1. Mở màn hình quét QR
2. Click "Chọn QR từ ảnh"
3. Chọn ảnh có QR code

**Kết quả:**
- Mở thư viện ảnh
- Hiển thị progress bar
- Quét QR từ ảnh
- Trả về kết quả

### TC4: Bật/Tắt Flash
**Bước:**
1. Mở màn hình quét QR
2. Click icon flash

**Kết quả:**
- Đèn flash bật/tắt
- Icon thay đổi

### TC5: Từ Chối Permission
**Bước:**
1. Mở màn hình quét QR
2. Từ chối quyền camera

**Kết quả:**
- Hiển thị dialog yêu cầu cấp quyền
- Có nút "Cài đặt" để mở settings
- Có nút "Hủy" để đóng

## Lưu Ý

1. **Camera Permission:**
   - Bắt buộc phải có quyền camera
   - Hiển thị dialog nếu bị từ chối

2. **Flash:**
   - Chỉ hiển thị nếu thiết bị có flash
   - Tự động tắt khi pause/destroy

3. **Performance:**
   - Sử dụng STRATEGY_KEEP_ONLY_LATEST để tối ưu
   - Single thread executor cho camera

4. **QR Format:**
   - Hỗ trợ FORMAT_QR_CODE
   - Có thể mở rộng cho các format khác

## Tích Hợp Vào App

### Từ Transfer Activity
```java
// In TransferActivity.java
private void openQrScanner() {
    Intent intent = new Intent(this, QrScanPaymentActivity.class);
    startActivityForResult(intent, REQUEST_CODE_SCAN_QR);
}

@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    
    if (requestCode == REQUEST_CODE_SCAN_QR && resultCode == RESULT_OK) {
        String qrData = data.getStringExtra("qr_data");
        parseQrData(qrData);
    }
}

private void parseQrData(String qrData) {
    // Parse QR data format: accountNumber|amount|message
    String[] parts = qrData.split("\\|");
    if (parts.length >= 1) {
        String accountNumber = parts[0];
        etRecipientAccount.setText(accountNumber);
        
        if (parts.length >= 2) {
            String amount = parts[1];
            etAmount.setText(amount);
        }
        
        if (parts.length >= 3) {
            String message = parts[2];
            etNote.setText(message);
        }
    }
}
```

### Từ Home Activity
```java
// Add button in home screen
btnScanQr.setOnClickListener(v -> {
    Intent intent = new Intent(this, QrScanPaymentActivity.class);
    startActivityForResult(intent, REQUEST_CODE_SCAN_QR);
});
```

## So Sánh Với QrScannerActivity Cũ

| Feature | QrScannerActivity (Cũ) | QrScanPaymentActivity (Mới) |
|---------|------------------------|------------------------------|
| UI Theme | Light | Dark |
| Khung quét | Vuông | Bo tròn với góc chỉ dẫn |
| Dán QR | ❌ | ✅ |
| Chọn từ ảnh | ❌ | ✅ |
| Logo ngân hàng | ❌ | ✅ |
| Instructions | Dưới khung | Trên khung |
| Bottom buttons | 1 nút | 2 nút |
| Use case | CCCD scanning | Payment QR |

## Kết Luận

Màn hình quét QR mới đã hoàn thành với:
- ✅ Giao diện hiện đại, dark theme
- ✅ Khung quét bo tròn đẹp mắt
- ✅ 3 cách quét: Camera, Clipboard, Gallery
- ✅ Hỗ trợ flash
- ✅ Xử lý permissions đầy đủ
- ✅ Performance tối ưu

Sẵn sàng để tích hợp vào flow thanh toán!
