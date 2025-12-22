# ⚠️ Lỗi Package Name Không Khớp

## Vấn Đề
- **Package name trong app**: `com.example.mobilebanking`
- **Package name trong google-services.json**: `com.ibanking.app`
- **Kết quả**: Firebase sẽ KHÔNG hoạt động!

## Giải Pháp 1: Cập Nhật Firebase Project (Khuyến Nghị)

### Bước 1: Truy cập Firebase Console
1. Vào https://console.firebase.google.com/
2. Chọn project "ibanking-mobile-app"

### Bước 2: Thêm App Mới
1. Click vào biểu tượng Android (hoặc "Add app")
2. Nhập package name: `com.example.mobilebanking`
3. Nhập app nickname: "Banking App - Dev"
4. (Tùy chọn) Nhập SHA-1 certificate:
   ```bash
   # Windows
   keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
   ```
5. Click "Register app"

### Bước 3: Download google-services.json Mới
1. Download file `google-services.json` mới
2. Thay thế file hiện tại tại: `FrontEnd/banking-app/app/google-services.json`
3. Sync Gradle trong Android Studio

### Bước 4: Verify
File mới phải có:
```json
{
  "client": [
    {
      "client_info": {
        "android_client_info": {
          "package_name": "com.example.mobilebanking"  ← Phải đúng như này
        }
      }
    }
  ]
}
```

## Giải Pháp 2: Đổi Package Name App (Không Khuyến Nghị)

Nếu bạn muốn giữ nguyên Firebase config hiện tại, cần đổi package name của app từ `com.example.mobilebanking` sang `com.ibanking.app`. 

**LƯU Ý**: Việc này rất phức tạp và dễ gây lỗi vì phải:
- Đổi tên tất cả các package trong source code
- Cập nhật AndroidManifest.xml
- Cập nhật build.gradle.kts
- Refactor toàn bộ import statements

**→ KHÔNG khuyến nghị cách này!**

## Sau Khi Fix

1. Sync Gradle
2. Clean & Rebuild project
3. Chạy app và đăng nhập
4. Kiểm tra Logcat với filter "FCM" hoặc "Firebase"
5. Bạn sẽ thấy:
   ```
   D/FcmTokenManager: FCM Token: [token_string]
   D/FcmTokenManager: Token registered successfully
   ```

## Test FCM

Sau khi fix, test bằng cách:
1. Đăng nhập vào app
2. Copy FCM token từ Logcat
3. Vào Firebase Console > Cloud Messaging > "Send test message"
4. Paste token và gửi thử
5. Bạn sẽ nhận được notification trên device
