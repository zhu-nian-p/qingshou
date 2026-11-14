@echo off
setlocal
cd /d "%~dp0frontend-app"

where node >nul 2>nul
if errorlevel 1 (
  echo [ERROR] Node.js not found. Please install Node.js 18+ first: https://nodejs.org/
  pause
  exit /b 1
)

if not exist "node_modules" (
  echo [INFO] Installing frontend dependencies, please wait...
  call npm install
  if errorlevel 1 (
    echo [ERROR] npm install failed.
    pause
    exit /b 1
  )
)

echo [INFO] Starting Vite dev server at http://localhost:5174
start "" http://localhost:5174
call npm run dev
pause