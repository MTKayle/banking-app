# Mobile Banking App - Step-by-Step Testing Guide

## Prerequisites
- Android Studio installed
- Android SDK (API 26+)
- Android device or emulator running API 26+
- Project opened in Android Studio
- Gradle synced successfully

---

## Phase 1: Build Verification (5 minutes)

### Step 1.1: Clean Build
```bash
./gradlew clean
```
**Expected Output**: 
```
BUILD SUCCESSFUL
```
**What to Check**: No errors, only informational messages

### Step 1.2: Debug Build
```bash
./gradlew assembleDebug --stacktrace
```
**Expected Output**:
```
BUILD SUCCESSFUL in X seconds
35 actionable tasks: X executed, X up-to-date
```
**What to Check**: 
- ✅ No compilation errors
- ✅ No resource linking errors
- ✅ APK generated at `app/build/outputs/apk/debug/app-debug.apk`

### Step 1.3: Verify APK Generation
```bash
ls -la app/build/outputs/apk/debug/app-debug.apk
```
**Expected**: File exists and is > 5MB

---

## Phase 2: Installation (3 minutes)

### Step 2.1: Connect Device/Emulator
- Connect Android device via USB, OR
- Start Android emulator

### Step 2.2: Install APK
**Option A - Via Android Studio**:
1. Click green "Run" button
2. Select device
3. Wait for installation

**Option B - Via Command Line**:
```bash
./gradlew installDebug
```

**Expected Output**:
```
Installing APK 'app-debug.apk' on 'device_name'
Installed successfully
```

### Step 2.3: Verify Installation
```bash
adb shell pm list packages | grep mobilebanking
```
**Expected**: 
```
package:com.example.mobilebanking
```

---

## Phase 3: Launch and Initial Screen (2 minutes)

### Step 3.1: Launch App
- Tap app icon on device/emulator
- Or: `adb shell am start -n com.example.mobilebanking/.activities.LoginActivity`

### Step 3.2: Verify Login Screen
**Expected Screen Elements**:
- ✅ Bank logo at top (ic_bank_logo)
- ✅ "Mobile Banking" title
- ✅ Username input field
- ✅ Password input field
- ✅ "Remember Me" checkbox
- ✅ "Login" button
- ✅ "Login with Face ID" button
- ✅ "Register" link
- ✅ "Forgot Password" link

**What to Check**:
- No crashes
- All text visible
- All buttons clickable
- Material Design styling applied

---

## Phase 4: Authentication Testing (10 minutes)

### Test 4.1: Successful Login
**Steps**:
1. Enter Username: `customer1`
2. Enter Password: `123456`
3. Click "Login"

**Expected**:
- ✅ Navigate to CustomerDashboardActivity
- ✅ No crashes
- ✅ Welcome message shows "Welcome, customer1"

### Test 4.2: Failed Login
**Steps**:
1. Enter Username: `wronguser`
2. Enter Password: `wrongpass`
3. Click "Login"

**Expected**:
- ✅ Toast message: "Invalid credentials"
- ✅ Stay on login screen
- ✅ No crashes

### Test 4.3: Officer Login
**Steps**:
1. Clear fields
2. Enter Username: `officer1`
3. Enter Password: `123456`
4. Click "Login"

**Expected**:
- ✅ Navigate to OfficerDashboardActivity
- ✅ Welcome message shows "Welcome, Officer officer1"
- ✅ See 4 management cards (Customer, Account, Transaction, Reports)

### Test 4.4: Biometric Login
**Steps**:
1. Go back to login (click back button)
2. Click "Login with Face ID"

**Expected**:
- ✅ Navigate to BiometricAuthActivity
- ✅ See face scan animation
- ✅ After 3-4 seconds, auto-login to dashboard
- ✅ No crashes

### Test 4.5: Registration
**Steps**:
1. Go back to login
2. Click "Register"
3. Fill all fields:
   - Full Name: `Test User`
   - Email: `test@example.com`
   - Phone: `0901234567`
   - ID Number: `123456789`
   - Username: `testuser`
   - Password: `123456`
   - Confirm Password: `123456`
4. Click "Register"
5. Enter OTP: `123456` (any 6 digits accepted)
6. Click "Verify"

**Expected**:
- ✅ Navigate through registration flow
- ✅ OTP screen appears
- ✅ Return to login screen
- ✅ No crashes

---

## Phase 5: Customer Dashboard Testing (15 minutes)

### Test 5.1: Dashboard Layout
**Login as**: `customer1` / `123456`

**Expected Screen Elements**:
- ✅ Toolbar with "Dashboard" title
- ✅ Welcome message: "Welcome, customer1"
- ✅ Total balance display: "₫250,000,000"
- ✅ 3 account cards (Checking, Savings, Mortgage)
- ✅ Horizontal quick actions bar
- ✅ 3 large action cards (Transfer, Bill Pay, More)

### Test 5.2: Account Details
**Steps**:
1. Click on "Checking Account" card

**Expected**:
- ✅ Navigate to AccountDetailActivity
- ✅ See account details:
  - Account type
  - Account number
  - Available balance
  - Transaction history (3-5 items)
- ✅ Back button works

### Test 5.3: Transaction History
**Steps**:
1. From account details, scroll down

**Expected**:
- ✅ See transaction list with:
  - Transaction type (Transfer, Deposit, etc.)
  - Amount (₫ formatted)
  - Date
  - Status
- ✅ Smooth scrolling

### Test 5.4: Quick Actions
**Steps**:
1. Return to dashboard
2. Scroll right in quick actions bar
3. Click on different quick action items

**Expected**:
- ✅ Horizontal scroll works
- ✅ Each action navigates to correct screen
- ✅ No crashes

---

## Phase 6: Transfer Flow Testing (10 minutes)

### Test 6.1: Initiate Transfer
**Steps**:
1. From dashboard, click "Transfer" card
2. Select "From Account": Checking
3. Select "Bank": Same Bank
4. Enter "To Account": `9876543210`
5. Enter "Amount": `1000000`
6. Enter "Note": `Test transfer`
7. Click "Continue"

**Expected**:
- ✅ Navigate to TransactionConfirmationActivity
- ✅ See confirmation details:
  - From account
  - To account
  - Amount
  - Fee
  - Total
- ✅ No crashes

### Test 6.2: Confirm Transfer
**Steps**:
1. Review details
2. Click "Confirm"

**Expected**:
- ✅ Navigate to OtpVerificationActivity
- ✅ See OTP input screen with 6 digit boxes
- ✅ Timer showing countdown

### Test 6.3: OTP Verification
**Steps**:
1. Enter any 6 digits (e.g., `123456`)
2. Click "Verify"

**Expected**:
- ✅ Success message: "Transfer completed successfully"
- ✅ Navigate back to dashboard
- ✅ No crashes

---

## Phase 7: Services Testing (10 minutes)

### Test 7.1: Bill Payment
**Steps**:
1. From dashboard, click "Bill Payment"
2. Select bill type: "Electricity"
3. Enter customer code: `123456789`
4. Enter amount: `500000`
5. Click "Pay Bill"

**Expected**:
- ✅ Layout loads correctly
- ✅ Success toast message
- ✅ Back button works

### Test 7.2: Mobile Top-up
**Steps**:
1. From dashboard, click quick action "Top Up"
2. Select provider: "Viettel"
3. Enter phone: `0901234567`
4. Enter amount: `100000`
5. Click "Top Up"

**Expected**:
- ✅ Spinner loads with providers
- ✅ Form fields accept input
- ✅ Success message appears
- ✅ No crashes

### Test 7.3: Ticket Booking
**Steps**:
1. From dashboard, click "Services"
2. Click "Ticket Booking" card
3. Select "Flight Tickets" radio button
4. Click "Book Flight"

**Expected**:
- ✅ Services menu shows 6 cards
- ✅ Each card clickable
- ✅ Toast message appears
- ✅ No crashes

### Test 7.4: Hotel Booking
**Steps**:
1. From Services, click "Hotel Booking"
2. Enter location: `Hanoi`
3. Enter check-in: `2025-12-01`
4. Enter check-out: `2025-12-05`
5. Enter guests: `2`
6. Click "Search Hotels"

**Expected**:
- ✅ All form fields accept input
- ✅ Date picker works (if clicked)
- ✅ Success message appears
- ✅ No crashes

### Test 7.5: Branch Locator
**Steps**:
1. From Services, click "Branch Locator"

**Expected**:
- ✅ Google Maps loads (or placeholder if no API key)
- ✅ Toolbar visible
- ✅ Back button works
- ✅ No crashes

---

## Phase 8: Profile and Settings (5 minutes)

### Test 8.1: View Profile
**Steps**:
1. From dashboard, click menu (3 dots) or navigate to Profile
2. View profile information

**Expected**:
- ✅ See user information:
  - Full name
  - Username
  - Email
  - Phone number
  - ID number
- ✅ All fields populated correctly
- ✅ Back button works

### Test 8.2: Logout
**Steps**:
1. From dashboard, click menu (3 dots)
2. Click "Logout"

**Expected**:
- ✅ Navigate back to LoginActivity
- ✅ Session cleared
- ✅ Can login again with different user

---

## Phase 9: Navigation Testing (5 minutes)

### Test 9.1: Back Navigation
**Steps**:
1. Navigate through multiple screens
2. Use back button frequently

**Expected**:
- ✅ Back button works on all screens
- ✅ Correct parent activity shown
- ✅ No crashes
- ✅ No infinite loops

### Test 9.2: Intent Navigation
**Steps**:
1. Click various buttons and cards
2. Verify correct activities open

**Expected**:
- ✅ All intents navigate to correct activities
- ✅ No crashes
- ✅ No missing activities

---

## Phase 10: UI/UX Testing (5 minutes)

### Test 10.1: Material Design
**Check**:
- ✅ Toolbars have correct colors
- ✅ Buttons have ripple effects
- ✅ Cards have elevation/shadows
- ✅ Text colors are readable
- ✅ Spacing is consistent

### Test 10.2: Responsiveness
**Check**:
- ✅ Layouts work in portrait mode
- ✅ Rotate device - layouts adjust
- ✅ No text cutoff
- ✅ No overlapping elements

### Test 10.3: Performance
**Check**:
- ✅ App launches in < 3 seconds
- ✅ Navigation is smooth
- ✅ No lag when scrolling
- ✅ No memory leaks (check in Profiler)

---

## Summary Checklist

- [ ] Build completes successfully
- [ ] APK installs without errors
- [ ] App launches without crashes
- [ ] Login works with correct credentials
- [ ] Officer login shows officer dashboard
- [ ] Customer login shows customer dashboard
- [ ] Biometric login works
- [ ] Registration flow works
- [ ] Transfer flow completes
- [ ] OTP verification works
- [ ] Bill payment works
- [ ] Mobile top-up works
- [ ] Ticket booking works
- [ ] Hotel booking works
- [ ] Branch locator opens
- [ ] Profile displays correctly
- [ ] Logout works
- [ ] Back navigation works everywhere
- [ ] UI looks professional
- [ ] No crashes or exceptions

---

## Troubleshooting

### App Crashes on Launch
```bash
adb logcat | grep FATAL
```
Check Logcat for error messages

### Build Fails
```bash
./gradlew clean
./gradlew assembleDebug --stacktrace
```

### APK Won't Install
```bash
adb uninstall com.example.mobilebanking
./gradlew installDebug
```

### Maps Not Showing
Add Google Maps API key in AndroidManifest.xml

---

**Testing Complete!** 🎉

All tests passed = App is production-ready for demonstration.

