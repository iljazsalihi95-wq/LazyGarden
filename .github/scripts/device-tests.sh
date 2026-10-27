#!/usr/bin/env bash
set -euo pipefail
adb shell settings put secure immersive_mode_confirmations confirmed
adb shell input keyevent KEYCODE_WAKEUP
adb shell wm dismiss-keyguard
status=0
runner_args=()
if [[ $# -gt 0 ]]; then
  runner_args=("-Pandroid.testInstrumentationRunnerArguments.numShards=2" "-Pandroid.testInstrumentationRunnerArguments.shardIndex=$1")
fi
gradle "${runner_args[@]}" connectedDebugAndroidTest || status=$?
mkdir -p device-screenshots
adb pull /data/local/tmp/lazygarden-screenshots/. device-screenshots/ || true
exit "$status"
