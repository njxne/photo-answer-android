#!/bin/bash
# 拍照答题 —— Gradle + AGP 构建（界面用 Material Components）
# 用法: bash build.sh [gradle 任务]   默认 assembleRelease
set -e
cd "$(dirname "$0")"

export ANDROID_HOME=${ANDROID_HOME:-/root/android/sdk}

# 本机是 aarch64，AGP 自带的 aapt2 是 x86_64 跑不了；
# SDK 的 build-tools/35.0.0 里有一个 aarch64 静态版，指过去即可。
AAPT2_OVERRIDE=${AAPT2_OVERRIDE:-/root/android/sdk/build-tools/35.0.0/aapt2}

GRADLE=${GRADLE:-}
if [ -z "$GRADLE" ]; then
    if [ -x ./gradlew ]; then
        GRADLE=./gradlew
    else
        # 优先用 AGP 8.5 官方支持的 8.7，找不到再退到最新版
        GRADLE=$(ls -d /root/.gradle/wrapper/dists/gradle-8.7-bin/*/gradle-8.7/bin/gradle 2>/dev/null | head -1)
        if [ -z "$GRADLE" ]; then
            GRADLE=$(ls -d /root/.gradle/wrapper/dists/gradle-*/*/gradle-*/bin/gradle 2>/dev/null \
                | sort -V | tail -1)
        fi
    fi
fi
if [ -z "$GRADLE" ]; then
    echo "找不到 gradle，可用 GRADLE=/path/to/gradle 指定" >&2
    exit 1
fi

TASK=${1:-assembleRelease}
EXTRA=""
if [ -x "$AAPT2_OVERRIDE" ]; then
    EXTRA="-Pandroid.aapt2FromMavenOverride=$AAPT2_OVERRIDE"
fi

echo "gradle: $GRADLE"
echo "任务  : $TASK"
"$GRADLE" $EXTRA "$TASK" --no-daemon --console=plain

echo ""
echo "APK:"
ls -la app/build/outputs/apk/release/ 2>/dev/null || true
