"""Native UI regression test; only disposable emulator data is modified."""
import json, pathlib, re, subprocess, time, xml.etree.ElementTree as ET

APP = "nl.playlumi.app"
OUT = pathlib.Path("build/clean-quiz")
OUT.mkdir(parents=True, exist_ok=True)
VEG = {"🥕": "wortel", "🥦": "broccoli", "🌽": "maïs", "🥒": "komkommer", "🍅": "tomaat", "🫑": "paprika"}
BAD = ("💡", "vraag een hint", "lees de opdracht voor", "voorlezen", "🔊", "kies het juiste antwoord")

def adb(*args, data=None, check=True):
    return subprocess.run(["adb", *args], input=data, stdout=subprocess.PIPE, stderr=subprocess.PIPE, check=check).stdout

def shell(cmd, data=None):
    return adb("shell", cmd, data=data).decode(errors="replace").strip()

def ui(name):
    for i in range(4):
        try:
            shell("uiautomator dump /sdcard/clean-quiz.xml")
            raw = adb("exec-out", "cat", "/sdcard/clean-quiz.xml")
            nodes = list(ET.fromstring(raw).iter("node"))
            (OUT / (name + ".xml")).write_bytes(raw)
            return nodes
        except (ET.ParseError, subprocess.CalledProcessError):
            time.sleep(2)
    raise AssertionError("Could not read native UI hierarchy")

def labels(n):
    return (n.get("text", ""), n.get("content-desc", ""))

def exact(nodes, text):
    return [n for n in nodes if text in labels(n)]

def bounds(n):
    return list(map(int, re.findall(r"\d+", n.get("bounds", ""))))

def click(n):
    a = bounds(n)
    assert len(a) == 4 and a[2] > a[0] and a[3] > a[1], "Control not visible"
    shell(f"input tap {(a[0]+a[2])//2} {(a[1]+a[3])//2}")
    time.sleep(1.2)

def screenshot(name):
    data = adb("exec-out", "screencap", "-p")
    assert data.startswith(b"\x89PNG\r\n\x1a\n"), "Invalid screenshot"
    (OUT / (name + ".png")).write_bytes(data)
    assert len(data) > 50000, "Possible blank screenshot"

def no_help(nodes):
    for n in nodes:
        for s in labels(n):
            assert not any(x in s.lower() for x in BAD), "Unexpected hint/speech UI: " + s
    assert not exact(nodes, "Tip"), "Tip button visible"

def page(number):
    for _ in range(8):
        logs = shell("logcat -d -s PlaylumiQA:I")
        pages = re.findall(r"screen=(\d+)", logs)
        if pages and int(pages[-1]) == number:
            return
        time.sleep(1)
    raise AssertionError(f"Expected page {number}; log: {logs[-2000:]}")

def choices(nodes):
    return {name:n for name in VEG.values() for n in exact(nodes, name)
            if n.get("class") == "android.widget.Button" and n.get("enabled") == "true"}

def persisted_quiz(count, attempts, balance):
    # The same preference file must contain the score, wallet and quiz snapshot.
    for _ in range(8):
        try:
            raw = shell(f"run-as {APP} cat shared_prefs/MainActivity.xml")
            root = ET.fromstring(raw)
            values = {node.get("name"): node for node in root}
            snaps = [json.loads(node.text) for key,node in values.items()
                     if key.startswith("library_resume_v2_2_") and node.tag=="string"]
            if (len(snaps)==1 and snaps[0]["count"]==count and snaps[0]["attempts"]==attempts
                    and int(values["score_20_good"].get("value"))==count
                    and int(values["score_20_tries"].get("value"))==attempts
                    and int(values["wardrobe_stars"].get("value"))==balance):
                return
        except (ET.ParseError, ValueError, KeyError, subprocess.CalledProcessError):
            pass
        time.sleep(.4)
    raise AssertionError(f"Quiz score, reward or saved state not consistent: {count}/{attempts}/{balance}")

def test(viewport, manual):
    print("Testing", viewport, "manual" if manual else "automatic", flush=True)
    shell(f"am force-stop {APP}")
    shell(f"pm clear {APP}")
    # Set only emulator preferences, including the legacy auto-speech flag.
    xml = ('<?xml version="1.0" encoding="utf-8" standalone="yes" ?>'
           '<map><boolean name="auto_speech" value="true" />'
           '<boolean name="sound_on" value="true" />'
           '<boolean name="reduced_motion" value="true" />'
           f'<int name="quiz_pace" value="{2 if manual else 0}" /></map>')
    shell(f"run-as {APP} sh -c 'mkdir -p shared_prefs; cat > shared_prefs/MainActivity.xml'", xml.encode())
    shell("logcat -c")
    shell(f"am start -n {APP}/.MainActivity --ez playlumi_ci_home true --ei playlumi_ci_age 2")
    time.sleep(5)
    page(1)
    w,h = map(int, re.findall(r"(\d+)x(\d+)", shell("wm size"))[-1])
    logs = shell("logcat -d -s PlaylumiQA:I")
    safe = re.findall(r"safeTop=(\d+).*?safeBottom=(\d+)", logs)
    assert safe, "Missing Android insets"
    top,bottom = map(int,safe[-1])
    # Use the actual visible native view bounds, not a reconstructed window size.
    # The unfolded layout is a grid of clickable world cards, not the village image.
    for attempt in range(6):
        home_nodes = ui(viewport+"-home-"+str(attempt))
        card = next((n for n in home_nodes if n.get("clickable")=="true" and
                    n.get("content-desc","").startswith("Open Speelplein")), None)
        if card is not None:
            click(card)
        else:
            village = next((n for n in home_nodes if
                        n.get("content-desc","").startswith("Magisch Playlumi-dorp")), None)
            if village is None:
                # On a newly booted emulator a system dialog can temporarily obscure the app.
                time.sleep(2)
                continue
            left,vt,right,vb = bounds(village)
            vw,vh = right-left,vb-vt
            assert vw>0 and vh>0, "Village is not visible"
            if vw/vh <= .72:
                dw,dh = vw,vh
            else:
                scale = min(vw/941,vh/1672)
                dw,dh = 941*scale,1672*scale
            x = round(left+(vw-dw)/2+185*dw/941)
            y = round(vt+(vh-dh)/2+1040*dh/1672)
            shell(f"input tap {x} {y}")
            time.sleep(2)
        pages = re.findall(r"screen=(\d+)", shell("logcat -d -s PlaylumiQA:I"))
        if pages and int(pages[-1]) == 44:
            break
        time.sleep(1)
    else:
        raise AssertionError("Could not open Speelplein from native home layout")
    page(44)
    for attempt in range(12):
        nodes = ui(viewport+"-library")
        hit = next((n for n in nodes if n.get("clickable")=="true" and
                    n.get("content-desc","").startswith("Speel Groente herkennen") and
                    len(bounds(n))==4 and bounds(n)[3]-bounds(n)[1]>20), None)
        if hit is not None:
            click(hit)
            break
        shell(f"input swipe {w//2} {int(h*.78)} {w//2} {int(h*.32)} 450")
        time.sleep(.8)
    else:
        raise AssertionError("Groente herkennen not found in actual game library")
    page(6)
    nodes = ui(viewport+"-question")
    assert exact(nodes,"Welke groente is dit?"), "Question missing"
    no_help(nodes)
    assert not exact(nodes,"Volgende vraag"), "Continue shown before answer"
    image = next((emoji for emoji in VEG if exact(nodes,emoji)), None)
    assert image, "No recognized vegetable illustration"
    answer = VEG[image]
    opts = choices(nodes)
    assert len(opts)>=3 and answer in opts, "Quiz choices not accessible"
    for n in opts.values():
        a = bounds(n)
        assert a[3] <= h-bottom and a[1]>=top and a[3]>a[1], "Answer outside safe area"
    screenshot(viewport+"-question")
    wrong = next(name for name in opts if name!=answer)
    click(opts[wrong])
    nodes = ui(viewport+"-wrong")
    no_help(nodes)
    assert not exact(nodes,"Volgende vraag"), "Continue shown after incorrect answer"
    assert wrong not in choices(nodes), "Incorrect choice not disabled"
    screenshot(viewport+"-wrong")
    persisted_quiz(0,1,18)
    click(choices(nodes)[answer])
    persisted_quiz(1,2,33)
    if manual:
        time.sleep(2)
        nodes = ui(viewport+"-solved")
        no_help(nodes)
        next_nodes = exact(nodes,"Volgende vraag")
        assert len(next_nodes)==1 and next_nodes[0].get("clickable")=="true", "Manual continuation missing"
        screenshot(viewport+"-solved")
        click(next_nodes[0])
    time.sleep(3)
    nodes = ui(viewport+"-next")
    no_help(nodes)
    assert not exact(nodes,"Volgende vraag"), "Next question did not start"
    assert any("1/8" in s for n in nodes for s in labels(n)), "Round progress not preserved"
    assert exact(nodes,"Welke groente is dit?"), "Next quiz prompt missing"
    screenshot(viewport+"-next")
    print("PASS:", viewport, "no hint or speech, wrong choice disabled, correct answer, next round and progress", flush=True)

adb("wait-for-device")
shell("pm disable-user --user 0 com.android.launcher3")
shell("am force-stop com.android.launcher3")
adb("install", "-r", "app/build/outputs/apk/debug/app-debug.apk")
try:
    test("phone-auto", False)
    shell("wm size 1200x1320")
    shell("wm density 210")
    time.sleep(4)
    test("fold-manual", True)
finally:
    (OUT / "logcat.txt").write_bytes(adb("logcat", "-d", "-v", "brief"))
    shell("wm size reset")
    shell("wm density reset")
