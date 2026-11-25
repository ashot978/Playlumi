#!/usr/bin/env bash
# Playlumi: sign a CI-produced unsigned release APK with the ORIGINAL private key.
# Credentials and keystore NEVER enter GitHub/CI or appear in command arguments.
set -euo pipefail
if [[ $# -ne 3 ]]; then
  echo "Usage: PLAYLUMI_KEYSTORE=/private/release.p12 PLAYLUMI_SIGNING_PASSWORD=*** bash scripts/sign-release.sh unsigned.apk final.apk /path/to/android-build-tools"
  exit 2
fi
: "${PLAYLUMI_KEYSTORE:?Set PLAYLUMI_KEYSTORE}"
: "${PLAYLUMI_SIGNING_PASSWORD:?Set PLAYLUMI_SIGNING_PASSWORD}"
UNSIGNED=$1
FINAL=$2
TOOLS=$3
EXPECTED=d2b74f018bf6111382f901c2acdc03e787811ce207d6c4435fcca9559ae9b0ef
[[ -s "$UNSIGNED" ]] || { echo "Unsigned APK is missing"; exit 1; }
[[ -s "$PLAYLUMI_KEYSTORE" ]] || { echo "Private keystore is missing"; exit 1; }
[[ -x "$TOOLS/apksigner" ]] || { echo "Official apksigner not available"; exit 1; }
keytool -list -v -storetype PKCS12 -keystore "$PLAYLUMI_KEYSTORE" \
    -storepass:env PLAYLUMI_SIGNING_PASSWORD > /tmp/playlumi-release-certinfo.txt
fingerprint=$(grep 'SHA256:' /tmp/playlumi-release-certinfo.txt | head -1 | sed 's/.*SHA256: //' | tr -d ':' | tr '[:upper:]' '[:lower:]')
rm -f /tmp/playlumi-release-certinfo.txt
[[ "$fingerprint" == "$EXPECTED" ]] || { echo "WRONG PRIVATE KEY; release blocked"; exit 1; }
TEMP=$(mktemp --suffix=.apk)
trap 'rm -f "$TEMP"' EXIT
python3 "$(dirname "$0")/check-zipalignment.py" "$UNSIGNED"
cp "$UNSIGNED" "$TEMP"
"$TOOLS/apksigner" sign \
    --ks "$PLAYLUMI_KEYSTORE" \
    --ks-type PKCS12 \
    --ks-key-alias playlumi-release \
    --ks-pass env:PLAYLUMI_SIGNING_PASSWORD \
    --key-pass env:PLAYLUMI_SIGNING_PASSWORD \
    --min-sdk-version 23 \
    --out "$FINAL" "$TEMP"
"$TOOLS/apksigner" verify --verbose --print-certs --min-sdk-version 23 "$FINAL" | tee "${FINAL}.verification.txt"
grep -i "Signer #1 certificate SHA-256 digest: $EXPECTED" "${FINAL}.verification.txt" || {
    echo "Final APK certificate mismatch"; exit 1;
}
python3 "$(dirname "$0")/check-zipalignment.py" "$FINAL"
sha256sum "$FINAL" | tee "${FINAL}.sha256"
echo "Validated Playlumi release: $FINAL"
