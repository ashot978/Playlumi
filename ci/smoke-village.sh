#!/usr/bin/env bash
set -euo pipefail
mkdir -p build/qa/screenshots
adb wait-for-device
adb install -r app/build/outputs/apk/debug/app-debug.apk
app=nl.playlumi.app
activity="$app/.MainActivity"
adb shell am start -n "$activity" --ez playlumi_ci_home true >/dev/null
sleep 5
adb shell pidof "$app" >/dev/null
adb shell screencap -p /sdcard/playlumi-home.png
adb pull /sdcard/playlumi-home.png build/qa/screenshots/home.png >/dev/null
# Reopen home in a fresh process for each independent game navigation.
# Coordinates are mapped from the 941x1672 concept art onto emulator pixels.
read -r screenW screenH < <(adb shell wm size | sed -n 's/.*: \([0-9][0-9]*\)x\([0-9][0-9]*\).*/\1 \2/p' | tail -1)
if [[ -z "${screenW:-}" || -z "${screenH:-}" ]]; then
  echo "Could not read emulator display size"
  exit 1
fi
# Android UI content height excludes approximate status/navigation-bar insets.
# These taps are an additional smoke test; unit tests verify coordinate mapping.
for entry in "rekenraket:280:398" "letterhuis:740:396" "vormkasteel:173:693" "muziekstudio:760:690" "speelplein:195:1080" "kleurentuin:762:1070"; do
  IFS=: read -r label artX artY <<< "$entry"
  adb shell am force-stop "$app" || true
  adb shell am start -n "$activity" --ez playlumi_ci_home true >/dev/null
  sleep 2
  x=$((screenW * artX / 941))
  y=$((68 + (screenH-150) * artY / 1672))
  adb shell input tap "$x" "$y"
  sleep 2
  adb shell pidof "$app" >/dev/null
  adb shell screencap -p "/sdcard/playlumi-${label}.png"
  adb pull "/sdcard/playlumi-${label}.png" "build/qa/screenshots/${label}.png" >/dev/null
done
# A fatal exception is never acceptable, even if the process has restarted.
adb logcat -d -v brief | grep -E 'FATAL EXCEPTION|Process: nl.playlumi.app.*Exception' > build/qa/fatal.log || true
if [[ -s build/qa/fatal.log ]]; then
  cat build/qa/fatal.log
  exit 1
fi
echo "Playlumi emulator startup and game navigation smoke test passed"
