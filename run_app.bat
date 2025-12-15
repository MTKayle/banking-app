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
powershell -Command "$env:ANDROID_HOME\platform-tools\adb.exe shell am start -n com.example.mobilebanking/com.example.mobilebanking.activities.LoginActivity"

echo.
echo ========================================
echo   APP LAUNCHED SUCCESSFULLY!
echo ========================================
echo.
echo Check your emulator for the app.
echo.
pause

