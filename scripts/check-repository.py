#!/usr/bin/env python3
"""Guard against the accident that caused Playlumi 0.25.3 to crash."""
import pathlib
import subprocess
import sys

root = pathlib.Path(__file__).resolve().parents[1]
result = subprocess.run(["git", "-C", str(root), "ls-files"], check=True,
                        capture_output=True, text=True)
for name in result.stdout.splitlines():
    base = pathlib.PurePosixPath(name).name.lower()
    if (base.endswith((".apk", ".aab", ".dex", ".p12", ".jks", ".keystore", ".key")) or
            "password" in base or "keystore" in base or "signing-key" in base):
        sys.exit("BLOCKED: private/binary application material tracked: " + name)
expected = ["docs/DEVELOPMENT_LOOP.md", "docs/ANIMATION_BACKLOG.md",
            "docs/INTEGRATION.md", "docs/BASELINE.md",
            "motion-core/src/main/java/nl/playlumi/motion/MotionEffects.java"]
for name in expected:
    if not (root / name).is_file():
        sys.exit("BLOCKED: mandatory file missing: " + name)
print("PASS: secret/binary exclusion and mandatory development documentation")
print("NOTE: this is NOT an Android app launch test or APK release approval.")
