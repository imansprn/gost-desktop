#!/bin/bash
set -euo pipefail
cd "$(dirname "$0")/../.."
brand=assets/branding
magick "$brand/gost-mark-source.png" -trim +repage -resize 800x800 -gravity center -background none -extent 1024x1024 "$brand/gost-mark-1024.png"
cp "$brand/gost-mark-1024.png" composeApp/src/jvmMain/composeResources/drawable/gost_brand_mark.png
rsvg-convert -w 1024 -h 1024 "$brand/gost-icon-background.svg" -o "$brand/gost-app-icon-1024.png"
magick "$brand/gost-app-icon-1024.png" "$brand/gost-mark-1024.png" -gravity center -compose over -composite "$brand/gost-app-icon-1024.png"
cp "$brand/gost-app-icon-1024.png" composeApp/icons/gost.png
magick composeApp/icons/gost.png -define icon:auto-resize=256,128,64,48,32,16 composeApp/icons/gost.ico
iconset=$(mktemp -d /tmp/gost-icon.XXXXXX.iconset)
trap 'rm -rf "$iconset"' EXIT
for size in 16 32 128 256 512; do
  sips -z "$size" "$size" composeApp/icons/gost.png --out "$iconset/icon_${size}x${size}.png" >/dev/null
  double=$((size * 2))
  sips -z "$double" "$double" composeApp/icons/gost.png --out "$iconset/icon_${size}x${size}@2x.png" >/dev/null
done
iconutil -c icns "$iconset" -o composeApp/icons/gost.icns
