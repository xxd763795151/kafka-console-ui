@echo off

rem MAIN_CLASS=org.springframework.boot.loader.JarLauncher
rem JAVA_HOME=jre1.8.0_66

setlocal enabledelayedexpansion

set "BIN_DIR=%~dp0"
if not "%BIN_DIR:~-1%"=="\" set "BIN_DIR=%BIN_DIR%\"

set "BASE_DIR=%BIN_DIR%.."
for %%I in ("%BASE_DIR%") do set "BASE_DIR=%%~fI"

if defined JAVA_HOME (
    set "JAVA_CMD=%JAVA_HOME%\bin\java"
) else (
    echo ERROR: JAVA_HOME is not defined
    exit /b 1
)

set "JAVA_OPTS=-Xmx512m -Xms512m -Xmn256m -Xss256k -Dfile.encoding=utf-8"

set "CONFIG_FILE=%BASE_DIR%\config\application.yml"
set "TARGET=%BASE_DIR%\lib\kafka-console-ui.jar"
set "DATA_DIR=%BASE_DIR%"
set "LOG_HOME=%BASE_DIR%"

if not exist "%TARGET%" (
    echo ERROR: Jar file not found at [%TARGET%]
    exit /b 1
)
if not exist "%CONFIG_FILE%" (
    echo WARNING: Config file not found at [%CONFIG_FILE%]
)

"%JAVA_CMD%" %JAVA_OPTS% -jar "%TARGET%" --spring.config.location="%CONFIG_FILE%" --data.dir="%DATA_DIR%" --logging.home="%LOG_HOME%"

endlocal