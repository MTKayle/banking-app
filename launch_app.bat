@echo off
echo ========================================
echo   LAUNCH MOBILE BANKING APP
echo ========================================
echo.

REM Try to find adb in common locations
set ADB_PATH=
if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" (
    set ADB_PATH=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe
) else if exist "%ANDROID_HOME%\platform-tools\adb.exe" (
    set ADB_PATH=%ANDROID_HOME%\platform-tools\adb.exe
) else if exist "%USERPROFILE%\AppData\Local\Android\Sdk\platform-tools\adb.exe" (
    set ADB_PATH=%USERPROFILE%\AppData\Local\Android\Sdk\platform-tools\adb.exe
)

if defined ADB_PATH (
    echo Found adb at: %ADB_PATH%
    echo Launching Mobile Banking app...
    "%ADB_PATH%" shell am start -n com.example.mobilebanking/com.example.mobilebanking.activities.LoginActivity
    if %errorlevel% equ 0 (
        echo.
        echo ========================================
        echo   APP LAUNCHED SUCCESSFULLY!
        echo ========================================
    ) else (
        echo.
        echo ERROR: Could not launch app.
        echo Please check:
        echo   1. Device/Emulator is connected
        echo   2. USB Debugging is enabled
        echo   3. App is installed on device
    )
) else (
    REM Try using adb from PATH
    echo Trying to use adb from PATH...
    adb shell am start -n com.example.mobilebanking/com.example.mobilebanking.activities.LoginActivity
    if %errorlevel% neq 0 (
        echo.
        echo ERROR: Could not find adb.
        echo Please:
        echo   1. Install Android SDK Platform Tools
        echo   2. Add adb to PATH, OR
        echo   3. Open the app manually from your device
        echo.
        echo App package: com.example.mobilebanking
    ) else (
        echo.
        echo ========================================
        echo   APP LAUNCHED SUCCESSFULLY!
        echo ========================================
    )
)

echo.
pause
