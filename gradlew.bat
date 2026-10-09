@rem Gradle start up script for Windows
@if "%DEBUG%"=="" @echo off
@rem Try wrapper jar
if exist "%~dp0gradle\wrapper\gradle-wrapper.jar" (
    java -jar "%~dp0gradle\wrapper\gradle-wrapper.jar" %*
    exit /b %ERRORLEVEL%
) else (
    echo gradle-wrapper.jar not found, trying system gradle...
    gradle %*
)
