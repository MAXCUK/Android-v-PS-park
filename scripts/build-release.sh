#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

if [ ! -f local.properties ]; then
  echo "缺少 local.properties，请先参考 local.properties.example 创建。" >&2
  exit 1
fi

./gradlew assembleRelease

echo
printf 'Release APK 输出目录：%s\n' "$(pwd)/app/build/outputs/apk/release"
