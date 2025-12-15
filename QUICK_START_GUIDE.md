# Mobile Banking App - Quick Start Guide

## 🚀 Getting Started in 5 Minutes

### Step 1: Open Project
```
1. Open Android Studio
2. File → Open → Select the 'cuoiki' folder
3. Wait for Gradle sync to complete
```

### Step 2: Build Project
```bash
# In Android Studio terminal or command line:
./gradlew assembleDebug
```
**Expected Output**: `BUILD SUCCESSFUL`

### Step 3: Run on Device
```
1. Connect Android device or start emulator
2. Click the green "Run" button in Android Studio
3. Select your device
4. Wait for installation
```

### Step 4: Login and Test
```
Username: customer1
Password: 123456
```

---

## 📱 Test Scenarios

### Scenario 1: Customer Login & Dashboard
1. Launch app
2. Enter `customer1` / `123456`
3. Click "Login"
4. ✅ Should see Customer Dashboard with:
   - Welcome message
   - Total balance
   - Three accounts (Checking, Savings, Mortgage)
   - Quick action buttons

### Scenario 2: Money Transfer
1. From dashboard, click "Transfer"
2. Select from account
3. Select bank (Same Bank)
4. Enter recipient account: `9876543210`
5. Enter amount: `1000000`
6. Enter note: `Test transfer`
7. Click "Continue"
8. Review details, click "Confirm"
9. Enter OTP: `123456` (any 6 digits)
10. ✅ Should see success message

### Scenario 3: View Account Details
1. From dashboard, click on any account card
2. ✅ Should see:
   - Account type and number
   - Current balance
   - Transaction history
   - (For Savings: interest rate, monthly profit)
   - (For Mortgage: loan amount, monthly payment)

### Scenario 4: Bill Payment
1. From dashboard, click "Bill Payment"
2. Select bill type: "Electricity"
3. Enter customer code: `123456789`
4. Enter amount: `500000`
5. Click "Pay Bill"
6. ✅ Should see success message

### Scenario 5: Mobile Top-up
1. From dashboard, click quick action "Top Up"
2. Select provider: "Viettel"
3. Enter phone: `0901234567`
4. Enter amount: `100000`
5. Click "Top Up"
6. ✅ Should see success message

### Scenario 6: Branch Locator
1. From dashboard, click "ATM/Branch" or navigate to Services → Branch Locator
2. ✅ Should see Google Maps with bank branch markers
3. Click on marker to see branch details

### Scenario 7: Officer Login
1. Logout from customer account
2. Login with `officer1` / `123456`
3. ✅ Should see Officer Dashboard with:
   - Customer Management
   - Account Management
   - Transaction Monitoring
   - Reports

### Scenario 8: Biometric Login
1. From login screen, click "Login with Face ID"
2. ✅ Should see face scan animation
3. Wait 3-4 seconds
4. ✅ Should auto-login to customer dashboard

### Scenario 9: Registration
1. From login screen, click "Register"
2. Fill all fields:
   - Full Name: `Test User`
   - Email: `test@example.com`
   - Phone: `0901234567`
   - ID Number: `123456789`
   - Username: `testuser`
   - Password: `123456`
   - Confirm Password: `123456`
3. Click "Register"
4. Enter OTP: `123456` (any 6 digits)
5. ✅ Should return to login screen

### Scenario 10: Profile View
1. Login as customer
2. Click menu icon (three dots) → Profile
3. ✅ Should see user information:
   - Full name
   - Username
   - Email
   - Phone number
   - ID number

---

## 🐛 Troubleshooting

### Build Fails
```bash
# Clean and rebuild
./gradlew clean
./gradlew assembleDebug
```

### App Crashes on Launch
- Check if minimum SDK version is met (API 26+)
- Clear app data and reinstall
- Check Logcat for error messages

### Maps Not Showing
- Add Google Maps API key in AndroidManifest.xml
- Enable Maps SDK in Google Cloud Console
- Check internet permission

### Gradle Sync Issues
- File → Invalidate Caches / Restart
- Delete `.gradle` folder and sync again

---

## 📊 Mock Data Reference

### Test Credentials
| Role | Username | Password |
|------|----------|----------|
| Customer | customer1 | 123456 |
| Officer | officer1 | 123456 |

### Account Balances
| Type | Balance | Details |
|------|---------|---------|
| Checking | ₫50,000,000 | Standard account |
| Savings | ₫100,000,000 | 6.5% interest, ₫541,667/month profit |
| Mortgage | -₫500,000,000 | ₫15,000,000/month payment, 36 months remaining |

### Sample Transaction Data
- Transfer to friend: ₫5,000,000
- Salary deposit: ₫10,000,000
- Electricity bill: ₫1,500,000

---

## 🎯 Feature Checklist

Test all features to ensure everything works:

- [ ] Login with username/password
- [ ] Login with biometric (face scan)
- [ ] Register new user
- [ ] View customer dashboard
- [ ] View officer dashboard
- [ ] View account details
- [ ] View transaction history
- [ ] Money transfer
- [ ] Bill payment
- [ ] Mobile top-up
- [ ] Ticket booking
- [ ] Hotel booking
- [ ] Branch locator
- [ ] View profile
- [ ] Logout
- [ ] Navigation between screens
- [ ] Back button navigation

---

## 📸 Screenshots to Capture

For your project report, capture screenshots of:
1. Login screen
2. Customer dashboard
3. Account details
4. Transfer screen
5. Transaction confirmation
6. OTP verification
7. Bill payment
8. Services menu
9. Branch locator map
10. Profile screen
11. Officer dashboard

---

## 💡 Tips for Demonstration

1. **Start with Login**: Show both customer and officer logins
2. **Highlight UI**: Point out Material Design components
3. **Show Navigation**: Demonstrate smooth transitions
4. **Test Transfer**: Complete a full transfer flow with OTP
5. **Show Map**: Display branch locations
6. **Explain Mock Data**: Clarify that it's frontend-only
7. **Discuss Architecture**: Mention MVC pattern, adapters, etc.
8. **Show Code Quality**: Highlight clean code and comments

---

## 🔗 Quick Links

- **Full Documentation**: See `MOBILE_BANKING_README.md`
- **Bug Fixes**: See `BUG_FIX_SUMMARY.md`
- **Project Status**: See `FINAL_PROJECT_STATUS.md`
- **Completion Guide**: See `PROJECT_COMPLETION_GUIDE.md`

---

## ✅ Pre-Demonstration Checklist

Before presenting:
- [ ] App builds successfully
- [ ] App installs on device/emulator
- [ ] Login works
- [ ] Dashboard loads
- [ ] At least 3 features tested
- [ ] Screenshots captured
- [ ] Presentation notes prepared
- [ ] Demo device charged/ready

---

**Ready to demonstrate!** 🎉

*For questions or issues, refer to the comprehensive documentation files.*

