@echo off
echo ========================================
echo   FIX CONNECTION - MOBILE HOTSPOT
echo ========================================
echo.

REM Check if running as admin
net session >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: This script must be run as Administrator!
    echo.
    echo Right-click and select "Run as administrator"
    pause
    exit /b 1
)

echo [1/3] Opening Firewall Port 8089...
netsh advfirewall firewall delete rule name="Backend Port 8089" >nul 2>&1
netsh advfirewall firewall add rule name="Backend Port 8089" dir=in action=allow protocol=TCP localport=8089

if %errorlevel% equ 0 (
    echo ✓ Firewall port 8089 opened successfully!
) else (
    echo ✗ Failed to open firewall port
    pause
    exit /b 1
)

echo.
echo [2/3] Checking IP address...
echo.
echo Your Hotspot IP should be: 192.168.137.1
echo.
ipconfig | findstr /i "192.168.137"

echo.
echo [3/3] Testing backend connection...
echo.
echo Testing: http://192.168.137.1:8089
echo.

curl -s -o nul -w "HTTP Status: %%{http_code}\n" http://192.168.137.1:8089 2>nul
if %errorlevel% equ 0 (
    echo ✓ Backend is accessible!
) else (
    echo ⚠ Could not test connection (curl not found or backend not running)
    echo   Please test manually from your phone browser:
    echo   http://192.168.137.1:8089
)

echo.
echo ========================================
echo   NEXT STEPS
echo ========================================
echo.
echo 1. Make sure backend is running on port 8089
echo 2. Make sure Mobile Hotspot is enabled on your computer
echo 3. Phone is connected to your hotspot
echo 4. Test from phone browser: http://192.168.137.1:8089
echo 5. Rebuild app: .\gradlew.bat installDebug
echo.
echo IP in ApiClient.java should be: 192.168.137.1
echo.
pause

