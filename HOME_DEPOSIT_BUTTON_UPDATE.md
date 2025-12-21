# Home Deposit Button Update

## Overview
Thay đổi nút "QR của tôi" thành "Nạp tiền" trong trang Home với giao diện mới và chuẩn bị cho luồng nạp tiền.

## Changes Made

### 1. Layout Update
**File**: `res/layout/ui_home_view_quick_actions.xml`

**Before**:
- Button ID: `uihome_action_my_qr`
- Text: "QR của tôi" (from strings.xml)
- Icon: `ic_qr_scanner`
- Action: Navigate to MyQRActivity

**After**:
- Button ID: `uihome_action_deposit`
- Text: "Nạp tiền" (hardcoded)
- Icon: `ic_deposit` (new wallet icon)
- Action: Placeholder toast message

### 2. New Icon Created
**File**: `res/drawable/ic_deposit.xml`

**Design**:
- Wallet/money icon
- Color: #4CAF50 (green)
- Size: 24dp x 24dp
- Style: Material Design filled icon

### 3. HomeFragment Update
**File**: `ui_home/HomeFragment.java`

**Changes**:
```java
// Old code (removed)
setupQuickAction(view, R.id.uihome_action_my_qr, 
    new Intent(requireContext(), MyQRActivity.class));

// New code (added)
View depositButton = view.findViewById(R.id.uihome_action_deposit);
if (depositButton != null) {
    depositButton.setOnClickListener(v -> {
        Toast.makeText(requireContext(), 
            "Tính năng nạp tiền đang được phát triển", 
            Toast.LENGTH_SHORT).show();
    });
}
```

## UI Layout

### Quick Actions Grid (4 buttons)
```
┌──────────┬──────────┬──────────┬──────────┐
│ Chuyển   │ Nạp tiền │   Vay    │  Tiết    │
│  tiền    │   💰     │  nhanh   │  kiệm    │
│   💸     │          │   💵     │   🏦     │
└──────────┴──────────┴──────────┴──────────┘
```

### Button Details

#### 1. Chuyển tiền (Transfer)
- Icon: ic_transfer
- Color: Green (#4CAF50)
- Action: Navigate to TransferActivity

#### 2. Nạp tiền (Deposit) - NEW
- Icon: ic_deposit (wallet)
- Color: Green (#4CAF50)
- Action: Placeholder toast (ready for implementation)

#### 3. Vay nhanh (Loan)
- Icon: ic_vaytien
- Color: Green (#4CAF50)
- Action: Navigate to AccountActivity (Loan tab)

#### 4. Tiết kiệm (Saving)
- Icon: ic_bank_logo
- Color: Green (#4CAF50)
- Action: Navigate to AccountActivity (Saving tab)

## Design Specifications

### Button Style
- **Card**: CardView with 16dp corner radius
- **Elevation**: 2dp
- **Size**: 50dp x 50dp
- **Padding**: 10dp
- **Icon Size**: 30dp x 30dp
- **Ripple Effect**: selectableItemBackground

### Text Style
- **Size**: 10sp
- **Color**: uihome_text_primary
- **Alignment**: Center
- **Max Lines**: 2
- **Margin Top**: 4dp

### Colors
- **Icon Tint**: #4CAF50 (green)
- **Text**: uihome_text_primary
- **Card Background**: White (default)

## Placeholder Implementation

### Current Behavior
When user clicks "Nạp tiền" button:
```
Click → Toast: "Tính năng nạp tiền đang được phát triển"
```

### Ready for Implementation
The button is ready to be connected to the deposit flow:

```java
// Replace placeholder with actual implementation
View depositButton = view.findViewById(R.id.uihome_action_deposit);
if (depositButton != null) {
    depositButton.setOnClickListener(v -> {
        // TODO: Navigate to deposit activity
        Intent intent = new Intent(requireContext(), DepositActivity.class);
        startActivity(intent);
    });
}
```

## Files Modified

1. **Layout**:
   - `res/layout/ui_home_view_quick_actions.xml`
     - Changed button ID from `uihome_action_my_qr` to `uihome_action_deposit`
     - Changed text from "@string/my_qr" to "Nạp tiền"
     - Changed icon from `ic_qr_scanner` to `ic_deposit`

2. **Drawable**:
   - `res/drawable/ic_deposit.xml` (NEW)
     - Created wallet icon for deposit button

3. **Java**:
   - `ui_home/HomeFragment.java`
     - Removed MyQRActivity navigation
     - Added placeholder toast for deposit button

## Old QR Feature

### What Happened to "QR của tôi"?
- **Removed from Home**: No longer in quick actions
- **Still Accessible**: Can be accessed from Account → Thanh toán tab
- **Reason**: Replaced with more commonly used "Nạp tiền" feature

### Access QR Code
Users can still access QR code through:
1. Home → Tài khoản
2. Select Thanh toán tab
3. Click "QR của tôi" button in account details

## Next Steps

### Deposit Flow Implementation
When implementing the deposit feature, you'll need to:

1. **Create DepositActivity**:
   - Amount input screen
   - Payment method selection (card, bank transfer, etc.)
   - Confirmation screen
   - Success screen

2. **Update HomeFragment**:
   ```java
   setupQuickAction(view, R.id.uihome_action_deposit, 
       new Intent(requireContext(), DepositActivity.class));
   ```

3. **Add to AndroidManifest.xml**:
   ```xml
   <activity
       android:name=".activities.DepositActivity"
       android:parentActivityName=".ui_home.UiHomeActivity" />
   ```

## Testing Guide

### Test Case 1: Button Display
1. Login → Home
2. **Verify**: 
   - 4 quick action buttons displayed
   - Second button shows "Nạp tiền" with wallet icon
   - Icon is green (#4CAF50)
   - Text is centered below icon

### Test Case 2: Button Click
1. Login → Home
2. Click "Nạp tiền" button
3. **Verify**: Toast message "Tính năng nạp tiền đang được phát triển"

### Test Case 3: Other Buttons
1. Login → Home
2. Click other buttons (Chuyển tiền, Vay nhanh, Tiết kiệm)
3. **Verify**: All other buttons still work correctly

### Test Case 4: QR Access
1. Login → Home → Tài khoản
2. Select Thanh toán tab
3. **Verify**: QR code feature still accessible from account details

## Visual Comparison

### Before
```
┌──────────┬──────────┬──────────┬──────────┐
│ Chuyển   │ QR của   │   Vay    │  Tiết    │
│  tiền    │   tôi    │  nhanh   │  kiệm    │
│   💸     │   📱     │   💵     │   🏦     │
└──────────┴──────────┴──────────┴──────────┘
```

### After
```
┌──────────┬──────────┬──────────┬──────────┐
│ Chuyển   │ Nạp tiền │   Vay    │  Tiết    │
│  tiền    │   💰     │  nhanh   │  kiệm    │
│   💸     │          │   💵     │   🏦     │
└──────────┴──────────┴──────────┴──────────┘
```

## Status
✅ **COMPLETE** - Button updated, ready for deposit flow implementation

## Notes
- Icon design follows Material Design guidelines
- Button maintains consistent style with other quick actions
- Placeholder allows for easy future implementation
- Old QR feature still accessible through Account screen
