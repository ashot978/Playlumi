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
