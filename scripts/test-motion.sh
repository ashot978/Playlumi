#!/usr/bin/env bash
set -euo pipefail
root="$(cd "$(dirname "$0")/.." && pwd)"
build_dir="$(mktemp -d)"
trap 'rm -rf "$build_dir"' EXIT
javac -encoding UTF-8 -d "$build_dir" \
  "$root/motion-core/src/main/java/nl/playlumi/motion/MotionEffects.java" \
  "$root/motion-core/src/main/java/nl/playlumi/motion/ShapeMagnet.java" \
  "$root/motion-core/src/test/java/nl/playlumi/motion/MotionEffectsTest.java" \
  "$root/motion-core/src/test/java/nl/playlumi/motion/ShapeMagnetTest.java"
java -cp "$build_dir" nl.playlumi.motion.MotionEffectsTest
java -cp "$build_dir" nl.playlumi.motion.ShapeMagnetTest
