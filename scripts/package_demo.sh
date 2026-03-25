#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
DIST_DIR="$ROOT_DIR/dist/demo"
BUILD_DIR="$ROOT_DIR/build_classes"
SOURCES_FILE="$ROOT_DIR/build_sources.txt"
WEB_ZIP_TARGET="${1:-$ROOT_DIR/../Web Page/www/assets/downloads/demo.zip}"

rm -rf "$DIST_DIR"
mkdir -p "$DIST_DIR/Swap"

find "$ROOT_DIR/Swap/src" -name '*.java' | sort > "$SOURCES_FILE"
javac --release 17 -d "$BUILD_DIR" @"$SOURCES_FILE"

cp -r "$BUILD_DIR" "$DIST_DIR/"
cp -r "$ROOT_DIR/Swap/res" "$DIST_DIR/Swap/"
cp "$ROOT_DIR/README.md" "$DIST_DIR/README.md"

cat > "$DIST_DIR/run.sh" <<'EOF'
#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
java -cp build_classes:Swap/res app.Main
EOF

cat > "$DIST_DIR/run.bat" <<'EOF'
@echo off
cd /d %~dp0
java -cp "build_classes;Swap/res" app.Main
pause
EOF

chmod +x "$DIST_DIR/run.sh"

rm -f "$WEB_ZIP_TARGET"
(
  cd "$ROOT_DIR/dist"
  zip -qr "$WEB_ZIP_TARGET" demo
)

echo "Demo package created at: $WEB_ZIP_TARGET"
