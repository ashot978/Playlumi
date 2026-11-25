#!/usr/bin/env bash
set -euo pipefail
# After a normal first launch, verify navigation and capture real views.
# This does not claim a complete game round or a real-device Fold test.
OUT="app/build/reports/playlumi-visual"
mkdir -p "$OUT"

ui_dump() {
  for attempt in 1 2 3 4 5; do
    if adb shell uiautomator dump /sdcard/playlumi-ui.xml >/dev/null 2>&1 &&
       adb exec-out cat /sdcard/playlumi-ui.xml > "$OUT/hierarchy.xml" &&
       grep -q '<hierarchy' "$OUT/hierarchy.xml"; then return 0; fi
    sleep 2
  done
  echo "FAIL: Android UI hierarchy unavailable"
  return 1
}
tap_text() {
  local desired="$1"
  ui_dump
  local center
  center="$(python3 - "$OUT/hierarchy.xml" "$desired" <<'PY'
import re,sys,xml.etree.ElementTree as ET
root=ET.parse(sys.argv[1]).getroot()
needle=sys.argv[2].casefold()
for node in root.iter("node"):
    name=(node.get("text","")+" "+node.get("content-desc","")).casefold()
    bounds=node.get("bounds","")
    if needle not in name: continue
    if node.get("clickable")!="true": continue
    match=re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",bounds)
    if match:
        x1,y1,x2,y2=map(int,match.groups())
        print((x1+x2)//2,(y1+y2)//2)
        break
else:
    print("NOT_FOUND",file=sys.stderr)
    sys.exit(1)
PY
)" || { echo "FAIL: no clickable text matching $desired"; return 1; }
  adb shell input tap $center
  sleep 2
}
assert_text() {
  ui_dump
  if ! grep -Fq "$1" "$OUT/hierarchy.xml"; then
    echo "FAIL: UI screen missing $1"
    return 1
  fi
}
capture() {
  adb exec-out screencap -p > "$OUT/$1.png"
  test -s "$OUT/$1.png"
}
# Emulator snapshots may retain a previous age choice.
if ! adb shell dumpsys package nl.playlumi.app | grep -Fq "versionName=0.68.0"; then
  echo "FAIL: emulator is not running Playlumi 0.68.0"
  exit 1
fi
ui_dump
if grep -Fq "Klaar om te ontdekken" "$OUT/hierarchy.xml"; then
  tap_text "3–4 jaar"
fi
assert_text "Klaar voor een avontuur"
capture home-phone
tap_text "Speel meteen"
assert_text "Kies het juiste antwoord"
capture game-phone
# Fold/tablet portrait-like window: 720dp wide at 320dpi, still app-resizable.
adb shell wm size 1440x1760
adb shell wm density 320
trap 'adb shell wm size reset; adb shell wm density reset' EXIT
adb shell am force-stop nl.playlumi.app
adb shell am start -W -n nl.playlumi.app/.MainActivity >/dev/null
sleep 3
assert_text "Klaar voor een avontuur"
capture home-wide
tap_text "Speel meteen"
assert_text "Kies het juiste antwoord"
capture game-wide
if adb logcat -d -v brief | grep -F "Process: nl.playlumi.app"; then
    echo "FAIL: Playlumi crashed during home/game screen navigation"
    exit 1
fi
echo "PASS: home and game screens opened in phone and 720dp-wide emulator"
