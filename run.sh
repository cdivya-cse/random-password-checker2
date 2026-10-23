#!/usr/bin/env bash
echo "=================================================================="
echo "  Building and Running SecurePass (Java OOP Backend Server)"
echo "=================================================================="

mkdir -p bin

echo "[1/2] Compiling Java source files..."
javac -d bin src/*.java
if [ $? -ne 0 ]; then
    echo "[ERROR] Compilation failed. Please check your JDK installation."
    exit 1
fi

echo "[2/2] Starting PasswordServer on http://localhost:8080 ..."
java -cp bin PasswordServer
