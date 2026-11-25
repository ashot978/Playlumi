#!/usr/bin/env bash
# Real rendered-screen gate. AOSP ATD is useful for navigation but captures black
# framebuffers; this test uses a full Android system image with graphics.
set -euo pipefail
mkdir -p build/visual/screenshots
package=nl.playlumi.app
activity="$package/.MainActivity"
adb wait-for-device
adb install -r app/build/outputs/apk/debug/app-debug.apk
device_size=$(adb shell wm size | tr -d '\r' | grep -E 'Physical size|Override size' | tail -1 | sed -nE 's/.*: ([0-9]+x[0-9]+).*/\1/p')
IFS=x read -r screenW screenH <<< "$device_size"
test -n "$screenW" && test -n "$screenH"

home() {
  adb shell am force-stop "$package" >/dev/null || true
  adb logcat -c
  adb shell am start -n "$activity" --ez playlumi_ci_home true >/dev/null
  sleep 6
  adb shell pidof "$package" >/dev/null
  adb logcat -d -s PlaylumiQA:I | grep -q 'screen=1'
  local safe
  safe=$(adb logcat -d -s PlaylumiQA:I | grep 'safeTop=' | tail -1)
  safeTop=$(echo "$safe" | sed -nE 's/.*safeTop=([0-9]+).*/\1/p')
  safeBottom=$(echo "$safe" | sed -nE 's/.*safeBottom=([0-9]+).*/\1/p')
  [[ "$safeTop" =~ ^[0-9]+$ && "$safeBottom" =~ ^[0-9]+$ ]]
  echo "Rendered Android safe area: top=$safeTop bottom=$safeBottom"
}
snap() {
  local name="$1",file="build/visual/screenshots/$1.png"
  adb shell screencap -p "/sdcard/visual-${name}.png"
  adb pull "/sdcard/visual-${name}.png" "$file" >/dev/null
  local bytes
  bytes=$(stat -c%s "$file")
  if ((bytes<100000)); then
    echo "BLOCK RELEASE: likely blank/black screenshot $file ($bytes bytes)" >&2
    exit 1
  fi
}
tap() {
  local x=$((screenW*$1/941))
  local y=$((safeTop+(screenH-safeTop-safeBottom)*$2/1672))
  adb shell input tap "$x" "$y"
  sleep 3
}
assertPage() {
  local expected="$1"
  adb shell pidof "$package" >/dev/null
  adb logcat -d -s PlaylumiQA:I | grep -Eq "screen=${expected}( |$)" || {
    echo "Navigation failed to expected page $expected" >&2
    adb logcat -d -s PlaylumiQA:I >&2
    exit 1
  }
}
home
snap "00-home"
for entry in \
 "01-rekenraket:280:395:3" \
 "02-letterhuis:740:398:3" \
 "03-vormkasteel:185:690:13" \
 "04-muziekstudio:770:699:37" \
 "05-speelplein:185:1040:12" \
 "06-kleurentuin:785:1050:3"
do
 IFS=: read -r label x y target <<< "$entry"
 home
 tap "$x" "$y"
 assertPage "$target"
 snap "$label"
done
echo "PASS: nonblank graphical screenshots for the village and six real game pages"
