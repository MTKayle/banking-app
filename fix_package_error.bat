@echo off
echo ========================================
echo   FIX PACKAGE ERROR - UNINSTALL OLD APP
echo ========================================
echo.

echo [1/4] Uninstalling old app (com.example.myapplication)...
call gradlew.bat uninstallAll
powershell -Command "& { $env:ANDROID_HOME\platform-tools\adb.exe uninstall com.example.myapplication 2>$null }"
if %errorlevel% neq 0 (
    echo Note: Old package may not exist, continuing...
)

echo.
echo [2/4] Uninstalling current app (com.example.mobilebanking)...
powershell -Command "& { $env:ANDROID_HOME\platform-tools\adb.exe uninstall com.example.mobilebanking 2>$null }"
if %errorlevel% neq 0 (
    echo Note: App may not be installed, continuing...
)

echo.
echo [3/4] Cleaning build...
call gradlew.bat clean
if %errorlevel% neq 0 (
    echo WARNING: Clean failed, but continuing...
)

echo.
echo [4/4] Building and installing new app...
call gradlew.bat assembleDebug
if %errorlevel% neq 0 (
    echo ERROR: Build failed!
    pause
    exit /b 1
)

call gradlew.bat installDebug
if %errorlevel% neq 0 (
    echo ERROR: Install failed!
    pause
    exit /b 1
)

echo.
echo ========================================
echo   FIX COMPLETED!
echo ========================================
echo.
echo The old app has been uninstalled and the new app has been installed.
echo You can now run the app normally.
echo.
pause


