@echo off
rem Start a tiny static server for the frontend and open it in the browser.
cd /d "%~dp0frontend"
start "qingshou-frontend" python -m http.server 5173
timeout /t 2 >nul
start "" http://localhost:5173/index.html
