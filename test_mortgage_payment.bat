@echo off
echo ========================================
echo Test Mortgage Payment Flow
echo ========================================
echo.
echo Watching logcat for MortgagePayment and OtpVerification...
echo Press Ctrl+C to stop
echo.
echo Expected logs:
echo   - MortgagePayment: Phone for OTP: [phone_number]
echo   - OtpVerification: FROM_ACTIVITY key value: MORTGAGE_PAYMENT
echo   - OtpVerification: MORTGAGE_PAYMENT - Processing payment
echo.
echo ========================================
echo.

"C:\Users\HUNG\AppData\Local\Android\Sdk\platform-tools\adb.exe" logcat -c
"C:\Users\HUNG\AppData\Local\Android\Sdk\platform-tools\adb.exe" logcat | findstr /i "MortgagePayment OtpVerification"
