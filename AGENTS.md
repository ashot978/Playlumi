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
