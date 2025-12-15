@echo off
echo ========================================
echo   CHECK AND FIX RUN CONFIGURATION
echo ========================================
echo.

echo [INFO] This script will help you fix the run configuration issue.
echo.
echo The error occurs because Android Studio is using an old run configuration
echo with package name "com.example.myapplication" instead of "com.example.mobilebanking".
echo.
echo ========================================
echo   MANUAL STEPS REQUIRED:
echo ========================================
echo.
echo 1. Open Android Studio
echo 2. Go to Run ^> Edit Configurations...
echo 3. Delete the old "app" configuration (if it exists)
echo 4. Click + ^> Android App
echo 5. Set:
echo    - Name: app
echo    - Module: app
echo    - Launch: Default Activity
echo 6. Click OK
echo 7. Go to Build ^> Clean Project
echo 8. Go to Build ^> Rebuild Project
echo 9. Go to File ^> Invalidate Caches... ^> Invalidate and Restart
echo.
echo ========================================
echo   AUTOMATIC STEPS (Running now):
echo ========================================
echo.

echo [1/3] Uninstalling old apps from device/emulator...
powershell -Command "& { $env:ANDROID_HOME\platform-tools\adb.exe uninstall com.example.myapplication 2>$null }"
powershell -Command "& { $env:ANDROID_HOME\platform-tools\adb.exe uninstall com.example.mobilebanking 2>$null }"
echo Done.

echo.
echo [2/3] Cleaning build...
call gradlew.bat clean
if %errorlevel% neq 0 (
    echo WARNING: Clean failed, but continuing...
)

echo.
echo [3/3] Building app...
call gradlew.bat assembleDebug
if %errorlevel% neq 0 (
    echo ERROR: Build failed!
    echo Please check the errors above.
    pause
    exit /b 1
)

echo.
echo ========================================
echo   AUTOMATIC STEPS COMPLETED!
echo ========================================
echo.
echo Now please follow the MANUAL STEPS above to fix the run configuration
echo in Android Studio, then try running the app again.
echo.
pause


