#!/usr/bin/env bash
# Playlumi 0.80: full Android rendering, real controls and regression tests.
set -euo pipefail
mkdir -p build/qa/screenshots
app=nl.playlumi.app
activity="$app/.MainActivity"
apk=app/build/outputs/apk/debug/app-debug.apk
adb wait-for-device
adb shell pm disable-user --user 0 com.android.launcher3 || true
adb shell am force-stop com.android.launcher3 || true
adb install -r "$apk"

device_size=$(adb shell wm size | tr -d '\r' | grep -E 'Physical size|Override size' | tail -1 | sed -nE 's/.*: ([0-9]+x[0-9]+).*/\1/p')
if [[ -z "$device_size" ]]; then echo "No emulator dimensions"; exit 1; fi
IFS=x read -r screenW screenH <<< "$device_size"
echo "Emulator screen: ${screenW}x${screenH}"

home() {
  adb shell am force-stop "$app" >/dev/null || true
  adb logcat -c
  adb shell am start -n "$activity" --ez playlumi_ci_home true >/dev/null
  sleep 4
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
  # Mirror PlaylumiVillageHit.destination: wide screens center the artwork.
  # The raw display width is not the width of the village on an unfolded device.
  local x y
  read -r x y < <(python3 - "$screenW" "$usableH" "$safeTop" "$artX" "$artY" <<'PY'
import sys
w,h,top,ax,ay=map(float,sys.argv[1:])
if w/h <= .72:
    dw,dh=w,h
else:
    factor=min(w/941,h/1672)
    dw,dh=941*factor,1672*factor
print(round((w-dw)/2+ax*dw/941),round(top+(h-dh)/2+ay*dh/1672))
PY
)
  adb shell input tap "$x" "$y"
  sleep 2
}

assertScreen() {
  local expected="$1"
  adb shell pidof "$app" >/dev/null
  adb logcat -d -s PlaylumiQA:I | grep 'screen=' | tail -1 | grep -Eq "screen=${expected}( |$)" || {
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
  case "$name" in home|game-*|letterhuis|kleurentuin|muziekstudio|speelplein|fold-*)
    bytes=$(stat -c%s "build/qa/screenshots/${name}.png")
    if (( bytes < 50000 )); then echo "BLOCK RELEASE: blank/too small rendered screenshot $name ($bytes bytes)" >&2; exit 1; fi;;
  esac
}

# Every page has two obligations: open the intended game and go back logically.
home
screenshot home
for entry in \
  "rekenraket:280:395:43" \
  "letterhuis:740:398:43" \
  "vormkasteel:185:690:43" \
  "muziekstudio:770:699:43" \
  "speelplein:185:1040:43" \
  "kleurentuin:785:1050:43" \
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

# Exercise real menu buttons and their individual game screens.
tapText() {
  adb shell uiautomator dump /sdcard/playlumi-ui.xml >/dev/null
  adb pull /sdcard/playlumi-ui.xml build/qa/ui.xml >/dev/null
  local xy
  xy=$(python3 - "$1" <<'PY'
import re,sys,xml.etree.ElementTree as ET
for n in ET.parse('build/qa/ui.xml').iter('node'):
    if n.get('content-desc','').startswith(sys.argv[1]) and n.get('clickable')=='true':
        a=list(map(int,re.findall(r'\d+',n.get('bounds',''))))
        if len(a)==4 and a[2]>a[0] and a[3]>a[1]:
            print((a[0]+a[2])//2,(a[1]+a[3])//2)
            sys.exit(0)
sys.exit('Missing clickable menu item: '+sys.argv[1])
PY
)
  adb shell input tap $xy
  sleep 2
}
for entry in \
  "rocket:280:395:Voorwerpen tellen:6" \
  "letters:740:398:Kleine letters:6" \
  "shapes:185:690:Sleep de vormen:13" \
  "music:770:699:Speel het deuntje:37" \
  "playground:185:1040:Dierenmemory:13" \
  "colors:785:1050:Kleuren herkennen:6"
do
  IFS=: read -r label x y button expected <<< "$entry"
  home
  tapVillage "$x" "$y"
  assertScreen 43
  tapText "$button"
  assertScreen "$expected"
  screenshot "game-$label"
  adb shell uiautomator dump /sdcard/playlumi-ui.xml >/dev/null
  adb pull /sdcard/playlumi-ui.xml build/qa/ui.xml >/dev/null
  python3 - "$label" <<'PY'
import re,sys,xml.etree.ElementTree as ET
nodes=list(ET.parse('build/qa/ui.xml').iter('node'))
for n in nodes:
    text=n.get('text','')+' '+n.get('content-desc','')
    for forbidden in ('Luister naar de opdracht','Lees de vraag voor','Tik op het plaatje om de vraag te horen'):
        assert forbidden not in text, 'Synthetic narration control: '+text
if sys.argv[1]=='colors':
    swatch=next((n for n in nodes if n.get('content-desc')=='Groot kleurvlak'),None)
    assert swatch is not None, 'Missing real color swatch'
    bounds=list(map(int,re.findall(r'\d+',swatch.get('bounds',''))))
    assert len(bounds)==4 and bounds[3]-bounds[1]>=100, 'Color swatch too small'
PY
  # Actual approved illustration is exposed as a native non-clickable view.
  grep -q "Geillustreerde spelwereld" build/qa/ui.xml
  if [[ "$label" == "letters" ]]; then
    choice=$(python3 - <<'PY'
import xml.etree.ElementTree as E
nodes=list(E.parse('build/qa/ui.xml').iter('node'))
for n in nodes:
    d=n.get('content-desc','')
    if d in ('A','B','M','S','O','P','K','T','L','R') and n.get('clickable')=='false':
        print(d.lower());break
else: raise SystemExit('Missing live uppercase letter')
PY
)
    tapText "$choice"
    adb shell uiautomator dump /sdcard/playlumi-ui.xml >/dev/null
    adb pull /sdcard/playlumi-ui.xml build/qa/ui.xml >/dev/null
    grep -q "Volgende" build/qa/ui.xml
    screenshot game-letters-solved
  elif [[ "$label" == "music" ]]; then
    tapText "▶  Speel de melodie af"
    sleep 5
    tapText "Kikker"
    screenshot game-music-played
  elif [[ "$label" == "playground" ]]; then
    tapText "Gesloten kaart 1"
    adb shell uiautomator dump /sdcard/playlumi-ui.xml >/dev/null
    adb pull /sdcard/playlumi-ui.xml build/qa/ui.xml >/dev/null
    if grep -q 'content-desc="Gesloten kaart 1"' build/qa/ui.xml; then echo "Memory card did not flip" >&2; exit 1; fi
    screenshot game-playground-flipped
  fi
  adb shell input keyevent KEYCODE_BACK
  sleep 2
  assertScreen 43
done
home
tapVillage 70 158
assertScreen 32
screenshot characters
tapText "Milo"
adb shell uiautomator dump /sdcard/playlumi-ui.xml >/dev/null
adb pull /sdcard/playlumi-ui.xml build/qa/ui.xml >/dev/null
grep -q "Milo, gekozen" build/qa/ui.xml
! grep -q "Lumi, kies maatje" build/qa/ui.xml
screenshot character-selected
adb shell input keyevent KEYCODE_BACK
sleep 2
assertScreen 1
screenshot home-character
# The star balance must lead to real rewards, not a separate star wardrobe.
home
tapVillage 417 52
assertScreen 11
adb shell uiautomator dump /sdcard/playlumi-ui.xml >/dev/null
adb pull /sdcard/playlumi-ui.xml build/qa/ui.xml >/dev/null
grep -q "Mijn beloningen" build/qa/ui.xml
! grep -q "Sterkleur" build/qa/ui.xml
screenshot rewards
# Count actual balloons and verify that the number is separate from the balloon itself.
home
tapVillage 185 1040
assertScreen 43
tapText "Ballonnen tellen"
assertScreen 13
adb shell uiautomator dump /sdcard/playlumi-ui.xml >/dev/null
adb pull /sdcard/playlumi-ui.xml build/qa/ui.xml >/dev/null
balloon_count=$(python3 - <<'PY'
import xml.etree.ElementTree as E
nodes=list(E.parse('build/qa/ui.xml').iter('node'))
balloons=[n for n in nodes if n.get('content-desc')=='Ballon']
assert 1<=len(balloons)<=10, 'No actual balloons to count'
assert all(n.get('text')=='🎈' for n in balloons), 'Number printed on balloon'
answers=[n.get('content-desc') for n in nodes if n.get('content-desc','').startswith('Antwoord ')]
assert 'Antwoord '+str(len(balloons)) in answers, 'Correct quantity is missing'
print(len(balloons))
PY
)
tapText "Ballon"
adb shell uiautomator dump /sdcard/playlumi-ui.xml >/dev/null
adb pull /sdcard/playlumi-ui.xml build/qa/ui.xml >/dev/null
grep -q 'content-desc="Geprikte ballon"' build/qa/ui.xml
tapText "Antwoord $balloon_count"
adb shell uiautomator dump /sdcard/playlumi-ui.xml >/dev/null
adb pull /sdcard/playlumi-ui.xml build/qa/ui.xml >/dev/null
grep -q 'Verder spelen' build/qa/ui.xml
screenshot game-balloon-count-solved
adb shell input keyevent KEYCODE_BACK
sleep 2
assertScreen 43

# Distractors must remain visible and never contribute to progress.
home
tapVillage 185 1040
assertScreen 43
tapText "Vang de sterren"
assertScreen 42
tapText "Hartje "
adb shell uiautomator dump /sdcard/playlumi-ui.xml >/dev/null
adb pull /sdcard/playlumi-ui.xml build/qa/ui.xml >/dev/null
star_count=$(python3 - <<'PY'
import xml.etree.ElementTree as E
nodes=list(E.parse('build/qa/ui.xml').iter('node'))
stars=[n for n in nodes if n.get('content-desc','').startswith('Ster ')]
assert 3<=len(stars)<=5, 'Incorrect number of actual stars'
assert any(n.get('content-desc','').startswith('Hartje ') for n in nodes), 'Missing hearts'
assert any('0 / '+str(len(stars)) in n.get('text','') for n in nodes), 'Heart changed progress'
print(len(stars))
PY
)
tapText "Ster "
adb shell uiautomator dump /sdcard/playlumi-ui.xml >/dev/null
adb pull /sdcard/playlumi-ui.xml build/qa/ui.xml >/dev/null
grep -q "1 / $star_count" build/qa/ui.xml
for ((i=1;i<star_count;i++)); do tapText "Ster "; done
adb shell uiautomator dump /sdcard/playlumi-ui.xml >/dev/null
adb pull /sdcard/playlumi-ui.xml build/qa/ui.xml >/dev/null
grep -q "Volgende ronde" build/qa/ui.xml
screenshot game-star-catch-solved
adb shell input keyevent KEYCODE_BACK
sleep 2
assertScreen 43
# Exercise unfolded / tablet proportions on the same real rendered emulator.
adb shell wm size 1600x1768
adb shell wm density 280
sleep 3
device_size=$(adb shell wm size | tr -d '\r' | grep -E 'Physical size|Override size' | tail -1 | sed -nE 's/.*: ([0-9]+x[0-9]+).*/\1/p')
IFS=x read -r screenW screenH <<< "$device_size"
echo "Unfolded emulator screen: ${screenW}x${screenH}"
for entry in \
  "letters:740:398:Kleine letters:6" \
  "colors:785:1050:Kleuren herkennen:6" \
  "music:770:699:Speel het deuntje:37" \
  "memory:185:1040:Dierenmemory:13"
do
  IFS=: read -r label x y button expected <<< "$entry"
  home
  tapVillage "$x" "$y"
  assertScreen 43
  tapText "$button"
  assertScreen "$expected"
  screenshot "fold-$label"
done
adb shell wm size reset
adb shell wm density reset
# The native layout must not have crashed anywhere during the recorded journeys.
adb logcat -d -v brief | grep -E 'FATAL EXCEPTION|Process: nl.playlumi.app.*Exception' > build/qa/fatal.log || true
if [[ -s build/qa/fatal.log ]]; then cat build/qa/fatal.log; exit 1; fi
echo "PASS: six real game routes, approved graphics, letter answer, sound pad, memory flip, balloon quantity, rejected hearts, fold screenshots, companion, rewards, color swatch, no narration and safe insets"
