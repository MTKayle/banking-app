# Hướng dẫn sửa lỗi Run Configuration

## Lỗi: Activity class {com.example.myapplication/com.example.mobilebanking.activities.LoginActivity} does not exist

### Nguyên nhân:
Android Studio đang sử dụng cấu hình run cũ với package name `com.example.myapplication` thay vì `com.example.mobilebanking`.

### Cách khắc phục:

#### Bước 1: Xóa Run Configuration cũ
1. Mở Android Studio
2. Vào **Run** → **Edit Configurations...**
3. Tìm configuration có tên "app" hoặc "My Application"
4. Xóa configuration đó (click vào dấu `-` ở trên)
5. Click **OK**

#### Bước 2: Tạo Run Configuration mới
1. Vẫn trong **Run** → **Edit Configurations...**
2. Click dấu `+` ở trên → chọn **Android App**
3. Đặt tên: `app`
4. **Module**: chọn `app`
5. **Launch**: chọn `Default Activity`
6. **Launch Options**:
   - **Launch**: `Default Activity`
   - Hoặc nếu muốn chỉ định: `com.example.mobilebanking.activities.LoginActivity`
7. Click **OK**

#### Bước 3: Clean và Rebuild
1. **Build** → **Clean Project**
2. **Build** → **Rebuild Project**
3. Đợi build xong

#### Bước 4: Invalidate Caches (nếu vẫn lỗi)
1. **File** → **Invalidate Caches...**
2. Chọn **Invalidate and Restart**
3. Đợi Android Studio restart

#### Bước 5: Gỡ app cũ trên thiết bị/emulator
Chạy lệnh sau trong Terminal:
```bash
adb uninstall com.example.myapplication
adb uninstall com.example.mobilebanking
```

#### Bước 6: Chạy lại app
1. Chọn device/emulator
2. Click nút **Run** (màu xanh) hoặc nhấn `Shift + F10`

---

## Hoặc sử dụng script tự động:

Chạy file `fix_package_error.bat` để tự động:
- Gỡ app cũ
- Clean build
- Build và cài app mới

Sau đó trong Android Studio:
1. **Run** → **Edit Configurations...**
2. Xóa config cũ và tạo mới như hướng dẫn trên

---

## Kiểm tra cấu hình đúng:

Sau khi tạo run configuration mới, kiểm tra:
- **Module**: `app` (không phải `myapplication`)
- **Package name**: `com.example.mobilebanking` (không phải `com.example.myapplication`)
- **Launch Activity**: `com.example.mobilebanking.activities.LoginActivity`


