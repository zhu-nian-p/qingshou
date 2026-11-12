@echo off
rem Start backend. Real logic lives in start-backend.ps1 (port check + file logging).
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0start-backend.ps1"
