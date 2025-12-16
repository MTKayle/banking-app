@echo off
echo ========================================
echo   OPEN FIREWALL PORT 8089
echo ========================================
echo.
echo This script will open port 8089 in Windows Firewall
echo You need to run this as Administrator!
echo.
pause

echo Adding firewall rule...
netsh advfirewall firewall add rule name="Backend Port 8089" dir=in action=allow protocol=TCP localport=8089

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo   SUCCESS! Port 8089 is now open
    echo ========================================
) else (
    echo.
    echo ERROR: Failed to add firewall rule
    echo Please run this script as Administrator!
)

echo.
pause

