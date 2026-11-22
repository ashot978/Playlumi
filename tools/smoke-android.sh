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

# Navigate through the real first-run age picker for a home-screen visual audit.
adb shell uiautomator dump /sdcard/playlumi-layout.xml >/dev/null
adb pull /sdcard/playlumi-layout.xml app/build/reports/playlumi-visual/layout.xml >/dev/null
python3 - <<'PY'
import re
import subprocess
import time
import xml.etree.ElementTree as ET

tree=ET.parse("app/build/reports/playlumi-visual/layout.xml")
for node in tree.iter("node"):
    label=(node.get("text","")+" "+node.get("content-desc","")).lower()
    if "3" not in label or "jaar" not in label:
        continue
    match=re.search(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",node.get("bounds",""))
    if not match:
        continue
    left,top,right,bottom=map(int,match.groups())
    subprocess.check_call(["adb","shell","input","tap",
                           str((left+right)//2),str((top+bottom)//2)])
    time.sleep(3)
    break
else:
    for node in tree.iter("node"):
        print("UI NODE:",node.get("text",""),node.get("content-desc",""),node.get("bounds",""))
    raise RuntimeError("Age selection missing from text AND content description")
PY
adb exec-out screencap -p > app/build/reports/playlumi-visual/home.png
test -s app/build/reports/playlumi-visual/home.png

echo "PASS: Playlumi launch process alive (pid=$pid), no reported process crash"
echo "NOTE: this does not test gameplay, in-place upgrades or Samsung hardware."
