@echo off
setlocal
set "ROOT=%~dp0"
set "JAVA_HOME=%ROOT%.tools\jdk-17.0.21+9"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "MVN=%ROOT%.tools\apache-maven-3.9.16\bin\mvn.cmd"
set "SETTINGS=%ROOT%.tools\maven-settings.xml"

cd /d "%ROOT%backend"
echo ============================================
echo   Building campus-market backend
echo ============================================
call "%MVN%" -s "%SETTINGS%" -DskipTests clean package
pause
