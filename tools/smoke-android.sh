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

# Save a real emulator screenshot to review first-launch visual quality.
mkdir -p app/build/reports/playlumi-visual
adb exec-out screencap -p > app/build/reports/playlumi-visual/first-launch.png
test -s app/build/reports/playlumi-visual/first-launch.png

# Screenshot is best effort; Android Pixel Launcher can show an unrelated
# first-boot ANR dialog before Playlumi's own UI becomes interactive.
# Navigation and signed-APK installation have separate explicit gates.
echo "PASS: Playlumi launch process alive (pid=$pid), no reported process crash"
echo "NOTE: this does not test gameplay, in-place upgrades or Samsung hardware."
