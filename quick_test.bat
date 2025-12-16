@echo off
echo ========================================
echo   QUICK CONNECTION TEST
echo ========================================
echo.

echo [1] Testing localhost connection...
curl -s -o nul -w "Localhost: HTTP %%{http_code}\n" http://localhost:8089 2>nul
if %errorlevel% neq 0 (
    echo   ⚠ Backend might not be running!
    echo   Please start backend first.
    echo.
) else (
    echo   ✓ Backend is running on localhost
    echo.
)

echo [2] Testing hotspot IP connection...
curl -s -o nul -w "Hotspot IP: HTTP %%{http_code}\n" http://192.168.137.1:8089 2>nul
if %errorlevel% neq 0 (
    echo   ⚠ Cannot connect to hotspot IP
    echo   This might be a firewall issue.
    echo.
) else (
    echo   ✓ Hotspot IP is accessible
    echo.
)

echo [3] Checking firewall rules...
netsh advfirewall firewall show rule name="Backend Port 8089" >nul 2>&1
if %errorlevel% equ 0 (
    echo   ✓ Firewall rule exists
) else (
    echo   ⚠ Firewall rule not found!
    echo   Run fix_connection.bat as Administrator
)

echo.
echo ========================================
echo   RECOMMENDATIONS
echo ========================================
echo.
echo If backend is not running:
echo   1. Open IntelliJ IDEA
echo   2. Run the Spring Boot application
echo   3. Wait for "Started IbankingApplication" message
echo.
echo If connection is slow:
echo   - Try: http://192.168.137.1:8089/api/auth/register
echo   - Check firewall settings
echo   - Restart Mobile Hotspot
echo.
pause


