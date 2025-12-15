# Mobile Banking Android Application - Frontend Implementation

## Project Overview
This is a comprehensive mobile banking application frontend developed in Java for Android. The application simulates a full-featured banking app similar to Vietcombank, BIDV, or OCB apps in Vietnam.

## Technology Stack
- **Platform**: Android (Native)
- **Language**: Java
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)
- **Build Tool**: Gradle with Kotlin DSL
- **UI Framework**: Material Design Components
- **Architecture**: MVC Pattern

## Project Structure

```
app/src/main/java/com/example/mobilebanking/
├── activities/          # All activity classes
│   ├── LoginActivity.java
│   ├── RegisterActivity.java
│   ├── OtpVerificationActivity.java
│   ├── BiometricAuthActivity.java
│   ├── CustomerDashboardActivity.java
│   ├── OfficerDashboardActivity.java
│   ├── AccountDetailActivity.java
│   ├── TransferActivity.java
│   ├── TransactionConfirmationActivity.java
│   ├── BillPaymentActivity.java
│   ├── MobileTopUpActivity.java
│   ├── TicketBookingActivity.java
│   ├── HotelBookingActivity.java
│   ├── ServicesActivity.java
│   ├── BranchLocatorActivity.java
│   └── ProfileActivity.java
├── models/              # Data models
│   ├── User.java
│   ├── Account.java
│   ├── Transaction.java
│   ├── BankBranch.java
│   └── QuickAction.java
├── adapters/            # RecyclerView adapters
│   ├── AccountAdapter.java
│   ├── QuickActionAdapter.java
│   ├── TransactionAdapter.java
│   └── BranchAdapter.java
└── utils/               # Utility classes
    └── DataManager.java
```

## Features Implemented

### 1. Authentication System
- **Login Screen**: Username/password authentication with mock validation
- **Registration Screen**: New user registration with form validation
- **OTP Verification**: 6-digit OTP input with timer and resend functionality
- **Biometric Authentication**: Face scan simulation UI

### 2. User Roles
- **Customer Interface**: Full banking features for customers
- **Officer Interface**: Separate dashboard for bank officers
- Role-based navigation and access control

### 3. Account Management
- **Dashboard**: Overview of all accounts and total balance
- **Account Types**:
  - Checking Account: Standard transaction account
  - Savings Account: With interest rate and monthly profit display
  - Mortgage Account: Loan details with payment schedule
- **Transaction History**: List of recent transactions per account

### 4. Transaction Features
- **Money Transfer**: Within same bank transfers
- **Inter-bank Transfer**: Transfer to other banks
- **Deposit/Withdrawal**: Cash operations
- **Transaction Confirmation**: 2FA/OTP verification before completion

### 5. Utility Services
- **Bill Payment**: Electricity, water, internet bills
- **Mobile Top-up**: Prepaid mobile recharge
- **Ticket Booking**: Flight and movie tickets
- **Hotel Booking**: Hotel reservation interface
- **E-commerce Payment**: Payment gateway simulation

### 6. Map Integration
- **Branch Locator**: Google Maps integration showing bank branches
- **ATM Locations**: Display nearby ATMs
- **Navigation**: Route to nearest branch

## Mock Data

The application uses mock data stored in `DataManager.java`:

### Default Users
1. **Customer Account**
   - Username: `customer1`
   - Password: `123456`
   - Role: CUSTOMER

2. **Officer Account**
   - Username: `officer1`
   - Password: `123456`
   - Role: OFFICER

### Mock Accounts (per user)
- Checking Account: ₫50,000,000
- Savings Account: ₫100,000,000 (6.5% interest rate)
- Mortgage Account: -₫500,000,000 (₫15,000,000/month payment)

### Bank Branches (Hanoi)
- Hoan Kiem Branch
- Cau Giay Branch
- Dong Da Branch
- Ba Dinh Branch

## Key Components

### DataManager
Singleton class managing:
- Mock user data
- Mock account data
- Mock transaction data
- Bank branch locations
- User session management (SharedPreferences)

### Adapters
- **AccountAdapter**: Displays account cards in RecyclerView
- **QuickActionAdapter**: Horizontal scrolling quick action buttons
- **TransactionAdapter**: Transaction history list
- **BranchAdapter**: Bank branch list for map

### Material Design Components
- TextInputLayout with validation
- MaterialButton with custom styling
- CardView for content containers
- RecyclerView for lists
- CoordinatorLayout for scrolling behavior
- AppBarLayout with Toolbar

## UI/UX Features

### Design Principles
- Material Design 3 guidelines
- Consistent color scheme (Blue primary theme)
- Responsive layouts for different screen sizes
- Smooth transitions between screens
- Loading states and error handling
- Form validation with user feedback

### Navigation
- Intent-based navigation between activities
- Parent activity relationships for back navigation
- Role-based routing (Customer vs Officer dashboards)
- Session management with auto-login

## Setup Instructions

### 1. Prerequisites
- Android Studio (latest version)
- JDK 8 or higher
- Android SDK API 26+
- Google Play Services (for Maps)

### 2. Configuration

#### Google Maps API Key
1. Get API key from Google Cloud Console
2. Update in `AndroidManifest.xml`:
```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_API_KEY_HERE" />
```

### 3. Build and Run
```bash
# Sync Gradle
./gradlew sync

# Build Debug APK
./gradlew assembleDebug

# Install on device
./gradlew installDebug
```

## Testing the Application

### Login Flow
1. Launch app → Login screen appears
2. Enter credentials: `customer1` / `123456`
3. Click Login → Navigate to Customer Dashboard
4. Or click "Login with Face ID" → Biometric simulation

### Registration Flow
1. Click "Register" on login screen
2. Fill all required fields
3. Click Register → OTP verification screen
4. Enter any 6-digit code → Success, return to login

### Dashboard Features
1. View total balance across all accounts
2. Click on account card → Account details
3. Use quick action buttons for services
4. Access menu for Profile and Logout

### Transfer Money
1. Dashboard → Transfer button
2. Select from/to accounts
3. Enter amount and note
4. Confirm → OTP verification
5. Success message

## Future Enhancements (Backend Integration)

When connecting to a real backend:
1. Replace mock data with API calls
2. Implement real authentication (JWT tokens)
3. Add network error handling
4. Implement data caching
5. Add push notifications
6. Real-time transaction updates
7. Secure storage for sensitive data

## Code Quality

### Best Practices Followed
- Clean code with meaningful variable names
- Comprehensive comments
- Proper error handling
- Input validation
- Memory leak prevention
- Lifecycle-aware components
- Separation of concerns

### Android Lifecycle Management
- Proper onCreate/onDestroy handling
- SavedInstanceState for configuration changes
- Timer cleanup in onDestroy
- Activity result handling

## Troubleshooting

### Common Issues

1. **Build Errors**
   - Sync Gradle files
   - Clean and rebuild project
   - Check SDK versions

2. **Maps Not Showing**
   - Verify API key is correct
   - Enable Maps SDK in Google Cloud Console
   - Check internet permission

3. **App Crashes**
   - Check Logcat for stack traces
   - Verify all activities are declared in Manifest
   - Check for null pointer exceptions

## Project Deliverables

✅ Complete Android Java project structure
✅ All authentication screens (Login, Register, OTP, Biometric)
✅ Customer and Officer dashboards
✅ Account management screens
✅ Transaction screens
✅ Utility service screens
✅ Map integration for branch locator
✅ Material Design UI components
✅ Mock data for demonstration
✅ Clean, commented code
✅ Responsive layouts

## Contact & Support

For questions or issues with this project:
- Review code comments for implementation details
- Check Android documentation for component usage
- Refer to Material Design guidelines for UI patterns

## License

This is an educational project for Mobile Apps Development course.

---

**Note**: This is a frontend-only implementation. No actual banking transactions are performed. All data is mock data for demonstration purposes only.

