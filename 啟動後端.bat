@echo off
chcp 65001 >nul
cd /d "%~dp0backend"

if not exist ".env" (
  echo Missing backend\.env
  echo Copy backend\.env.example to backend\.env, then fill in the Aiven MySQL password.
  pause
  exit /b 1
)

if not exist "node_modules" (
  echo Installing backend dependencies...
  call npm install
  if errorlevel 1 (
    echo npm install failed.
    pause
    exit /b 1
  )
)

echo Starting backend on http://localhost:4000
call npm start
pause
