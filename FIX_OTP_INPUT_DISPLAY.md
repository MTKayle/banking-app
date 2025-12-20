# Khắc phục lỗi OTP Input - Không hiển thị số và Cursor xanh lá

## Vấn đề
1. Khi nhập số vào các ô OTP, số không hiển thị
2. Cursor không có màu xanh lá

## Nguyên nhân
1. **EditText thiếu thuộc tính:**
   - Thiếu `inputType="number"` để cho phép nhập số
   - Thiếu `textSize`, `textColor` để hiển thị rõ ràng
   - Style `@style/OtpEditText` có thể bị lỗi hoặc thiếu thuộc tính

2. **Cursor màu xanh:**
   - Chưa có file `cursor_green.xml`

## Giải pháp đã áp dụng

### 1. Sửa Layout OTP EditText

**File:** `activity_otp_verification.xml`

Đã thay thế style bằng các thuộc tính inline đầy đủ:

```xml
<EditText
    android:id="@+id/et_otp_1"
    android:layout_width="48dp"
    android:layout_height="56dp"
    android:layout_margin="4dp"
    android:gravity="center"
    android:inputType="number"           ← Cho phép nhập số
    android:maxLength="1"                 ← Giới hạn 1 ký tự
    android:textSize="24sp"               ← Kích thước chữ lớn
    android:textStyle="bold"              ← Chữ đậm
    android:textColor="@android:color/black"  ← Màu đen
    android:textCursorDrawable="@drawable/cursor_green"  ← Cursor xanh lá
    android:background="@drawable/otp_edittext_background"
    android:importantForAutofill="no"/>
```

### 2. Tạo Cursor màu xanh lá

**File mới:** `cursor_green.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <size android:width="2dp"/>
    <solid android:color="#4CAF50"/>  ← Màu xanh lá cây
</shape>
```

### 3. Sửa OTP EditText Background

**File:** `otp_edittext_background.xml`

Format lại file cho đúng chuẩn:

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="@android:color/white"/>
    <corners android:radius="8dp"/>
    <stroke
        android:width="2dp"
        android:color="@color/primary_color"/>
</shape>
```

## Cần làm: Sync Project

### Bước 1: Sync Gradle
Android Studio chưa nhận diện file `cursor_green.xml` mới tạo.

**Thực hiện:**
1. Nhấn **Ctrl + Shift + O** (Windows) hoặc **Cmd + Shift + O** (Mac)
2. Hoặc: **File → Sync Project with Gradle Files**
3. Đợi sync hoàn tất

### Bước 2: Clean và Rebuild
1. **Build → Clean Project**
2. Đợi clean xong
3. **Build → Rebuild Project**
4. Đợi rebuild xong

### Bước 3: Run lại app
1. Uninstall app cũ trên thiết bị (nếu cần)
2. Run app mới

## Các thuộc tính quan trọng của OTP EditText

### Để hiển thị số:
```xml
android:inputType="number"        ← Bắt buộc!
android:textSize="24sp"           ← Đủ lớn để nhìn rõ
android:textColor="@android:color/black"  ← Màu tương phản
android:gravity="center"          ← Căn giữa
```

### Để có cursor xanh:
```xml
android:textCursorDrawable="@drawable/cursor_green"
```

### Layout tổng thể:
- **Width**: 48dp - Đủ rộng cho 1 chữ số
- **Height**: 56dp - Đủ cao để dễ nhấn
- **Margin**: 4dp - Khoảng cách giữa các ô
- **Background**: Border xanh, nền trắng, góc bo tròn

## Kết quả sau khi sửa

### Trước khi sửa:
- ❌ Nhập số không hiển thị
- ❌ Cursor màu mặc định (đen/xám)

### Sau khi sửa:
- ✅ Nhập số hiển thị rõ ràng (màu đen, size 24sp, bold)
- ✅ Cursor màu xanh lá (#4CAF50)
- ✅ Auto focus chuyển sang ô tiếp theo khi nhập xong
- ✅ Background đẹp với border xanh

## Giao diện OTP hoàn chỉnh

```
┌─────────────────────────────────┐
│                                 │
│      Xác Thực OTP               │
│                                 │
│   Nhập mã 6 số đã gửi đến       │
│   điện thoại của bạn            │
│                                 │
│   ┌──┐ ┌──┐ ┌──┐ ┌──┐ ┌──┐ ┌──┐ │
│   │1 │ │2 │ │3 │ │4 │ │5 │ │6 │ │ ← Số hiển thị rõ
│   └──┘ └──┘ └──┘ └──┘ └──┘ └──┘ │   Cursor xanh lá
│                                 │
│   Gửi lại sau 60s               │
│                                 │
│   [    Xác thực    ]            │
│   [   Gửi lại OTP  ]            │
│                                 │
└─────────────────────────────────┘
```

## Test lại tính năng

### Test nhập số:
1. Click vào ô OTP đầu tiên
2. Nhập số "1"
3. → **Số "1" phải hiển thị rõ ràng**
4. → **Focus tự động chuyển sang ô 2**
5. → **Cursor màu xanh lá**
6. Tiếp tục nhập "2", "3", "4", "5", "6"

### Test OTP fake:
- Nhập: **123456**
- Nhấn "Xác thực"
- → Chuyển đến Success screen

## Lưu ý

### Nếu vẫn không hiển thị số:
1. **Kiểm tra textColor**: Phải khác màu background
2. **Kiểm tra textSize**: Phải đủ lớn (24sp)
3. **Kiểm tra padding**: Background không che số

### Nếu cursor không xanh:
1. Đảm bảo đã Sync Project
2. Check file `cursor_green.xml` có trong `drawable/`
3. Rebuild project

### Màu sắc sử dụng:
- **Cursor**: #4CAF50 (Material Green 500)
- **Border**: @color/primary_color (màu chủ đạo app)
- **Text**: Black (#000000)
- **Background**: White

## Status

✅ Layout OTP đã có đầy đủ thuộc tính
✅ Cursor màu xanh lá đã tạo
✅ Background đã format đúng
⏳ Cần Sync Project để hoàn tất
⏳ Test lại trên thiết bị

## Các file đã sửa/tạo

1. ✅ `activity_otp_verification.xml` - Sửa 6 EditText với thuộc tính đầy đủ
2. ✅ `cursor_green.xml` - MỚI - Cursor màu xanh lá
3. ✅ `otp_edittext_background.xml` - Format lại cho đẹp

**Không có breaking changes!**

