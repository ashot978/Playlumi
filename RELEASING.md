# Playlumi release contract (from 0.73.0)

This is a blocking procedure, not a suggestion. No release should be described as
working until the technical and visual gates below pass.

## Stable identity and source
- Package/application ID: `nl.playlumi.app`.
- Playlumi 0.73.0: `versionName=0.73.0`, `versionCode=600730`.
- Original certificate SHA-256:
  `d2b74f018bf6111382f901c2acdc03e787811ce207d6c4435fcca9559ae9b0ef`.
- Private release signing material MUST stay outside GitHub/CI/repo.
- Release must come from a recorded commit SHA and successful Android workflow.

## Real illustration is mandatory
The approved 941x1672 image is in ten text-safe, versioned parts in
`app/src/main/village-art/`. Gradle reconstructs
`app/build/generated/playlumi-village-assets/playlumi_village.webp`.
The artwork SHA-256 must equal
`bc39c748fb82a101e0280d16d6d3de1cd3e44d39453f87bb6d30870e155f5804`.
The release pipeline validates the actual WebP inside BOTH APK variants.
Do not substitute a flat illustration or an empty screen.

## CI build
1. Create a feature branch from the latest accepted app branch.
2. Implement art/code/tests in commits; open a pull request.
3. Run GitHub Actions `.github/workflows/android.yml` (JDK17, Gradle8.9, AGP8.7.3, SDK35).
4. Require successful `testDebugUnitTest lintDebug assembleDebug assembleRelease`.
5. Require APK package/versionCode/art SHA-256 checks.
6. Require emulator install, startup, screenshot, game navigation without crash.
7. Download `playlumi-073-unsigned-release` and (if necessary) Android build-tools artifact.

## Private offline signing
Read the original PKCS12 keystore from the private key archive. Never upload its
contents or password to GitHub, logs or release artifacts.
Check Gradle's existing APK ZIP alignment with the portable checker, then use official Android SDK apksigner (v1 and v2 compatible from minSdk 23),
through `scripts/sign-release.sh`. No private key should enter any Git commit.
Compare the certificate fingerprint against the fixed original fingerprint.

## Device and visual acceptance
- Install over last signed Playlumi release; preserve saved child profiles and stars.
- View real screenshot on target Samsung and a wide/foldable emulator.
- Compare against the approved detailed isometric village, not a simplified drawing.
- Six interactive buildings must route to functioning existing games.
- Verify settings/parental controls, five bottom tabs, return navigation, reduced motion.
- No overlap, clipped labels, blank whitespace, crash, fake progress or missing assets.
- A build's success does NOT establish complete UI acceptance.

## Packaging
- Produce APK and its SHA-256/checks, record commit, GitHub run, cert and version.
- Keep source asset, source hash and signed file accessible for auditing.
- Mark any unverified device acceptance as unverified, never silently PASS.
- Tag/release only after acceptance; never report a debug APK as a signed update.

## Updating for 0.74 and beyond
Increase BOTH versionName and the explicitly reserved versionCode; verify it is
above the highest installed/published release versionCode. Update workflow checks,
release notes and immutable asset hash if art changes. Never replace the original
signing key to work around installation failures.
