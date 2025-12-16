@echo off
echo ========================================
echo   FINAL FIX - WI-FI CONNECTION
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

echo [1/4] Opening Firewall Port 8089...
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
echo [2/4] Checking IP address...
echo.
echo Your Wi-Fi IP should be: 10.0.221.236
echo.
ipconfig | findstr /i "10.0.221"

echo.
echo [3/4] Testing localhost connection...
curl -s -o nul -w "Localhost: HTTP %%{http_code}\n" http://localhost:8089 2>nul
if %errorlevel% neq 0 (
    echo   ⚠ Backend might not be running!
    echo   Please start backend in IntelliJ IDEA
) else (
    echo   ✓ Backend is running on localhost
)

echo.
echo [4/4] Testing Wi-Fi IP connection...
curl -s -o nul -w "Wi-Fi IP: HTTP %%{http_code}\n" http://10.0.221.236:8089 2>nul
if %errorlevel% neq 0 (
    echo   ⚠ Cannot connect to Wi-Fi IP
    echo   This might be a firewall or backend configuration issue
) else (
    echo   ✓ Wi-Fi IP is accessible
)

echo.
echo ========================================
echo   CHECKLIST
echo ========================================
echo.
echo ✓ IP in code: 10.0.221.236
echo ✓ Firewall: Port 8089 opened
echo.
echo Please verify:
echo   1. Backend is running (http://localhost:8089)
echo   2. Backend has server.address=0.0.0.0 in application.properties
echo   3. Backend was restarted after adding server.address
echo   4. Phone and computer are on the SAME Wi-Fi network
echo   5. Test from phone browser: http://10.0.221.236:8089/api/auth/register
echo.
echo If all above are correct, rebuild app:
echo   .\gradlew.bat installDebug
echo.
pause

