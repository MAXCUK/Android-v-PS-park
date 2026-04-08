#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

if [ ! -f local.properties ]; then
  echo "缺少 local.properties，请先参考 local.properties.example 创建。" >&2
  exit 1
fi

if [ -f keystore.properties ]; then
  echo "检测到 keystore.properties，将尝试生成已签名 Release 包。"
else
  echo "未检测到 keystore.properties，将生成未签名或默认 release 包。"
  echo "如需正式签名，请复制 keystore.properties.example -> keystore.properties 并填写。"
fi

./gradlew assembleRelease

echo
printf 'Release APK 输出目录：%s\n' "$(pwd)/app/build/outputs/apk/release"
