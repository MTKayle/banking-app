@echo off
echo ========================================
echo   LAUNCHING MOBILE BANKING APP
echo ========================================
echo.

REM Find adb.exe
set ADB_PATH=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe
if not exist "%ADB_PATH%" (
    set ADB_PATH=%ANDROID_HOME%\platform-tools\adb.exe
)
if not exist "%ADB_PATH%" (
    echo ERROR: adb.exe not found!
    echo Please set ANDROID_HOME environment variable.
    pause
    exit /b 1
)

echo Using ADB: %ADB_PATH%
echo.

echo Starting LoginActivity...
"%ADB_PATH%" shell am start -n com.example.mobilebanking/.activities.LoginActivity

echo.
echo ========================================
echo   APP LAUNCHED!
echo ========================================
echo.
echo Check your emulator/device.
echo Login credentials:
echo   Username: customer1
echo   Password: 123456
echo.
pause

