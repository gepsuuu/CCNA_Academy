#!/bin/sh
set -eu
cd "$(dirname "$0")/.."
python3 scripts/validate_content.py
command -v java >/dev/null 2>&1 || { echo 'JDK 17 required'; exit 2; }
command -v gradle >/dev/null 2>&1 || { echo 'Gradle 8.11.1 required; no fake wrapper supplied'; exit 2; }
gradle --no-daemon testDebugUnitTest lintDebug assembleDebug assembleRelease
echo 'Candidate build finished. Device acceptance is a separate gate.'
