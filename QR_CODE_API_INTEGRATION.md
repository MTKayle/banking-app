# Tích Hợp API QR Code

## Tổng Quan

Đã tích hợp API backend để generate QR code thay vì tạo local. API sẽ trả về ảnh QR code dạng PNG.

## API Endpoint

**URL:** `POST /api/accounts/checking/qr-code`

**Headers:**
- `Authorization: Bearer {token}`
- `Content-Type: application/json`

**Request Body:**
```json
{
  "amount": 300000,
  "description": "Truong Duong Hung Chuyen tien"
}
```

**Lưu ý:**
- `amount` và `description` là optional
- Khi mới vào trang, gọi API với body rỗng `{}` hoặc cả 2 field null
- Sau khi user nhập thông tin và lưu, gọi lại API với amount và description

**Response:**
- Content-Type: `image/png`
- Body: Binary image data (PNG format)

## Files Đã Tạo/Cập Nhật

### 1. QRCodeRequest.java (Mới)
```
FrontEnd/banking-app/app/src/main/java/com/example/mobilebanking/api/dto/QRCodeRequest.java
```

DTO class cho request body:
- `amount` (Long, optional): Số tiền
- `description` (String, optional): Nội dung chuyển khoản

### 2. AccountApiService.java (Cập nhật)
```
FrontEnd/banking-app/app/src/main/java/com/example/mobilebanking/api/AccountApiService.java
```

Thêm endpoint:
```java
@POST("accounts/checking/qr-code")
Call<ResponseBody> getCheckingQRCode(@Body QRCodeRequest request);
```

### 3. MyQRActivity.java (Cập nhật)
```
FrontEnd/banking-app/app/src/main/java/com/example/mobilebanking/activities/MyQRActivity.java
```

**Thay đổi chính:**
- Thêm import `ResponseBody`, `Callback`, `SessionManager`
- Thêm fields: `accountApiService`, `sessionManager`
- Thêm method `loadQRCodeFromAPI(Long amount, String description)`
- Giữ lại method `generateQRCode()` làm fallback (nếu cần)
- Cập nhật `onCreate()`: Gọi `loadQRCodeFromAPI(null, null)` thay vì `generateQRCode()`
- Cập nhật save button: Gọi `loadQRCodeFromAPI(amountValue, message)` sau khi validate

## Luồng Hoạt Động

### 1. Khi Mở Màn Hình QR
```java
// onCreate()
loadQRCodeFromAPI(null, null);
```

**Request:**
```json
{
  "amount": null,
  "description": null
}
```

**Response:** QR code chỉ chứa thông tin tài khoản

### 2. Khi User Nhập Thông Tin và Lưu
```java
// Save button click
long amountValue = 1000000;
String message = "Thanh toán hóa đơn";
loadQRCodeFromAPI(amountValue, message);
```

**Request:**
```json
{
  "amount": 1000000,
  "description": "Thanh toán hóa đơn"
}
```

**Response:** QR code chứa thông tin tài khoản + số tiền + nội dung

### 3. Xử Lý Response
```java
private void loadQRCodeFromAPI(Long amount, String description) {
    QRCodeRequest request = new QRCodeRequest(amount, description);
    
    Call<ResponseBody> call = accountApiService.getCheckingQRCode(request);
    call.enqueue(new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (response.isSuccessful() && response.body() != null) {
                // Convert response body to bitmap
                InputStream inputStream = response.body().byteStream();
                qrBitmap = BitmapFactory.decodeStream(inputStream);
                
                if (qrBitmap != null) {
                    ivQRCode.setImageBitmap(qrBitmap);
                }
            }
        }
        
        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            // Handle error
        }
    });
}
```

## Test Cases

### TC1: Load QR Code Lần Đầu
**Bước:**
1. Đăng nhập vào app
2. Vào màn hình "Nhận tiền" / "Mã QR của tôi"

**Kết quả mong đợi:**
- API được gọi với `amount=null`, `description=null`
- QR code hiển thị thành công
- Nút "+ Thêm số tiền" hiển thị

### TC2: Thêm Số Tiền và Nội Dung
**Bước:**
1. Click "+ Thêm số tiền"
2. Nhập số tiền: 1000000 (hiển thị "1.000.000")
3. Nhập nội dung: "Thanh toán hóa đơn"
4. Click "Lưu"

**Kết quả mong đợi:**
- API được gọi với `amount=1000000`, `description="Thanh toán hóa đơn"`
- QR code mới hiển thị với thông tin đã nhập
- Hiển thị số tiền "1.000.000 VND" với gạch chân
- Hiển thị nội dung "Thanh toán hóa đơn"

### TC3: Chỉ Nhập Số Tiền
**Bước:**
1. Click "+ Thêm số tiền"
2. Nhập số tiền: 500000
3. Để trống nội dung
4. Click "Lưu"

**Kết quả mong đợi:**
- API được gọi với `amount=500000`, `description=null`
- QR code mới hiển thị
- Hiển thị số tiền "500.000 VND"
- Không hiển thị dòng nội dung

### TC4: Chỉnh Sửa Thông Tin
**Bước:**
1. Click vào thông tin đã lưu
2. Thay đổi số tiền: 2000000
3. Thay đổi nội dung: "Chuyển tiền mới"
4. Click "Lưu"

**Kết quả mong đợi:**
- API được gọi với thông tin mới
- QR code cập nhật
- Thông tin hiển thị cập nhật

### TC5: Lỗi Kết Nối
**Bước:**
1. Tắt internet hoặc backend
2. Mở màn hình QR

**Kết quả mong đợi:**
- Toast hiển thị: "Lỗi kết nối: ..."
- QR code không hiển thị hoặc giữ nguyên QR cũ

## Xử Lý Lỗi

### 1. API Response Không Thành Công
```java
if (!response.isSuccessful()) {
    Toast.makeText(this, "Không thể tải mã QR từ server", Toast.LENGTH_SHORT).show();
}
```

### 2. Không Thể Decode Image
```java
if (qrBitmap == null) {
    Toast.makeText(this, "Không thể tải mã QR", Toast.LENGTH_SHORT).show();
}
```

### 3. Lỗi Kết Nối
```java
@Override
public void onFailure(Call<ResponseBody> call, Throwable t) {
    Toast.makeText(this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
}
```

## Lưu Ý Quan Trọng

1. **Authorization Header:**
   - API yêu cầu Bearer token
   - Token được tự động thêm bởi `ApiClient` interceptor

2. **Response Type:**
   - Response là binary image data (PNG)
   - Sử dụng `ResponseBody` để nhận raw bytes
   - Convert sang Bitmap bằng `BitmapFactory.decodeStream()`

3. **Null Values:**
   - Khi amount hoặc description là null, backend sẽ generate QR code cơ bản
   - Không cần kiểm tra null trước khi gọi API

4. **Fallback:**
   - Method `generateQRCode()` cũ vẫn được giữ lại
   - Có thể dùng làm fallback nếu API fail

5. **Performance:**
   - QR code được cache trong `qrBitmap`
   - Có thể save/share QR code đã load

## Debug

### Enable Logging
```java
private static final String TAG = "MyQRActivity";

// Log API call
Log.d(TAG, "Loading QR code with amount: " + amount + ", description: " + description);

// Log success
Log.d(TAG, "QR code loaded successfully");

// Log error
Log.e(TAG, "Failed to load QR code: " + response.code());
```

### Check Request Body
Sử dụng OkHttp logging interceptor để xem request/response:
```java
// In ApiClient.java
HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
logging.setLevel(HttpLoggingInterceptor.Level.BODY);
```

## Tương Thích

- ✅ Android API 21+
- ✅ Retrofit 2.x
- ✅ OkHttp 3.x
- ✅ Gson converter

## Kết Luận

Tích hợp API QR code hoàn tất. QR code giờ được generate từ backend với đầy đủ thông tin amount và description. UI vẫn giữ nguyên với format số tiền, chữ số bằng chữ, và gạch chân.
