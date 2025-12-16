@echo off
setlocal enabledelayedexpansion

echo ========================================
echo   USB TETHERING SOLUTION
echo ========================================
echo.
echo This solution uses USB tethering instead of Wi-Fi
echo More stable and doesn't require IP configuration
echo.

REM Find ADB path from local.properties or default location
set ADB_PATH=
if exist "local.properties" (
    for /f "tokens=2 delims==" %%a in ('findstr "sdk.dir" local.properties') do (
        set SDK_DIR=%%a
        set SDK_DIR=!SDK_DIR:\=!
        set SDK_DIR=!SDK_DIR:C:=C:!
        set ADB_PATH=!SDK_DIR!\platform-tools\adb.exe
    )
)

REM If not found, try default location
if not exist "!ADB_PATH!" (
    set ADB_PATH=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe
)

REM If still not found, try in PATH
if not exist "!ADB_PATH!" (
    where adb >nul 2>&1
    if !errorlevel! equ 0 (
        set ADB_PATH=adb
    ) else (
        echo ERROR: ADB not found!
        echo Please install Android SDK or add ADB to PATH
        pause
        exit /b 1
    )
)

echo [1/3] Checking ADB connection...
echo Using ADB: !ADB_PATH!
"!ADB_PATH!" devices
if !errorlevel! neq 0 (
    echo ERROR: ADB not found or device not connected!
    echo.
    echo Please:
    echo 1. Connect phone via USB
    echo 2. Enable USB Debugging on phone
    echo 3. Install ADB drivers if needed
    pause
    exit /b 1
)

echo.
echo [2/3] Setting up port forwarding...
"!ADB_PATH!" reverse tcp:8089 tcp:8089
if !errorlevel! equ 0 (
    echo ✓ Port forwarding set up successfully!
) else (
    echo ✗ Failed to set up port forwarding
    pause
    exit /b 1
)

echo.
echo [3/3] Status:
echo.
echo ✓ Code đã được cập nhật: CONNECTION_MODE = "USB"
echo ✓ Port forwarding đã được thiết lập
echo.
echo Bây giờ bạn có thể:
echo 1. Rebuild app: .\gradlew.bat installDebug
echo 2. Test app - nó sẽ kết nối qua USB!
echo.
echo LƯU Ý: Giữ terminal này mở trong khi test
echo        (Port forwarding sẽ dừng khi đóng terminal)
echo.
pause
endlocal
