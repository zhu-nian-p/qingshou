@echo off
rem Stop the backend process listening on port 8080.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0stop-backend.ps1"
