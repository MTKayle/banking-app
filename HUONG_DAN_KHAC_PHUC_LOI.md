# Hướng Dẫn Khắc Phục Lỗi Khi Chạy App Mobile Banking

## 🔍 Cách Xem Lỗi Chi Tiết

### Bước 1: Mở Logcat trong Android Studio
1. Mở Android Studio
2. Click vào tab **Logcat** ở phía dưới màn hình
3. Chạy app trên emulator hoặc thiết bị thật
4. Xem lỗi hiển thị màu đỏ trong Logcat

### Bước 2: Lọc Lỗi
Trong Logcat, chọn filter:
- **Error** - Chỉ hiện lỗi nghiêm trọng
- **Package**: `com.example.mobilebanking`

---

## ❌ Các Lỗi Thường Gặp và Cách Khắc Phục

### Lỗi 1: App Crash Ngay Khi Mở (Theme Error)

**Triệu chứng**: App đóng ngay sau khi mở, không hiện gì

**Lỗi trong Logcat**:
```
java.lang.RuntimeException: Unable to start activity
Caused by: android.content.res.Resources$NotFoundException: 
Resource ID #0x7f0f0xxx "Theme.MobileBanking"
```

**Nguyên nhân**: Theme không tồn tại hoặc sai tên

**Cách khắc phục**:
1. Mở file `app/src/main/res/values/themes.xml`
2. Kiểm tra có dòng này:
```xml
<style name="Theme.MobileBanking" parent="Base.Theme.MobileBanking" />
```
3. Nếu không có, thêm vào

**Đã fix**: ✅ Theme đã được sửa đúng

---

### Lỗi 2: NullPointerException khi findViewById

**Triệu chứng**: App crash khi click vào một nút hoặc mở một màn hình

**Lỗi trong Logcat**:
```
java.lang.NullPointerException: Attempt to invoke virtual method 
'void android.widget.Button.setOnClickListener' on a null object reference
```

**Nguyên nhân**: View ID không tồn tại trong layout XML

**Cách khắc phục**:
1. Kiểm tra file Java (ví dụ: `LoginActivity.java`)
2. Tìm dòng `findViewById(R.id.xxx)`
3. Mở file layout tương ứng (ví dụ: `activity_login.xml`)
4. Kiểm tra ID có tồn tại không

**Ví dụ**:
```java
// Trong LoginActivity.java
btnLogin = findViewById(R.id.btn_login);

// Trong activity_login.xml phải có:
<Button
    android:id="@+id/btn_login"
    ... />
```

**Đã fix**: ✅ Tất cả view IDs đã được kiểm tra và khớp

---

### Lỗi 3: Resources$NotFoundException (Layout không tìm thấy)

**Triệu chứng**: App crash khi mở một màn hình cụ thể

**Lỗi trong Logcat**:
```
android.content.res.Resources$NotFoundException: 
Resource ID #0x7f0c0xxx layout/activity_xxx not found
```

**Nguyên nhân**: File layout XML không tồn tại

**Cách khắc phục**:
1. Kiểm tra file Java có dòng `setContentView(R.layout.activity_xxx)`
2. Kiểm tra file `app/src/main/res/layout/activity_xxx.xml` có tồn tại không
3. Nếu không có, tạo file layout

**Đã fix**: ✅ Tất cả 16 layout files đã được tạo

---

### Lỗi 4: ActivityNotFoundException

**Triệu chứng**: App crash khi click vào một nút để chuyển màn hình

**Lỗi trong Logcat**:
```
android.content.ActivityNotFoundException: 
Unable to find explicit activity class 
{com.example.mobilebanking/com.example.mobilebanking.activities.XxxActivity}
```

**Nguyên nhân**: Activity chưa được khai báo trong AndroidManifest.xml

**Cách khắc phục**:
1. Mở `app/src/main/AndroidManifest.xml`
2. Thêm activity:
```xml
<activity
    android:name="com.example.mobilebanking.activities.XxxActivity"
    android:parentActivityName="com.example.mobilebanking.activities.ParentActivity" />
```

**Đã fix**: ✅ Tất cả 16 activities đã được khai báo

---

### Lỗi 5: Google Maps Crash

**Triệu chứng**: App crash khi mở Branch Locator

**Lỗi trong Logcat**:
```
Google Maps Android API: Authorization failure
```

**Nguyên nhân**: Chưa có Google Maps API Key

**Cách khắc phục** (TẠM THỜI):
Sửa file `BranchLocatorActivity.java`:

```java
// Tìm dòng này (khoảng line 49-53):
private void initializeMap() {
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
            .findFragmentById(R.id.map);
    if (mapFragment != null) {
        mapFragment.getMapAsync(this);
    }
}

// Thay bằng:
private void initializeMap() {
    try {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    } catch (Exception e) {
        Toast.makeText(this, "Google Maps không khả dụng", Toast.LENGTH_SHORT).show();
        finish();
    }
}
```

**Cách khắc phục ĐÚNG**:
1. Lấy Google Maps API Key từ Google Cloud Console
2. Mở `AndroidManifest.xml`
3. Thay `YOUR_GOOGLE_MAPS_API_KEY_HERE` bằng API key thật

---

### Lỗi 6: ClassCastException

**Triệu chứng**: App crash khi tương tác với UI

**Lỗi trong Logcat**:
```
java.lang.ClassCastException: android.widget.TextView cannot be cast to android.widget.Button
```

**Nguyên nhân**: Sai kiểu dữ liệu khi findViewById

**Cách khắc phục**:
Kiểm tra Java code và XML layout khớp nhau:
```java
// Java
Button btnLogin = findViewById(R.id.btn_login);

// XML phải là Button, không phải TextView
<Button android:id="@+id/btn_login" ... />
```

---

## 🔧 Các Bước Khắc Phục Chung

### 1. Clean và Rebuild Project
```bash
./gradlew clean
./gradlew assembleDebug
```

Hoặc trong Android Studio:
- **Build** → **Clean Project**
- **Build** → **Rebuild Project**

### 2. Invalidate Caches
Trong Android Studio:
- **File** → **Invalidate Caches / Restart**
- Chọn **Invalidate and Restart**

### 3. Sync Gradle
- Click vào icon **Sync Project with Gradle Files** (biểu tượng voi)

### 4. Xóa và Cài Lại App
```bash
./gradlew uninstallDebug
./gradlew installDebug
```

---

## 📱 Cách Test App Đúng Cách

### Test Login
1. Mở app
2. Nhập:
   - Username: `customer1`
   - Password: `123456`
3. Click **Login**
4. Phải chuyển sang CustomerDashboard

### Test Officer Login
1. Nhập:
   - Username: `officer1`
   - Password: `123456`
2. Click **Login**
3. Phải chuyển sang OfficerDashboard

### Test Registration
1. Click **Register**
2. Điền đầy đủ thông tin
3. Click **Register**
4. Nhập OTP (bất kỳ 6 số nào)
5. Click **Verify**
6. Quay lại màn hình Login

### Test Transfer
1. Login với `customer1`
2. Click **Transfer**
3. Điền thông tin chuyển tiền
4. Click **Continue**
5. Click **Confirm**
6. Nhập OTP
7. Click **Verify**

---

## 🆘 Nếu Vẫn Bị Lỗi

### Gửi cho tôi thông tin sau:

1. **Screenshot màn hình lỗi**
2. **Logcat error** (copy text màu đỏ trong Logcat)
3. **Bước nào gây lỗi**:
   - Mở app?
   - Click vào đâu?
   - Nhập gì?

### Cách copy Logcat:
1. Trong Android Studio, tab Logcat
2. Click chuột phải vào dòng lỗi màu đỏ
3. Chọn **Copy**
4. Paste và gửi cho tôi

---

## ✅ Checklist Trước Khi Chạy App

- [ ] Đã chạy `./gradlew clean`
- [ ] Đã chạy `./gradlew assembleDebug` thành công
- [ ] Không có lỗi compilation (màu đỏ trong code)
- [ ] Emulator hoặc thiết bị đã kết nối
- [ ] Đã cài đặt app: `./gradlew installDebug`
- [ ] Mở Logcat để xem lỗi (nếu có)

---

## 📞 Thông Tin Test

### Tài khoản test:
- **Customer**: `customer1` / `123456`
- **Officer**: `officer1` / `123456`

### Lệnh build:
```bash
# Clean
./gradlew clean

# Build
./gradlew assembleDebug

# Install
./gradlew installDebug

# Uninstall
./gradlew uninstallDebug
```

---

**Hãy cho tôi biết lỗi cụ thể bạn gặp phải để tôi có thể hỗ trợ tốt hơn!**

