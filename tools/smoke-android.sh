#!/usr/bin/env bash
set -euo pipefail
# A boot smoke check, NOT a full game or real-device acceptance test.
gradle assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb logcat -c
adb shell am start -W -n nl.playlumi.app/.MainActivity
sleep 5

pid="$(adb shell pidof nl.playlumi.app | tr -d '\r')"
if [ -z "$pid" ]; then
  echo "FAIL: Playlumi process disappeared after launch"
  adb logcat -d -v brief | tail -n 150
  exit 1
fi

if adb logcat -d -v brief | grep -F "Process: nl.playlumi.app"; then
  echo "FAIL: Android log reported Playlumi process crash"
  adb logcat -d -v brief | tail -n 150
  exit 1
fi

echo "PASS: Playlumi launch process alive (pid=$pid), no reported process crash"
echo "NOTE: this does not test gameplay, in-place upgrades or Samsung hardware."
