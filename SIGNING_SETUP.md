# Playlumi release signing (internal build instructions)

**Never check in a private keystore, keystore password, signing backup ZIP, or base64 encoded signing key.**

Current permanent Android application ID: `nl.playlumi.app`.

A permanent PKCS#12 keystore named `playlumi-release.p12` was generated on 2026-11-20.
Its public signing certificate SHA-256 fingerprint is:

`D2:B7:4F:01:8B:F6:11:13:82:F9:01:C2:AC:DC:03:E7:87:81:1C:E2:07:D6:C4:43:5F:CC:A9:55:9A:E9:B0:EF`

Private signing material was given to the user as an offline backup, **not committed** to GitHub. The signing key alias is `playlumi-release`. The store and key use the same secret password.

The repository owner must configure these two encrypted GitHub Actions repository secrets **once**:

- `PLAYLUMI_KEYSTORE_BASE64` — base64-encoded binary contents of the original permanent PKCS#12 keystore.
- `PLAYLUMI_SIGNING_PASSWORD` — password of that PKCS#12 keystore.

The `.github/workflows/android.yml` workflow runs tests and lint on every build. When both secrets are available on a non-PR run, it reconstructs the keystore **outside** the repository, signs and verifies `app-release.apk`, then uploads only the release APK. Its `versionCode` is automatically increased using `10000 + GITHUB_RUN_NUMBER` (do not manually reset it). Update `versionName` in `app/build.gradle` for user-facing releases.

All future Playlumi APKs must be signed by this **same** permanent key, preserving the `nl.playlumi.app` application ID and increasing `versionCode`. Otherwise Android will reject installation over an existing release.

Earlier debug APKs were signed with ephemeral CI debug keys and generally cannot upgrade in place to the permanent release. An **initial one-time uninstall** may be required, with potential loss of locally stored progress. Once on the permanent signed release, subsequent properly signed updates can install in place.

Do **not** hand users a debug APK marketed as an updateable release. Do not publish signing material in issues, workflow logs, source code, or artifact uploads.
