#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "$0")/../../.." && pwd)"
ASSET_DIR="$ROOT_DIR/app/src/main/assets/singbox"
mkdir -p "$ASSET_DIR"

# Prefer db names expected by current app assets layout.
GEOIP_URL="https://github.com/SagerNet/sing-geoip/releases/latest/download/geoip.db"
GEOSITE_URL="https://github.com/SagerNet/sing-geosite/releases/latest/download/geosite.db"

curl -L --fail --retry 3 -o "$ASSET_DIR/geoip.db" "$GEOIP_URL"
curl -L --fail --retry 3 -o "$ASSET_DIR/geosite.db" "$GEOSITE_URL"

echo "Prepared singbox assets:"
ls -lh "$ASSET_DIR"
