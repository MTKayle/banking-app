@echo off
echo ========================================
echo Install and Run Android App
echo ========================================
echo.

REM Chuyển đến thư mục project
cd /d "%~dp0"

echo [1/4] Checking connected devices...
adb devices
echo.

echo [2/4] Building debug APK...
call gradlew.bat assembleDebug
if %errorlevel% neq 0 (
    echo ERROR: Build failed!
    pause
    exit /b %errorlevel%
)
echo Build completed!
echo.

echo [3/4] Installing APK to device...
adb install -r app\build\outputs\apk\debug\app-debug.apk
if %errorlevel% neq 0 (
    echo ERROR: Installation failed!
    echo Make sure:
    echo 1. Device is connected via USB
    echo 2. USB debugging is enabled
    echo 3. Device is authorized
    pause
    exit /b %errorlevel%
)
echo Installation completed!
echo.

echo [4/4] Launching app...
adb shell am start -n com.example.mobilebanking/.activities.LoginActivity
if %errorlevel% neq 0 (
    echo ERROR: Failed to launch app!
    pause
    exit /b %errorlevel%
)
echo.

echo ========================================
echo APP LAUNCHED SUCCESSFULLY!
echo ========================================
echo.
pause
