# Playlumi release and download gate

This checklist is a standing project rule for **every future version**.

## Never advertise a download before approval

For updates over the existing v0.45 release-signed installation, offer **only** `playlumi-original-signed-release-update` signed with the verified original certificate (SHA-256 D2:B7:4F:01:8B:F6:11:13:82:F9:01:C2:AC:DC:03:E7:87:81:1C:E2:07:D6:C4:43:5F:CC:A9:55:9A:E9:B0:EF). Never offer a debug-signed APK as an update.

Only show an APK download to the user after **all** applicable checks have completed successfully and the exact artifact has been verified:

1. Unit tests pass.
2. Android lint passes.
3. Debug build and release build succeed.
4. Emulator launch smoke test passes.
5. The expected APK artifact is present, not expired, and is the output of the successful run for the exact commit/version.
6. Installation target and signing certificate are clear. A debug-signed APK is for a **separate test install**, not an update over an existing release-signed installation.
7. Before calling an APK a production-ready update, verify the **original release signing identity** and versionCode compatibility, and complete the required real-device acceptance test (including Samsung).

If any check is pending or missing, report the version as *in development* or *awaiting verification*, **without offering an APK download**. Do not describe a GitHub Actions URL or an unsigned release artifact as an approved installable download.

Never reveal, commit, transmit, regenerate or replace the original private signing key. Keep existing installation data safe. Do not change `main` or Play Store unless explicitly requested.

## Mandatory preflight and failure recovery for all agents

- Before editing any feature, inspect the latest GitHub branch, PR, exact HEAD commit, workflow, signing configuration and the most recent run. Never assume that a prior successful run certifies a newer commit.
- Do not add another feature on top of a broken release pipeline: repair and verify the previous blocking failure first.
- **Explicit release CI (`workflow_dispatch`) must fail**, not return green, when the original-signing secrets are absent/incomplete, the fingerprint mismatches or the signed release APK cannot be published. Ordinary PR/push CI validates tests, lint, emulator, screenshots and unsigned packaging independently; its green status does **not** authorize an APK download.
- On deterministic failures, inspect logs, fix code and tests and push a new commit before rerunning. Retry transient infrastructure failures only after identifying their nature. Never endlessly rerun missing secrets or a wrong signature.
- Keep inspecting GitHub jobs and artifacts until the **entire exact-SHA workflow** is completed and successful. Require an original-certificate-signed APK with the package, fingerprint and update versionCode verified before offering any download.
- Download the real, nonexpired signed APK from that exact run and offer it directly in the conversation only after verification; do not substitute debug/unsigned builds.
- Real-device acceptance with preservation of local profile data is a separate requirement; a CI success does not prove real-device update success.
- If a hard external blocker remains (for example, unavailable original signing key), report it truthfully and do not claim successful delivery or promise continuous work outside the active session.
- The original keystore and password may only be configured through secure GitHub Actions repository secrets; never include them in PRs, logs, chats, or code.

## Recovered signing archive and local emergency release path

- Original v0.45 private signing backup is in the owner's **private** ChatGPT File Library: `/Playlumi-release-sleutel-BEWAAR-PRIVE.zip`. It contains the PKCS12 keystore and password in private files. Recover via File Library tools; **never** transfer them to a public GitHub branch, issue, pull request, log, chat response, or sandbox link.
- The original keystore in this archive was verified to match the SHA-256 fingerprint above on 2026-11-22. The extra `/Playlumi/Private signing/playlumi-release-2026-11-22.p12` did **not** unlock with that password; prefer the verified archive.
- Where GitHub release-signing Secrets cannot be administered through connected tools, do not claim the CI release gate is green. Use the latest exactly identified *already successful* CI-built unsigned APK only for a **separately verified local signing** with the original key. A direct signed APK is acceptable as an explicitly labeled locally checked update, but is not a claim of fully green current GitHub release CI or tested Samsung installation.
- Verify original v0.45 cert, Android v1 and v2 signing, unmodified package `nl.playlumi.app`, higher versionCode and file integrity before presenting that APK. Prefer official Android build-tools `apksigner` and `aapt`; if they are unavailable, do not claim they were run.
- No new keystore, no replacement certificate, no lost profile data, no installation/uninstallation on the user's device without authorization.

## Real device installation gate: ZIP alignment

An actual clean installation failed for a locally signed 0.61.0 APK. Diagnosis found improperly aligned uncompressed resources in the final APK: both `resources.arsc` and `res/J7.webp` started at byte offset 1 modulo 4. The CI-produced unsigned APK had correctly aligned `resources.arsc`. This is a packaging/signing-time defect, not a missing certificate after uninstall.

- Follow official Android SDK process: `zipalign` first, original-v0.45-key `apksigner sign` second, `zipalign -c -v 4` and `apksigner verify --min-sdk-version 23 --verbose --print-certs` on the **final signed APK**.
- Never rely on generic ZIP rewriting or homemade APK v2 signing for install-ready releases.
- Validate every stored ZIP member's data offset, particularly `resources.arsc`; CRC and cryptographic checks are additional but not sufficient for Android install acceptance.
- Confirm on an actual Android device before asserting a fixed install. If impossible, label release as locally checked and awaiting device acceptance.

## Normal development

Continue from the latest verified development branch, one focused version increment at a time. Add tests and open a draft PR against the previous development branch. Review GitHub Actions status before declaring completion. Describe clearly what is coded vs. what is CI-tested vs. what is device-approved.
