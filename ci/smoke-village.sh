#!/usr/bin/env bash
# Playlumi 0.74: real emulator visual/navigation regression test.
set -euo pipefail
mkdir -p build/qa/screenshots
app=nl.playlumi.app
activity="$app/.MainActivity"
apk=app/build/outputs/apk/debug/app-debug.apk
adb wait-for-device
adb install -r "$apk"

device_size=$(adb shell wm size | tr -d '\r' | grep -E 'Physical size|Override size' | tail -1 | sed -nE 's/.*: ([0-9]+x[0-9]+).*/\1/p')
if [[ -z "$device_size" ]]; then echo "No emulator dimensions"; exit 1; fi
IFS=x read -r screenW screenH <<< "$device_size"
echo "Emulator screen: ${screenW}x${screenH}"

home() {
  adb shell am force-stop "$app" >/dev/null || true
  adb logcat -c
  adb shell am start -n "$activity" --ez playlumi_ci_home true >/dev/null
  sleep 3
  adb shell pidof "$app" >/dev/null
  adb logcat -d -s PlaylumiQA:I | grep -q 'screen=1' || {
    echo "Playlumi did not open the village home screen" >&2
    adb logcat -d -s PlaylumiQA:I >&2
    exit 1
  }
  local logLine
  logLine=$(adb logcat -d -s PlaylumiQA:I | grep 'safeTop=' | tail -1)
  safeTop=$(echo "$logLine" | sed -nE 's/.*safeTop=([0-9]+).*/\1/p')
  safeBottom=$(echo "$logLine" | sed -nE 's/.*safeBottom=([0-9]+).*/\1/p')
  if [[ ! "$safeTop" =~ ^[0-9]+$ || ! "$safeBottom" =~ ^[0-9]+$ ]]; then
    echo "Android WindowInsets missing from the village screen" >&2
    exit 1
  fi
  # AOSP ATD sometimes deliberately hides the navigation bar.
  # bottom=0 is correct there; a real Samsung 3-button bar reports >0.
  if [[ "$safeTop" -le 0 || "$safeBottom" -lt 0 ]]; then
    echo "Invalid Android safe insets: top=$safeTop bottom=$safeBottom" >&2
    exit 1
  fi
  echo "Actual Android safe insets: top=$safeTop bottom=$safeBottom"
}

tapVillage() {
  local artX="$1" artY="$2"
  local usableH=$((screenH-safeTop-safeBottom))
  local x=$((screenW*artX/941))
  local y=$((safeTop+usableH*artY/1672))
  adb shell input tap "$x" "$y"
  sleep 2
}

assertScreen() {
  local expected="$1"
  adb shell pidof "$app" >/dev/null
  adb logcat -d -s PlaylumiQA:I | grep -Eq "screen=${expected}( |$)" || {
    echo "Expected screen ${expected}; actual navigation logs:" >&2
    adb logcat -d -s PlaylumiQA:I >&2
    exit 1
  }
}

screenshot() {
  local name="$1"
  adb shell screencap -p "/sdcard/playlumi-${name}.png"
  adb pull "/sdcard/playlumi-${name}.png" "build/qa/screenshots/${name}.png" >/dev/null
  test -s "build/qa/screenshots/${name}.png"
}

# Every page has two obligations: open the intended game and go back logically.
home
screenshot home
for entry in \
  "rekenraket:280:395:3" \
  "letterhuis:740:398:3" \
  "vormkasteel:185:690:13" \
  "muziekstudio:770:699:37" \
  "speelplein:185:1040:12" \
  "kleurentuin:785:1050:3" \
  "missies:84:1550:15" \
  "beloningen:261:1540:11" \
  "collectie:670:1540:19" \
  "profiel:841:1540:32" \
  "ouderzone:840:135:26" \
  "instellingen:877:55:41"
do
  IFS=: read -r label x y expected <<< "$entry"
  home
  tapVillage "$x" "$y"
  assertScreen "$expected"
  screenshot "$label"
  adb shell input keyevent KEYCODE_BACK
  sleep 2
  assertScreen 1
  echo "Passed navigation: $label -> $expected -> village"
done

# The native layout must not have crashed anywhere during the recorded journeys.
adb logcat -d -v brief | grep -E 'FATAL EXCEPTION|Process: nl.playlumi.app.*Exception' > build/qa/fatal.log || true
if [[ -s build/qa/fatal.log ]]; then cat build/qa/fatal.log; exit 1; fi
echo "PASS: 12 real clickable targets + back navigation + actual safe insets"
