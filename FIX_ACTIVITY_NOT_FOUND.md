# Fix: Activity Not Found Error

## Lỗi
```
android.content.ActivityNotFoundException: Unable to find explicit activity class 
{com.example.mobilebanking/com.example.mobilebanking.activities.SavingWithdrawConfirmActivity}; 
have you declared this activity in your AndroidManifest.xml?
```

## Nguyên nhân
Activity `SavingWithdrawConfirmActivity` chưa được khai báo trong `AndroidManifest.xml`

## Giải pháp

Thêm activity vào `app/src/main/AndroidManifest.xml`:

```xml
<activity
    android:name="com.example.mobilebanking.activities.SavingWithdrawConfirmActivity"
    android:parentActivityName="com.example.mobilebanking.activities.AccountActivity" />
```

### Vị trí
Thêm sau `SavingSuccessActivity` và trước phần `<!-- Transaction Activities -->`

## ✅ Đã sửa xong

Build lại app:
```bash
./gradlew clean assembleDebug
```

Hoặc trong Android Studio: Build → Rebuild Project
