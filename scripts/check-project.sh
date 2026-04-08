#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

echo "[1/4] 检查关键文件"
for f in settings.gradle.kts build.gradle.kts app/build.gradle.kts app/src/main/AndroidManifest.xml; do
  [ -f "$f" ] || { echo "缺少 $f"; exit 1; }
done

echo "[2/4] 检查 Gradle Wrapper"
[ -f gradle/wrapper/gradle-wrapper.properties ] || { echo "缺少 gradle-wrapper.properties"; exit 1; }
[ -f gradlew ] || { echo "缺少 gradlew"; exit 1; }

echo "[3/4] 检查打包脚本"
[ -x scripts/build-debug.sh ] || { echo "缺少 build-debug.sh"; exit 1; }
[ -x scripts/build-release.sh ] || { echo "缺少 build-release.sh"; exit 1; }

echo "[4/4] 检查运行时目录"
for d in app/src/main/assets/singbox app/src/main/jniLibs/arm64-v8a app/src/main/jniLibs/armeabi-v7a app/src/main/jniLibs/x86_64; do
  [ -d "$d" ] || { echo "缺少目录 $d"; exit 1; }
done

echo "检查通过：项目已具备后续直接打包的基础结构。"
