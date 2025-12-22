@echo off
echo ========================================
echo View Android App Logs
echo ========================================
echo.

REM Chuyển đến thư mục project
cd /d "%~dp0"

echo Checking connected devices...
adb devices
echo.

echo Starting logcat...
echo Press Ctrl+C to stop viewing logs
echo.
echo ========================================
echo.

REM Filter logs for our app only
adb logcat -s "FcmTokenManager:D" "OtpVerification:D" "LoginActivity:D" "okhttp.OkHttpClient:I" "*:E"

pause
