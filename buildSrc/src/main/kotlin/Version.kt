object Version {
    // App Version
    const val MAJOR = 1
    const val MINOR = 0
    const val PATCH = 0
    const val BUILD = 1
    
    const val NAME = "$MAJOR.$MINOR.$PATCH-mvp"
    const val CODE = MAJOR * 10000 + MINOR * 100 + PATCH * 10 + BUILD
    
    // SDK Versions
    const val COMPILE_SDK = 34
    const val MIN_SDK = 28
    const val TARGET_SDK = 34
    
    // Version History
    const val CHANGELOG = """
        v1.0.0-mvp (Build 1)
        - ✨ EPUB图书管理和阅读功能
        - 🤖 AI智能问答（需配置OpenAI API Key）
        - 🎤 语音输入支持
        - 🖥️ 墨水屏显示优化
        - 📱 支持Android 9.0+设备
        - �� 完全本地数据存储
    """
} 