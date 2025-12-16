@echo off
echo ========================================
echo   CHECK HOTSPOT IP
echo ========================================
echo.
echo Finding IP address when Mobile Hotspot is enabled...
echo.

ipconfig | findstr /i "192.168.137 192.168.43"

echo.
echo ========================================
echo   INSTRUCTIONS
echo ========================================
echo.
echo When Mobile Hotspot is enabled, your computer IP is usually:
echo   - 192.168.137.1 (Windows 10/11)
echo   - 192.168.43.1 (some versions)
echo.
echo Update this IP in ApiClient.java:
echo   private static final String IP_MÁY_TÍNH_CỦA_BẠN = "192.168.137.1";
echo.
echo Then rebuild app: .\gradlew.bat installDebug
echo.
pause


