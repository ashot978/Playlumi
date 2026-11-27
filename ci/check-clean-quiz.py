"""Native UI regression test; only disposable emulator data is modified."""
import argparse, json, pathlib, re, subprocess, time, xml.etree.ElementTree as ET

APP = "nl.playlumi.app"
OUT = pathlib.Path("build/clean-quiz")
OUT.mkdir(parents=True, exist_ok=True)
VEG = {"🥕": "wortel", "🥦": "broccoli", "🌽": "maïs", "🥒": "komkommer", "🍅": "tomaat", "🫑": "paprika"}
BAD = ("💡", "vraag een hint", "lees de opdracht voor", "voorlezen", "🔊", "kies het juiste antwoord")

def adb(*args, data=None, check=True, timeout=40):
    return subprocess.run(["adb", *args], input=data, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                          check=check, timeout=timeout).stdout

def shell(cmd, data=None):
    return adb("shell", cmd, data=data).decode(errors="replace").strip()

def ui(name):
    for i in range(4):
        try:
            shell("rm -f /sdcard/clean-quiz.xml; uiautomator dump /sdcard/clean-quiz.xml")
            raw = adb("exec-out", "cat", "/sdcard/clean-quiz.xml")
            nodes = list(ET.fromstring(raw).iter("node"))
            (OUT / (name + ".xml")).write_bytes(raw)
            return nodes
        except (ET.ParseError, subprocess.CalledProcessError, subprocess.TimeoutExpired):
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

def selected_correct(nodes, answer):
    markers = [node for node in nodes if node.get("class") == "android.widget.Button"
               and node.get("content-desc") == answer+", goed gekozen"]
    assert len(markers)==1, "Correct choice lost its spoken feedback marker"
    assert markers[0].get("selected")=="true" and markers[0].get("enabled")=="false", "Correct choice must stay selected and disabled"
    assert markers[0].get("text")=="✓ "+answer, "Correct choice has no visible checkmark"

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
                     if key and key.startswith("library_resume_v2_2_") and node.tag=="string"]
            good = int(values["score_20_good"].get("value")) if "score_20_good" in values else 0
            tries = int(values["score_20_tries"].get("value")) if "score_20_tries" in values else 0
            if (len(snaps)==1 and snaps[0]["count"]==count and snaps[0]["attempts"]==attempts
                    and good==count and tries==attempts
                    and int(values["wardrobe_stars"].get("value"))==balance):
                return snaps[0]
        except (ET.ParseError, ValueError, KeyError, subprocess.CalledProcessError, subprocess.TimeoutExpired):
            pass
        time.sleep(.4)
    raise AssertionError(f"Quiz score, reward or saved state not consistent: {count}/{attempts}/{balance}")

def open_quiz(label, resume):
    shell(f"am force-stop {APP}")
    shell("logcat -c")
    shell(f"am start -W -n {APP}/.MainActivity --ei playlumi_ci_library 4 --ei playlumi_ci_age 2")
    time.sleep(3)
    page(44)
    w,h = map(int, re.findall(r"(\d+)x(\d+)", shell("wm size"))[-1])
    prefix = ("Verder spelen " if resume else "Speel ")+"Groente herkennen"
    for attempt in range(12):
        nodes = ui(label+"-library-"+str(attempt))
        hit = next((n for n in nodes if n.get("clickable")=="true" and
                    n.get("content-desc","").startswith(prefix) and
                    len(bounds(n))==4 and bounds(n)[3]-bounds(n)[1]>20), None)
        if hit is not None:
            click(hit)
            page(6)
            return
        next_page = next((n for n in nodes if n.get("content-desc") == "Volgende inhoudspagina"
                          and n.get("enabled") == "true"), None)
        if next_page is None:
            break
        click(next_page)
        time.sleep(.4)
    raise AssertionError("Groente herkennen not found: "+prefix)

def quiz_insets():
    # Non-village screens reserve their own bottom interaction area. Read
    # only the quiz screen's actual padding, not a stale home/library event.
    for _ in range(12):
        logs = shell("logcat -d -s PlaylumiQA:I")
        safe = re.findall(r"gameReservedBottom=(\d+) gameSafeTop=(\d+) gamePage=6\b", logs)
        if safe:
            bottom,top = map(int,safe[-1])
            if top>=0 and bottom>0:
                return top,bottom
        time.sleep(.5)
    raise AssertionError("Missing quiz-specific Android safe-area log: "+logs[-1500:])

def question_identity(nodes):
    image = next((emoji for emoji in VEG if exact(nodes,emoji)), None)
    assert image, "No recognized vegetable illustration"
    options = sorted((bounds(n)[1],bounds(n)[0],name) for name,n in choices(nodes).items())
    return image, [name for _,_,name in options]

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
    open_quiz(viewport+"-fresh", False)
    w,h = map(int, re.findall(r"(\d+)x(\d+)", shell("wm size"))[-1])
    top,bottom = quiz_insets()
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
    first_question = question_identity(nodes)
    # Confirm the zero-answer snapshot reached disk before killing the process.
    zero_snapshot = persisted_quiz(0,0,18)
    open_quiz(viewport+"-resume-zero", True)
    nodes = ui(viewport+"-resumed-zero")
    no_help(nodes)
    assert question_identity(nodes)==first_question, "Unanswered question changed after restart"
    assert any("0/8" in s for n in nodes for s in labels(n)), "Initial progress changed"
    restored_zero = persisted_quiz(0,0,18)
    for key in ("display","answers","correct","rejected"):
        assert zero_snapshot[key]==restored_zero[key], "Initial question snapshot changed: "+key
    print("PASS:", viewport, "unanswered question survives full restart", flush=True)
    opts = choices(nodes)
    wrong = next(name for name in opts if name!=answer)
    click(opts[wrong])
    nodes = ui(viewport+"-wrong")
    no_help(nodes)
    assert not exact(nodes,"Volgende vraag"), "Continue shown after incorrect answer"
    assert wrong not in choices(nodes), "Incorrect choice not disabled"
    assert exact(nodes,"Probeer het nog eens."), "Gentle retry feedback missing"
    assert exact(nodes,"× "+wrong), "Rejected answer has no visible cross"
    screenshot(viewport+"-wrong")
    wrong_snapshot = persisted_quiz(0,1,18)
    open_quiz(viewport+"-resume-wrong", True)
    nodes = ui(viewport+"-resumed-wrong")
    no_help(nodes)
    assert wrong not in choices(nodes), "Wrong answer was re-enabled by restart"
    assert exact(nodes,"Probeer het nog eens.") and exact(nodes,"× "+wrong), "Retry feedback lost on restore"
    assert answer in choices(nodes), "Correct answer missing after restart"
    assert question_identity(nodes)[0]==image, "Question changed after wrong answer"
    restored_snapshot = persisted_quiz(0,1,18)
    for key in ("display","answers","correct","rejected"):
        assert wrong_snapshot[key]==restored_snapshot[key], "Saved question changed: "+key
    print("PASS:", viewport, "incorrect answer and wallet survive full restart", flush=True)
    click(choices(nodes)[answer])
    persisted_quiz(1,2,33)
    if manual:
        time.sleep(2)
        nodes = ui(viewport+"-solved")
        no_help(nodes)
        selected_correct(nodes, answer)
        next_nodes = exact(nodes,"Volgende vraag")
        assert len(next_nodes)==1 and next_nodes[0].get("clickable")=="true", "Manual continuation missing"
        screenshot(viewport+"-solved")
        open_quiz(viewport+"-resume-solved", True)
        nodes = ui(viewport+"-resumed-solved")
        no_help(nodes)
        selected_correct(nodes, answer)
        next_nodes = exact(nodes,"Volgende vraag")
        assert len(next_nodes)==1 and next_nodes[0].get("clickable")=="true", "Manual continuation lost on restart"
        assert not choices(nodes), "Previously rewarded answer became clickable"
        persisted_quiz(1,2,33)
        click(next_nodes[0])
    time.sleep(3)
    nodes = ui(viewport+"-next")
    no_help(nodes)
    assert not exact(nodes,"Volgende vraag"), "Next question did not start"
    assert any("1/8" in s for n in nodes for s in labels(n)), "Round progress not preserved"
    assert exact(nodes,"Welke groente is dit?"), "Next quiz prompt missing"
    screenshot(viewport+"-next")
    print("PASS:", viewport, "no hint or speech, wrong choice disabled, correct answer, next round and progress", flush=True)

if __name__=="__main__":
    parser=argparse.ArgumentParser()
    parser.add_argument("--viewport",choices=["phone","fold"],required=True)
    parser.add_argument("--keep-viewport",action="store_true",help="Keep emulator dimensions for a following test script")
    args=parser.parse_args()
    adb("wait-for-device",timeout=60)
    shell("pm disable-user --user 0 com.android.launcher3")
    shell("am force-stop com.android.launcher3")
    adb("install","-r","app/build/outputs/apk/debug/app-debug.apk",timeout=120)
    try:
        if args.viewport=="fold":
            shell("wm size 1200x1320")
            shell("wm density 210")
            time.sleep(4)
        test(args.viewport+"-quiz",args.viewport=="fold")
    finally:
        for filename,command in (("logcat.txt",("logcat","-d","-v","brief")),
                                 ("window.txt",("shell","dumpsys window")),
                                 ("last-screen.png",("exec-out","screencap","-p"))):
            try:
                (OUT/filename).write_bytes(adb(*command,timeout=5))
            except (subprocess.SubprocessError,OSError) as error:
                print("Diagnostic unavailable:",filename,type(error).__name__,flush=True)
        if not args.keep_viewport:
            for command in ("wm size reset","wm density reset"):
                try:
                    adb("shell",command,timeout=5)
                except (subprocess.SubprocessError,OSError) as error:
                    print("Cleanup unavailable:",command,type(error).__name__,flush=True)
