@echo off
setlocal
cd /d "%~dp0frontend-app"

rem Prefer the bundled Node runtime used by this project, fall back to system Node
set "NODE_EXE="
if exist "C:\Users\L\.dsh\dsh-runtimes\dsh-primary-runtime\dependencies\node\bin\node.exe" set "NODE_EXE=C:\Users\L\.dsh\dsh-runtimes\dsh-primary-runtime\dependencies\node\bin\node.exe"
if not defined NODE_EXE (
  where node >nul 2>nul && set "NODE_EXE=node"
)
if not defined NODE_EXE (
  echo [ERROR] Node.js not found. Please install Node.js 18+ first: https://nodejs.org/
  pause
  exit /b 1
)

if not exist "node_modules\vite\bin\vite.js" (
  echo [ERROR] frontend dependencies are missing.
  echo Please install Node.js 18+ and run: cd frontend-app ^&^& npm install
  pause
  exit /b 1
)

echo [INFO] Starting Qingshou frontend at http://localhost:5174
start "" http://localhost:5174
"%NODE_EXE%" "node_modules\vite\bin\vite.js" --host 0.0.0.0 --port 5174
pause