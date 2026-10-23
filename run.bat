@echo off
echo ==================================================================
echo   Building and Running SecurePass (Java OOP Backend Server)
echo ==================================================================

if not exist bin mkdir bin

echo [1/2] Compiling Java source files...
javac -d bin src/*.java
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed. Please ensure JDK 17+ or 21+ is in your PATH.
    pause
    exit /b %errorlevel%
)

echo [2/2] Starting PasswordServer on http://localhost:8080 ...
echo Opening default browser in 2 seconds...
start "" http://localhost:8080

java -cp bin PasswordServer
pause
