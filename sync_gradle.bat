@echo off
echo ========================================
echo Sync Gradle Dependencies
echo ========================================
echo.

REM Chuyển đến thư mục project
cd /d "%~dp0"

echo [1/2] Downloading dependencies...
call gradlew.bat --refresh-dependencies
if %errorlevel% neq 0 (
    echo ERROR: Sync failed!
    pause
    exit /b %errorlevel%
)
echo.

echo [2/2] Building project to verify...
call gradlew.bat build -x test
if %errorlevel% neq 0 (
    echo ERROR: Build verification failed!
    pause
    exit /b %errorlevel%
)
echo.

echo ========================================
echo GRADLE SYNC SUCCESSFUL!
echo ========================================
echo All dependencies are up to date.
echo You can now open the project in Android Studio.
echo.
pause
