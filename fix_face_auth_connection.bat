@echo off
setlocal enabledelayedexpansion
echo ========================================
echo   FIX FACE AUTHENTICATION CONNECTION
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

echo [1/5] Checking current IP addresses...
echo.
echo Your computer IPs:
ipconfig | findstr /i "IPv4"
echo.

echo [2/5] Opening Firewall Port 8089...
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
echo [3/5] Finding Wi-Fi IP address...
set WIFI_IP=

REM Get Wi-Fi IP (look for 10.0.x.x addresses)
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /i /c:"IPv4 Address"') do (
    set IP=%%a
    set IP=!IP: =!
    echo Found IP: !IP!
    
    REM Check if it's in 10.0.x.x range
    echo !IP! | findstr /r "^10\.0\." >nul
    if !errorlevel! equ 0 (
        set WIFI_IP=!IP!
        echo ✓ Wi-Fi IP found: !WIFI_IP!
        goto :found_ip
    )
)

:found_ip
if "!WIFI_IP!"=="" (
    echo ⚠ Could not find Wi-Fi IP in 10.0.x.x range
    echo Please check your Wi-Fi connection
    echo.
    echo Your IPs:
    ipconfig | findstr /i "IPv4"
    echo.
    echo Please manually update IP_MÁY_TÍNH_CỦA_BẠN in ApiClient.java
    pause
    exit /b 1
)

echo.
echo [4/5] Testing localhost connection...
curl -s -o nul -w "Localhost: HTTP %%{http_code}\n" http://localhost:8089 2>nul
if %errorlevel% neq 0 (
    echo   ⚠ Backend might not be running!
    echo   Please start backend in IntelliJ IDEA
) else (
    echo   ✓ Backend is running on localhost
)

echo.
echo [5/5] Testing Wi-Fi IP connection...
curl -s -o nul -w "Wi-Fi IP: HTTP %%{http_code}\n" http://!WIFI_IP!:8089 2>nul
if %errorlevel% neq 0 (
    echo   ⚠ Cannot connect to Wi-Fi IP
    echo   This might be a firewall or backend configuration issue
) else (
    echo   ✓ Wi-Fi IP is accessible
)

echo.
echo ========================================
echo   DIAGNOSIS
echo ========================================
echo.
echo Current Wi-Fi IP: !WIFI_IP!
echo IP in ApiClient.java: 10.0.221.236
echo.
if not "!WIFI_IP!"=="10.0.221.236" (
    echo ⚠ WARNING: IP mismatch!
    echo.
    echo Your current IP is: !WIFI_IP!
    echo But ApiClient.java has: 10.0.221.236
    echo.
    echo SOLUTION:
    echo 1. Update ApiClient.java with IP: !WIFI_IP!
    echo 2. Rebuild the app: .\gradlew.bat installDebug
    echo.
) else (
    echo ✓ IP matches!
)

echo.
echo ========================================
echo   CHECKLIST
echo ========================================
echo.
echo Please verify:
echo   1. Backend is running (http://localhost:8089)
echo   2. Backend has server.address=0.0.0.0 in application.properties
echo   3. Backend was restarted after adding server.address
echo   4. Phone and computer are on the SAME Wi-Fi network
echo   5. Phone IP should be in same subnet (10.0.221.x)
echo   6. Test from phone browser: http://!WIFI_IP!:8089/api/auth/register
echo.
echo If phone IP is 10.0.220.x but computer is 10.0.221.x:
echo   - They are NOT on the same network!
echo   - Connect both to the SAME Wi-Fi network
echo.
echo If all above are correct, rebuild app:
echo   .\gradlew.bat installDebug
echo.
endlocal
pause

