#!/bin/bash
if [ ! -d "gradle-8.7" ]; then
    echo "Descargando Gradle..."
    curl -s -L "https://services.gradle.org/distributions/gradle-8.7-bin.zip" -o gradle-8.7.zip
    unzip -q gradle-8.7.zip
fi
./gradle-8.7/bin/gradle "$@"
