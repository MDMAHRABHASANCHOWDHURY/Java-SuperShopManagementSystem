@echo off
setlocal
cd /d "%~dp0"
if not exist build mkdir build
javac -encoding UTF-8 -cp "lib/*" -sourcepath src -d build src\Main.java
if errorlevel 1 (
    echo Compilation failed.
    pause
    exit /b 1
)
echo Amar Dokan compiled successfully.
