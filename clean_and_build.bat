@echo off
echo ========================================
echo Clean and Build Android Project
echo ========================================
echo.

REM Chuyển đến thư mục project
cd /d "%~dp0"

echo [1/3] Cleaning project...
call gradlew.bat clean
if %errorlevel% neq 0 (
    echo ERROR: Clean failed!
    pause
    exit /b %errorlevel%
)
echo Clean completed successfully!
echo.

echo [2/3] Building project...
call gradlew.bat build
if %errorlevel% neq 0 (
    echo ERROR: Build failed!
    pause
    exit /b %errorlevel%
)
echo Build completed successfully!
echo.

echo [3/3] Assembling debug APK...
call gradlew.bat assembleDebug
if %errorlevel% neq 0 (
    echo ERROR: Assemble failed!
    pause
    exit /b %errorlevel%
)
echo Assemble completed successfully!
echo.

echo ========================================
echo BUILD SUCCESSFUL!
echo ========================================
echo APK location: app\build\outputs\apk\debug\app-debug.apk
echo.
pause
