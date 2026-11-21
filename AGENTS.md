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
