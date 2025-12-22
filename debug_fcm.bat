@echo off
echo ========================================
echo Debug FCM Token and Notifications
echo ========================================
echo.

REM Chuyển đến thư mục project
cd /d "%~dp0"

echo Checking connected devices...
adb devices
echo.

echo ========================================
echo Watching FCM logs...
echo Press Ctrl+C to stop
echo ========================================
echo.

REM Filter logs for FCM only
adb logcat -c
adb logcat | findstr /I "FCM FcmToken FirebaseMessaging MyFirebaseMessagingService notification"

pause
