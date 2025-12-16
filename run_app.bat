@echo off
echo ========================================
echo   MOBILE BANKING APP - RUN SCRIPT
echo ========================================
echo.

echo [1/4] Cleaning build...
call gradlew.bat clean
if %errorlevel% neq 0 (
    echo WARNING: Clean failed, but continuing...
)

echo.
echo [2/4] Building app...
call gradlew.bat assembleDebug
if %errorlevel% neq 0 (
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo.
echo [3/4] Installing app...
call gradlew.bat installDebug
if %errorlevel% neq 0 (
    echo ERROR: Install failed!
    pause
    exit /b 1
)

echo.
echo [4/4] Launching app...
echo.
echo Opening Mobile Banking app on emulator...
echo Login with: customer1 / 123456
echo.

REM Start the app using am (activity manager)
echo Starting LoginActivity...

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
    "%ADB_PATH%" shell am start -n com.example.mobilebanking/com.example.mobilebanking.activities.LoginActivity
    if %errorlevel% equ 0 (
        echo App launched successfully!
    ) else (
        echo WARNING: Could not launch app automatically. Please open it manually from your device.
    )
) else (
    REM Try using adb from PATH
    adb shell am start -n com.example.mobilebanking/com.example.mobilebanking.activities.LoginActivity
    if %errorlevel% neq 0 (
        echo WARNING: Could not find adb. Please open the app manually from your device.
        echo App package: com.example.mobilebanking
    )
)

echo.
echo ========================================
echo   APP LAUNCHED SUCCESSFULLY!
echo ========================================
echo.
echo Check your emulator for the app.
echo.
pause

