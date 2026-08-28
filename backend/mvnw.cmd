@echo off
setlocal
set "SCRIPT=%~dp0mvnw.ps1"
powershell -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT%" %*
exit /b %ERRORLEVEL%
