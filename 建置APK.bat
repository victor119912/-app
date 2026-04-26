@echo off
set JAVA_HOME=D:\android-studio\jbr
set PATH=%JAVA_HOME%\bin;%PATH%
cd /d "%~dp0mobile-app"
gradlew.bat :app:assembleDebug
echo.
echo APK 位置：
echo %~dp0mobile-app\app\build\outputs\apk\debug\app-debug.apk
pause
