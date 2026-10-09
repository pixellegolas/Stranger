#!/usr/bin/env sh
#
# Gradle start up script - fallback version for Stranger Pro
# Tries wrapper jar, falls back to system gradle
#
set -e
APP_HOME=$(cd "$(dirname "$0")" && pwd)
if [ -f "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" ]; then
    exec java -jar "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" "$@"
else
    echo "gradle-wrapper.jar not found, trying system gradle..."
    if command -v gradle >/dev/null 2>&1; then
        exec gradle "$@"
    else
        echo "Gradle not found. In Android Studio, open project to generate wrapper."
        echo "Or install gradle 8.9 and run: gradle wrapper --gradle-version 8.9"
        exit 1
    fi
fi
