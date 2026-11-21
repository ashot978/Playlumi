# Playlumi release signing — agent-maintained

Playlumi is built on GitHub, and its signed release APK is prepared privately by the development assistant. **The app owner supplies product requirements, not CI configuration. No manual GitHub Secrets setup is needed.**

## Stable application identity

- Android application ID: `nl.playlumi.app`
- Permanent signing certificate SHA-256:
  `D2:B7:4F:01:8B:F6:11:13:82:F9:01:C2:AC:DC:03:E7:87:81:1C:E2:07:D6:C4:43:5F:CC:A9:55:9A:E9:B0:EF`
- PKCS#12 key alias: `playlumi-release`
- Keystore creation: November 20, 2026
- Version code: `13000 + GITHUB_RUN_NUMBER` from the Android workflow. Always increase with every release.

## Normal unattended release process

1. Commit app changes to the `playlumi/eerste-telspel` branch; CI runs Android unit tests, lint and builds `app-release-unsigned.apk`.
2. Download the `playlumi-unsigned-release-apk` build artifact. The artifact is safe to share.
3. Retrieve the original private Playlumi signing backup from the user's previous conversation attachment `Playlumi-release-sleutel-BEWAAR-PRIVE.zip`. The backup contains `playlumi-release.p12` and a password file. **Do not store or copy either into this public repository, job logs, issues, or artifacts.**
4. Sign locally, outside GitHub, using `tools/sign-release.py` and the private backup:
   `python tools/sign-release.py app-release-unsigned.apk private-key.p12 private-password.txt Playlumi-release.apk`.
5. Verify that the signed APK certificate fingerprint is **identical** to the permanent fingerprint above. Verify ZIP integrity, v1/v2 APK signatures, application ID, and higher versionCode. Never distribute an unverified APK.
6. Deliver a directly downloadable signed APK to the user in the chat. **Do not give a GitHub link as the app download.** The user does not need to operate GitHub, handle secrets, or manage technical signing.

The permanent key backup belongs to the user's private conversation and must remain accessible for future releases. If it becomes unavailable, **do not generate a new key silently**: a new key would break in-place updates.

## Migration from early debug APKs

Early 0.3.0–0.9.0 APK builds used temporary CI debug keys. Android generally will not allow those APKs to be upgraded in-place to the permanent release. A one-time uninstall/reinstall can be required, potentially losing locally stored game progress. Once the app is installed using the permanent release signer, subsequent correctly signed releases with increasing version codes install over it.

The permanent private key is never committed, and GitHub Actions intentionally uploads an **unsigned** release APK for private signing, rather than requiring the user to configure repository secrets.
