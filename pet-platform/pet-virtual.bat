@echo off
rem Pet Virtual Platform Management Script (Windows)
rem Usage: pet-virtual.bat {start|stop|status}

setlocal enabledelayedexpansion

set APP_NAME=pet-virtual
set JAR_NAME=pet-virtual.jar
set JAR_PATH=pet-virtual\target\%JAR_NAME%
set LOG_FILE=logs\virtual.log
set PROFILE=prod

if "%1"=="" goto usage
if "%1"=="start" goto start
if "%1"=="stop" goto stop
if "%1"=="status" goto status
goto usage

:start
echo Starting %APP_NAME%...

if not exist "%JAR_PATH%" (
    echo Error: JAR file not found at %JAR_PATH%
    echo Please run 'mvn clean package' first
    exit /b 1
)

if not exist logs mkdir logs

start /b javaw -jar "%JAR_PATH%" --spring.profiles.active=%PROFILE% > "%LOG_FILE%" 2>&1

timeout /t 3 /nobreak >nul

echo %APP_NAME% started successfully
echo View logs: type logs\virtual.log
goto end

:stop
echo Stopping %APP_NAME%...

for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8182 ^| findstr LISTENING') do (
    taskkill /F /PID %%a
)

echo %APP_NAME% stopped
goto end

:status
netstat -ano | findstr :8182 | findstr LISTENING >nul
if %errorlevel%==0 (
    echo %APP_NAME% is running on port 8182
) else (
    echo %APP_NAME% is not running
)
goto end

:usage
echo Usage: %0 {start^|stop^|status}
echo.
echo Commands:
echo   start   - Start the application
echo   stop    - Stop the application
echo   status  - Check application status
exit /b 1

:end
endlocal
