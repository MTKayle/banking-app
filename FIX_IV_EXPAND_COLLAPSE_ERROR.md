# Hướng dẫn khắc phục lỗi "cannot find symbol iv_expand_collapse"

## Nguyên nhân
Lỗi này xảy ra vì Android Studio chưa sync file R.java sau khi thêm ID mới vào layout.

## Giải pháp

### Cách 1: Sync Project với Gradle Files (Khuyến nghị)
1. Mở Android Studio
2. Click vào menu **File** → **Sync Project with Gradle Files**
3. Hoặc nhấn tổ hợp phím: **Ctrl + Shift + O** (Windows/Linux) hoặc **Cmd + Shift + O** (Mac)
4. Đợi Gradle sync hoàn tất
5. Build lại project

### Cách 2: Clean và Rebuild Project
1. Click menu **Build** → **Clean Project**
2. Đợi clean hoàn tất
3. Click menu **Build** → **Rebuild Project**
4. Đợi rebuild hoàn tất

### Cách 3: Invalidate Caches and Restart (Nếu vẫn lỗi)
1. Click menu **File** → **Invalidate Caches / Restart**
2. Chọn **Invalidate and Restart**
3. Android Studio sẽ restart và rebuild cache

## Xác nhận đã sửa

Sau khi sync, kiểm tra:
- ✅ File `activity_transfer_success.xml` đã có `android:id="@+id/iv_expand_collapse"`
- ✅ File `ic_expand_more.xml` tồn tại trong thư mục `drawable`
- ✅ Không còn lỗi compile trong `TransferSuccessActivity.java`

## Nếu vẫn lỗi

Nếu sau khi sync vẫn lỗi, có thể do:
1. **Lỗi XML syntax**: Kiểm tra file `activity_transfer_success.xml` có lỗi cú pháp không
2. **Missing drawable**: Kiểm tra `@drawable/ic_expand_more` có tồn tại không
3. **Gradle cache**: Xóa thư mục `.gradle` và `.idea`, sau đó mở lại project

## Ghi chú
File layout đã được cập nhật với:
```xml
<ImageView
    android:id="@+id/iv_expand_collapse"
    android:layout_width="32dp"
    android:layout_height="32dp"
    android:layout_gravity="center_horizontal"
    android:layout_marginTop="16dp"
    android:src="@drawable/ic_expand_more"
    android:rotation="180"
    android:contentDescription="@string/back"
    app:tint="#888888"/>
```

Icon `ic_expand_more.xml` đã được sửa lại thành mũi tên xuống chính xác.

