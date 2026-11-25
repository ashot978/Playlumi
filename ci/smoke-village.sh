#!/usr/bin/env bash
# Playlumi 0.82: full Android rendering, real controls and regression tests.
set -euo pipefail
mkdir -p build/qa/screenshots
app=nl.playlumi.app
activity="$app/.MainActivity"
apk=app/build/outputs/apk/debug/app-debug.apk
mode="${1:-phone}"
if [[ "$mode" != phone && "$mode" != fold ]]; then echo "Unknown test mode $mode"; exit 1; fi
adb wait-for-device
if [[ "$mode" == fold ]]; then
  # Fresh emulator, same unfolded dp/aspect ratio with lower GPU memory pressure.
  adb shell wm size 1200x1320
  adb shell wm density 210
  sleep 4
  adb wait-for-device
fi
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
  adb shell am start -n "$activity" --ez playlumi_ci_home true --ei playlumi_ci_age "${1:-0}" >/dev/null
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
# Verify every number representation and automatic advancement using live controls.
numberRound() {
  local label="$1" menu="$2" mode="$3"
  home
  tapVillage 280 395
  assertScreen 43
  tapText "$menu"
  assertScreen 6
  adb shell uiautomator dump /sdcard/playlumi-ui.xml >/dev/null
  adb pull /sdcard/playlumi-ui.xml build/qa/ui.xml >/dev/null
  local reserved
  reserved=$(adb logcat -d -s PlaylumiQA:I | grep 'gameReservedBottom=' | tail -1 | sed -nE 's/.*gameReservedBottom=([0-9]+).*/\1/p')
  test "$reserved" -gt 0
  local coordinates
  coordinates=$(python3 - "$mode" "$screenH" "$reserved" <<'PY'
import re,sys,xml.etree.ElementTree as E
mode,h,reserved=sys.argv[1],int(sys.argv[2]),int(sys.argv[3])
words=['één','twee','drie','vier','vijf','zes','zeven','acht','negen','tien']
nodes=list(E.parse('build/qa/ui.xml').iter('node'))
if mode=='hands':
    node=next(n for n in nodes if n.get('content-desc','').startswith('Aantal opgestoken vingers: '))
    answer=node.get('content-desc').split(': ')[1]
    assert not any(n.get('text','').startswith('hand:') for n in nodes)
elif mode=='word':
    node=next(n for n in nodes if n.get('content-desc','') in [str(i) for i in range(1,11)] and n.get('clickable')=='false')
    answer=words[int(node.get('content-desc'))-1]
else:
    node=next(n for n in nodes if n.get('content-desc','') in words and n.get('clickable')=='false')
    answer=str(words.index(node.get('content-desc'))+1)
choices=[]
for n in nodes:
    d=n.get('content-desc','')
    if n.get('clickable')=='true' and d in ([str(i) for i in range(1,11)] if mode!='word' else words):
        b=list(map(int,re.findall(r'\d+',n.get('bounds',''))))
        assert len(b)==4 and b[3]<=h-reserved and b[3]>b[1], 'Answer outside safe viewport: '+str(b)
        choices.append((d,(b[0]+b[2])//2,(b[1]+b[3])//2))
assert 3<=len(choices)<=6
assert not any('Volgende' in n.get('content-desc','') for n in nodes)
good=next(c for c in choices if c[0]==answer)
bad=next(c for c in choices if c[0]!=answer)
print(bad[1],bad[2],good[1],good[2])
PY
)
  screenshot "game-number-$label"
  local bx by gx gy
  read -r bx by gx gy <<< "$coordinates"
  adb shell input tap "$bx" "$by"
  sleep 2
  adb shell uiautomator dump /sdcard/playlumi-ui.xml >/dev/null
  adb pull /sdcard/playlumi-ui.xml build/qa/ui.xml >/dev/null
  grep -q "Probeer nog eens" build/qa/ui.xml
  grep -q "0/8" build/qa/ui.xml
  adb shell input tap "$gx" "$gy"
  sleep 3
  assertScreen 6
  adb shell uiautomator dump /sdcard/playlumi-ui.xml >/dev/null
  adb pull /sdcard/playlumi-ui.xml build/qa/ui.xml >/dev/null
  grep -q "1/8" build/qa/ui.xml
  grep -q "Kies het juiste antwoord" build/qa/ui.xml
  ! grep -q "Volgende" build/qa/ui.xml
  echo "PASS: $label wrong/correct answer, automatic advance, safe answer bounds ($reserved px)"
}

# Test real six-option shape boards and actual drag gestures.
shapeRound() {
  home 2
  tapVillage 185 690
  assertScreen 43
  tapText "Sleep de vormen"
  assertScreen 13
  adb shell uiautomator dump /sdcard/playlumi-ui.xml >/dev/null
  adb pull /sdcard/playlumi-ui.xml build/qa/ui.xml >/dev/null
  local reserved coords
  reserved=$(adb logcat -d -s PlaylumiQA:I | grep 'gameReservedBottom=' | tail -1 | sed -nE 's/.*gameReservedBottom=([0-9]+).*/\1/p')
  coords=$(python3 - "$screenW" "$screenH" "$reserved" "$mode" <<'PY'
import re,sys,xml.etree.ElementTree as E
w,h,inset=map(int,sys.argv[1:4]);mode=sys.argv[4]
nodes=list(E.parse('build/qa/ui.xml').iter('node'))
gate=next(n for n in nodes if n.get('content-desc','').startswith('Vormpoort: '))
symbol=gate.get('content-desc').split(': ',1)[1]
def bounds(n):
    b=list(map(int,re.findall(r'\d+',n.get('bounds',''))))
    assert len(b)==4 and b[2]>b[0] and b[3]>b[1] and b[3]<=h-inset,(n.get('content-desc'),b)
    return b
def center(b):return ((b[0]+b[2])//2,(b[1]+b[3])//2)
gb=bounds(gate)
cards=[n for n in nodes if n.get('clickable')=='true' and n.get('content-desc','') in ['⭐','🔺','🔵','🟩','💜','🔶','❤️','⬛','🔷','🔸','🟢','⬜']]
assert len(cards)==6, 'Expected six advanced shapes'
cb=[bounds(n) for n in cards]
assert len({b[3]-b[1] for b in cb})==1,'Inconsistent card heights'
assert len({b[2]-b[0] for b in cb})<=2,'Inconsistent card widths'
if mode=='fold':
    assert gb[2]<=min(b[0] for b in cb), 'Wide target should sit beside choices'
    assert abs((gb[3]-gb[1])-(max(b[3] for b in cb)-min(b[1] for b in cb)))<=40,'Wide gate and card grid height mismatch'
else:
    assert gb[3]<=min(b[1] for b in cb),'Portrait target should sit above choices'
good=next(n for n in cards if n.get('content-desc')==symbol)
bad=next(n for n in cards if n.get('content-desc')!=symbol)
print(*center(bounds(bad)),*center(bounds(good)),*center(gb))
PY
)
  adb logcat -d -s PlaylumiQA:I | grep -F "headerFit=true title=Vormen slepen"
  screenshot "game-shapes-${mode}"
  local bx by gx gy tx ty
  read -r bx by gx gy tx ty <<< "$coords"
  adb shell input swipe "$bx" "$by" "$tx" "$ty" 1200
  sleep 2
  adb shell uiautomator dump /sdcard/playlumi-ui.xml >/dev/null
  adb pull /sdcard/playlumi-ui.xml build/qa/ui.xml >/dev/null
  grep -q "Probeer eens een andere" build/qa/ui.xml
  ! grep -q "Verder spelen!" build/qa/ui.xml
  adb shell input swipe "$gx" "$gy" "$tx" "$ty" 1200
  sleep 2
  adb shell uiautomator dump /sdcard/playlumi-ui.xml >/dev/null
  adb pull /sdcard/playlumi-ui.xml build/qa/ui.xml >/dev/null
  grep -q "Goed gedaan!" build/qa/ui.xml
  grep -q "Verder spelen!" build/qa/ui.xml
  screenshot "game-shapes-${mode}-solved"
  echo "PASS: $mode six responsive shape choices, wrong and correct real drag"
}
wordRound() {
  home 2
  tapVillage 740 398
  assertScreen 43
  tapText "Woorden maken"
  assertScreen 6
  adb shell uiautomator dump /sdcard/playlumi-ui.xml >/dev/null
  adb pull /sdcard/playlumi-ui.xml build/qa/ui.xml >/dev/null
  local answer bad full blank
  read -r answer bad full blank < <(python3 - <<'PY'
import re,xml.etree.ElementTree as E
nodes=list(E.parse('build/qa/ui.xml').iter('node'))
words='koe kat vis mus kip mol uil aap bok das hond beer muis geit haan poes eend duif slak wolf konijn paard leeuw tijger schaap varken dolfijn olifant giraf panda'.split()
for n in nodes:
    if n.get('clickable')=='false':
        m=re.search(r'[a-z]*_[a-z]*',n.get('content-desc',''))
        if m and len(m.group())>1:
            blank=m.group();break
else:raise SystemExit('Missing word blank')
matching=[w for w in words if re.fullmatch(blank.replace('_','[a-z]'),w)]
assert len(matching)==1,(blank,matching)
full=matching[0];answer=full[blank.index('_')]
choices=[n.get('content-desc','') for n in nodes if n.get('clickable')=='true']
assert answer in choices
bad=next(c for c in choices if len(c)==1 and c.isalpha() and c!=answer)
print(answer,bad,full,blank)
PY
)
  tapText "$bad"
  adb shell uiautomator dump /sdcard/playlumi-ui.xml >/dev/null
  adb pull /sdcard/playlumi-ui.xml build/qa/ui.xml >/dev/null
  grep -Fq "$blank" build/qa/ui.xml
  grep -q "Probeer nog eens" build/qa/ui.xml
  tapText "$answer"
  adb shell uiautomator dump /sdcard/playlumi-ui.xml >/dev/null
  adb pull /sdcard/playlumi-ui.xml build/qa/ui.xml >/dev/null
  grep -Fq "$full" build/qa/ui.xml
  ! grep -Fq "$blank" build/qa/ui.xml
  grep -q "Volgende" build/qa/ui.xml
  screenshot "game-word-${mode}-filled"
  echo "PASS: $mode word $blank -> $full; wrong letter did not fill the blank"
}
finishNumberRound() {
  # Called after numberRound word-to-digit; seven correct answers remain.
  for i in {2..8}; do
    adb shell uiautomator dump /sdcard/playlumi-ui.xml >/dev/null
    adb pull /sdcard/playlumi-ui.xml build/qa/ui.xml >/dev/null
    local xy
    xy=$(python3 - <<'PY'
import re,xml.etree.ElementTree as E
words=['één','twee','drie','vier','vijf','zes','zeven','acht','negen','tien']
nodes=list(E.parse('build/qa/ui.xml').iter('node'))
n=next(n for n in nodes if n.get('content-desc','') in words and n.get('clickable')=='false')
answer=str(words.index(n.get('content-desc'))+1)
b=next(n for n in nodes if n.get('content-desc','')==answer and n.get('clickable')=='true')
a=list(map(int,re.findall(r'\d+',b.get('bounds',''))))
print((a[0]+a[2])//2,(a[1]+a[3])//2)
PY
)
    adb shell input tap $xy
    sleep 2
  done
  adb shell uiautomator dump /sdcard/playlumi-ui.xml >/dev/null
  adb pull /sdcard/playlumi-ui.xml build/qa/ui.xml >/dev/null
  grep -q 'Knap gedaan!' build/qa/ui.xml
  grep -q '8 vragen goed!' build/qa/ui.xml
  adb logcat -d -s PlaylumiQA:I | grep -F "headerFit=true title=Knap gedaan!"
  screenshot "game-completed-${mode}"
  echo "PASS: $mode complete session and fully visible completion title"
}

if [[ "$mode" == phone ]]; then
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
# Real taps: choose a wrong balloon quantity, then the correct quantity.
home
tapVillage 185 1040
assertScreen 43
tapText "Tel de ballonnen"
assertScreen 13
adb shell uiautomator dump /sdcard/playlumi-ui.xml >/dev/null
adb pull /sdcard/playlumi-ui.xml build/qa/ui.xml >/dev/null
read -r good bad < <(python3 - <<'PY'
import re,xml.etree.ElementTree as E
nodes=list(E.parse('build/qa/ui.xml').iter('node'))
target=next(int(m.group(1)) for n in nodes if (m:=re.search(r'Waar zie je (\d+) ballon',n.get('text',''))))
groups=[]
for n in nodes:
    m=re.fullmatch(r'Groep met (\d+) ballon(?:nen)?',n.get('content-desc',''))
    if m and n.get('clickable')=='true':
        assert n.get('text','')=='', 'Balloon card must not show a printed number'
        groups.append(int(m.group(1)))
assert target in groups and len(set(groups))==len(groups)
print(target,next(x for x in groups if x!=target))
PY
)
tapText "Groep met $bad ballon"
adb shell uiautomator dump /sdcard/playlumi-ui.xml >/dev/null
adb pull /sdcard/playlumi-ui.xml build/qa/ui.xml >/dev/null
grep -q "Probeer eens een andere" build/qa/ui.xml
! grep -q "Verder spelen!" build/qa/ui.xml
tapText "Groep met $good ballon"
adb shell uiautomator dump /sdcard/playlumi-ui.xml >/dev/null
adb pull /sdcard/playlumi-ui.xml build/qa/ui.xml >/dev/null
grep -q "Verder spelen!" build/qa/ui.xml
screenshot game-balloon-count-solved
# A heart must leave the score at zero; only real stars complete the round.
home
tapVillage 185 1040
assertScreen 43
tapText "Vang de sterren"
assertScreen 42
tapText "Hartje "
adb shell uiautomator dump /sdcard/playlumi-ui.xml >/dev/null
adb pull /sdcard/playlumi-ui.xml build/qa/ui.xml >/dev/null
target=$(python3 - <<'PY'
import re,xml.etree.ElementTree as E
nodes=list(E.parse('build/qa/ui.xml').iter('node'))
for n in nodes:
    m=re.search(r'0 / ([345])',n.get('text',''))
    if m:
        print(m.group(1));break
else: raise SystemExit('Heart changed the star score')
PY
)
tapText "Hartje "
adb shell uiautomator dump /sdcard/playlumi-ui.xml >/dev/null
adb pull /sdcard/playlumi-ui.xml build/qa/ui.xml >/dev/null
grep -q "0 / $target" build/qa/ui.xml
for ((i=0;i<target;i++)); do tapText "Vang ster "; done
adb shell uiautomator dump /sdcard/playlumi-ui.xml >/dev/null
adb pull /sdcard/playlumi-ui.xml build/qa/ui.xml >/dev/null
grep -q "$target / $target" build/qa/ui.xml
grep -q "Volgende ronde" build/qa/ui.xml
screenshot game-stars-solved
numberRound hands "Vingers tellen" hands
numberRound digit-to-word "Cijfer naar woord" word
numberRound word-to-digit "Woord naar cijfer" digit
finishNumberRound
shapeRound
wordRound

else
  echo "Fresh unfolded emulator: ${screenW}x${screenH}"
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
numberRound fold-hands "Vingers tellen" hands
numberRound fold-digit-to-word "Cijfer naar woord" word
numberRound fold-word-to-digit "Woord naar cijfer" digit
finishNumberRound
shapeRound
wordRound

fi
# The native layout must not have crashed anywhere during the recorded journeys.
adb logcat -d -v brief | grep -E 'FATAL EXCEPTION|Process: nl.playlumi.app.*Exception' > build/qa/fatal.log || true
if [[ -s build/qa/fatal.log ]]; then cat build/qa/fatal.log; exit 1; fi
echo "PASS: $mode emulator game and screenshot regression, number learning, auto advance and safe answer bounds"
