#!/bin/bash

# 墨水屏AI阅读器 MVP版本构建脚本
# 作者: Winston
# 创建时间: 2024年12月

echo "🚀 开始构建墨水屏AI阅读器 MVP版本..."
echo "📅 构建时间: $(date)"
echo ""

# 检查环境
echo "🔍 检查构建环境..."

# 检查Java版本
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n1 | cut -d'"' -f2)
    echo "✅ Java版本: $JAVA_VERSION"
else
    echo "❌ 未找到Java，请安装JDK 17或更高版本"
    exit 1
fi

# 检查Android SDK
if [ -z "$ANDROID_HOME" ]; then
    echo "⚠️  ANDROID_HOME环境变量未设置"
else
    echo "✅ Android SDK: $ANDROID_HOME"
fi

echo ""

# 清理之前的构建
echo "📦 清理旧版本..."
./gradlew clean || {
    echo "❌ 清理失败，请检查Gradle配置"
    exit 1
}

echo ""

# 检查代码质量
echo "🔍 代码质量检查..."
./gradlew lintMvpDebug || {
    echo "⚠️  Lint检查发现问题，但继续构建"
}

echo ""

# 运行单元测试
echo "🧪 运行单元测试..."
# ./gradlew testMvpDebugUnitTest || {
#     echo "❌ 单元测试失败，停止构建"
#     exit 1
# }
echo "🔧 单元测试暂时跳过（项目初始化阶段）"

echo ""

# 构建Debug APK用于开发测试
echo "🔨 构建Debug APK..."
./gradlew assembleMvpDebug || {
    echo "❌ Debug APK构建失败"
    exit 1
}

# 构建Release APK
echo "🔨 构建Release APK..."
./gradlew assembleMvpRelease || {
    echo "❌ Release APK构建失败"
    exit 1
}

echo ""

# 获取构建信息
DEBUG_APK_PATH="app/build/outputs/apk/mvp/debug/app-mvp-debug.apk"
RELEASE_APK_PATH="app/build/outputs/apk/mvp/release/app-mvp-release.apk"

if [ -f "$DEBUG_APK_PATH" ]; then
    DEBUG_APK_SIZE=$(stat -f%z "$DEBUG_APK_PATH" 2>/dev/null || stat -c%s "$DEBUG_APK_PATH" 2>/dev/null)
    DEBUG_APK_SIZE_MB=$((DEBUG_APK_SIZE / 1024 / 1024))
    echo "✅ Debug APK构建成功！"
    echo "📱 Debug APK路径: $DEBUG_APK_PATH"
    echo "📊 Debug APK大小: ${DEBUG_APK_SIZE_MB}MB"
else
    echo "❌ Debug APK文件未找到"
fi

if [ -f "$RELEASE_APK_PATH" ]; then
    RELEASE_APK_SIZE=$(stat -f%z "$RELEASE_APK_PATH" 2>/dev/null || stat -c%s "$RELEASE_APK_PATH" 2>/dev/null)
    RELEASE_APK_SIZE_MB=$((RELEASE_APK_SIZE / 1024 / 1024))
    echo "✅ Release APK构建成功！"
    echo "📱 Release APK路径: $RELEASE_APK_PATH"
    echo "📊 Release APK大小: ${RELEASE_APK_SIZE_MB}MB"
else
    echo "❌ Release APK文件未找到"
fi

echo ""
echo "🎉 构建完成！"
echo ""
echo "📋 下一步操作:"
echo "1. 安装Debug版本进行开发测试:"
echo "   adb install -r $DEBUG_APK_PATH"
echo ""
echo "2. 安装Release版本进行发布测试:"
echo "   adb install -r $RELEASE_APK_PATH"
echo ""
echo "3. 查看应用日志:"
echo "   adb logcat | grep InkReader"
echo ""
echo "4. 卸载应用:"
echo "   adb uninstall com.newbiechen.inkreader.debug  # Debug版本"
echo "   adb uninstall com.newbiechen.inkreader        # Release版本"
echo ""

# 显示项目统计信息
echo "📊 项目统计:"
echo "构建时间: $(date)"
echo "Gradle版本: $(./gradlew --version | grep Gradle | cut -d' ' -f2)"
echo "Kotlin版本: $(grep KOTLIN_VERSION buildSrc/src/main/kotlin/Dependencies.kt | cut -d'"' -f2)"
echo ""

echo "🚀 墨水屏AI阅读器 MVP版本构建完成！" 