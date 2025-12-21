# Fix: cannot find symbol getPhoneNumber()

## Lỗi
```
error: cannot find symbol
String phone = dataManager.getPhoneNumber();
                          ^
  symbol:   method getPhoneNumber()
  location: variable dataManager of type DataManager
```

## Nguyên nhân
Method trong DataManager là `getUserPhone()` chứ không phải `getPhoneNumber()`

## Giải pháp

### Sửa trong SavingConfirmActivity.java

**Trước**:
```java
DataManager dataManager = DataManager.getInstance(this);
String phone = dataManager.getPhoneNumber();
```

**Sau**:
```java
String phone = dataManager.getUserPhone();
```

## DataManager methods liên quan

```java
// Lưu số điện thoại
public void saveUserPhone(String phone)

// Lấy số điện thoại
public String getUserPhone()

// Lưu tên đầy đủ
public void saveUserFullName(String fullName)

// Lấy tên đầy đủ
public String getUserFullName()

// Lưu email
public void saveUserEmail(String email)

// Lấy email
public String getUserEmail()
```

## ✅ Đã sửa xong

Build lại app và test:
```bash
./gradlew clean assembleDebug
```
