@echo off
echo ========================================
echo Rebuild and Install Banking App
echo ========================================
echo.

echo [1/4] Uninstalling old app...
"C:\Users\HUNG\AppData\Local\Android\Sdk\platform-tools\adb.exe" uninstall com.example.mobilebanking
echo.

echo [2/4] Building new APK...
cd /d "%~dp0"
call gradlew.bat assembleDebug
if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    pause
    exit /b 1
)
echo.

echo [3/4] Installing new app...
"C:\Users\HUNG\AppData\Local\Android\Sdk\platform-tools\adb.exe" install "app\build\outputs\apk\debug\app-debug.apk"
if %ERRORLEVEL% NEQ 0 (
    echo Install failed!
    pause
    exit /b 1
)
echo.

echo [4/4] Launching app...
"C:\Users\HUNG\AppData\Local\Android\Sdk\platform-tools\adb.exe" shell am start -n com.example.mobilebanking/.activities.LoginActivity
echo.

echo ========================================
echo Done! App installed and launched.
echo ========================================
pause
