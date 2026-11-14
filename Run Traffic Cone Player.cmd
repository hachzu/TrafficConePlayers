@echo off
setlocal
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\run-player.ps1" %*
set "PLAYER_EXIT=%ERRORLEVEL%"
if not "%PLAYER_EXIT%"=="0" pause
exit /b %PLAYER_EXIT%
