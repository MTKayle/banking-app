# Tích hợp TrackAsia Directions API - Tìm đường trong App

## ✅ Đã hoàn thành

Đã tích hợp TrackAsia Directions API vào tính năng "Tìm Chi Nhánh" để hiển thị đường đi thực tế trực tiếp trong app, không cần mở Google Maps.

## 🎯 Tính năng mới

### 1. Tìm đường thực tế
- ✅ Sử dụng TrackAsia Directions API để tính toán tuyến đường tối ưu
- ✅ Vẽ đường đi chi tiết lên bản đồ (không còn là đường thẳng)
- ✅ Hiển thị khoảng cách và thời gian di chuyển chính xác
- ✅ Hỗ trợ phương tiện: xe hơi (driving)

### 2. Hướng dẫn từng bước
- ✅ Hiển thị hướng dẫn rẽ trái/phải/đi thẳng
- ✅ Khoảng cách từng bước (km/m)
- ✅ Hướng dẫn bằng tiếng Việt

### 3. Xử lý lỗi thông minh
- ✅ Fallback về đường thẳng nếu API lỗi
- ✅ Hiển thị thông báo lỗi rõ ràng
- ✅ Không làm crash app

## 📁 Files đã tạo/cập nhật

### Files mới:
1. **TrackAsiaApiService.java** - Service gọi TrackAsia API
2. **DirectionsResponse.java** - DTO cho response từ API
3. **PolylineDecoder.java** - Utility decode encoded polyline

### Files cập nhật:
1. **ApiClient.java** - Thêm TrackAsia service
2. **BranchLocatorActivity.java** - Tích hợp tìm đường
3. **build.gradle.kts** - Thêm TrackAsia SDK (optional)

## 🔧 Cách sử dụng

### Trong app:
1. Mở màn hình "Tìm Chi Nhánh"
2. Chọn một chi nhánh từ danh sách
3. Nhấn nút **"Chỉ đường"**
4. Đường đi sẽ được vẽ trên bản đồ
5. Dialog hiển thị:
   - Khoảng cách (km)
   - Thời gian di chuyển (phút)
   - Nút "Xem hướng dẫn" để xem từng bước

### Xem hướng dẫn chi tiết:
1. Nhấn "Xem hướng dẫn" trong dialog
2. Hiển thị danh sách các bước:
   - Rẽ trái/phải tại đường nào
   - Đi thẳng bao nhiêu km/m
   - Đến đích

## 🌐 TrackAsia API

### Endpoint:
```
GET https://api.trackasia.com/directions/v1/{profile}/{coordinates}
```

### Parameters:
- **profile**: `driving` (xe hơi), `walking` (đi bộ), `cycling` (xe đạp)
- **coordinates**: `lng1,lat1;lng2,lat2` (tọa độ điểm đầu và cuối)
- **alternatives**: `false` (không cần tuyến đường thay thế)
- **steps**: `true` (trả về hướng dẫn từng bước)
- **geometries**: `polyline` (định dạng encoded polyline)
- **overview**: `full` (chi tiết đầy đủ)
- **language**: `vi` (tiếng Việt)

### Response:
```json
{
  "code": "Ok",
  "routes": [
    {
      "distance": 7234.5,
      "duration": 1234.0,
      "geometry": "encoded_polyline_string",
      "legs": [
        {
          "steps": [
            {
              "distance": 234.5,
              "duration": 45.0,
              "name": "Nguyễn Văn Cừ",
              "maneuver": {
                "type": "turn",
                "modifier": "left",
                "instruction": "Rẽ trái vào Nguyễn Văn Cừ",
                "location": [106.123, 10.456]
              }
            }
          ]
        }
      ]
    }
  ]
}
```

## 🎨 UI/UX

### Màu sắc đường đi:
- **Xanh dương (#1976D2)**: Đường đi từ TrackAsia API (thành công)
- **Cam (#FF9800)**: Đường thẳng fallback (khi API lỗi)

### Dialog thông tin:
```
📍 HAT Chi nhánh Quận 1 TP.HCM

🚗 Khoảng cách: 7.23 km
⏱️ Thời gian: 21 phút

Đường đi đã được vẽ trên bản đồ

[Xem hướng dẫn] [Đóng]
```

### Hướng dẫn từng bước:
```
1. Đi về hướng đông trên Đường ABC
   (234 m)

2. Rẽ trái vào Nguyễn Văn Cừ
   (1.2 km)

3. Rẽ phải vào Lê Lợi
   (500 m)

4. Đến đích ở bên trái
   (50 m)
```

## 🔍 So sánh với Google Maps

| Tính năng | TrackAsia (Mới) | Google Maps (Cũ) |
|-----------|-----------------|------------------|
| Hiển thị trong app | ✅ Có | ❌ Mở app ngoài |
| Đường đi thực tế | ✅ Có | ❌ Chỉ đường thẳng |
| Hướng dẫn từng bước | ✅ Có | ❌ Không |
| Khoảng cách chính xác | ✅ Có | ⚠️ Đường chim bay |
| Thời gian di chuyển | ✅ Có | ❌ Không |
| Chi phí | ✅ Miễn phí | 💰 Có giới hạn |
| Dữ liệu Việt Nam | ✅ Tốt | ✅ Tốt |

## 🚀 Ưu điểm

1. **Trải nghiệm liền mạch**: Không cần chuyển app
2. **Thông tin chi tiết**: Khoảng cách, thời gian, hướng dẫn
3. **Miễn phí**: Không giới hạn số lượng request
4. **Dữ liệu Việt Nam**: Hỗ trợ tốt đường phố VN
5. **Fallback thông minh**: Vẫn hoạt động khi API lỗi

## 🧪 Test

### Test case 1: Tìm đường thành công
1. Mở "Tìm Chi Nhánh"
2. Nhấn "Gần nhất" để tìm chi nhánh gần
3. Nhấn "Chỉ đường" ở chi nhánh đầu tiên
4. **Kết quả mong đợi**:
   - Đường đi màu xanh được vẽ trên map
   - Dialog hiển thị khoảng cách và thời gian
   - Có nút "Xem hướng dẫn"

### Test case 2: Xem hướng dẫn từng bước
1. Sau khi vẽ đường (test case 1)
2. Nhấn "Xem hướng dẫn"
3. **Kết quả mong đợi**:
   - Hiển thị danh sách các bước
   - Mỗi bước có hướng dẫn và khoảng cách
   - Hướng dẫn bằng tiếng Việt

### Test case 3: API lỗi (fallback)
1. Tắt internet hoặc API lỗi
2. Nhấn "Chỉ đường"
3. **Kết quả mong đợi**:
   - Vẽ đường thẳng màu cam
   - Toast hiển thị "Khoảng cách: X km (đường chim bay)"
   - App không crash

### Test case 4: Không có vị trí hiện tại
1. Tắt GPS
2. Nhấn "Chỉ đường"
3. **Kết quả mong đợi**:
   - Toast: "Không thể lấy vị trí hiện tại"
   - Không vẽ đường

## 📝 Lưu ý

### 1. API Key (Tùy chọn)
- TrackAsia API hiện tại không yêu cầu API key
- Nếu sau này cần, thêm vào header: `Authorization: Bearer YOUR_API_KEY`

### 2. Rate Limiting
- TrackAsia có giới hạn request/phút
- Nếu gặp lỗi 429, đợi 1 phút rồi thử lại

### 3. Offline Mode
- API cần internet để hoạt động
- Khi offline, sẽ fallback về đường thẳng

### 4. Độ chính xác
- Đường đi phụ thuộc vào dữ liệu bản đồ
- Có thể không chính xác 100% ở vùng sâu vùng xa

## 🔮 Tính năng tương lai (có thể mở rộng)

1. **Nhiều phương tiện**: Thêm walking, cycling
2. **Tuyến đường thay thế**: Hiển thị nhiều tuyến đường
3. **Tránh tắc đường**: Tích hợp traffic data
4. **Navigation thời gian thực**: Cập nhật vị trí liên tục
5. **Voice guidance**: Hướng dẫn bằng giọng nói
6. **Offline maps**: Tải bản đồ về máy

## 🐛 Troubleshooting

### Lỗi: "Không tìm thấy đường đi"
- **Nguyên nhân**: Tọa độ không hợp lệ hoặc quá xa
- **Giải pháp**: Kiểm tra tọa độ chi nhánh trong database

### Lỗi: "Lỗi kết nối"
- **Nguyên nhân**: Không có internet hoặc API down
- **Giải pháp**: Kiểm tra kết nối internet, app sẽ tự fallback

### Đường đi không chính xác
- **Nguyên nhân**: Dữ liệu bản đồ chưa cập nhật
- **Giải pháp**: Báo cáo cho TrackAsia hoặc dùng Google Maps

## 📚 Tài liệu tham khảo

- TrackAsia Directions API: https://docs.trackasia.com/directions/
- Polyline Encoding: https://developers.google.com/maps/documentation/utilities/polylinealgorithm
- Google Maps Android SDK: https://developers.google.com/maps/documentation/android-sdk

---

**Tích hợp hoàn tất!** 🎉

Bây giờ app có thể tìm đường và hiển thị trực tiếp trong app mà không cần mở Google Maps.
