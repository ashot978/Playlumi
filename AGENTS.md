# Playlumi project

- Build a child-friendly Android app for ages 3-5; keep game rules independent from the UI.
- Preserve locally stored progress across updates. Avoid accounts, ads, analytics and extra permissions without explicit approval and a privacy review.
- Run unit tests, lint and the debug build when the Android SDK and Gradle are available. Verify interaction and accessibility on a real device before claiming device support.
- Do not publish, merge or release without explicit authorization.

## Playlumi 0.27.0 - release agreement
Every completed app feature update MUST include a newly built signed Android APK
as a direct conversation download, not just an open PR. Never mislabel an
unbuilt or untested APK as ready. Build from original source on the app branch,
run tests, lint, emulator launch, and verify package, versionCode and stable
signer. Keep the private keystore/password out of GitHub and CI. Do not
merge to main or publish to stores without an explicit request.

## Look & feel 0.28.0
Apply the shared native design language across screens. Preserve game rules, stored scores, profiles and stable app identity. A finished update must include a newly built signed APK and passing Android build, lint and emulator launch; never publish private signing materials.

## 0.29.0 Question decks
Every one of the 24 learning games, five styles and three difficulty levels
must expose 300 valid distinct question cards in a random order. Reshuffle
on every game opening and on "Nog een ronde". Preserve the eight-question
short-session length and local progress. Theme cards may repeat preschool
concepts; never misrepresent them as 300 new facts. Ship a signed APK after
passing Android and emulator checks; do not publish credentials or merge.

## Playlumi 0.30.0 interactive minigame decks
Every one of the three interactive board minigames (shape drag, number balloons,
animal memory) uses 300 distinct board layouts at each of three difficulty
levels, shuffled freshly on entry and again on Replay. Preserve the required
drag/drop rule, correct matches, small-screen layouts, local data and rewards.
A feature update must deliver a newly signed installable APK, with test and
emulator evidence; never upload the private signing key.

## Playlumi 0.31.0
Bellenfeest and Muziekatelier use 300 distinctly shuffled playable layouts at each level. Preserve note identity, scores, local progress and saved bubble colors on rotation. Each finished feature release MUST include signed APK tested in CI and emulator; do not upload signing secrets.

## Playlumi 0.32.0 living garden
The existing twelve plants now advance one stage per completed learning session. Existing water/sun actions and reward mask remain authoritative and persistent; first-bloom bonus may only be paid once. Animate growth visually in the native garden scene but do not change scores, profiles or child privacy. Include fresh installable APK after successful Android CI and emulator launch; private signing materials stay out of GitHub.

## Playlumi 0.33.0 navigation and customization
Always show four intuitive menu tabs (Start, Spelen, Aanpassen, Tuin) without covering content or blocking games. Show every customization category and all seven items directly without page navigation; preserve all owned items, worn items and earned star balances. Do not change parent gates. Every release must provide a new privately signed downloadable APK after CI and emulator smoke checks.

## Playlumi 0.34.0 safe visual customization
Do not auto-purchase a locked wardrobe or room option on tap. Show a temporary in-memory preview first, followed by a separate clearly priced confirmation. Owned items remain free to equip. Provide a horizontal category strip and All / Owned / To unlock filters. Save only confirmed purchases and user-chosen owned items. Maintain consistent profile-scoped state and never upload private signing keys. A completed update requires a newly signed APK with CI+emulator evidence.

## Playlumi 0.35.0 navigation to recent games
Per child, store up to four last-used standard activities, including variant and difficulty, without answers or identifiers. Recent replay resets a new eight-question session; adventure progress is separate. Show all eight learning games in a world and all five variants without paging. Never reset previous saves or awards. Finished updates require signed APK, full CI, emulator start and matching private certificate.

## Playlumi 0.36.0 favorites and outfit presets
Favorites and three saved outfit selections are strictly per child profile and local. Favorite game tap starts a new shuffled round. A saved outfit may equip only already-owned items, never award/unlock/spend stars. Do not mutate outfit preview or child data on saving favorites. Publish each completed update as a new signed APK with stable package and signing cert following full Android tests and emulator start.

## Playlumi 0.37.0 picture-first play
Provide six big illustrated one-tap learning games suitable for each age (2 columns on phone, 3 on tablet), preserving 300 shuffled question cards and every prior progression mechanic. Picture-play back and end-of-round 'another game' return to picture selector, not advanced variant menus. Keep full advanced world/category/variant navigation accessible. Maintain offline and privacy rules, same private release signing identity and release APK requirement.

## Playlumi 0.38.0 — organic storybook interface
The child-facing UI uses rounded, organic cloud shapes rather than uniform rectangular cards. Six one-tap age-appropriate games use real circular, animated Canvas choices with independently accessible labels and tap targets. A native animated meadow with clouds, flowers and butterflies makes Home, world-selection and customization screens feel alive. Gameplay questions/illustrations/answer selections use soft curved organic shapes; keep question text fully accessible. All movement must be decorative, finite or lifecycle-cancelled; reduced-motion setting freezes all decorative motion. Never require motion to understand a question or earn a reward. Keep the full 300-question randomized decks, privacy, profiles and original signing requirements.

## Playlumi 0.39.0 - listening before choosing
The six age-specific picture buttons keep their original one-tap launch. Holding
one briefly reads its title and explains the tap action using existing local
Android text-to-speech, only when sound is enabled. Keep the preview harmless:
no launch, score mutation, account, internet, new permission or saved answers.
Verify every age/choice, preserve Playlumi's stable certificate and app ID,
and deliver a new privately signed, verified APK after passing Android CI.

## Playlumi 0.40.0 - visible sound choice
When sound is enabled, each of the six picture choices must have its own visible
48dp speaker target, independent from the large primary play bubble. Listening
cannot start a game, change scores, or save answers. Keep the existing one-tap
game launch and optional long press, the exact 3-age/6-choice mappings, and
TalkBack labels for the separate play and listen actions. Hide the separate
speaker when sound is disabled. Preserve the original signer and local saves.
Complete Android tests, lint, emulator startup, and a privately signed APK.

## Playlumi 0.41.0 - gentle optional learning hints
When a multiple-choice learning round has at least three available answers,
show a voluntary "Help mij" button. Each tap safely removes exactly one
incorrect visible answer with a quiet cloud, never the correct answer.
Never reduce below two remaining choices. Hints must not answer questions,
change scores, earn or spend stars, or modify persistent child data.
The controls are screen-reader compatible and cannot select faded answers.
Keep hints associated only with the current Round, including redraws.
Preserve every offline deck, the parent gate, original release signer,
version continuity, and the passing Android tests/emulator/release-APK gate.

## Playlumi 0.42.0 - accessible spoken answer choices
Each live multi-choice learning question offers an optional visible "Hoor de
antwoorden" control, only when sound is on. It uses the existing local Dutch
Android speech system to announce only visible answer labels in screen order.
Long-pressing an enabled choice announces that one answer without submitting
it; tapping selects it exactly as before. Hints from 0.41 may hide wrong
choices; neither speech path must mention the hidden choices. All audio
previews are independent of game scoring, awards, stars, stored data and
correct answer disclosure. Hide the audio button after successful selection,
keep minimum accessible targets and reduced motion. Keep tests, lint,
emulator gate and signed APK with the original private signer; no main merge.

## Playlumi 0.43.0 — calm, manual-only narration
Automatic TTS is forbidden. Questions, picture selection, minigames and stories
may speak only from an explicit speaker-button tap or deliberate long press.
Ignore legacy auto_read preferences even if previously set to true; never
silently re-enable or recreate the old automatic voice setting.
Use neutral Dutch speech pitch and near-natural rate, prefer an offline
Netherlands Dutch voice already supplied by the device. Do not download
voices or add Internet permission. No unsolicited welcome/badge audio.
Provide independent locally saved on/off control for gentle game sounds
without disabling the requested voice. Remove harsh ToneGenerator fallbacks.
Preserve all local saves, original signing identity, privacy and Android
CI emulator gates; a completed change needs a signed, verified APK.

## Playlumi 0.44.0 — deliberate voice preview and calm defaults
No auto narration. Keep sound effects OFF by default for profiles that have
not actively stored a preference; preserve existing effects_on values.
In parent settings, add a manual (explicit tap) voice preview and a safe
3-step speed setting (slow/normal/brisk). If Android supplies multiple offline
Dutch voices, allow parents to cycle between them and preview. Persist only
selected device voice name and speed within the child's local preferences.
Never list non-Dutch or network-required voices. Never auto-play a preview on
initialization or after settings navigation, and never add Internet permission.
Keep original signing certificate, app-ID and saved gameplay data.

## Playlumi 0.45.0 — keep musical learning audible but peaceful
Separate intentionally requested learning notes from decorative jingles:
Muziekatelier's notes must work whenever the global Geluid switch is on,
even when optional Geluidseffecten is off (default). Global mute remains
authoritative for all audio. Every melody playback is explicitly triggered;
never automatically play on opening or advancing a screen. A replay request
invalidates earlier scheduled notes. Pressing the dedicated Stop control,
playing an instrument, leaving the page or backgrounding the app cancels
queued preview notes and stops the currently playing note. Never overlap
multiple notes from earlier replays. Preserve saved state, rewards, privacy,
Android compatibility, and original signed APK release requirements.

## Playlumi 0.46.0 - no-pressure next question
On unsolved standard learning rounds, show a quiet, child-friendly
"🍃 Andere vraag" button. It skips the displayed card without penalty,
reward, star, badge, score, success, failure or completed-session progress.
The existing 8-correct-answer session remains eight correct answers,
regardless of skipped questions. Never allow skipping a solved round.
After skipping, the newly drawn card has its own fresh hints and audio;
do not accidentally replay or speak skipped prompts. Preserve 300-card
shuffled offline decks and all profile-specific local data. Unit-test
skip and reward invariants, lint, compile, emulator start and signed APK.
No main-branch merge or store publication.

## Playlumi 0.47.0 - joyful jingles instead of human speech
The user explicitly wants music back and human narration removed. Do not
initialize TextToSpeech or expose spoken choice / question / voice settings.
Restore default on for decorative effects when no prior stored mute exists;
respect explicit effects_on=false and global sound_off.
Provide 12 new original musical motifs (24 total, including four fixed notes).
Vary successful answers, taps, dancing, unlocks and bubble pops; add themed
chimes for story, garden, pairs and session completion. The music demo in
parent settings and picture-long-press play only instrumental audio.
Preserve offline saves, scores, 300 question decks, Android support and
existing release certificate. Test and privately sign APK; do not merge main.

## Playlumi 0.48.0 - children's Muziekdoos
A new two-page kid-friendly music box offers all 12 original instrument-only
themes created in 0.47. Each large tile plays one deliberately selected
melody, without human TTS, accounts, stars or internet. Stop immediately cuts
off the selected sound; another tile interrupts the preceding one. Surprise
avoids immediately repeating the previously selected tune. Leaving the page,
rotating the phone or backgrounding the app cannot start music on its own.
Keep global sound mute authoritative; parent decorative-effects preference
does not block an explicitly selected musical tune. Preserve existing saved
game data and Android minSdk 23, with clean CI and signed APK.

## Playlumi 0.49.0 - child-composed personal melody
Within the existing Muziekdoos, children can tap four large animal instrument
buttons to record up to 12 notes, listen using an explicit playback button,
undo the last note or clear the tune. Automatically save notes as a compact
0-3 string in each child's existing per-profile local SharedPreferences.
Re-entering the music box or rotating the device must restore the composition,
without any automatic playback. A sequence uses the existing original
instrument sound bank and can be stopped, canceled by another musical choice,
or canceled by leaving the screen/backgrounding. No human TTS, network,
microphone, rewards or scoring. Keep original Android package and signing.

## Playlumi 0.50.0 - three separate original songs
Upgrade the 0.49 personal tune into an album of three independent locally saved
songs per child. The original song MUST retain its storage key
"music_box_composer" in slot 0; do not overwrite or migrate it destructively.
The two new slots use "music_box_composer_2" and "_3" within the existing
per-profile preference namespace. Switching slots cancels playing sounds but
never plays music automatically or gives rewards. Offer a child-controlled
three-step tempo for the currently played composition, persisted locally,
and a nearby explicit stop button. No human TTS, uploads, account,
microphone, new permission or third-party sound source. Keep Android minSdk 23,
original signer, app-ID, scores and parental controls unchanged.

## Playlumi 0.51.0 — musical pauses and rhythm ideas
Keep the three existing saved melodies and playback tempi intact. Extend the
per-profile composer note encoding with digit 4 for a silent beat ("🤫 Rust"),
while digits 0-3 retain their exact previous meaning and storage keys.
Recording or playing a rest must NOT play a note or a human speech sound.
Add four small, original four-step rhythm ideas with a silent beat; tapping
the button adds the next idea only when there is room for ALL four steps.
Never overwrite existing steps, clear another slot, or automatically replay
the pattern. Keep original WAV jingles and the global sound mute, and stop
queued sound on page changes and backgrounding. Continue verifying Android
unit tests, lint, emulator and original release-signing cert.

## Playlumi 0.52.0 — six extra original jingles
The child-controlled Muziekdoos grows from 12 to 18 short offline instrumental
melodies (three pages of six). The six newly added original WAV motifs are
Zonnestraal, Regenboog, Bosavontuur, Maanlicht, Konfettidans and Feesttrein.
Preserve tracks 0-11 in the same order and mapping, as well as existing 0-3
instrument notes and silent rest. Ensure next-page wording works on all three
pages, with a return to the first page on the last. Surprise must never repeat
the previous melody and must offer all 18 alternatives. Add all new jingles
to the deliberate parental sound sample, without automatically starting music
or reintroducing human narration. No scoring, ads, internet or new permissions.
Keep child profiles and original release signing identity.
