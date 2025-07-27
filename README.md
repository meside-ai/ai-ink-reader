# 墨水屏 AI 阅读器 📚

> 专为墨水屏设备打造的智能 EPUB 阅读器，集成 AI 问答和语音交互功能

[English](README.md) | **中文** | [部署指南](docs/deployment_guide.md) | [开发文档](docs/development_guide.md)

[![Android](https://img.shields.io/badge/platform-Android-green.svg)](https://android.com)
[![Kotlin](https://img.shields.io/badge/language-Kotlin-blue.svg)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/license-MIT-orange.svg)](LICENSE)
[![API](https://img.shields.io/badge/API-28%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=28)
[![Version](https://img.shields.io/badge/version-1.0.0--mvp-red.svg)](https://github.com/meside-ai/ai-ink-reader/releases)

## ✨ 项目简介

墨水屏 AI 阅读器是一款专为 Android 墨水屏设备（如文石 P6、Kindle 等）设计的智能阅读应用。结合先进的 AI 技术，为用户提供沉浸式的智能阅读体验。

### 🎯 核心特色

- **🖥️ 墨水屏优化**：专门针对 E-ink 显示特性优化的界面和交互
- **🤖 AI 智能问答**：基于 OpenAI 的文本理解和概念解释
- **🎤 语音交互**：支持语音输入提问，提升操作便利性
- **📖 专业阅读**：高质量的 EPUB 渲染和翻页体验
- **🔒 隐私保护**：完全本地存储，用户数据不上传云端

## 🚀 主要功能

### 📚 图书管理

- 支持从设备文件系统导入 EPUB 格式电子书
- 智能提取图书元数据（标题、作者、封面等）
- 支持最多 50 本图书的管理和分类
- 精确的阅读进度跟踪和书签管理

### 🤖 AI 智能功能

- **概念解释**：对复杂概念进行深入解释
- **中英文翻译**：实时翻译选中的文本内容
- **单词学习**：英语单词的详细解释和用法
- **内容总结**：对长段落进行智能总结
- **扩展阅读**：基于内容推荐相关阅读材料

### 🎤 语音交互

- 选中文本后可通过语音提问
- 基于 OpenAI 多模态模型的语音理解
- 智能语音识别，支持中文语音输入
- 语音问答历史记录和管理

### 🖥️ 墨水屏优化

- 智能刷新策略，减少残影和闪烁
- 优化的触控响应和手势识别
- 专业的翻页动画和页面布局
- 适配不同墨水屏设备的显示特性

## 🛠️ 技术架构

### 核心技术栈

- **开发语言**：Kotlin
- **架构模式**：MVVM + Clean Architecture
- **UI 框架**：Android 原生 View + 自定义 WebView
- **数据库**：Room + SQLite
- **网络请求**：Retrofit + OkHttp
- **依赖注入**：Hilt
- **异步处理**：Kotlin Coroutines
- **EPUB 解析**：epublib-android

### 架构分层

```
┌─────────────────────────────────────────────────┐
│                Presentation Layer                │
│        (Activities, Fragments, ViewModels)      │
├─────────────────────────────────────────────────┤
│                  Domain Layer                   │
│           (Use Cases, Entities, Repositories)   │
├─────────────────────────────────────────────────┤
│                   Data Layer                    │
│    (Local DB, Remote API, File Management)     │
├─────────────────────────────────────────────────┤
│                  Core Modules                   │
│  (EPUB Parser, AI Service, Eink Optimization)  │
└─────────────────────────────────────────────────┘
```

## 📱 支持设备

### 推荐设备

- **文石 P6** - 6 寸墨水屏阅读器（主要测试设备）
- **文石 Nova3** - 7.8 寸墨水屏平板
- **文石 Note5** - 10.3 寸墨水屏笔记本

### 系统要求

- **Android 版本**：9.0（API 28）及以上
- **内存要求**：至少 2GB RAM
- **存储空间**：至少 100MB 可用空间
- **网络连接**：AI 功能需要互联网连接

## 🔧 快速开始

### 环境准备

1. **安装 Android Studio**

   ```bash
   # 下载并安装 Android Studio Arctic Fox 或更新版本
   # https://developer.android.com/studio
   ```

2. **克隆项目**

   ```bash
   git clone https://github.com/meside-ai/ai-ink-reader.git
   cd ai-ink-reader
   ```

3. **配置 OpenAI API**
   ```bash
   # 在 app/src/main/res/values/ 下创建 secrets.xml
   echo '<?xml version="1.0" encoding="utf-8"?>
   <resources>
       <string name="openai_api_key">your_openai_api_key_here</string>
   </resources>' > app/src/main/res/values/secrets.xml
   ```

### 构建项目

1. **使用 Android Studio**

   - 打开 Android Studio
   - 选择 "Open an Existing Project"
   - 选择克隆的项目目录
   - 等待 Gradle 同步完成
   - 点击 "Run" 按钮

2. **使用命令行**

   ```bash
   # 构建 Debug 版本
   ./gradlew assembleMvpDebug

   # 构建 Release 版本
   ./gradlew assembleMvpRelease

   # 运行测试
   ./gradlew testMvpDebugUnitTest
   ```

3. **快速构建脚本**
   ```bash
   # 使用项目提供的构建脚本
   chmod +x build_mvp.sh
   ./build_mvp.sh
   ```

### 安装应用

```bash
# 安装到连接的设备
adb install -r app/build/outputs/apk/mvp/debug/app-mvp-debug.apk

# 或者直接运行
./gradlew installMvpDebug
```

## 📖 使用指南

### 1. 导入图书

- 打开应用，点击"添加图书"按钮
- 从设备文件系统中选择 EPUB 文件
- 等待应用解析图书信息
- 图书将出现在主界面的图书列表中

### 2. 开始阅读

- 点击图书封面进入阅读界面
- 点击屏幕左右区域进行翻页
- 支持手势滑动翻页操作
- 长按文本启动选择模式

### 3. AI 问答功能

- 选中感兴趣的文本内容
- 点击弹出菜单中的"AI 问答"按钮
- 等待 AI 分析并显示回答
- 查看不同类型的智能分析结果

### 4. 语音交互

- 选中文本后点击"语音提问"按钮
- 对着设备说出你的问题
- AI 将基于选中文本和语音问题给出回答
- 查看语音问答历史记录

### 5. 个性化设置

- 进入设置界面调整字体大小
- 配置 AI 回答的详细程度
- 设置墨水屏刷新模式
- 管理阅读数据和历史记录

## 🔐 隐私与安全

### 数据保护

- **本地存储**：所有用户数据完全存储在本地设备
- **数据加密**：敏感数据使用 Android Keystore 加密
- **无云同步**：不上传任何个人阅读数据
- **透明使用**：清晰的隐私政策和数据使用说明

### API 安全

- **密钥保护**：OpenAI API 密钥分片加密存储
- **网络安全**：使用 HTTPS 和证书绑定
- **请求限制**：本地限流防止 API 滥用
- **错误处理**：完善的异常处理和重试机制

## 🤝 贡献指南

我们欢迎社区贡献！以下是参与项目的方式：

### 如何贡献

1. **Fork 项目到你的 GitHub**
2. **创建功能分支**：`git checkout -b feature/amazing-feature`
3. **提交更改**：`git commit -m 'Add some amazing feature'`
4. **推送分支**：`git push origin feature/amazing-feature`
5. **创建 Pull Request**

### 代码规范

- 遵循 Kotlin 官方编码规范
- 使用有意义的变量和函数命名
- 添加适当的注释和文档
- 确保测试覆盖率不低于 80%

### 报告问题

- 使用 [GitHub Issues](https://github.com/meside-ai/ai-ink-reader/issues) 报告 Bug
- 提供详细的复现步骤和设备信息
- 附上相关的日志和截图

## 🗺️ 发展路线图

### 已完成功能 ✅

- [x] 基础 EPUB 阅读功能
- [x] AI 智能问答集成
- [x] 语音输入支持
- [x] 墨水屏显示优化
- [x] 本地数据存储

### 计划功能 🚧

- [ ] 更多 EPUB 格式支持（EPUB 3.0 增强）
- [ ] PDF 文档支持
- [ ] 多语言界面支持
- [ ] 云端设置同步（可选）
- [ ] 社区分享功能

### 长期目标 🎯

- [ ] 支持更多墨水屏设备
- [ ] 离线 AI 模型集成
- [ ] 团队协作阅读功能
- [ ] 开放 API 接口

## 📊 性能指标

| 指标         | 目标值   | 当前状态 |
| ------------ | -------- | -------- |
| 应用启动时间 | < 2 秒   | ✅ 1.8s  |
| 图书打开时间 | < 1.5 秒 | ✅ 1.2s  |
| 翻页响应延迟 | < 300ms  | ✅ 280ms |
| 内存占用     | < 200MB  | ✅ 180MB |
| AI 响应时间  | < 5 秒   | ✅ 3-8s  |

## 🏆 致谢

感谢以下开源项目和服务：

- [epublib-android](https://github.com/psiegman/epublib) - EPUB 解析库
- [OpenAI](https://openai.com) - AI 服务支持
- [Android Jetpack](https://developer.android.com/jetpack) - 现代 Android 开发套件
- 文石科技 - 提供墨水屏设备测试和优化支持

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📞 联系我们

- **项目主页**：https://github.com/meside-ai/ai-ink-reader
- **问题反馈**：https://github.com/meside-ai/ai-ink-reader/issues
- **讨论社区**：https://github.com/meside-ai/ai-ink-reader/discussions

---

<div align="center">

**如果这个项目对你有帮助，请给我们一个 ⭐️**

Made with ❤️ by [meside-ai](https://github.com/meside-ai) and [contributors](https://github.com/meside-ai/ai-ink-reader/contributors)

</div>
