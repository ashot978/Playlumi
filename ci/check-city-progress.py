"""Exercise the real city/daily/wardrobe UI on a disposable Android emulator.

City tests use HOME as their only debug entrance, then native controls. The optional
upgrade check also uses the baseline's existing debug library entrance to capture
a real quiz snapshot. Fixtures are synthetic; no physical device is ever modified.
Question content is verified by the Java curriculum suite. Here the saved answer is
used only to exercise wallet/state transitions through the actual answer button.
"""
import argparse
import datetime
import hashlib
import json
import pathlib
import re
import struct
import subprocess
import time
import xml.etree.ElementTree as ET
import zlib


APP = "nl.playlumi.app"
OUT = pathlib.Path("build/city-progress")
CHECKS = []
PAGED_UI = True
LANDMARKS = ("Vriendjespark", "Observatorium", "Toverbibliotheek", "Muziekpodium",
             "Dierenpark", "Regenboogbrug", "Lumi-standbeeld")
LANDMARK_ACHIEVEMENTS = (5, 24, 25, 27, 28, 29, 19)
FORBIDDEN = ("vraag een hint", "lees de opdracht voor", "voorlezen", "🔊")


def adb(*args, data=None, check=True, timeout=40):
    result = subprocess.run(["adb", *args], input=data, stdout=subprocess.PIPE,
                            stderr=subprocess.PIPE, check=check, timeout=timeout)
    return result.stdout


def shell(command, data=None):
    return adb("shell", command, data=data).decode(errors="replace").strip()


def pass_check(message):
    CHECKS.append(message)
    print("PASS:", message, flush=True)


def prefs(profile=0):
    assert profile in (0, 1, 2), "Unknown local player"
    name = "MainActivity" if profile == 0 else "playlumi-child-" + str(profile)
    raw = adb("exec-out", "run-as", APP, "cat", "shared_prefs/" + name + ".xml")
    root = ET.fromstring(raw)
    result = {}
    for node in root:
        key = node.get("name")
        if node.tag in ("int", "long"):
            result[key] = int(node.get("value"))
        elif node.tag == "boolean":
            result[key] = node.get("value") == "true"
        elif node.tag == "set":
            result[key] = {item.text for item in node}
        else:
            result[key] = node.text or ""
    return result


def wait_prefs(predicate, expectation, profile=0):
    last = {}
    for _ in range(20):
        try:
            last = prefs(profile)
            if predicate(last):
                return last
        except (ET.ParseError, ValueError, KeyError, subprocess.CalledProcessError):
            pass
        time.sleep(.3)
    # This file is synthetic emulator data, with no account or user information.
    (OUT / "failed-preferences.json").write_text(
        json.dumps(last, indent=2, ensure_ascii=False, default=list))
    raise AssertionError("Preferences did not reach: " + expectation)


def seed(values):
    shell(f"am force-stop {APP}")
    shell(f"pm clear {APP}")
    base = {"chosen_age": 0, "quiz_pace": 2, "reduced_motion": True,
            "sound_on": False, "auto_speech": True, "catalog_adaptive": False,
            "wardrobe_initialized": True, "wardrobe_stars": 18,
            "wardrobe_total_earned": 18}
    base.update(values)
    root = ET.Element("map")
    for key, value in base.items():
        if isinstance(value, bool):
            ET.SubElement(root, "boolean", name=key, value=str(value).lower())
        elif isinstance(value, int):
            tag = "long" if key in ("daily_day_v1", "achievements_v1") else "int"
            ET.SubElement(root, tag, name=key, value=str(value))
        elif isinstance(value, set):
            node = ET.SubElement(root, "set", name=key)
            for item in sorted(value):
                ET.SubElement(node, "string").text = item
        else:
            ET.SubElement(root, "string", name=key).text = str(value)
    raw = ET.tostring(root, encoding="utf-8", xml_declaration=True)
    shell(f"run-as {APP} sh -c 'mkdir -p shared_prefs; cat > shared_prefs/MainActivity.xml'", raw)


def page(number):
    for _ in range(12):
        log = shell("logcat -d -s PlaylumiQA:I")
        pages = re.findall(r"screen=(\d+)", log)
        if pages and int(pages[-1]) == number:
            return
        time.sleep(.5)
    raise AssertionError(f"Expected page {number}; log: {log[-2000:]}")


def start_home(age=0):
    shell(f"am force-stop {APP}")
    shell("logcat -c")
    shell(f"am start -W -n {APP}/.MainActivity --ez playlumi_ci_home true --ei playlumi_ci_age {age}")
    time.sleep(2)
    page(1)


def ui(label):
    for _ in range(4):
        try:
            shell("rm -f /sdcard/city-progress.xml; uiautomator dump /sdcard/city-progress.xml")
            raw = adb("exec-out", "cat", "/sdcard/city-progress.xml")
            nodes = list(ET.fromstring(raw).iter("node"))
            (OUT / (label + ".xml")).write_bytes(raw)
            return nodes
        except (ET.ParseError, subprocess.CalledProcessError, subprocess.TimeoutExpired):
            time.sleep(1)
    raise AssertionError("Could not read native UI hierarchy: " + label)


def labels(node):
    return node.get("text", ""), node.get("content-desc", "")


def rect(node):
    return tuple(map(int, re.findall(r"-?\d+", node.get("bounds", ""))))


def dimensions():
    return tuple(map(int, re.findall(r"(\d+)x(\d+)", shell("wm size"))[-1]))


def visible(node):
    box = rect(node)
    return len(box) == 4 and box[2] > box[0] and box[3] - box[1] >= 20


def exact(value):
    return lambda node: value in labels(node)


def description_prefix(value):
    return lambda node: node.get("content-desc", "").startswith(value)


def pager_button(nodes, forward):
    desc = "Volgende inhoudspagina" if forward else "Vorige inhoudspagina"
    return next((n for n in nodes if n.get("content-desc") == desc and
                 n.get("enabled") == "true" and visible(n)), None)


def turn_page(nodes, forward):
    button = pager_button(nodes, forward)
    if button is None:
        return False
    click(button)
    return True


def page_indicator(nodes):
    return next((n.get("content-desc", "") for n in nodes
                 if n.get("content-desc", "").startswith("Inhoudspagina ")), "")


def find_control(predicate, label, scroll=True, enabled=True, scroll_x=None):
    w, h = dimensions()
    def hit(nodes):
        return next((node for node in nodes if predicate(node) and visible(node)
                    and node.get("clickable") == "true"
                    and (enabled is None or node.get("enabled") == str(enabled).lower())), None)
    if PAGED_UI:
        nodes = ui(label + "-current")
        result = hit(nodes)
        if result is not None:
            return result
        # The previous tests' "scroll" argument only applied to the old
        # baseline. All actual content pages may now be reached via arrows.
        for i in range(60):
            if not turn_page(nodes, True):
                break
            nodes = ui(label + "-next-" + str(i))
            result = hit(nodes)
            if result is not None:
                return result
        for i in range(60):
            if not turn_page(nodes, False):
                break
            nodes = ui(label + "-previous-" + str(i))
            result = hit(nodes)
            if result is not None:
                return result
        for i in range(2):
            time.sleep(.4)
            result = hit(ui(label + "-settle-" + str(i)))
            if result is not None:
                return result
    else:
        for attempt in range(11 if scroll else 3):
            nodes = ui(label + "-baseline-" + str(attempt))
            result = hit(nodes)
            if result is not None:
                return result
            if scroll:
                x = w//2 if scroll_x is None else scroll_x
                shell(f"input swipe {x} {int(h*.78)} {x} {int(h*.34)} 400")
            time.sleep(.5)
    raise AssertionError("Native control not found: " + label)


def click(node):
    left, top, right, bottom = rect(node)
    assert right > left and bottom > top, "Control is not visible"
    shell(f"input tap {(left+right)//2} {(top+bottom)//2}")
    time.sleep(.8)


def click_named(value, label, scroll=True):
    click(find_control(exact(value), label, scroll))


def screenshot(label, expected_texts=(), expected_descriptions=()):
    # Screen logs and saved preferences precede drawing. Wait for accessibility
    # idle, then give rendering a short settle window before the capture.
    normal = lambda text: " ".join(text.split())
    for _ in range(4):
        nodes = ui(label + "-capture")
        missing = [text for text in expected_texts
                   if not any(visible(node) and any(normal(value) == normal(text) for value in labels(node))
                              for node in nodes)]
        missing += [description for description in expected_descriptions
                    if not any(visible(node) and description_prefix(description)(node) for node in nodes)]
        if not missing:
            break
        time.sleep(.4)
    assert not missing, "Capture has not reached the expected UI: " + label + ": " + repr(missing)
    time.sleep(.4)
    raw = adb("exec-out", "screencap", "-p")
    assert raw.startswith(b"\x89PNG\r\n\x1a\n") and len(raw) > 30000, "Invalid/blank screenshot"
    (OUT / (label + ".png")).write_bytes(raw)
    return nodes


def no_speech(nodes):
    for node in nodes:
        for value in labels(node):
            assert not any(bad in value.lower() for bad in FORBIDDEN), "Unexpected speech/hint UI: " + value


def correct_marker(nodes, answer):
    normal = lambda text: " ".join(text.split())
    marked = [node for node in nodes if node.get("class") == "android.widget.Button"
              and normal(node.get("content-desc", "")) == normal(answer + ", goed gekozen")]
    assert len(marked) == 1, "Correct answer has no unique accessible selection marker"
    assert marked[0].get("selected") == "true" and marked[0].get("enabled") == "false", "Correct answer must remain selected and disabled"
    assert normal(marked[0].get("text", "")) == normal("✓ " + answer), "Correct answer checkmark missing"


def open_city(label):
    click_named("Open Lumi's stad", label + "-entry", False)
    page(49)


def open_daily(label):
    click(find_control(description_prefix("Dagelijkse uitdagingen"), label + "-entry", False))
    page(48)


def daily_quiz(index, label):
    click_named("Daguitdaging " + str(index) + " spelen", label + "-play")
    page(6)
    state = wait_prefs(lambda p: any(key.startswith("library_resume_v2_0_") for key in p),
                       "daily button opens a saved age-0 quiz")
    snapshots = [json.loads(value) for key, value in state.items()
                 if key.startswith("library_resume_v2_0_")]
    assert len(snapshots) == 1, "Daily test should have exactly one quiz session"
    assert snapshots[0]["id"].split(":")[1] == "0", "Daily goal sent child to a non-quiz game"
    return snapshots[0]


def snapshot(route):
    return json.loads(prefs()["library_resume_v2_0_" + route])


def answer_saved(route, count, expected_stars, label):
    saved = snapshot(route)
    assert saved["count"] == count - 1 and not saved["solved"], "Wrong question state before answer"
    answer = saved["answers"][saved["correct"]]
    nodes = ui(label + "-question")
    no_speech(nodes)
    button = next((node for node in nodes if exact(answer)(node)
                   and node.get("class") == "android.widget.Button"
                   and node.get("enabled") == "true" and visible(node)), None)
    assert button is not None, "Correct native answer is not accessible: " + answer
    click(button)
    key = "library_resume_v2_0_" + route
    if count < 8:
        state = wait_prefs(lambda p: p.get("wardrobe_stars") == expected_stars
                           and key in p and json.loads(p[key])["count"] == count
                           and json.loads(p[key])["solved"],
                           f"answer {count}, wallet {expected_stars}, solved snapshot")
    else:
        state = wait_prefs(lambda p: p.get("wardrobe_stars") == expected_stars
                           and key not in p and p.get("library_rounds_v1_0_" + route) == 1,
                           "completed quiz removed, one played round, exact final wallet")
    return state


def next_question(label):
    click_named("Volgende vraag", label, False)
    page(6)


def emulator_day():
    # Use the emulator's local calendar, matching PlaylumiPlayDays.today().
    date = datetime.date.fromisoformat(shell("date +%Y-%m-%d"))
    return (date - datetime.date(1970, 1, 1)).days


def assert_simple_library(label):
    nodes = screenshot(label, expected_descriptions=("Dorp", "Werelden"))
    values = [" ".join(value.lower().split()) for node in nodes for value in labels(node)]
    assert not any("favoriet" in value or "speelidee" in value
                   or "eerder gespeeld" in value or "eerder gespeelde spellen" in value
                   or value.startswith("zoeken in alle spellen")
                   or value.startswith("bekijk alle ") and "onafgemaakte spellen" in value
                   or value.startswith("⌕ zoeken") or value.startswith("▶ verder (")
                   or value in ("▶ spellen", "alle spellen") for value in values), "Retired library control is visible"
    assert any(value.startswith("werelden") for value in values), "Worlds navigation disappeared"
    assert any(value == "dorp" for value in values), "Village navigation disappeared"


def start_upgrade_quiz(label, resume):
    shell(f"am force-stop {APP}")
    shell("logcat -c")
    shell(f"am start -W -n {APP}/.MainActivity --ei playlumi_ci_library 4 --ei playlumi_ci_age 2")
    time.sleep(2)
    page(44)
    if resume:
        assert_simple_library(label + "-simplified-library")
    prefix = ("Verder spelen " if resume else "Speel ") + "Groente herkennen"
    click(find_control(description_prefix(prefix), label + "-library"))
    page(6)
    key = "library_resume_v2_2_4:0:20:2"
    state = wait_prefs(lambda p: key in p, "real vegetable quiz snapshot")
    return json.loads(state[key])


def test_debug_upgrade(baseline_apk, viewport):
    baseline = pathlib.Path(baseline_apk)
    global PAGED_UI
    PAGED_UI = True  # Both baseline and candidate use horizontal pages.
    candidate = pathlib.Path("app/build/outputs/apk/debug/app-debug.apk")
    assert baseline.is_file() and candidate.is_file(), "Both debug upgrade APKs must exist"
    # Install the older debuggable build as fixture setup, then seed its clean data.
    # Avoid uninstall/launcher churn on the emulator. The actual forward upgrade
    # below uses install -r exclusively, with no clearing between versions.
    adb("shell", "am force-stop " + APP)
    adb("install", "-r", "-d", str(baseline), timeout=120)
    assert "versionName=0.123" in shell(f"dumpsys package {APP}"), "Wrong upgrade baseline"
    fixture = {
        "chosen_age": 2, "chosen_avatar": 3,
        "wardrobe_stars": 137, "wardrobe_total_earned": 599,
        "wardrobe_owned_0": 17, "wardrobe_worn_0": 4,
        "wardrobe_owned_1": 3, "wardrobe_worn_1": 1,
        "wardrobe_owned_2": 5, "wardrobe_worn_2": 2,
        "wardrobe_owned_3": 33, "wardrobe_worn_3": 5,
        "wardrobe_owned_4": 3, "wardrobe_worn_4": 1,
        "wardrobe_owned_5": 5, "wardrobe_worn_5": 2,
        "library_favorites": {"4:0:20:2", "4:6:2:0"},
        "library_completed_v1_2": {"4:6:2:0"},
        "library_rounds_v1_2_4:6:2:0": 6,
        "library_completed_v1_0": {"0:6:0:0"},
        "library_rounds_v1_0_0:6:0:0": 2,
        # HOME synchronizes already-earned milestones in the old app too:
        # 599-18 = 581 earned stars => bits 10/11; 12 owned items minus
        # six defaults = six accessories => bit 20. Keep legacy bits 17/23.
        "achievements_v1": (1 << 10) | (1 << 11) | (1 << 17) | (1 << 20) | (1 << 23),
        "daily_day_v1": emulator_day(), "daily_a_v1": 2, "daily_b_v1": 1,
        "daily_round_v1": 0, "daily_claimed_v1": 0, "daily_sets_v1": 4,
    }
    seed(fixture)
    # This is actual baseline artwork, made through HOME/world/library and a
    # touch gesture. Do not synthesize a PNG or award a creative completion.
    launch_native_home()
    canvas = open_drawing(viewport + "-upgrade-baseline-drawing")
    left, top, right, bottom = rect(canvas)
    shell(f"input swipe {int(left+.25*(right-left))} {(top+bottom)//2} "
          f"{int(left+.75*(right-left))} {(top+bottom)//2} 500")
    screenshot(viewport + "-upgrade-baseline-drawing",
               expected_descriptions=("Teken met je vinger op dit witte vel",))
    click_named("Terug naar het magische dorp", viewport + "-upgrade-baseline-drawing-home", False)
    page(1)
    before_drawing = drawing_file(viewport + "-upgrade-baseline-drawing-file")
    assert not prefs().get("drawing_saved_once", False), "Baseline autosave granted a creative reward"
    before_question = start_upgrade_quiz(viewport + "-upgrade-baseline", False)
    assert before_question["count"] == 0 and not before_question["solved"], "Baseline quiz must be unanswered"
    before = prefs()
    (OUT / (viewport + "-upgrade-baseline-state.json")).write_text(json.dumps({
        "expectedPreferences": fixture,
        "actualPreferences": {key: before.get(key) for key in fixture},
        "drawingSavedOnce": before.get("drawing_saved_once", False),
        "drawingSha256": hashlib.sha256(before_drawing).hexdigest(),
        "scope": "Synthetic baseline preferences after actual native artwork creation, before package upgrade."
    }, indent=2, ensure_ascii=False, default=list))
    for key, expected in fixture.items():
        assert before.get(key) == expected, "Baseline did not retain its fixture: " + key + ": " + repr(before.get(key)) + " != " + repr(expected)
    screenshot(viewport + "-upgrade-baseline-question",
               expected_texts=(before_question["prompt"], before_question["display"], *before_question["answers"]))
    shell(f"am force-stop {APP}")
    adb("install", "-r", str(candidate), timeout=120)
    assert "versionName=0.126" in shell(f"dumpsys package {APP}"), "Wrong candidate version after upgrade"
    PAGED_UI = True
    installed = prefs()
    for key in fixture:
        assert installed.get(key) == before[key], "Package upgrade lost preference: " + key
    assert drawing_file(viewport + "-upgrade-installed-drawing-file") == before_drawing, "Package upgrade changed the real baseline artwork"
    after_question = start_upgrade_quiz(viewport + "-upgrade-current", True)
    identity = ("id", "age", "level", "count", "total", "attempts", "solved", "hadMistake",
                "rejected", "feedback", "prompt", "display", "color", "correct", "fingers", "answers", "colors")
    for key in identity:
        assert after_question[key] == before_question[key], "Upgrade changed saved question: " + key
    after = prefs()
    for key in fixture:
        assert after.get(key) == before[key], "Opening upgraded quiz changed stored progress: " + key
    screenshot(viewport + "-upgrade-current-question",
               expected_texts=(after_question["prompt"], after_question["display"], *after_question["answers"]))
    click_named("Terug naar het magische dorp", viewport + "-upgrade-current-drawing-home", False)
    page(1)
    open_drawing(viewport + "-upgrade-current-drawing")
    drawing_nodes = screenshot(viewport + "-upgrade-current-drawing",
                               expected_descriptions=("Teken met je vinger op dit witte vel",))
    if viewport == "fold":
        # These controls share the first visible viewport with the old artwork.
        # Catch an elevated Canvas painting over them before the longer cases.
        for index, tool in enumerate(("Gum", "Lijndikte Normaal", "💾 Bewaar tekening")):
            find_control(description_prefix(tool), viewport + "-upgrade-tool-" + str(index))
            drawing_controls_pixels(viewport + "-upgrade-current-tool-" + str(index),
                                    screenshot(viewport + "-upgrade-current-tool-" + str(index)), (tool,))
    click_named("Terug naar het magische dorp", viewport + "-upgrade-drawing-noop-save", False)
    page(1)
    after_drawing = drawing_file(viewport + "-upgrade-current-drawing-file")
    assert after_drawing == before_drawing, "Opening/resizing unchanged old artwork rewrote or lost its saved PNG"
    after_drawing_prefs = prefs()
    assert not after_drawing_prefs.get("drawing_saved_once", False), "Opening old artwork granted a creative reward"
    for key in fixture:
        assert after_drawing_prefs.get(key) == before[key], "Opening old artwork changed stored progress: " + key
    (OUT / (viewport + "-upgrade-comparison.json")).write_text(json.dumps({
        "baselineVersion": "0.123", "candidateVersion": "0.126",
        "baselineSha256": hashlib.sha256(baseline.read_bytes()).hexdigest(),
        "candidateSha256": hashlib.sha256(candidate.read_bytes()).hexdigest(),
        "preferencesCompared": list(fixture), "questionFieldsCompared": list(identity),
        "drawingSource": "Actual native touchstroke saved by baseline Home navigation",
        "drawingBytesCompared": len(before_drawing),
        "baselineDrawingSha256": hashlib.sha256(before_drawing).hexdigest(),
        "candidateDrawingSha256": hashlib.sha256(after_drawing).hexdigest(),
        "scope": "Same-runner debug-signed install -r upgrade on a disposable emulator. Original release signing and physical-device acceptance are separate checks."
    }, indent=2, ensure_ascii=False))
    pass_check(viewport + ": actual debug 0.123 → 0.126 install -r preserves wallet, owned/equipped items, favourites, achievements, daily state, age-specific replay counts, exact unanswered quiz and real baseline artwork")


def scroll_top():
    if PAGED_UI:
        nodes = ui("first-page")
        for _ in range(60):
            if not turn_page(nodes, False):
                break
            nodes = ui("first-page-step")
        return
    w, h = dimensions()
    for _ in range(4):
        shell(f"input swipe {w//2} {int(h*.30)} {w//2} {int(h*.82)} 350")
        time.sleep(.2)


def scroll_bottom():
    if PAGED_UI:
        nodes = ui("last-page")
        for _ in range(60):
            if not turn_page(nodes, True):
                break
            nodes = ui("last-page-step")
        return
    w, h = dimensions()
    for _ in range(4):
        shell(f"input swipe {w//2} {int(h*.82)} {w//2} {int(h*.30)} 350")
        time.sleep(.2)


def test_new_quiz_routes(viewport):
    cases = ((0, "Rekenraket", "Evenveel", "0:0:11:5"),
             (2, "Rekenraket", "Tientallen en eenheden", "0:0:12:8"),
             (2, "Speelplein", "Eerst en daarna", "4:0:19:5"),
             (0, "Rekenraket", "Tel de vormen", "0:0:0:5"),
             (1, "Rekenraket", "Dagen van de week", "0:0:16:5"),
             (2, "Letterhuis", "Lees en kies", "1:0:6:5"),
             (0, "Speelplein", "Wat hoort er niet bij?", "4:0:15:5"),
              (0, "Rekenraket", "Eentje erbij of eraf", "0:0:0:6"),
              (0, "Vormkasteel", "Hoeken tellen", "2:0:3:6"),
              (1, "Muziekstudio", "Notenpatroon", "3:0:5:6"),
              (1, "Kleurentuin", "Kleur bij het woord", "5:0:1:6"))
    normal = lambda text: " ".join(text.split())
    for index, (age, world, title, route) in enumerate(cases):
        label = viewport + "-new-quiz-" + str(index + 1)
        seed({"chosen_age": age})
        if index < 3:
            start_home(age)
        else:
            launch_native_home()
        click(find_control(description_prefix(world), label + "-world", False))
        page(44)
        click(find_control(description_prefix("Speel " + title), label + "-entry"))
        page(6)
        key = "library_resume_v2_" + str(age) + "_" + route
        state = wait_prefs(lambda p: key in p, "new quiz initial snapshot: " + title)
        saved = json.loads(state[key])
        assert saved["count"] == 0 and not saved["solved"], "New quiz did not start unanswered"
        nodes = ui(label + "-unanswered")
        no_speech(nodes)
        for expected in (saved["prompt"], saved["display"]):
            assert any(visible(node) and any(normal(text) == normal(expected) for text in labels(node))
                       for node in nodes), "Question not visible/accessibly labelled: " + title
        w, h = dimensions()
        insets = re.findall(r"gameReservedBottom=(\d+) gameSafeTop=(\d+) gamePage=6\b",
                            shell("logcat -d -s PlaylumiQA:I"))
        assert insets, "New quiz did not report safe insets: " + title
        bottom, top = map(int, insets[-1])
        choices = {}
        for answer in saved["answers"]:
            button = next((node for node in nodes if node.get("class") == "android.widget.Button"
                           and node.get("enabled") == "true" and visible(node)
                           and any(normal(text) == normal(answer) for text in labels(node))), None)
            assert button is not None, "New quiz answer not accessible: " + title + ": " + answer
            left, y1, right, y2 = rect(button)
            assert left >= 0 and right <= w and y1 >= top and y2 <= h-bottom, "New quiz answer outside safe area: " + title
            choices[answer] = button
        assert len(choices) == len(saved["answers"]) >= 3, "Missing/duplicate new quiz options"
        screenshot(label + "-unanswered")
        click(choices[saved["answers"][saved["correct"]]])
        mode = route.split(":")[2]
        rewarded = wait_prefs(lambda p: p.get("wardrobe_stars") == 33 and key in p
                             and json.loads(p[key])["count"] == 1 and json.loads(p[key])["solved"]
                             and p.get("score_" + mode + "_good") == 1,
                             "one credited native answer in " + title)
        assert rewarded["score_" + mode + "_tries"] == 1, "One answer created multiple attempts"
        solved_nodes = ui(label + "-correct")
        no_speech(solved_nodes)
        correct_marker(solved_nodes, saved["answers"][saved["correct"]])
        assert any(exact("Volgende vraag")(node) and visible(node) for node in solved_nodes), "New quiz lost manual continuation"
        screenshot(label + "-correct")
        if route == "4:0:15:5":
            solved = json.loads(rewarded[key])
            launch_native_home()
            click(find_control(description_prefix(world), label + "-restart-world", False))
            page(44)
            click(find_control(description_prefix("Verder spelen " + title), label + "-restart-entry"))
            page(6)
            restored = prefs()
            same_quiz(solved, json.loads(restored[key]), "New classification quiz restart")
            assert restored["wardrobe_stars"] == 33 and restored["score_" + mode + "_tries"] == 1, "Restart repeated the new quiz reward/attempt"
            restored_nodes = screenshot(label + "-restored")
            no_speech(restored_nodes)
            correct_marker(restored_nodes, saved["answers"][saved["correct"]])
        pass_check(viewport + ": " + title + " opens through HOME/world, displays all answer controls in safe area and credits one correct answer")


def test(viewport):
    prefix = viewport + "-city"
    seed({})
    start_home()
    screenshot(prefix + "-home")
    open_city(prefix + "-fresh")
    park = find_control(description_prefix("Vriendjespark,"), prefix + "-locked-park")
    assert "nog te ontdekken" in park.get("content-desc", ""), "Fresh park incorrectly unlocked"
    density = int(re.findall(r"(\d+)", shell("wm density"))[-1]) / 160
    left, top, right, bottom = rect(park)
    assert min(right-left, bottom-top) >= 48*density-1, "City tap target smaller than 48 dp"
    no_speech(ui(prefix + "-locked"))
    screenshot(prefix + "-locked")
    click(park)
    dialog = ui(prefix + "-locked-detail")
    assert any("Eerste quizronde" in text for node in dialog for text in labels(node)), "Locked plot missing requirement"
    assert any("Vlindervriend" in text for node in dialog for text in labels(node)), "Locked plot missing real gift"
    screenshot(prefix + "-locked-detail")
    shell("input keyevent KEYCODE_BACK")
    time.sleep(.5)
    click_named("Open kledingkast", prefix + "-fresh-wardrobe")
    page(10)
    screenshot(prefix + "-fresh-wardrobe")
    shell("input keyevent KEYCODE_BACK")
    page(49)
    pass_check(viewport + ": real HOME → city → locked plot → wardrobe → city navigation")

    # Synthetic learning history suppresses already-tested answer badge overlays.
    # No completed session or city collectible is seeded, so the park remains locked.
    values = {"daily_day_v1": emulator_day(), "daily_a_v1": 3, "daily_b_v1": 0,
              "daily_round_v1": 0, "daily_claimed_v1": 0, "daily_sets_v1": 0}
    for mode in range(32):
        values.update({f"score_{mode}_good": 200, f"score_{mode}_tries": 200,
                       f"score_{mode}_best": 5, f"score_{mode}_streak": 0,
                       f"score_{mode}_sessions": 0})
    seed(values)
    start_home()
    open_daily(prefix + "-daily")
    nodes = screenshot(prefix + "-daily-before")
    assert all(any(exact("Daguitdaging " + str(i) + " spelen")(n) and visible(n) for n in nodes) for i in (1, 2, 3)), "Daily goals require paging"
    assert not page_indicator(nodes), "Daily menu has an unnecessary paginator"
    initial = daily_quiz(1, prefix + "-daily")
    route = initial["id"]
    state = answer_saved(route, 1, 36, prefix + "-daily-first")
    assert state.get("daily_a_v1") == 4 and state.get("daily_claimed_v1") == 1, "Daily goal not awarded once"
    solved = json.loads(state["library_resume_v2_0_" + route])
    screenshot(prefix + "-daily-earned")
    start_home()
    open_daily(prefix + "-daily-restart")
    restored = daily_quiz(1, prefix + "-daily-restart")
    for field in ("id", "count", "attempts", "solved", "display", "answers", "correct", "rejected"):
        assert restored[field] == solved[field], "Daily shortcut changed saved question: " + field
    assert prefs()["wardrobe_stars"] == 36, "Rendering/resuming paid the daily reward twice"
    next_question(prefix + "-daily-next")
    answer_saved(route, 2, 39, prefix + "-daily-second")
    pass_check(viewport + ": daily goal launches a valid quiz, pays +15 once, and restores solved state exactly")

    for count in range(3, 9):
        next_question(prefix + "-continue-" + str(count))
        expected = 18 + 3*count + 15 + (32 if count == 8 else 0)
        state = answer_saved(route, count, expected, prefix + "-answer-" + str(count))
    assert state.get("daily_claimed_v1") == 5 and state.get("daily_round_v1") == 1, "Completed-round daily goal not persisted"
    assert route in state.get("library_completed_v1_0", set()), "Replay completion mark missing"
    assert state.get("library_rounds_v1_0_" + route) == 1, "One round must count exactly once"
    pass_check(viewport + ": full round saves exact 89-star wallet, daily goals and one replay count")

    start_home()
    open_city(prefix + "-earned")
    park = find_control(description_prefix("Vriendjespark,"), prefix + "-earned-park")
    assert "vrijgespeeld" in park.get("content-desc", ""), "First quiz did not visibly build the park"
    earned = wait_prefs(lambda p: p.get("wardrobe_owned_3", 1) & 2 != 0,
                        "permanent butterfly gift")
    assert earned["wardrobe_stars"] == 89, "Opening city changed spendable stars"
    screenshot(prefix + "-park-unlocked")
    click(park)
    screenshot(prefix + "-park-detail")
    shell("input keyevent KEYCODE_BACK")
    time.sleep(.5)
    click_named("Open kledingkast", prefix + "-gift-wardrobe")
    page(10)
    preview = next(n for n in ui(prefix + "-closet-categories") if exact("Lumi, voorbeeld van je outfit")(n) and visible(n))
    preview_rect = rect(preview)
    click(find_control(lambda n: n.get("content-desc", "").startswith("Vriendjes"), prefix + "-friends-tab"))
    friend_nodes = ui(prefix + "-closet-items")
    assert any(exact("Lumi, voorbeeld van je outfit")(n) and rect(n) == preview_rect for n in friend_nodes), "Lumi moved or disappeared while browsing accessories"
    assert any(exact("Kies een andere accessoirecategorie")(n) and visible(n) for n in friend_nodes), "Category shortcut disappeared"
    click(find_control(lambda n: "Vlindervriend" in n.get("content-desc", ""), prefix + "-equip-butterfly"))
    equipped = wait_prefs(lambda p: p.get("wardrobe_worn_3") == 1, "butterfly equipped")
    assert equipped["wardrobe_stars"] == 89, "Earned gift cost stars to equip"
    screenshot(prefix + "-gift-equipped")
    start_home()
    restored = prefs()
    assert restored["wardrobe_worn_3"] == 1 and restored["wardrobe_owned_3"] & 2, "Equipped gift lost after restart"
    assert restored["wardrobe_stars"] == 89, "Restart repeated/lost earned rewards"
    assert restored["library_rounds_v1_0_" + route] == 1, "Restart counted completed round again"
    open_city(prefix + "-restored")
    assert "vrijgespeeld" in find_control(description_prefix("Vriendjespark,"), prefix + "-restored-park").get("content-desc", "")
    pass_check(viewport + ": first park and free butterfly unlock, equip and persist without duplicate rewards")

    # A complete interactive board is another reward transaction, using its real
    # world/library controls and one restart between correct steps.
    start_home()
    click(find_control(description_prefix("Rekenraket"), prefix + "-rocket-entry", False))
    page(44)
    click(find_control(description_prefix("Speel Getallenrace"), prefix + "-numbers-entry"))
    page(45)
    click_named("Kies 1", prefix + "-numbers-one")
    interactive_key = "library_resume_v2_0_0:6:0:0"
    first_step = wait_prefs(lambda p: interactive_key in p
                           and json.loads(p[interactive_key])["count"] == 1,
                           "interactive first step saved")
    assert first_step["wardrobe_stars"] == 89, "Partial interactive board awarded stars"
    first_board = json.loads(first_step[interactive_key])
    start_home()
    click(find_control(description_prefix("Rekenraket"), prefix + "-rocket-resume", False))
    page(44)
    click(find_control(description_prefix("Verder spelen Getallenrace"), prefix + "-numbers-resume"))
    page(45)
    resumed_board = json.loads(prefs()[interactive_key])
    for field in ("id", "age", "seed", "engine", "count", "total"):
        assert resumed_board[field] == first_board[field], "Interactive restart changed board: " + field
    click_named("Kies 2", prefix + "-numbers-two")
    click_named("Kies 3", prefix + "-numbers-three")
    board_result = wait_prefs(lambda p: p.get("wardrobe_stars") == 92
                             and interactive_key not in p
                             and p.get("library_rounds_v1_0_0:6:0:0") == 1,
                             "interactive reward, completion and cleared snapshot together")
    assert "0:6:0:0" in board_result.get("library_completed_v1_0", set()), "Interactive completion missing"
    screenshot(prefix + "-interactive-complete")
    start_home()
    final = prefs()
    assert final["wardrobe_stars"] == 92 and final["library_rounds_v1_0_0:6:0:0"] == 1, "Interactive reward repeated on restart"
    pass_check(viewport + ": interactive partial state resumes exactly; final board saves one reward and replay count")

    # Separate visual fixture: do not describe seeded buildings as earned by this test.
    seed({"achievements_v1": sum(1 << bit for bit in LANDMARK_ACHIEVEMENTS)})
    start_home()
    open_city(prefix + "-full-fixture")
    find_control(description_prefix("Vriendjespark,"), prefix + "-first-plot")
    top_nodes = screenshot(prefix + "-all-unlocked-fixture",
                           expected_texts=("Lumi's groeiende stad",),
                           expected_descriptions=("Vriendjespark, vrijgespeeld.",))
    first_plot = next(node for node in top_nodes if description_prefix("Vriendjespark,")(node))
    plot_height = rect(first_plot)[3] - rect(first_plot)[1]
    for name in LANDMARKS:
        node = find_control(description_prefix(name + ","), prefix + "-all-" + str(LANDMARKS.index(name)))
        assert "vrijgespeeld" in node.get("content-desc", ""), "Saved achievement not rendered: " + name
    # A native node may be only partly visible. Move to the actual footer so the
    # final illustration and its nameplate are both present in the evidence.
    find_control(description_prefix("Lumi-standbeeld,"), prefix + "-last-plot")
    bottom_nodes = screenshot(prefix + "-all-unlocked-fixture-last-plot",
                              expected_descriptions=("Lumi-standbeeld, vrijgespeeld.",))
    monument = next(node for node in bottom_nodes if description_prefix("Lumi-standbeeld,")(node))
    assert rect(monument)[3] - rect(monument)[1] >= min(plot_height, 48)-2, "Final city plot/nameplate is clipped"
    find_control(exact("Open kledingkast"), prefix + "-city-wardrobe")
    screenshot(prefix + "-all-unlocked-fixture-wardrobe", expected_descriptions=("Open kledingkast",))
    find_control(description_prefix("Dagelijkse uitdagingen"), prefix + "-city-daily")
    screenshot(prefix + "-all-unlocked-fixture-daily", expected_descriptions=("Dagelijkse uitdagingen",))
    pass_check(viewport + ": all seven saved landmark unlocks have distinct accessible map controls")

    start_home()
    open_city(prefix + "-outfit-fixture")
    click_named("Open kledingkast", prefix + "-outfit-wardrobe")
    page(10)
    click(find_control(description_prefix("Kleding"), prefix + "-clothes-tab"))
    for item, name in ((6, "Astronautenpak"), (5, "Regenboogtrui"), (4, "Sterrenpak")):
        click(find_control(lambda node: name in node.get("content-desc", ""),
                           prefix + "-outfit-" + str(item)))
        state = wait_prefs(lambda p: p.get("wardrobe_worn_4") == item, name + " equipped")
        assert state["wardrobe_stars"] == 18, "Achievement outfit deducted stars"
        assert state["wardrobe_owned_4"] & (1 << item), "Equipped achievement outfit is not owned"
        scroll_top()
        ui(prefix + "-outfit-" + str(item) + "-equipped")
        screenshot(prefix + "-outfit-" + str(item) + "-equipped")
    pass_check(viewport + ": earned astronaut, rainbow and star outfits equip for free; preview screenshots captured")

    if viewport == "phone":
        shell("settings put system font_scale 1.5")
        start_home()
        open_city(prefix + "-large-font")
        find_control(description_prefix("Vriendjespark,"), prefix + "-large-font-park")
        screenshot(prefix + "-large-font-city")
        shell("settings put system font_scale 1.0")
        pass_check("phone: city remains reachable with 150% system text; screenshot captured for visual review")


def launch_native_home():
    """A fresh normal launcher task, without a debug screen or age override."""
    shell(f"am force-stop {APP}")
    shell("logcat -c")
    shell(f"am start -W -f 0x10008000 -a android.intent.action.MAIN "
          f"-c android.intent.category.LAUNCHER -n {APP}/.MainActivity")
    time.sleep(2)
    page(1)


def test_cosmetic_fixture(viewport):
    # Explicit owned-items fixture for visual review; these accessories are not
    # described as earned or purchased by this scenario.
    label = viewport + "-cosmetic-fixture"
    seed({"wardrobe_owned_4": 127})
    launch_native_home()
    open_city(label + "-city")
    click_named("Open kledingkast", label + "-wardrobe")
    page(10)
    click(find_control(description_prefix("Kleding"), label + "-clothes-tab"))
    for item, name in ((1, "Strikje"), (3, "Warme sjaal")):
        click(find_control(lambda node: name in node.get("content-desc", ""), label + "-equip-" + str(item)))
        state = wait_prefs(lambda p: p.get("wardrobe_worn_4") == item, "cosmetic fixture " + name + " equipped")
        assert state["wardrobe_stars"] == 18 and state["wardrobe_owned_4"] == 127, "Equipping a fixture accessory changed ownership/wallet"
        scroll_top()
        screenshot(label + "-outfit-" + str(item) + "-equipped")
    pass_check(viewport + ": explicitly seeded bow/scarf ownership is unchanged by real equip actions; both preview screenshots captured")


def native_foreground(expected_page):
    # Do not force-stop or clear the task: this resumes the actual Activity state.
    shell(f"am start -W -f 0x10200000 -a android.intent.action.MAIN "
          f"-c android.intent.category.LAUNCHER -n {APP}/.MainActivity")
    time.sleep(1)
    if expected_page is not None:
        page(expected_page)


def app_pid():
    return adb("shell", "pidof " + APP, check=False).decode().strip()


def background_app():
    # The disposable emulator's launcher is disabled by the acceptance setup.
    # A real external Activity still exercises onPause/onStop without a debug hook.
    shell("am start -W -a android.settings.SETTINGS")
    time.sleep(1)


def patch_stopped_preferences(values):
    assert not app_pid(), "Synthetic clock fixture may only be changed while the process is stopped"
    raw = adb("exec-out", "run-as", APP, "cat", "shared_prefs/MainActivity.xml")
    root = ET.fromstring(raw)
    for key, value in values.items():
        assert key in ("usage_day", "usage_seconds", "usage_limit", "usage_break_shown"), "Only explicit playtime fixture fields may be patched"
        tag = "boolean" if isinstance(value, bool) else "int"
        node = next((node for node in root if node.get("name") == key), None)
        if node is None:
            node = ET.SubElement(root, tag, name=key)
        assert node.tag == tag, "Unexpected preference type for " + key
        node.set("value", str(value).lower() if isinstance(value, bool) else str(value))
    raw = ET.tostring(root, encoding="utf-8", xml_declaration=True)
    shell(f"run-as {APP} sh -c 'cat > shared_prefs/MainActivity.xml; rm -f shared_prefs/MainActivity.xml.bak'", raw)


def recreate_from_background(expected_page, clock_fixture=None):
    old_pid = app_pid()
    assert old_pid, "App was not running before lifecycle recreation"
    background_app()
    shell("am kill " + APP)
    for _ in range(12):
        if not app_pid():
            break
        time.sleep(.3)
    assert not app_pid(), "Background process was not killed; recreation was not exercised"
    if clock_fixture is not None:
        patch_stopped_preferences(clock_fixture)
    native_foreground(expected_page)
    assert app_pid() and app_pid() != old_pid, "Expected a new Activity process"


def open_drawing(label):
    click(find_control(description_prefix("Kleurentuin"), label + "-world", False))
    page(44)
    click(find_control(description_prefix("Speel Mijn kunstwerk"), label + "-entry"))
    page(25)
    nodes = ui(label + "-canvas")
    canvas = next((node for node in nodes if visible(node)
                   and exact("Teken met je vinger op dit witte vel")(node)), None)
    assert canvas is not None, "Native drawing canvas is not visible"
    return canvas


def drawing_path(profile=0):
    assert profile in (0, 1, 2), "Unknown local artwork player"
    return "files/playlumi-kunstwerk" + ("" if profile == 0 else "-" + str(profile)) + ".png"


def drawing_file(label, profile=0):
    raw = adb("exec-out", "run-as", APP, "cat", drawing_path(profile))
    assert raw.startswith(b"\x89PNG\r\n\x1a\n") and len(raw) > 100, "Missing/corrupt saved drawing"
    (OUT / (label + ".png")).write_bytes(raw)
    return raw


def drawing_top():
    if PAGED_UI:
        scroll_top()
        return
    # Old debug baseline only.
    _, h = dimensions()
    for _ in range(4):
        shell(f"input swipe 3 {int(h*.30)} 3 {int(h*.82)} 350")
        time.sleep(.2)


def drawing_control(predicate, label, enabled=True):
    drawing_top()
    return find_control(predicate, label, enabled=enabled, scroll_x=3)


def drawing_click(value, label):
    click(drawing_control(exact(value), label))


def drawing_home(label):
    drawing_top()
    click_named("Terug naar het magische dorp", label, False)
    page(1)


def drawing_canvas(label):
    drawing_top()
    nodes = ui(label + "-canvas")
    canvas = next((node for node in nodes if visible(node)
                   and exact("Teken met je vinger op dit witte vel")(node)), None)
    assert canvas is not None, "Drawing paper did not return to the viewport"
    return canvas


def drawing_gestures(label, paths, profile=0):
    canvas = drawing_canvas(label)
    left, top, right, bottom = rect(canvas)
    raw = adb("exec-out", "run-as", APP, "cat", drawing_path(profile))
    assert raw.startswith(b"\x89PNG\r\n\x1a\n"), "Stroke fixture has no saved paper geometry"
    doc_w, doc_h = struct.unpack(">II", raw[16:24])
    density = int(re.findall(r"(\d+)", shell("wm density"))[-1]) / 160
    margin = 8*density
    scale = min((right-left-2*margin)/doc_w, (bottom-top-2*margin)/doc_h)
    assert scale > 0, "Drawing paper has no usable area"
    paper_w, paper_h = doc_w*scale, doc_h*scale
    paper_left, paper_top = (left+right-paper_w)/2, (top+bottom-paper_h)/2
    for start, end in paths:
        x1, y1 = round(paper_left+start[0]*paper_w), round(paper_top+start[1]*paper_h)
        x2, y2 = round(paper_left+end[0]*paper_w), round(paper_top+end[1]*paper_h)
        shell(f"input swipe {x1} {y1} {x2} {y2} {500 if len(paths) == 1 else 120}")


def drawing_stroke(label, start=(.2, .5), end=(.8, .5), profile=0):
    drawing_gestures(label, [(start, end)], profile)


def test_drawing_lifecycle(viewport):
    label = viewport + "-lifecycle-drawing"
    seed({})
    launch_native_home()
    open_drawing(label + "-blank")
    drawing_home(label + "-blank-home")
    blank = drawing_file(label + "-blank-file")

    open_drawing(label + "-stroke")
    drawing_stroke(label + "-stroke", (.2, .35), (.8, .65))
    screenshot(label + "-stroke")
    drawing_home(label + "-stroke-home")
    painted = drawing_file(label + "-saved-file")
    assert painted != blank, "The real touchstroke was not saved by the Home navigation"
    autosaved = prefs()
    assert autosaved["wardrobe_stars"] == 18 and not autosaved.get("drawing_saved_once", False), "Autosave incorrectly granted the explicit creative bonus"
    assert autosaved.get("collection_world_5", 0) == 0, "Autosave incorrectly counted a creative completion"

    open_drawing(label + "-reopen")
    assert drawing_file(label + "-reopened-file") == painted, "Reopening changed/lost the saved drawing"
    screenshot(label + "-reopened")
    # A new stroke belongs to this live View, so Undo must remain available after
    # explicit save and after the automatic save on a background/foreground cycle.
    drawing_stroke(label + "-second-stroke", (.25, .70), (.75, .20))
    drawing_click("💾 Bewaar tekening", label + "-explicit-save")
    saved = wait_prefs(lambda p: p.get("drawing_saved_once") is True
                      and p.get("wardrobe_stars") == 27 and p.get("collection_world_5") == 1,
                      "drawing marker, one +9 bonus and world progress saved together")
    assert saved["wardrobe_total_earned"] == 27, "Drawing earned total disagrees with the wallet"
    assert drawing_file(label + "-second-stroke-file") != painted, "Second real stroke was not saved"
    drawing_click("↩  Laatste lijn weg", label + "-undo-after-save")
    drawing_click("💾 Bewaar tekening", label + "-save-again")
    assert drawing_file(label + "-undo-after-save-file") == painted, "Explicit saving flattened the live undo history"
    assert prefs()["wardrobe_stars"] == 27 and prefs()["collection_world_5"] == 1, "Repeated save paid/counts the drawing twice"
    drawing_stroke(label + "-background-stroke", (.2, .75), (.8, .75))
    old_pid = app_pid()
    background_app()
    native_foreground(25)
    assert app_pid() == old_pid, "Expected a live-View pause/resume for the Undo check"
    assert drawing_file(label + "-background-file") != painted, "Background autosave missed the new stroke"
    drawing_click("↩  Laatste lijn weg", label + "-undo-after-background")
    drawing_click("💾 Bewaar tekening", label + "-save-after-background")
    assert drawing_file(label + "-undo-after-background-file") == painted, "Background saving flattened the live undo history"
    launch_native_home()
    open_drawing(label + "-restart")
    drawing_click("💾 Bewaar tekening", label + "-save-after-restart")
    restored = prefs()
    assert restored["wardrobe_stars"] == 27 and restored["collection_world_5"] == 1 and restored["drawing_saved_once"], "Restart repeated/lost the creative reward"
    drawing_top()
    screenshot(label + "-reward-restored")
    pass_check(viewport + ": drawing touchstroke survives Home/reopen, Undo survives live save/pause, autosave is free and explicit save grants +9/one collection step only once across restart")


def drawing_state(value, label, selected=None, enabled=None):
    node = drawing_control(lambda item: exact(value)(item) or description_prefix(value)(item),
                           label, enabled=None)
    assert node.get("class") == "android.widget.Button", "Drawing tool is not a native Button: " + value
    if selected is not None:
        assert node.get("selected") == str(selected).lower(), "Wrong selected tool state: " + value
        assert node.get("content-desc", "").endswith(", geselecteerd") == selected, "Wrong accessible selected marker: " + value
    if enabled is not None:
        assert node.get("enabled") == str(enabled).lower(), "Wrong enabled action state: " + value
    return node


def choose_drawing(value, label):
    click(drawing_control(description_prefix(value), label))
    drawing_state(value, label + "-selected", selected=True, enabled=True)


def save_drawing(label, profile=0):
    drawing_click("💾 Bewaar tekening", label + "-save")
    return drawing_file(label + "-file", profile)


def test_drawing_tools(viewport):
    label = viewport + "-drawing-tools"
    undo, redo = "↩  Laatste lijn weg", "↪  Laatste lijn terug"
    seed({})
    launch_native_home()
    open_drawing(label + "-blank")
    drawing_state("Tekenkleur Roze", label + "-default-color", selected=True)
    drawing_state("Potlood", label + "-default-pencil", selected=True)
    drawing_state("Gum", label + "-default-eraser", selected=False)
    drawing_state("Lijndikte Normaal", label + "-default-width", selected=True)
    drawing_state(undo, label + "-initial-undo", enabled=False)
    drawing_state(redo, label + "-initial-redo", enabled=False)
    drawing_home(label + "-blank-home")
    blank = drawing_file(label + "-blank-file")
    assert prefs()["wardrobe_stars"] == 18 and not prefs().get("drawing_saved_once", False), "Tool inspection/autosave awarded stars"

    open_drawing(label + "-thin")
    choose_drawing("Lijndikte Dun", label + "-thin-width")
    drawing_stroke(label + "-thin-stroke")
    thin = save_drawing(label + "-thin")
    assert thin != blank, "Thin pencil did not leave a real stroke"
    wait_prefs(lambda p: p.get("wardrobe_stars") == 27 and p.get("drawing_saved_once") is True
               and p.get("collection_world_5") == 1, "one creative reward for new tools scenario")
    drawing_home(label + "-thin-home")
    open_drawing(label + "-old-png")
    assert drawing_file(label + "-reopened-thin-file") == thin, "Loading the saved pencil image changed its bytes"
    drawing_state(undo, label + "-reopened-undo", enabled=False)
    drawing_state(redo, label + "-reopened-redo", enabled=False)

    # Erase the entire thin stroke in the flattened, previously saved PNG.
    # The larger eraser follows the exact same native gesture, including caps.
    choose_drawing("Gum", label + "-eraser")
    choose_drawing("Lijndikte Dik", label + "-thick-eraser")
    drawing_state("Potlood", label + "-pencil-off", selected=False)
    drawing_state(redo, label + "-before-erase-redo", enabled=False)
    for index, tool in enumerate(("Gum, geselecteerd", "Lijndikte Dik, geselecteerd")):
        find_control(description_prefix(tool), label + "-selected-tool-" + str(index))
        tool_nodes = screenshot(label + "-selected-tool-" + str(index), expected_descriptions=(tool,))
        drawing_controls_pixels(label + "-selected-tool-" + str(index), tool_nodes, (tool,))
    drawing_stroke(label + "-erase-old-png")
    erased = save_drawing(label + "-erased")
    assert erased == blank, "Eraser did not restore white paper underneath the loaded PNG stroke"
    drawing_top()
    screenshot(label + "-erased-old-artwork")
    drawing_click(undo, label + "-undo-erase")
    assert save_drawing(label + "-undo-erase") == thin, "Undo did not restore the loaded image exactly"
    drawing_state(redo, label + "-redo-available", enabled=True)

    # Selecting a color chooses the pencil but must not discard a redo action.
    choose_drawing("Tekenkleur Blauw", label + "-blue")
    drawing_state("Potlood", label + "-color-selects-pencil", selected=True)
    drawing_state("Gum", label + "-color-clears-eraser", selected=False)
    drawing_state(redo, label + "-color-keeps-redo", enabled=True)
    drawing_click(redo, label + "-redo-erase")
    assert save_drawing(label + "-redo-erase") == blank, "Redo changed an eraser action into the newly selected pencil"
    drawing_click(undo, label + "-restore-before-new-line")
    assert save_drawing(label + "-restored-old-line") == thin, "Undo after redo did not restore the original PNG"

    drawing_stroke(label + "-blue-stroke", (.3, .65), (.7, .65))
    drawing_state(redo, label + "-new-action-clears-redo", enabled=False)
    combined = save_drawing(label + "-combined")
    assert combined not in (blank, thin), "Thick blue pencil did not add a new stroke"
    drawing_click(undo, label + "-undo-blue")
    assert save_drawing(label + "-undo-blue") == thin, "Undo removed more than the last real gesture"
    drawing_click(redo, label + "-redo-blue")
    assert save_drawing(label + "-redo-blue") == combined, "Redo did not restore the same blue gesture"

    drawing_click("🧽 Begin met leeg papier", label + "-arm-clear")
    armed = drawing_state("🧽 Begin met leeg papier", label + "-clear-confirmation", enabled=True)
    assert "Tik nog eens" in armed.get("text", ""), "Clear did not require its second native tap"
    drawing_click("🧽 Begin met leeg papier", label + "-confirm-clear")
    assert save_drawing(label + "-cleared") == blank, "Confirmed clear did not produce a blank saved page"
    drawing_click(undo, label + "-undo-clear")
    assert save_drawing(label + "-undo-clear") == combined, "Undo did not recover the full drawing after clear"
    drawing_state(redo, label + "-redo-clear-before-pause", enabled=True)
    old_pid = app_pid()
    background_app()
    native_foreground(25)
    assert app_pid() == old_pid, "Expected live drawing history for pause/redo check"
    drawing_state(redo, label + "-redo-clear-after-pause", enabled=True)
    drawing_click(redo, label + "-redo-clear-after-background")
    assert save_drawing(label + "-redo-clear-after-background") == blank, "Save/pause lost the pending clear action"
    drawing_click(undo, label + "-restore-after-background")
    assert save_drawing(label + "-restored-after-background") == combined, "Undo after background/redo lost existing artwork"
    drawing_top()
    screenshot(label + "-restored-artwork")
    current = prefs()
    assert current["wardrobe_stars"] == 27 and current["wardrobe_total_earned"] == 27 \
        and current["collection_world_5"] == 1 and current["drawing_saved_once"], "Drawing tool/history actions repeated the creative reward"
    pass_check(viewport + ": native pencil/eraser/width selections work; erasing loaded PNG, exact Undo/Redo, new-action branching, confirmed clear and live pause/redo preserve artwork and one creative reward")

    if viewport == "phone":
        shell("settings put system font_scale 1.3")
        time.sleep(1)
        page(25)
        drawing_top()
        screenshot(label + "-large-font-top", expected_texts=("Tekenstudio",))
        drawing_state("Lijndikte Dik", label + "-large-font-width", selected=True)
        drawing_state("🧽 Begin met leeg papier", label + "-large-font-clear", enabled=True)
        for index, tool in enumerate(("💾 Bewaar tekening", "🧽 Begin met leeg papier")):
            find_control(description_prefix(tool), label + "-large-font-tool-" + str(index))
            large_nodes = screenshot(label + "-large-font-tool-" + str(index), expected_descriptions=(tool,))
            drawing_controls_pixels(label + "-large-font-tool-" + str(index), large_nodes, (tool,))
        shell("settings put system font_scale 1.0")
        time.sleep(1)
        page(25)
        assert drawing_file(label + "-font-restored-file") == combined, "Font/viewport change rewrote unchanged artwork"
        pass_check("phone: drawing tools remain reachable with 130% text and unchanged saved artwork; top and controls captured")



def png_raster(raw):
    """Decode Android's 8-bit non-interlaced RGB/RGBA output without a CI dependency."""
    assert raw.startswith(b"\x89PNG\r\n\x1a\n"), "Raster evidence is not a PNG"
    offset, parts, header = 8, [], None
    while offset + 12 <= len(raw):
        length = struct.unpack(">I", raw[offset:offset+4])[0]
        kind = raw[offset+4:offset+8]
        data = raw[offset+8:offset+8+length]
        assert len(data) == length, "Truncated PNG chunk"
        if kind == b"IHDR":
            header = struct.unpack(">IIBBBBB", data)
        elif kind == b"IDAT":
            parts.append(data)
        elif kind == b"IEND":
            break
        offset += length + 12
    assert header is not None, "PNG has no image header"
    width, height, depth, color, compression, filtering, interlace = header
    assert 0 < width <= 4096 and 0 < height <= 4096, "Unexpected drawing dimensions"
    assert depth == 8 and color in (2, 6) and (compression, filtering, interlace) == (0, 0, 0), "Unexpected Android PNG format"
    channels = 3 if color == 2 else 4
    stride = width*channels
    packed = zlib.decompress(b"".join(parts))
    assert len(packed) == (stride+1)*height, "Unexpected PNG raster length"
    pixels = bytearray(stride*height)
    previous = bytearray(stride)
    for y in range(height):
        pos = y*(stride+1)
        method = packed[pos]
        row = bytearray(packed[pos+1:pos+1+stride])
        assert method <= 4, "Unknown PNG row filter"
        for x in range(stride):
            left = row[x-channels] if x >= channels else 0
            up = previous[x]
            corner = previous[x-channels] if x >= channels else 0
            if method == 1:
                prediction = left
            elif method == 2:
                prediction = up
            elif method == 3:
                prediction = (left+up)//2
            elif method == 4:
                estimate = left+up-corner
                dl, du, dc = abs(estimate-left), abs(estimate-up), abs(estimate-corner)
                prediction = left if dl <= du and dl <= dc else up if du <= dc else corner
            else:
                prediction = 0
            row[x] = (row[x]+prediction) & 255
        pixels[y*stride:(y+1)*stride] = row
        previous = row

    def sample(x, y):
        index = round(y*(height-1))*stride + round(x*(width-1))*channels
        rgba = tuple(pixels[index:index+channels])
        return rgba if channels == 4 else rgba+(255,)

    return width, height, sample


def drawing_controls_pixels(label, nodes, descriptions):
    """Require painted native buttons, not only their accessibility nodes.

    The former unbounded Canvas drawColor covered real, enabled buttons with
    uniform FRAME_COLOR. Inspect their inset interiors to exclude rounded
    corners/shadows, while requiring both their lighter fill and dark labels.
    This detects that occlusion; it is not an OCR or general contrast audit.
    """
    raw = (OUT / (label + ".png")).read_bytes()
    width, height, pixel = png_raster(raw)
    frame = (222, 217, 233)
    results, failures = [], []
    for description in descriptions:
        matches = [node for node in nodes if description_prefix(description)(node)
                   and node.get("class") == "android.widget.Button" and visible(node)
                   and node.get("enabled") == "true"]
        assert len(matches) == 1, "Raster check needs one visible enabled button: " + description
        left, top, right, bottom = rect(matches[0])
        assert 0 <= left < right <= width and 0 <= top < bottom <= height, "Button lies outside the screenshot: " + description
        assert right-left >= 50 and bottom-top >= 40, "Button region is too clipped for pixel evidence: " + description
        dx, dy = max(4, (right-left)//12), max(4, (bottom-top)//10)
        box = (left+dx, top+dy, right-dx, bottom-dy)
        colors = [pixel(x/(width-1), y/(height-1))[:3]
                  for y in range(box[1], box[3]) for x in range(box[0], box[2])]
        non_frame = sum(max(abs(value-ref) for value, ref in zip(color, frame)) > 8
                        for color in colors) / len(colors)
        dark_text = sum(max(color) < 160 for color in colors) / len(colors)
        unique = len(set(colors))
        results.append({"description": matches[0].get("content-desc"),
                        "bounds": [left, top, right, bottom], "sampleBounds": box,
                        "samples": len(colors), "uniqueColors": unique,
                        "nonFrameFraction": round(non_frame, 6),
                        "darkTextFraction": round(dark_text, 6)})
        if non_frame < .8 or dark_text < .005 or unique < 8:
            failures.append(description + ": " + repr(results[-1]))
    (OUT / (label + "-control-pixels.json")).write_text(json.dumps({
        "screenshotSha256": hashlib.sha256(raw).hexdigest(), "frameColor": frame,
        "minimumNonFrameFraction": .8, "minimumDarkTextFraction": .005,
        "minimumUniqueColors": 8, "buttons": results,
        "scope": "Actual screenshot pixels inside native enabled button bounds; no image fixture."
    }, indent=2, ensure_ascii=False))
    assert not failures, "Drawing controls are occluded or have no visible label: " + "; ".join(failures)


def test_drawing_compaction(viewport):
    label = viewport + "-drawing-compaction"
    white = (255, 255, 255, 255)
    seed({})
    launch_native_home()
    open_drawing(label + "-blank")
    drawing_home(label + "-blank-home")
    blank = drawing_file(label + "-blank-file")
    open_drawing(label + "-paint")
    drawing_stroke(label + "-old-line")
    original = save_drawing(label + "-original")
    width, height, original_pixel = png_raster(original)
    assert original_pixel(.3, .5) != white and original_pixel(.7, .5) != white, "Original native stroke missed the raster samples"
    drawing_home(label + "-load-home")
    open_drawing(label + "-loaded-png")
    assert drawing_file(label + "-loaded-file") == original, "Compaction fixture did not load unchanged real artwork"
    choose_drawing("Gum", label + "-eraser")
    choose_drawing("Lijndikte Dik", label + "-eraser-width")
    drawing_stroke(label + "-erase-section", (.5, .44), (.5, .56))
    erased = save_drawing(label + "-erased")
    _, _, erased_pixel = png_raster(erased)
    assert erased_pixel(.5, .5) == white, "Loaded-image eraser produced a dark or opaque patch"
    assert erased_pixel(.3, .5) == original_pixel(.3, .5) \
        and erased_pixel(.7, .5) == original_pixel(.7, .5), "Erasing the middle damaged untouched old artwork"

    choose_drawing("Tekenkleur Blauw", label + "-pencil")
    choose_drawing("Lijndikte Dun", label + "-pencil-width")
    def grid(first_y):
        return [((.1+(i%8)*.1, first_y+(i//8)*.055),
                 (.125+(i%8)*.1, first_y+(i//8)*.055)) for i in range(32)]

    upper = grid(.14)
    drawing_gestures(label + "-32-separate-gestures", upper)
    compacted = save_drawing(label + "-eraser-compacted")
    w, h, pixel = png_raster(compacted)
    assert (w, h) == (width, height), "History compaction changed document dimensions/density"
    assert pixel(.5, .5) == white, "Eraser became dark or restored old pixels after the 32-action limit"
    assert pixel(.3, .5) == original_pixel(.3, .5) and pixel(.7, .5) == original_pixel(.7, .5), "Compaction shifted or lost untouched old PNG content"
    for start, end in upper:
        assert pixel((start[0]+end[0])/2, start[1]) != white, "A separate native gesture was lost before compaction"
    drawing_top()
    screenshot(label + "-eraser-retained")

    drawing_click("🧽 Begin met leeg papier", label + "-arm-clear")
    drawing_click("🧽 Begin met leeg papier", label + "-confirm-clear")
    assert save_drawing(label + "-clear") == blank, "Clear did not remove compacted artwork"
    drawing_click("↩  Laatste lijn weg", label + "-undo-clear")
    assert save_drawing(label + "-undo-clear") == compacted, "Undo clear did not recover the exact pre-limit artwork"
    drawing_click("↪  Laatste lijn terug", label + "-redo-clear")
    assert save_drawing(label + "-redo-clear") == blank, "Redo clear did not restore a blank document"
    lower = grid(.66)
    drawing_gestures(label + "-32-after-clear", lower)
    cleared_compacted = save_drawing(label + "-clear-compacted")
    w, h, cleared_pixel = png_raster(cleared_compacted)
    assert (w, h) == (width, height), "Clear compaction changed document dimensions/density"
    assert all(cleared_pixel(x, .5) == white for x in (.3, .5, .7)), "Original loaded line returned or turned dark after clear compaction"
    for start, end in upper:
        assert cleared_pixel((start[0]+end[0])/2, start[1]) == white, "Cleared older pencil strokes returned after the history limit"
    for start, end in lower:
        assert cleared_pixel((start[0]+end[0])/2, start[1]) != white, "New drawing after clear was lost by compaction"
    drawing_top()
    screenshot(label + "-clear-retained")
    state = prefs()
    assert state["wardrobe_stars"] == 27 and state["wardrobe_total_earned"] == 27 \
        and state["collection_world_5"] == 1, "History-limit operations repeated the creative reward"
    (OUT / (label + "-pixels.json")).write_text(json.dumps({
        "document": [width, height], "nativeGesturesAfterEraser": len(upper),
        "nativeGesturesAfterClear": len(lower), "erasedCenterAfterCompaction": pixel(.5, .5),
        "untouchedOldLineBefore": original_pixel(.3, .5),
        "untouchedOldLineAfterEraserCompaction": pixel(.3, .5),
        "oldLineAfterClearCompaction": cleared_pixel(.3, .5),
        "scope": "Actual native strokes and Android PNG pixels; no generated artwork fixture."
    }, indent=2))
    pass_check(viewport + ": Android raster preserves white erasure and untouched loaded PNG through 32 later gestures; Clear/Undo/Redo and 32 more gestures retain exact pixels and one creative reward")


def unlock_parent_gate(label):
    nodes = ui(label + "-gate")
    sums = [re.search(r"(\d+)\s*\+\s*(\d+)\s*=\s*\?", value)
            for node in nodes for value in labels(node)]
    question = next((value for value in sums if value is not None), None)
    assert question is not None, "Parent gate has no visible native arithmetic challenge"
    correct = int(question.group(1)) + int(question.group(2))
    click_named("🔢 " + str(correct), label + "-answer", False)
    page(27)


def open_parent_settings(label):
    click(find_control(description_prefix("⚙️ Instellingen"), label, True))
    page(30)


def test_parent_lifecycle(viewport):
    label = viewport + "-lifecycle-parent"
    seed({})
    launch_native_home()
    click(find_control(lambda node: exact("Ouderzone")(node)
                       or description_prefix("Ouderzone")(node), label + "-entry", False))
    page(26)
    unlock_parent_gate(label + "-initial")
    open_parent_settings(label + "-settings")
    shell("input keyevent KEYCODE_BACK")
    page(27)
    open_parent_settings(label + "-settings-again")

    background_app()
    native_foreground(None)
    require_parent_gate(label + "-background")
    screenshot(label + "-background-gate", expected_texts=("🔐 Alleen voor ouders",))
    unlock_parent_gate(label + "-after-background")
    open_parent_settings(label + "-before-recreate")
    recreate_from_background(None)
    require_parent_gate(label + "-recreated")
    screenshot(label + "-recreated-gate", expected_texts=("🔐 Alleen voor ouders",))
    unlock_parent_gate(label + "-after-recreate")
    assert prefs()["wardrobe_stars"] == 18, "Parent lifecycle navigation changed the wallet"
    pass_check(viewport + ": real parent gate stays open for internal navigation, relocks after background and after process recreation")


def require_parent_gate(label):
    # A protected task may reopen at its gate or safely return to HOME on cold
    # launch. In both cases entering the parent area must require the challenge.
    for _ in range(12):
        pages = re.findall(r"screen=(\d+)", shell("logcat -d -s PlaylumiQA:I"))
        current = int(pages[-1]) if pages else -1
        if current in (1, 26):
            break
        time.sleep(.5)
    assert current in (1, 26), "Parent area remained unlocked after leaving/recreating the app"
    if current == 1:
        click(find_control(lambda node: exact("Ouderzone")(node)
                           or description_prefix("Ouderzone")(node), label + "-entry", False))
    page(26)


def same_quiz(before, after, label):
    for field in ("id", "age", "level", "count", "total", "attempts", "solved", "hadMistake",
                  "rejected", "feedback", "prompt", "display", "color", "correct", "fingers", "answers", "colors"):
        assert after[field] == before[field], label + " changed saved question field: " + field


def real_history_fingerprint():
    prefixes = ("wardrobe_", "score_", "library_resume_", "library_rounds_", "library_completed_",
                "daily_", "collection_world_", "learn_", "play_day_")
    return {key: value for key, value in prefs().items()
            if key.startswith(prefixes) or key in ("completed_all", "library_played")}


def parent_world_summary(world, rounds, completed, unfinished, label):
    counted = (str(rounds) + (" getelde ronde" if rounds == 1 else " getelde rondes")
               + " · " + str(completed) + (" oefening" if completed == 1 else " oefeningen")
               + " met een afgeronde ronde")
    saved = str(unfinished) + (" begonnen ronde om verder te spelen" if unfinished == 1
                               else " begonnen rondes om verder te spelen")
    scroll_top()
    w, h = dimensions()
    for _ in range(60):
        nodes = ui(label)
        title = next((node for node in nodes if node.get("text") == world and visible(node)), None)
        if title is not None:
            parent = next((node for node in nodes if title in list(node)), None)
            values = {" ".join(value.split()) for child in parent.iter("node") if visible(child)
                      for value in labels(child)} if parent is not None else set()
            if counted in values and saved in values:
                screenshot(label)
                return
        if PAGED_UI:
            if not turn_page(nodes, True):
                break
        else:
            shell(f"input swipe {w//2} {int(h*.78)} {w//2} {int(h*.34)} 400")
    raise AssertionError("Parent world summary does not match actually played history: " + world)


def test_hands_on_worlds(viewport):
    label = viewport + "-hands-on"
    for world, title, route in (("Kleurentuin", "Kleurmozaïek", "5:6:3:0"),
                                ("Speelplein", "Dierenwereld: thuis", "4:6:2:0")):
        seed({})
        launch_native_home()
        click(find_control(description_prefix(world), label + "-world-" + world, False))
        page(44)
        click(find_control(description_prefix("Speel " + title), label + "-entry-" + world))
        page(45)
        nodes = ui(label + "-initial-" + world)
        if world == "Kleurentuin":
            options = [n.get("content-desc", "") for n in nodes
                       if n.get("content-desc", "").startswith("Voorbeeld vak ")]
            assert len(options) == 4, "Young mosaic has no real 2x2 example"
            expected = next(value.split(": ", 1)[1] for value in options if value.startswith("Voorbeeld vak 1: "))
        else:
            animal = next(n.get("content-desc", "").split(": ", 1)[1] for n in nodes
                          if n.get("content-desc", "").startswith("Dier 1 van 3: "))
            expected = {"🐠":"🌊 Zee","🐬":"🌊 Zee","🐙":"🌊 Zee",
                        "🐄":"🌾 Boerderij","🐑":"🌾 Boerderij","🐔":"🌾 Boerderij",
                        "🐿️":"🌳 Bos","🦉":"🌳 Bos","🦌":"🌳 Bos"}[animal]
        click_named("Kies " + expected, label + "-first-" + world)
        key = "library_resume_v2_0_" + route
        state = wait_prefs(lambda p: key in p and json.loads(p[key])["count"] == 1, "First interactive step")
        saved = json.loads(state[key])
        assert saved["engine"] == 3 and saved["total"] == (4 if world == "Kleurentuin" else 3), "New board/version"
        assert state["wardrobe_stars"] == 18, "Partial board awarded a reward"
        launch_native_home()
        click(find_control(description_prefix(world), label + "-restart-" + world, False))
        page(44)
        click(find_control(description_prefix("Verder spelen " + title), label + "-resume-" + world))
        page(45)
        restored = json.loads(prefs()[key])
        for field in ("id", "age", "seed", "engine", "count", "total"):
            assert restored[field] == saved[field], "New board did not restore: " + field
        screenshot(label + "-resumed-" + world)
        pass_check(viewport + ": " + title + " uses its new native board and restores the exact first step without duplicate rewards")


def test_memory_refresh(viewport):
    label = viewport + "-memory-refresh"
    seed({})
    launch_native_home()
    click(find_control(description_prefix("Speelplein"), label + "-world", False))
    page(44)
    click(find_control(description_prefix("Speel Dierenmemory"), label + "-entry"))
    page(13)
    nodes = screenshot(label + "-closed")
    backs = [n for n in nodes if n.get("content-desc", "").startswith("Gesloten kaart ")]
    assert len(backs) == 6 and all(n.get("text") == "✦" for n in backs), "Memory backs expose different faces"
    click_named("Gesloten kaart 1", label + "-first")
    key = "library_resume_v2_0_4:1:2:0"
    state = wait_prefs(lambda p: key in p and json.loads(p[key])["open"] == 0, "One open card saved")
    saved = json.loads(state[key])
    assert state["wardrobe_stars"] == 18 and saved["matched"] == 0, "Single card awarded progress"
    mate = next(i for i, card in enumerate(saved["choices"]) if i != 0 and card == saved["choices"][0])
    launch_native_home()
    click(find_control(description_prefix("Speelplein"), label + "-world-return", False))
    page(44)
    click(find_control(description_prefix("Verder spelen Dierenmemory"), label + "-resume"))
    page(13)
    nodes = screenshot(label + "-resumed")
    assert any(n.get("content-desc", "").startswith("Kaart 1: ") for n in nodes), "Open card missing after restart"
    click_named("Gesloten kaart " + str(mate+1), label + "-match")
    result = wait_prefs(lambda p: key in p and json.loads(p[key])["matched"] == ((1 << mate) | 1), "First pair saved")
    assert result["wardrobe_stars"] == 18, "Incomplete memory board awarded stars"
    screenshot(label + "-pair")
    pass_check(viewport + ": themed memory has identical hidden cards and exactly restores an open card and matched pair")


def test_puzzle_board(viewport):
    label = viewport + "-number-hunt"
    seed({})
    launch_native_home()
    click(find_control(description_prefix("Rekenraket"), label + "-world", False))
    page(44)
    click(find_control(description_prefix("Speel Getallenjacht"), label + "-entry"))
    page(45)
    nodes = screenshot(label + "-initial")
    start = next(n for n in nodes if re.fullmatch(r"Vak \d+: 1", n.get("content-desc", "")))
    click(start)
    key = "library_resume_v2_0_0:6:4:0"
    state = wait_prefs(lambda p: key in p and json.loads(p[key])["count"] == 1, "First puzzle move")
    saved = json.loads(state[key])
    assert saved["engine"] == 4 and saved["mask"] > 0 and saved["total"] == 5
    assert state["wardrobe_stars"] == 18, "Partial puzzle credited a reward"
    launch_native_home()
    click(find_control(description_prefix("Rekenraket"), label + "-restart", False))
    page(44)
    click(find_control(description_prefix("Verder spelen Getallenjacht"), label + "-resume"))
    page(45)
    restored = json.loads(prefs()[key])
    for field in ("seed", "engine", "count", "mask", "total"):
        assert restored[field] == saved[field], "Puzzle restore changed " + field
    screenshot(label + "-restored")
    for number in range(2, 6):
        click(find_control(lambda n: re.fullmatch(r"Vak \d+: " + str(number), n.get("content-desc", "")) is not None, label + "-tile-" + str(number), False))
    wait_prefs(lambda p: key not in p and p["wardrobe_stars"] >= 21, "Completed puzzle")
    assert prefs().get("expedition_v1_progress_0", 0) == 0, "Free play incorrectly advanced expedition"
    screenshot(label + "-finished")
    pass_check(viewport + ": genuine number grid plays and restores its exact selected cells")


def open_expedition(label):
    # Use the normal library on both the illustrated phone and native Fold home.
    click(find_control(description_prefix("Rekenraket"), label + "-world", False))
    page(44)
    click(find_control(description_prefix("Werelden"), label + "-worlds-tab", False))
    page(44)
    click_named("Open Lumi’s expeditie", label + "-open")
    page(51)


def test_expedition(viewport):
    label = viewport + "-expedition"
    seed({})
    launch_native_home()
    open_expedition(label + "-initial")
    nodes = screenshot(label + "-passport", expected_texts=("0 van 30 opdrachten · 0 van 6 zegels",))
    assert page_indicator(nodes).startswith("Inhoudspagina 1 van "), "Expedition does not open on its first page"
    mission = next((n for n in nodes if exact("Expeditie opdracht 1 spelen")(n) and visible(n)), None)
    assert mission is not None, "Next expedition mission requires paging"
    map_button = next((n for n in nodes if exact("Bekijk avonturenkaart")(n) and visible(n)), None)
    assert map_button is not None, "Expedition map shortcut is missing"
    click(map_button)
    map_nodes = screenshot(label + "-map-jump")
    assert any("Expeditiekaart:" in n.get("content-desc", "") and visible(n) for n in map_nodes), "Map shortcut did not open the map sheet"
    scroll_top()
    click_named("Expeditie opdracht 1 spelen", label + "-first")
    page(45)
    click(find_control(lambda n: re.fullmatch(r"Vak \d+: 1", n.get("content-desc", "")) is not None, label + "-one", False))
    key = "library_resume_v2_0_0:6:4:0"
    progress_key = "expedition_v1_progress_0"
    saved = wait_prefs(lambda p: key in p and json.loads(p[key])["count"] == 1, "Saved first move")
    first = json.loads(saved[key])
    assert saved.get(progress_key, 0) == 0 and saved["wardrobe_stars"] == 18, "Partial mission credited"
    launch_native_home()
    open_expedition(label + "-reopen")
    click_named("Expeditie opdracht 1 spelen", label + "-resume")
    page(45)
    resumed = json.loads(prefs()[key])
    for field in ("seed", "engine", "count", "mask", "total"):
        assert resumed[field] == first[field], "Expedition resume changed " + field
    for number in range(2, 6):
        click(find_control(lambda n: re.fullmatch(r"Vak \d+: " + str(number), n.get("content-desc", "")) is not None, label + "-tile-" + str(number), False))
    completed = wait_prefs(lambda p: p.get(progress_key) == 1 and key not in p, "First expedition mission completed")
    # First correct board earns +3 and the first category badge +12. No chapter reward yet.
    assert completed["wardrobe_stars"] == 33, "Unexpected expedition reward before seal"
    click_named("Terug naar de expeditie", label + "-return", False)
    page(51)
    screenshot(label + "-next", expected_texts=("1 van 30 opdrachten · 0 van 6 zegels",))
    # Seed only the four earlier expedition stages to exercise the real chapter finale.
    # Do not seed or describe a solved game or awarded chapter bonus.
    seed({progress_key: 4})
    launch_native_home()
    open_expedition(label + "-chapter-finale")
    click_named("Expeditie opdracht 5 spelen", label + "-finale")
    page(45)
    for number in range(1, 4):
        click_named("Kies " + str(number), label + "-rocket-" + str(number))
    seal = wait_prefs(lambda p: p.get(progress_key) == 5, "Chapter seal awarded")
    assert seal["wardrobe_stars"] == 48, "Chapter bonus was not exactly 15 plus first board/badge reward of 15"
    assert seal["wardrobe_total_earned"] == 48, "Chapter reward missing from lifetime balance"
    click_named("Terug naar de expeditie", label + "-seal-return", False)
    page(51)
    screenshot(label + "-seal", expected_texts=("5 van 30 opdrachten · 1 van 6 zegels",))
    launch_native_home()
    open_expedition(label + "-seal-restored")
    assert prefs()[progress_key] == 5 and prefs()["wardrobe_stars"] == 48, "Reopening claimed a seal twice"
    # Separate visual fixture of an already finished expedition. This does not
    # claim to play all 30 missions; the full reward sequence is covered in Java.
    seed({progress_key: 30, "wardrobe_stars": 138, "wardrobe_total_earned": 138})
    launch_native_home()
    open_expedition(label + "-completed-fixture")
    screenshot(label + "-complete-passport", expected_texts=("30 van 30 opdrachten · 6 van 6 zegels",))
    nodes = ui(label + "-completed-controls")
    assert not any(n.get("content-desc", "").startswith("Expeditie opdracht ") for n in nodes), "Finished expedition has another playable stage"
    assert prefs()["wardrobe_stars"] == 138, "Viewing diploma recredited reward"
    pass_check(viewport + ": expedition saves a real unfinished puzzle, advances exactly one mission, awards a chapter seal atomically, and restores without duplicate bonus")


def open_builder(label):
    click(find_control(description_prefix("Rekenraket"), label + "-world", False))
    page(44)
    click(find_control(description_prefix("Werelden"), label + "-worlds-tab", False))
    page(44)
    click_named("Open Lumi’s bouwatelier", label + "-open")
    page(52)


def test_builder(viewport):
    label = viewport + "-builder"
    seed({})
    launch_native_home()
    open_builder(label + "-initial")
    nodes = screenshot(label + "-start-choices")
    assert any(exact("Bouwopdracht 1 openen")(n) and visible(n) for n in nodes), "Builder's next plan is not on its first sheet"
    assert any(exact("Bekijk eigen ontwerpen")(n) and visible(n) for n in nodes), "Free design shortcut requires paging"
    click_named("Bouwopdracht 1 openen", label + "-plan")
    page(53)
    nodes = screenshot(label + "-tools-first")
    assert any(description_prefix("Kies Huis")(n) and visible(n) for n in nodes), "Builder does not open with useful tools"
    assert any(exact("Bouwvak 1: Leeg gras")(n) and visible(n) for n in nodes), "Builder grid is not pinned"
    click_named("Bouwvak 1: Leeg gras", label + "-house")
    key = "builder_v1_board_0_0"
    progress = "builder_v1_progress_0"
    state = wait_prefs(lambda p: key in p and p[key].startswith("H........|"), "First house saved")
    assert state.get(progress, 0) == 0 and state["wardrobe_stars"] == 18
    launch_native_home()
    open_builder(label + "-restored")
    click_named("Bouwopdracht 1 openen", label + "-resume")
    page(53)
    assert prefs()[key].startswith("H........|")
    click_named("Laatste bouwbewerking ongedaan maken", label + "-undo")
    wait_prefs(lambda p: p[key].startswith(".........|"), "Persisted undo")
    click_named("Bouwbewerking opnieuw uitvoeren", label + "-redo")
    wait_prefs(lambda p: p[key].startswith("H........|"), "Persisted redo")
    scroll_top()
    click_named("Kies Weg horizontaal", label + "-road")
    click_named("Bouwvak 2: Leeg gras", label + "-place-road")
    scroll_top()
    click_named("Kies Station", label + "-station")
    click_named("Bouwvak 3: Leeg gras", label + "-place-station")
    click_named("Controleer bouwtekening", label + "-check")
    result = wait_prefs(lambda p: p.get(progress) == 1, "First building completed")
    assert result["wardrobe_stars"] == 24 and result[key].startswith("HaS......|"), "Incorrect plan reward or board"
    click_named("Controleer bouwtekening", label + "-repeat", False)
    assert prefs()["wardrobe_stars"] == 24, "Plan bonus duplicated"
    screenshot(label + "-completed")
    shell("input keyevent KEYCODE_BACK")
    page(52)
    click_named("Open eigen ontwerp 1", label + "-free")
    page(53)
    click_named("Bouwvak 1: Leeg gras", label + "-free-house")
    free_key = "builder_v1_board_0_12"
    wait_prefs(lambda p: free_key in p and p[free_key].startswith("H"), "Independent creative plot")
    assert prefs()[key].startswith("HaS......|") and prefs()[progress] == 1 and prefs()["wardrobe_stars"] == 24
    screenshot(label + "-free")
    pass_check(viewport + ": real construction plan, persistent undo/redo after relaunch, once-only reward and separate creative plot")


def test_fixed_pages(viewport):
    label = viewport + "-fixed-pages"
    seed({})
    shell(f"am force-stop {APP}")
    shell("logcat -c")
    shell(f"am start -W -n {APP}/.MainActivity --ei playlumi_ci_library 0 --ei playlumi_ci_age 2")
    time.sleep(2)
    page(44)
    scroll_top()
    nodes = screenshot(label + "-first")
    first = page_indicator(nodes)
    assert first.startswith("Inhoudspagina 1 van ") and not first.endswith(" van 1"), "Long world was not paginated"
    assert not any(n.get("scrollable") == "true" or n.get("class", "").endswith("ScrollView") for n in nodes), "Vertical scroll container still present"
    def cards(values):
        return {n.get("content-desc") for n in values if visible(n) and
                n.get("clickable") == "true" and n.get("content-desc", "").startswith("Speel ")}
    initial = cards(nodes)
    assert initial, "First page contains no games"
    w,h = dimensions()
    # Gesture within the content, away from persistent bars.
    shell(f"input swipe {w//2} {int(h*.72)} {w//2} {int(h*.30)} 350")
    time.sleep(.5)
    same = ui(label + "-vertical-up")
    assert page_indicator(same) == first and cards(same) == initial, "Vertical swipe changed content"
    shell(f"input swipe {w//2} {int(h*.30)} {w//2} {int(h*.72)} 350")
    time.sleep(.5)
    assert page_indicator(ui(label + "-vertical-down")) == first, "Reverse vertical swipe changed page"
    shell(f"input swipe {int(w*.82)} {int(h*.45)} {int(w*.20)} {int(h*.45)} 350")
    time.sleep(.6)
    next_nodes = screenshot(label + "-swipe-left")
    assert page_indicator(next_nodes).startswith("Inhoudspagina 2 van "), "Horizontal swipe did not advance"
    assert cards(next_nodes) and not cards(next_nodes).intersection(initial), "Page repeated or cut game cards"
    assert any(exact("Rekenraket")(n) for n in next_nodes), "Header not pinned"
    assert any(description_prefix("Dorp")(n) and visible(n) for n in next_nodes) and any(description_prefix("Werelden")(n) and visible(n) for n in next_nodes), "Main navigation not pinned"
    shell(f"input swipe {int(w*.20)} {int(h*.45)} {int(w*.82)} {int(h*.45)} 350")
    time.sleep(.6)
    assert page_indicator(ui(label + "-swipe-right")) == first, "Horizontal reverse swipe failed"
    all_cards = set()
    seen = set()
    for i in range(60):
        nodes = screenshot(label + "-page-" + str(i))
        indicator = page_indicator(nodes)
        assert indicator not in seen, "Repeated content page"
        seen.add(indicator)
        current = cards(nodes)
        assert 1 <= len(current) <= 4, "Library page is empty or has too many cards"
        assert not all_cards.intersection(current), "Card duplicated across pages"
        all_cards.update(current)
        assert not any(n.get("scrollable") == "true" or n.get("class", "").endswith("ScrollView") for n in nodes)
        if not turn_page(nodes, True):
            break
    assert len(all_cards) == 27, "All 27 age-7+ rocket games must remain reachable"
    assert prefs()["wardrobe_stars"] == 18, "Paging changed wallet"
    pass_check(viewport + ": all fixed library pages have distinct cards, persistent navigation, zero vertical scrolling, both horizontal gestures and unchanged state")


def test_simplified_navigation(viewport):
    label = viewport + "-navigation"
    # Start with an empty profile. Every completion and unfinished game below is
    # created through native play; only the later playtime threshold is a fixture.
    seed({})
    launch_native_home()
    click(find_control(description_prefix("Rekenraket"), label + "-rocket", False))
    page(44)
    assert_simple_library(label + "-simplified-rocket-library")
    click(find_control(description_prefix("Werelden"), label + "-worlds-tab", False))
    page(44)
    assert_simple_library(label + "-six-worlds-overview")
    for title in ("Rekenraket", "Letterhuis", "Vormkasteel", "Muziekstudio", "Speelplein", "Kleurentuin"):
        find_control(description_prefix("Open " + title + " "), label + "-world-" + title)
    scroll_top()
    click(find_control(description_prefix("Open Rekenraket "), label + "-return-rocket", False))
    page(44)
    assert_simple_library(label + "-rocket-after-overview")
    click(find_control(description_prefix("Speel Getallenrace"), label + "-first-board"))
    page(45)
    for number in (1, 2, 3):
        click_named("Kies " + str(number), label + "-first-number-" + str(number))
    board_route = "0:6:0:0"
    board_key = "library_resume_v2_0_" + board_route
    rounds_key = "library_rounds_v1_0_" + board_route
    wait_prefs(lambda p: p.get(rounds_key) == 1 and p.get("wardrobe_stars") == 33,
               "one actually completed interactive board and its first +15 reward")
    click_named("Terug naar het magische dorp", label + "-home", False)
    page(1)
    click(find_control(description_prefix("Vormkasteel"), label + "-shapes", False))
    page(44)
    click(find_control(description_prefix("Speel Vormen zoeken"), label + "-shape-quiz"))
    page(6)
    quiz_route = "2:0:3:0"
    quiz_key = "library_resume_v2_0_" + quiz_route
    answer_saved(quiz_route, 1, 48, label + "-first-quiz-answer")
    saved_quiz = snapshot(quiz_route)
    correct_marker(ui(label + "-first-correct"), saved_quiz["answers"][saved_quiz["correct"]])
    shell("input keyevent KEYCODE_BACK")
    page(44)
    assert_simple_library(label + "-shapes-library")
    click(find_control(description_prefix("Verder spelen Vormen zoeken"), label + "-resume-quiz"))
    page(6)
    same_quiz(saved_quiz, snapshot(quiz_route), "Direct world resume")
    correct_marker(ui(label + "-resumed-correct"), saved_quiz["answers"][saved_quiz["correct"]])
    assert prefs()["wardrobe_stars"] == 48, "Resuming the selected correct answer paid twice"
    click_named("Terug naar het magische dorp", label + "-resume-home", False)
    page(1)
    click(find_control(description_prefix("Rekenraket"), label + "-replay-world", False))
    page(44)
    assert_simple_library(label + "-replay-library")
    click(find_control(description_prefix("Speel Getallenrace"), label + "-replay-board"))
    page(45)
    click_named("Kies 1", label + "-replay-first-step")
    first_step = wait_prefs(lambda p: board_key in p and json.loads(p[board_key])["count"] == 1,
                            "actual unfinished replay for newest-save priority")
    saved_board = json.loads(first_step[board_key])
    shell("input keyevent KEYCODE_BACK")
    page(44)
    click(find_control(description_prefix("Verder spelen Getallenrace"), label + "-resume-board"))
    page(45)
    restored_board = json.loads(prefs()[board_key])
    for field in ("id", "age", "seed", "engine", "count", "total"):
        assert restored_board[field] == saved_board[field], "Ideas changed the unfinished interactive board: " + field
    click_named("Kies 2", label + "-replay-two")
    click_named("Kies 3", label + "-replay-three")
    wait_prefs(lambda p: p.get(rounds_key) == 2 and p.get("wardrobe_stars") == 51 and board_key not in p,
               "second actually completed board pays exactly +3 and clears its save")
    screenshot(label + "-interactive-completion")
    click_named("Terug naar het magische dorp", label + "-completion-home", False)
    page(1)
    click(find_control(description_prefix("Vormkasteel"), label + "-before-pause-world", False))
    page(44)
    click(find_control(description_prefix("Verder spelen Vormen zoeken"), label + "-before-pause"))
    page(6)
    before_recreate = snapshot(quiz_route)
    date = datetime.date.fromisoformat(shell("date +%Y-%m-%d"))
    # Deterministic 15-minute threshold, only while the synthetic process is dead.
    # The actual question, answers, wallet and replay history remain untouched.
    recreate_from_background(6, {"usage_day": date.year*10000 + date.month*100 + date.day,
                                 "usage_seconds": 900, "usage_limit": 15, "usage_break_shown": False})
    same_quiz(before_recreate, snapshot(quiz_route), "Process recreation before pause")
    correct_marker(ui(label + "-recreated-correct"), before_recreate["answers"][before_recreate["correct"]])
    click_named("Volgende vraag", label + "-pause-trigger", False)
    page(31)
    paused = snapshot(quiz_route)
    assert paused["count"] == 1 and not paused["solved"], "Pause did not preserve the newly prepared unanswered question"
    assert prefs()["usage_break_shown"] is True and prefs()["wardrobe_stars"] == 51, "Pause changed earned stars or lost its shown marker"
    screenshot(label + "-pause")
    recreate_from_background(31)
    same_quiz(paused, snapshot(quiz_route), "Paused process recreation")
    click_named("🌟 Nog even verder spelen", label + "-pause-continue", False)
    page(6)
    same_quiz(paused, snapshot(quiz_route), "Continue after pause")
    answer_saved(quiz_route, 2, 54, label + "-after-pause-answer")
    shell("input keyevent KEYCODE_BACK")
    page(44)
    assert prefs()[rounds_key] == 2, "Pause/restart changed completed replay history"
    pass_check(viewport + ": real quiz task and break reminder survive background process recreation; exact question, one-time wallet and return to the world remain intact")

    click_named("Terug naar het magische dorp", label + "-parent-home", False)
    page(1)
    click(find_control(lambda node: exact("Ouderzone")(node)
                       or description_prefix("Ouderzone")(node), label + "-parent-entry", False))
    page(26)
    unlock_parent_gate(label + "-parent")
    parent_nodes = ui(label + "-parent-no-players")
    assert not any("spelers op dit toestel" in value.lower() for node in parent_nodes for value in labels(node)), "Player selector is still visible"
    parent_world_summary("Rekenraket", 2, 1, 0, label + "-parent-rounds")
    parent_world_summary("Vormkasteel", 0, 0, 1, label + "-parent-unfinished")
    scroll_top()
    click(find_control(description_prefix("Bekijk spellen Rekenraket"), label + "-parent-detail"))
    page(29)
    detail = ui(label + "-parent-detail-rocket")
    for i in range(12):
        if any("Getallenrace" in value for node in detail for value in labels(node)):
            break
        assert turn_page(detail, True), "Played game missing from parent detail"
        detail = ui(label + "-parent-detail-rocket-" + str(i))
    assert any("Getallenrace" in value for node in detail for value in labels(node)), "Played game not listed"
    assert any("2 getelde rondes" in value for node in detail for value in labels(node)), "Actual round total missing"
    screenshot(label + "-parent-detail-rocket")
    shell("input keyevent KEYCODE_BACK")
    page(28)
    shell("input keyevent KEYCODE_BACK")
    page(27)
    assert prefs()["wardrobe_stars"] == 54, "Reading parent history changed the wallet"
    pass_check(viewport + ": parent overview shows two actually played rounds, one completed board route and one unfinished quiz in their correct worlds")

    scroll_top()
    click_named("Terug naar het magische dorp", label + "-city-home", False)
    page(1)
    open_city(label + "-city")
    nodes = ui(label + "-city-no-ideas")
    assert not any("speelidee" in value.lower() for node in nodes for value in labels(node)), "Play ideas still visible in city"
    assert prefs()["wardrobe_stars"] == 54, "City navigation awarded currency"
    pass_check(viewport + ": simplified worlds and city navigation, no separate ideas or player controls; native replay and exact saved progress retained")


def sudoku_step(nodes, size):
    """Solve the highlighted native cell from visible clues, independently of the game model."""
    cells, current, options = {}, [], {}
    for node in nodes:
        description = node.get("content-desc", "")
        match = re.fullmatch(r"Sudoku rij (\d+), kolom (\d+): (.+)", description)
        if match and visible(node):
            position = (int(match.group(1)) - 1, int(match.group(2)) - 1)
            value = match.group(3)
            assert position not in cells, "Duplicate native Sudoku cell"
            cells[position] = None if value.startswith("leeg") else value.split(", ")[0]
            if value.endswith(", nu invullen"):
                current.append(position)
        option = re.fullmatch(r"Kies (.+) voor het gemarkeerde vak", description)
        if option and visible(node) and node.get("enabled") == "true":
            assert option.group(1) not in options, "Duplicate Sudoku animal option"
            options[option.group(1)] = node
    assert len(cells) == size * size, "Native Sudoku grid is clipped or incomplete"
    assert len(options) == size and len(current) == 1, "One highlighted blank and all animal options must be visible together"
    row, column = current[0]
    assert cells[current[0]] is None, "Highlighted Sudoku cell is not blank"
    used = {value for (r, c), value in cells.items()
            if value is not None and (r == row or c == column or
                size == 4 and r // 2 == row // 2 and c // 2 == column // 2)}
    choices = set(options) - used
    assert len(choices) == 1, "Highlighted cell is not independently solvable from visible clues"
    return options, choices.pop(), cells


def test_animal_sudoku(viewport):
    route = "2:6:13:0"
    for age in (0, 1, 2):
        label = viewport + "-sudoku-age-" + str(age)
        key = "library_resume_v2_" + str(age) + "_" + route
        rounds_key = "library_rounds_v1_" + str(age) + "_" + route
        seed({"chosen_age": age})
        launch_native_home()
        click(find_control(description_prefix("Vormkasteel"), label + "-world", False))
        page(44)
        click(find_control(description_prefix("Speel Dierensudoku"), label + "-entry"))
        page(45)
        size = 2 if age == 0 else 4
        nodes = screenshot(label + "-initial")
        options, answer, cells = sudoku_step(nodes, size)
        total = sum(value is None for value in cells.values())
        assert total >= (2 if age == 0 else 4), "Sudoku has no meaningful empty cells"
        before = real_history_fingerprint()
        wrong = next(animal for animal in options if animal != answer)
        click(options[wrong])
        after_wrong = ui(label + "-wrong")
        _, same_answer, same_cells = sudoku_step(after_wrong, size)
        assert same_cells == cells and same_answer == answer, "Incorrect Sudoku choice changed the puzzle"
        assert real_history_fingerprint() == before, "Incorrect Sudoku choice changed saved progress or currency"
        options, answer, _ = sudoku_step(after_wrong, size)
        click(options[answer])
        first_state = wait_prefs(lambda p: key in p and json.loads(p[key])["count"] == 1,
                                 "First correct Sudoku cell persisted")
        saved = json.loads(first_state[key])
        assert saved["id"] == route and saved["age"] == age and saved["engine"] == 4, "Wrong Sudoku identity or engine"
        assert saved["total"] == total and saved["mask"] != 0, "Sudoku cell mask or total was not persisted"
        assert first_state["wardrobe_stars"] == 18, "Partial Sudoku paid a completion reward"
        if age == 2:
            first_cells = sudoku_step(ui(label + "-before-restart"), size)[2]
            launch_native_home()
            click(find_control(description_prefix("Vormkasteel"), label + "-restart-world", False))
            page(44)
            click(find_control(description_prefix("Verder spelen Dierensudoku"), label + "-resume"))
            page(45)
            restored = json.loads(prefs()[key])
            for field in ("id", "age", "seed", "engine", "count", "mask", "total"):
                assert restored[field] == saved[field], "Sudoku restart changed " + field
            assert sudoku_step(ui(label + "-restored"), size)[2] == first_cells, "Restart changed visible Sudoku clues or filled cells"
        for count in range(2, total + 1):
            options, answer, _ = sudoku_step(ui(label + "-cell-" + str(count)), size)
            click(options[answer])
            if count < total:
                current = wait_prefs(lambda p: key in p and json.loads(p[key])["count"] == count,
                                     "Sudoku cell " + str(count) + " persisted")
                assert current["wardrobe_stars"] == 18, "Incomplete Sudoku awarded stars"
        completed = wait_prefs(lambda p: key not in p and p.get(rounds_key) == 1,
                               "One complete Sudoku round and cleared unfinished state")
        assert completed["wardrobe_stars"] == 33, "First Sudoku must award one answer and one badge (+15)"
        assert route in completed.get("library_completed_v1_" + str(age), set()), "Sudoku completion was not recorded"
        screenshot(label + "-complete")
        launch_native_home()
        assert prefs()["wardrobe_stars"] == 33 and prefs()[rounds_key] == 1, "Restart duplicated Sudoku completion rewards"
        pass_check(viewport + ": age " + str(age) + " native Dierensudoku has a complete " + str(size) + "x" + str(size) +
                   " grid, rejects a wrong animal without progress, solves " + str(total) + " cells from visible row/column/box clues, and pays one completion reward")
    pass_check(viewport + ": 4x4 Sudoku preserves its exact seed, engine, count, selected-cell mask and visible board across a full restart")


def test_release_expedition(viewport):
    label = viewport + "-release-expedition"
    seed({})
    launch_native_home()
    open_expedition(label)
    nodes = screenshot(label + "-first", expected_texts=("0 van 30 opdrachten · 0 van 6 zegels",))
    mission = next((n for n in nodes if exact("Expeditie opdracht 1 spelen")(n) and visible(n)), None)
    map_button = next((n for n in nodes if exact("Bekijk avonturenkaart")(n) and visible(n)), None)
    assert mission is not None and map_button is not None, "Next mission and map must be directly available on the first sheet"
    click(map_button)
    nodes = screenshot(label + "-map")
    assert any(description_prefix("Expeditiekaart:")(n) and visible(n) for n in nodes), "Expedition map shortcut shows the wrong sheet"
    click_named("Expeditie opdracht 1 spelen", label + "-mission")
    page(45)
    click(find_control(lambda n: re.fullmatch(r"Vak \d+: 1", n.get("content-desc", "")) is not None,
                       label + "-first-move", False))
    key = "library_resume_v2_0_0:6:4:0"
    state = wait_prefs(lambda p: key in p and json.loads(p[key])["count"] == 1, "Expedition shortcut starts the actual first puzzle")
    assert state.get("expedition_v1_progress_0", 0) == 0 and state["wardrobe_stars"] == 18, "Partial expedition mission was credited"
    pass_check(viewport + ": named expedition sheets expose mission and map directly and launch a real saved puzzle without premature credit")


def test_release_menus(viewport):
    label = viewport + "-release-menus"
    # An explicitly owned-color fixture tests equipping, not purchase or earning.
    seed({"wardrobe_owned_0": 3, "wardrobe_worn_0": 0})
    launch_native_home()
    open_city(label + "-city")
    click_named("Open kledingkast", label + "-wardrobe")
    page(10)
    initial = ui(label + "-categories")
    preview = next(n for n in initial if exact("Lumi, voorbeeld van je outfit")(n) and visible(n))
    preview_rect = rect(preview)
    click(find_control(description_prefix("Medaillonkleur"), label + "-colors"))
    color = find_control(lambda n: "Suikerspinroze" in n.get("content-desc", ""), label + "-owned-color")
    after = ui(label + "-pinned-preview")
    assert any(exact("Lumi, voorbeeld van je outfit")(n) and rect(n) == preview_rect for n in after), "Wardrobe preview disappeared or moved between sheets"
    assert any(exact("Kies een andere accessoirecategorie")(n) and visible(n) for n in after), "Pinned category shortcut disappeared"
    click(color)
    wait_prefs(lambda p: p.get("wardrobe_worn_0") == 1, "Owned color equipped")
    assert prefs()["wardrobe_stars"] == 18, "Equipping an owned accessory charged stars"
    screenshot(label + "-equipped")
    click_named("Kies een andere accessoirecategorie", label + "-category-shortcut", False)
    assert any(description_prefix("Hoedjes")(n) and visible(n) for n in ui(label + "-categories-returned")), "Category shortcut did not reveal the category sheet"
    launch_native_home()
    assert prefs()["wardrobe_worn_0"] == 1, "Equipped color was lost on restart"
    click(find_control(lambda n: exact("Ouderzone")(n) or description_prefix("Ouderzone")(n), label + "-parent", False))
    page(26)
    unlock_parent_gate(label + "-gate")
    nodes = screenshot(label + "-parent-first")
    for title in ("📊 Bekijk de voortgang", "⚙️ Instellingen", "Speelgegevens"):
        assert any(any(value.startswith(title) for value in labels(n)) and visible(n) for n in nodes), "Parent first sheet is missing " + title
    assert not any("spelers op dit toestel" in value.lower() for n in nodes for value in labels(n)), "Retired player selector returned"
    click_named("Speelgegevens", label + "-totals", False)
    totals = ui(label + "-totals-shown")
    assert any("0 goede antwoorden" in value for n in totals for value in labels(n)), "Parent totals shortcut opened the wrong sheet"
    click(find_control(description_prefix("⚙️ Instellingen"), label + "-settings"))
    page(30)
    background_app()
    native_foreground(None)
    require_parent_gate(label + "-locked-after-background")
    assert prefs()["wardrobe_stars"] == 18, "Menu navigation changed wallet"
    pass_check(viewport + ": wardrobe keeps Lumi visible, equips an owned color persistently, and jumps to categories; parent first sheet has direct navigation and relocks on background")


def run_stage(name, action, *arguments):
    started = time.monotonic()
    print("START:", name, flush=True)
    action(*arguments)
    print("FINISH:", name, "in", round(time.monotonic() - started, 1), "seconds", flush=True)


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--viewport", choices=("phone", "fold"), required=True)
    parser.add_argument("--no-install", action="store_true")
    parser.add_argument("--keep-viewport", action="store_true", help="Let the disposable CI emulator shut down without display reconfiguration")
    parser.add_argument("--baseline-apk", help="Optional exact 0.123 debug APK built with the same runner's debug key")
    parser.add_argument("--release-smoke", action="store_true", help="Focused 0.126 acceptance: exact upgrade, paging, new Sudoku, changed menus, expedition shortcuts and real construction; full regression remains the default")
    args = parser.parse_args()
    OUT.mkdir(parents=True, exist_ok=True)
    adb("wait-for-device", timeout=60)
    assert shell("getprop ro.kernel.qemu") == "1", "Refusing to clear data on a non-emulator device"
    if not args.no_install and not args.baseline_apk:
        adb("install", "-r", "app/build/outputs/apk/debug/app-debug.apk", timeout=120)
    try:
        shell("pm disable-user --user 0 com.android.launcher3")
        shell("am force-stop com.android.launcher3")
        if args.viewport == "fold":
            if dimensions() != (1200, 1320):
                shell("wm size 1200x1320")
            if int(re.findall(r"(\d+)", shell("wm density"))[-1]) != 210:
                shell("wm density 210")
        time.sleep(2)
        if args.baseline_apk:
            run_stage("exact-0.123-upgrade", test_debug_upgrade, args.baseline_apk, args.viewport)
        if args.release_smoke:
            stages = (test_fixed_pages, test_animal_sudoku, test_release_menus,
                      test_release_expedition, test_builder)
        else:
            stages = (test_fixed_pages, test, test_new_quiz_routes, test_simplified_navigation,
                      test_hands_on_worlds, test_memory_refresh, test_puzzle_board,
                      test_animal_sudoku, test_expedition, test_builder, test_cosmetic_fixture,
                      test_drawing_lifecycle, test_drawing_tools, test_drawing_compaction,
                      test_parent_lifecycle)
        for stage in stages:
            run_stage(stage.__name__, stage, args.viewport)
    finally:
        (OUT / "checks.json").write_text(json.dumps({"viewport": args.viewport, "passed": CHECKS,
            "suite": "0.126 release smoke" if args.release_smoke else "full native regression",
            "scope": "Native UI and persistence on a disposable emulator; synthetic fixtures explicitly labelled. Release smoke covers the exact upgrade, drawing preservation, paging, all Sudoku ages, changed menus, expedition shortcuts and construction; it does not replace the full historical regression."},
            indent=2, ensure_ascii=False))
        for filename, command in (("logcat.txt", ("logcat", "-d", "-v", "brief")),
                                  ("window.txt", ("shell", "dumpsys window")),
                                  ("last-screen.png", ("exec-out", "screencap", "-p"))):
            try:
                (OUT / filename).write_bytes(adb(*command, timeout=5))
            except (subprocess.SubprocessError, OSError) as error:
                print("Diagnostic unavailable:", filename, type(error).__name__, flush=True)
        if not args.keep_viewport:
            for command in ("wm size reset", "wm density reset", "settings put system font_scale 1.0"):
                try:
                    adb("shell", command, timeout=5)
                except (subprocess.SubprocessError, OSError) as error:
                    print("Cleanup unavailable:", command, type(error).__name__, flush=True)
