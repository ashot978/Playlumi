# Playlumi Magisch Dorp (0.71.0)

This release replaces the obsolete floating-island home screen with an illustrated game village based on the user's generated artwork ("Magisch Playlumi-leerdorp.png").

## Implementation
- `PlaylumiVillageView.java` renders the complete image from `assets/playlumi_village.webp`, sized proportionally for portrait phones and foldables and vertically scrollable.
- Buildings and interface locations have pixel-coordinate touch regions: Rekenraket (existing counting game), Letterhuis (language world), Vormkasteel (drag shapes), Muziekstudio, Speelplein, Kleurtuin (color game), center Stervriend, Ouderzone, and five bottom navigation destinations.
- Interaction highlight + very subtle animated stars; existing gameplay, profile data, local progress and parental tools are unchanged.
- The old `PlaylumiStarWorld.java` home is removed.

## Reproducible release packaging
The original large illustration is in the user's conversation/file library, not checked into GitHub. The GitHub Actions unsigned release APK therefore **must not be distributed without inserting the art asset**. To package a release:
1. Export or obtain the user's full 941 x 1672 artwork and convert it to a 941 x 1672 WEBP image named `playlumi_village.webp` (quality 85).
2. Build the unsigned release APK via GitHub Actions, then append `assets/playlumi_village.webp` to the unsigned APK with ZIP tooling, preserving alignment of existing uncompressed resource entries.
3. Sign the modified APK using APK Signature Scheme v2 with the original private Playlumi release key **offline**; do not place private keys in this repository or Actions.
4. Verify APK content digest, signature and signer certificate against original Playlumi SHA-256: `D2:B7:4F:01:8B:F6:11:13:82:F9:01:C2:AC:DC:03:E7:87:81:1C:E2:07:D6:C4:43:5F:CC:A9:55:9A:E9:B0:EF`.
5. Verify release APK manifest package `nl.playlumi.app`, versionName `0.71.0`, versionCode > 200547 (this release's code is 300558), WEBP presence and ZIP integrity.

The background's sample star/coin/heart counters are part of the original concept illustration and are **not yet wired to live counters**. Do not promise these are functioning economy systems. The screen is an interactive illustrated world, not a 3D town simulator.

## Validation
GitHub CI workflow run 45604805679 succeeded for source commit 3937965fadfc4c84c13cec7d94e783e468f720f8.
Locally packaged release v0.71.0: APK SHA-256 `b92cd5b78d1257b2cb70e99e793a52498025738d7e901e72299d6b15e9d28507`, v2 signature cryptographically checked and matched prior release signer.
Physical Samsung install and full game play-through remain untested.
