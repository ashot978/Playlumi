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
import subprocess
import time
import xml.etree.ElementTree as ET


APP = "nl.playlumi.app"
OUT = pathlib.Path("build/city-progress")
CHECKS = []
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


def prefs():
    raw = adb("exec-out", "run-as", APP, "cat", "shared_prefs/MainActivity.xml")
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


def wait_prefs(predicate, expectation):
    last = {}
    for _ in range(20):
        try:
            last = prefs()
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


def find_control(predicate, label, scroll=True):
    w, h = dimensions()
    for attempt in range(11 if scroll else 3):
        nodes = ui(label + "-" + str(attempt))
        hit = next((node for node in nodes if predicate(node) and visible(node)
                    and node.get("clickable") == "true" and node.get("enabled") == "true"), None)
        if hit is not None:
            return hit
        if scroll:
            shell(f"input swipe {w//2} {int(h*.78)} {w//2} {int(h*.34)} 400")
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


def start_upgrade_quiz(label, resume):
    shell(f"am force-stop {APP}")
    shell("logcat -c")
    shell(f"am start -W -n {APP}/.MainActivity --ei playlumi_ci_library 4 --ei playlumi_ci_age 2")
    time.sleep(2)
    page(44)
    prefix = ("Verder spelen " if resume else "Speel ") + "Groente herkennen"
    click(find_control(description_prefix(prefix), label + "-library"))
    page(6)
    key = "library_resume_v2_2_4:0:20:2"
    state = wait_prefs(lambda p: key in p, "real vegetable quiz snapshot")
    return json.loads(state[key])


def test_debug_upgrade(baseline_apk, viewport):
    baseline = pathlib.Path(baseline_apk)
    candidate = pathlib.Path("app/build/outputs/apk/debug/app-debug.apk")
    assert baseline.is_file() and candidate.is_file(), "Both debug upgrade APKs must exist"
    # Install the older debuggable build as fixture setup, then seed its clean data.
    # Avoid uninstall/launcher churn on the emulator. The actual forward upgrade
    # below uses install -r exclusively, with no clearing between versions.
    adb("shell", "am force-stop " + APP)
    adb("install", "-r", "-d", str(baseline), timeout=120)
    assert "versionName=0.108" in shell(f"dumpsys package {APP}"), "Wrong upgrade baseline"
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
        "achievements_v1": (1 << 17) | (1 << 23),
        "daily_day_v1": emulator_day(), "daily_a_v1": 2, "daily_b_v1": 1,
        "daily_round_v1": 0, "daily_claimed_v1": 0, "daily_sets_v1": 4,
    }
    seed(fixture)
    before_question = start_upgrade_quiz(viewport + "-upgrade-baseline", False)
    assert before_question["count"] == 0 and not before_question["solved"], "Baseline quiz must be unanswered"
    before = prefs()
    for key, expected in fixture.items():
        assert before.get(key) == expected, "Baseline did not retain its fixture: " + key
    screenshot(viewport + "-upgrade-baseline-question",
               expected_texts=(before_question["prompt"], before_question["display"], *before_question["answers"]))
    shell(f"am force-stop {APP}")
    adb("install", "-r", str(candidate), timeout=120)
    assert "versionName=0.109" in shell(f"dumpsys package {APP}"), "Wrong candidate version after upgrade"
    installed = prefs()
    for key in fixture:
        assert installed.get(key) == before[key], "Package upgrade lost preference: " + key
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
    (OUT / (viewport + "-upgrade-comparison.json")).write_text(json.dumps({
        "baselineVersion": "0.108", "candidateVersion": "0.109",
        "baselineSha256": hashlib.sha256(baseline.read_bytes()).hexdigest(),
        "candidateSha256": hashlib.sha256(candidate.read_bytes()).hexdigest(),
        "preferencesCompared": list(fixture), "questionFieldsCompared": list(identity),
        "scope": "Same-runner debug-signed install -r upgrade on a disposable emulator. Original release signing and physical-device acceptance are separate checks."
    }, indent=2, ensure_ascii=False))
    pass_check(viewport + ": actual debug 0.108 → 0.109 install -r preserves wallet, owned/equipped items, favourites, achievements, daily state, age-specific replay counts and exact unanswered quiz")


def scroll_top():
    w, h = dimensions()
    for _ in range(4):
        shell(f"input swipe {w//2} {int(h*.30)} {w//2} {int(h*.82)} 350")
        time.sleep(.2)


def scroll_bottom():
    w, h = dimensions()
    for _ in range(4):
        shell(f"input swipe {w//2} {int(h*.82)} {w//2} {int(h*.30)} 350")
        time.sleep(.2)


def test_new_quiz_routes(viewport):
    cases = ((0, "Rekenraket", "Evenveel", "0:0:11:5"),
             (2, "Rekenraket", "Tientallen en eenheden", "0:0:12:8"),
             (2, "Speelplein", "Eerst en daarna", "4:0:19:5"))
    normal = lambda text: " ".join(text.split())
    for index, (age, world, title, route) in enumerate(cases):
        label = viewport + "-new-quiz-" + str(index + 1)
        seed({"chosen_age": age})
        start_home(age)
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
    screenshot(prefix + "-daily-before")
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
    click(find_control(lambda n: n.get("content-desc", "").startswith("Vriendjes"), prefix + "-friends-tab"))
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
    scroll_bottom()
    bottom_nodes = screenshot(prefix + "-all-unlocked-fixture-bottom",
                              expected_texts=("Alle plekken zijn ontdekt! Je kunt overal blijven spelen.",),
                              expected_descriptions=("Lumi-standbeeld, vrijgespeeld.", "Open kledingkast",
                                                     "Dagelijkse uitdagingen"))
    monument = next(node for node in bottom_nodes if description_prefix("Lumi-standbeeld,")(node))
    assert rect(monument)[3] - rect(monument)[1] >= plot_height-2, "Final city plot/nameplate is clipped in capture"
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
    """A fresh normal launcher task; chosen_age comes from the explicit fixture."""
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


def drawing_file(label):
    raw = adb("exec-out", "run-as", APP, "cat", "files/playlumi-kunstwerk.png")
    assert raw.startswith(b"\x89PNG\r\n\x1a\n") and len(raw) > 100, "Missing/corrupt saved drawing"
    (OUT / (label + ".png")).write_bytes(raw)
    return raw


def test_drawing_lifecycle(viewport):
    label = viewport + "-lifecycle-drawing"
    seed({})
    launch_native_home()
    open_drawing(label + "-blank")
    click_named("Terug naar het magische dorp", label + "-blank-home", False)
    page(1)
    blank = drawing_file(label + "-blank-file")

    canvas = open_drawing(label + "-stroke")
    left, top, right, bottom = rect(canvas)
    x1, y1 = int(left + .2*(right-left)), int(top + .35*(bottom-top))
    x2, y2 = int(left + .8*(right-left)), int(top + .65*(bottom-top))
    shell(f"input swipe {x1} {y1} {x2} {y2} 500")
    screenshot(label + "-stroke")
    click_named("Terug naar het magische dorp", label + "-stroke-home", False)
    page(1)
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
    shell(f"input swipe {int(left+.25*(right-left))} {int(top+.70*(bottom-top))} "
          f"{int(left+.75*(right-left))} {int(top+.20*(bottom-top))} 500")
    click_named("💾 Bewaar tekening", label + "-explicit-save", False)
    saved = wait_prefs(lambda p: p.get("drawing_saved_once") is True
                      and p.get("wardrobe_stars") == 27 and p.get("collection_world_5") == 1,
                      "drawing marker, one +9 bonus and world progress saved together")
    assert saved["wardrobe_total_earned"] == 27, "Drawing earned total disagrees with the wallet"
    assert drawing_file(label + "-second-stroke-file") != painted, "Second real stroke was not saved"
    click_named("↩  Laatste lijn weg", label + "-undo-after-save", False)
    click_named("💾 Bewaar tekening", label + "-save-again", False)
    assert drawing_file(label + "-undo-after-save-file") == painted, "Explicit saving flattened the live undo history"
    assert prefs()["wardrobe_stars"] == 27 and prefs()["collection_world_5"] == 1, "Repeated save paid/counts the drawing twice"
    shell(f"input swipe {int(left+.2*(right-left))} {int(top+.75*(bottom-top))} "
          f"{int(left+.8*(right-left))} {int(top+.75*(bottom-top))} 500")
    old_pid = app_pid()
    background_app()
    native_foreground(25)
    assert app_pid() == old_pid, "Expected a live-View pause/resume for the Undo check"
    assert drawing_file(label + "-background-file") != painted, "Background autosave missed the new stroke"
    click_named("↩  Laatste lijn weg", label + "-undo-after-background", False)
    click_named("💾 Bewaar tekening", label + "-save-after-background", False)
    assert drawing_file(label + "-undo-after-background-file") == painted, "Background saving flattened the live undo history"
    launch_native_home()
    open_drawing(label + "-restart")
    click_named("💾 Bewaar tekening", label + "-save-after-restart", False)
    restored = prefs()
    assert restored["wardrobe_stars"] == 27 and restored["collection_world_5"] == 1 and restored["drawing_saved_once"], "Restart repeated/lost the creative reward"
    screenshot(label + "-reward-restored")
    pass_check(viewport + ": drawing touchstroke survives Home/reopen, Undo survives live save/pause, autosave is free and explicit save grants +9/one collection step only once across restart")


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


def open_ideas(label):
    scroll_top()
    click_named("Open speelideeën", label + "-entry")
    page(50)


def idea_cards(label):
    titles, seen_texts = [], set()
    scroll_top()
    for index in range(1, 4):
        button = find_control(description_prefix("Speelidee " + str(index) + ": "), label + "-card-" + str(index))
        titles.append(button.get("content-desc").split(": ", 1)[1])
        nodes = ui(label + "-visible-" + str(index))
        no_speech(nodes)
        seen_texts.update(" ".join(value.split()) for node in nodes if visible(node) for value in labels(node))
    assert len(set(titles)) == 3, "Play ideas duplicate a visible exercise"
    return titles, seen_texts


def idea_click(index, title, label):
    scroll_top()
    click_named("Speelidee " + str(index) + ": " + title, label)


def real_history_fingerprint():
    prefixes = ("wardrobe_", "score_", "library_resume_", "library_rounds_", "library_completed_",
                "daily_", "collection_world_", "learn_", "play_day_")
    return {key: value for key, value in prefs().items()
            if key.startswith(prefixes) or key in ("completed_all", "library_played")}


def parent_world_summary(world, rounds, completed, unfinished, label):
    counted = f"{rounds} getelde rondes · {completed} oefeningen met een afgeronde ronde"
    saved = str(unfinished) + (" begonnen ronde om verder te spelen" if unfinished == 1
                               else " begonnen rondes om verder te spelen")
    w, h = dimensions()
    for _ in range(10):
        nodes = ui(label)
        title = next((node for node in nodes if node.get("text") == world and visible(node)), None)
        if title is not None:
            parent = next((node for node in nodes if title in list(node)), None)
            values = {" ".join(value.split()) for child in parent.iter("node") if visible(child)
                      for value in labels(child)} if parent is not None else set()
            if counted in values and saved in values:
                screenshot(label)
                return
        shell(f"input swipe {w//2} {int(h*.78)} {w//2} {int(h*.34)} 400")
    raise AssertionError("Parent world summary does not match actually played history: " + world)


def test_play_ideas(viewport):
    label = viewport + "-ideas"
    # Start with an empty profile. Every completion and unfinished game below is
    # created through native play; only the later playtime threshold is a fixture.
    seed({})
    launch_native_home()
    click(find_control(description_prefix("Rekenraket"), label + "-rocket", False))
    page(44)
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
    open_ideas(label + "-library")
    titles, texts = idea_cards(label + "-real-history")
    assert titles[0] == "Vormen zoeken" and titles[1] == "Getallenrace", "Ideas did not prioritize the actual unfinished quiz and completed board"
    assert {"Verder spelen", "Nog eens spelen", "Ontdekken"} <= texts, "Idea labels do not distinguish resume, replay and discovery"
    assert "1 ronde gespeeld · Tijd voor een nieuwe ronde?" in texts, "Ideas invented or lost the actual replay count"
    scroll_top()
    screenshot(label + "-three-kinds", expected_texts=("Speelideeën",))

    before = real_history_fingerprint()
    variation = prefs().get("play_ideas_variation_v1", 0)
    click_named("Andere speelideeën", label + "-other")
    page(50)
    assert prefs().get("play_ideas_variation_v1") == variation + 1, "Other ideas did not remember the chosen variation"
    changed, _ = idea_cards(label + "-varied")
    assert changed[0] == "Vormen zoeken" and changed[1] == "Getallenrace", "Variation replaced the available saved/replay actions"
    assert real_history_fingerprint() == before, "Choosing other ideas changed earned history or saved rounds"

    if viewport == "phone":
        shell("settings put system font_scale 1.3")
        time.sleep(1)
        page(50)
        scroll_top()
        screenshot(label + "-large-font-top", expected_texts=("Speelideeën",))
        find_control(description_prefix("Speelidee 3: "), label + "-large-font-third")
        screenshot(label + "-large-font-bottom")
        shell("settings put system font_scale 1.0")
        time.sleep(1)
        page(50)

    idea_click(1, "Vormen zoeken", label + "-resume-quiz")
    page(6)
    same_quiz(saved_quiz, snapshot(quiz_route), "Ideas resume")
    correct_marker(ui(label + "-resumed-correct"), saved_quiz["answers"][saved_quiz["correct"]])
    assert prefs()["wardrobe_stars"] == 48, "Resuming the selected correct answer paid twice"
    shell("input keyevent KEYCODE_BACK")
    page(50)

    idea_click(2, "Getallenrace", label + "-replay-board")
    page(45)
    click_named("Kies 1", label + "-replay-first-step")
    first_step = wait_prefs(lambda p: board_key in p and json.loads(p[board_key])["count"] == 1,
                            "actual unfinished replay for newest-save priority")
    saved_board = json.loads(first_step[board_key])
    shell("input keyevent KEYCODE_BACK")
    page(50)
    titles, _ = idea_cards(label + "-newest-save")
    assert titles[0] == "Getallenrace", "Latest actual unfinished game is not first"
    idea_click(1, "Getallenrace", label + "-resume-board")
    page(45)
    restored_board = json.loads(prefs()[board_key])
    for field in ("id", "age", "seed", "engine", "count", "total"):
        assert restored_board[field] == saved_board[field], "Ideas changed the unfinished interactive board: " + field
    click_named("Kies 2", label + "-replay-two")
    click_named("Kies 3", label + "-replay-three")
    wait_prefs(lambda p: p.get(rounds_key) == 2 and p.get("wardrobe_stars") == 51 and board_key not in p,
               "second actually completed board pays exactly +3 and clears its save")
    screenshot(label + "-interactive-completion")
    click_named("Terug naar speelideeën", label + "-completion-return", False)
    page(50)
    pass_check(viewport + ": three honest ideas come from real played history; variation is free, newest quiz/board saves resume exactly and interactive Back/completion return to ideas")

    idea_click(1, "Vormen zoeken", label + "-before-pause")
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
    page(50)
    assert prefs()[rounds_key] == 2, "Pause/restart changed completed replay history"
    pass_check(viewport + ": real quiz task and break reminder survive background process recreation; exact question, one-time wallet and return to ideas remain intact")

    click_named("Terug naar het magische dorp", label + "-parent-home", False)
    page(1)
    click(find_control(lambda node: exact("Ouderzone")(node)
                       or description_prefix("Ouderzone")(node), label + "-parent-entry", False))
    page(26)
    unlock_parent_gate(label + "-parent")
    parent_world_summary("Rekenraket", 2, 1, 0, label + "-parent-rounds")
    parent_world_summary("Vormkasteel", 0, 0, 1, label + "-parent-unfinished")
    assert prefs()["wardrobe_stars"] == 54, "Reading parent history changed the wallet"
    pass_check(viewport + ": parent overview shows two actually played rounds, one completed board route and one unfinished quiz in their correct worlds")

    scroll_top()
    click_named("Terug naar het magische dorp", label + "-city-home", False)
    page(1)
    open_city(label + "-city")
    open_ideas(label + "-city-ideas")
    shell("input keyevent KEYCODE_BACK")
    page(49)
    assert prefs()["wardrobe_stars"] == 54, "City/ideas navigation awarded currency"
    pass_check(viewport + ": native city ideas entry returns to its originating city without changing progress")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--viewport", choices=("phone", "fold"), required=True)
    parser.add_argument("--no-install", action="store_true")
    parser.add_argument("--keep-viewport", action="store_true", help="Let the disposable CI emulator shut down without display reconfiguration")
    parser.add_argument("--baseline-apk", help="Optional exact 0.108 debug APK built with the same runner's debug key")
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
            test_debug_upgrade(args.baseline_apk, args.viewport)
        test(args.viewport)
        test_new_quiz_routes(args.viewport)
        test_play_ideas(args.viewport)
        test_cosmetic_fixture(args.viewport)
        test_drawing_lifecycle(args.viewport)
        test_parent_lifecycle(args.viewport)
    finally:
        (OUT / "checks.json").write_text(json.dumps({"viewport": args.viewport, "passed": CHECKS,
            "scope": "Native UI and persistence on a disposable emulator; synthetic fixtures explicitly labelled."},
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
