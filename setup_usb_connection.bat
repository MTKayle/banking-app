@echo off
echo ========================================
echo   SETUP USB CONNECTION + ADB REVERSE
echo ========================================
echo.
echo This is the EASIEST and MOST STABLE method!
echo.
echo Steps:
echo 1. Connect phone to computer via USB
echo 2. Enable USB Debugging on phone
echo 3. Run this script
echo.
pause

echo.
echo [1/3] Checking ADB connection...
adb devices
if %errorlevel% neq 0 (
    echo.
    echo ERROR: ADB not found or phone not connected!
    echo.
    echo Please:
    echo   1. Install Android SDK Platform Tools
    echo   2. Connect phone via USB
    echo   3. Enable USB Debugging
    echo   4. Allow USB Debugging on phone
    echo.
    pause
    exit /b 1
)

echo.
echo [2/3] Setting up port forwarding...
adb reverse tcp:8089 tcp:8089

if %errorlevel% equ 0 (
    echo ✓ Port forwarding set up successfully!
    echo.
    echo [3/3] Configuration complete!
    echo.
    echo ========================================
    echo   NEXT STEPS
    echo ========================================
    echo.
    echo 1. In ApiClient.java, change:
    echo    USE_REAL_DEVICE = false
    echo    (to use localhost instead of IP)
    echo.
    echo 2. Or keep USE_REAL_DEVICE = true
    echo    and set IP to: localhost
    echo.
    echo 3. Rebuild app: .\gradlew.bat installDebug
    echo.
    echo 4. Test app - it will connect via USB!
    echo.
    echo NOTE: Keep USB connected while testing
    echo.
) else (
    echo.
    echo ERROR: Failed to set up port forwarding
    echo Please check USB connection and try again
)

echo.
pause

