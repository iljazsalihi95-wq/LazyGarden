#!/usr/bin/env bash
set -euo pipefail
adb shell settings put secure immersive_mode_confirmations confirmed
adb shell input keyevent KEYCODE_WAKEUP
adb shell wm dismiss-keyguard
status=0
gradle connectedDebugAndroidTest || status=$?
mkdir -p device-screenshots
adb pull /data/local/tmp/lazygarden-screenshots/. device-screenshots/ || true
exit "$status"
