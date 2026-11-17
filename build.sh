#!/bin/bash
# Build Screen Margin (no-root screen margins) into out/ScreenMargin.apk.
# Needs: JDK (javac, keytool), Android SDK (build-tools 36, platform 34).
# Every step runs under cpulimit 60.
set -euo pipefail
export MSYS_NO_PATHCONV=1

HERE="$(cd "$(dirname "$0")" && pwd -W)"
SDK="${ANDROID_HOME:-C:/android_sdk}"
BT="$SDK/build-tools/36.0.0"
JAR="$SDK/platforms/android-34/android.jar"
LIB="$HERE/libs/hiddenapibypass-6.2.jar"
KS="$HERE/local.keystore"
OUT="$HERE/out"
LIMIT="cpulimit 60"

rm -rf "$HERE/cls"; mkdir -p "$OUT"; rm -rf "$OUT"/* "$OUT"/.[!.]* 2>/dev/null || true
mkdir -p "$OUT" "$HERE/cls"
if [ ! -f "$KS" ]; then
  keytool -genkeypair -keystore "$KS" -storepass android -keypass android -alias local \
    -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=local" >/dev/null
fi

$LIMIT "$BT/aapt2.exe" compile --dir "$HERE/res" -o "$OUT/res.zip"
$LIMIT "$BT/aapt2.exe" link -I "$JAR" --manifest "$HERE/AndroidManifest.xml" \
  --min-sdk-version 26 --target-sdk-version 34 -o "$OUT/unsigned.apk" "$OUT/res.zip"
$LIMIT javac -nowarn --release 11 -cp "$JAR;$LIB" -d "$HERE/cls" $(find "$HERE/src" -name '*.java')
$LIMIT "$BT/d8.bat" --release --min-api 26 --lib "$JAR" --output "$OUT" $(find "$HERE/cls" -name "*.class") "$LIB"
(cd "$OUT" && $LIMIT "$BT/aapt.exe" add unsigned.apk classes.dex >/dev/null)
$LIMIT "$BT/zipalign.exe" -f 4 "$OUT/unsigned.apk" "$OUT/aligned.apk"
$LIMIT "$BT/apksigner.bat" sign --ks "$KS" --ks-pass pass:android --ks-key-alias local \
  --out "$OUT/ScreenMargin.apk" "$OUT/aligned.apk"
rm -rf "$HERE/cls"
# keep only the finished app (also drops the temp files aapt leaves behind)
find "$OUT" -mindepth 1 ! -name ScreenMargin.apk -delete

# release copy: versioned name + checksum, ready to attach to a GitHub release
VER="$(grep -o 'android:versionName="[^"]*"' "$HERE/AndroidManifest.xml" | cut -d'"' -f2)"
mkdir -p "$HERE/release"
cp "$OUT/ScreenMargin.apk" "$HERE/release/ScreenMargin-v$VER.apk"
(cd "$HERE/release" && sha256sum "ScreenMargin-v$VER.apk" > SHA256SUMS.txt)
"$BT/apksigner.bat" verify --print-certs "$HERE/release/ScreenMargin-v$VER.apk" | head -3
echo "Release: release/ScreenMargin-v$VER.apk"
echo "Built: out/ScreenMargin.apk"
