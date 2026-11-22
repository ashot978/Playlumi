# Playlumi release and download gate

This checklist is a standing project rule for **every future version**.

## Never advertise a download before approval

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

## Normal development

Continue from the latest verified development branch, one focused version increment at a time. Add tests and open a draft PR against the previous development branch. Review GitHub Actions status before declaring completion. Describe clearly what is coded vs. what is CI-tested vs. what is device-approved.
