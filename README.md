# AIbomm - AI 闪念胶囊

AIbomm 是一款基于 Android 平台的智能笔记与灵感捕捉工具。它结合了现代化的 UI 设计语言与强大的 AI 整理能力，旨在帮助用户快速捕捉碎片化灵感，并自动将其整理为结构清晰的专业笔记。

> **闪念胶囊，不仅是你的笔记，更是你的 Android 智能助理。**

## ✨ 功能亮点

- **🚀 灵感快速捕捉**：
  - 支持文字、语音、图片多种输入方式。
  - 沉浸式输入界面，专注于灵感本身。
- **🤖 AI 智能整理 (Magic Organize)**：
  - **一键整理**：点击 “✨” 图标，将凌乱的随手记瞬间转化为逻辑通顺、格式优雅的 Markdown 笔记。
  - **智能标签**：自动分析内容并生成分类标签。
  - **非阻塞处理**：AI 逻辑在后台运行，确保界面操作丝滑流畅。
- **🪄 魔法动作 (Magic Action)**：
  - **意图识别**：自动识别笔记中的意图（如“提醒我...”、“发短信给...”）。
  - **系统集成**：直接调用系统闹钟、日历、短信等功能，实现从灵感到行动的无缝衔接。
- **🎨 现代化交互与视觉**：
  - **Material You + Magic**：动态取色配合精致的渐变色与星光图标。
  - **流光动效 (Shimmer)**：AI 处理时带有优雅的流光特效。
  - **秩序产生动画**：使用 `Crossfade` 动画展示从混乱到整洁的文本蜕变。
- **📱 精致桌面小组件**：
  - **1x1 灵感球**：类图标设计，高级渐变质感。
  - **4x1 工具条**：超薄无边框设计，支持快速录入。

## 🛠 技术栈

- **UI 框架**：Jetpack Compose (Material 3)
- **架构模式**：MVVM + Clean Architecture
- **本地存储**：Room Database
- **网络通信**：Retrofit + OkHttp + Kotlin Serialization
- **异步处理**：Kotlin Coroutines & Flow
- **AI 集成**：Digital Ocean AI API (Claude 3.7 Sonnet)
- **Markdown 渲染**：Compose Markdown

## ⚙️ 环境配置

1. **获取 API Key**：
   在根目录下创建 `.env` 文件，并配置您的 AI API 密钥：
   ```env
   DO_AI_API_KEY=您的API密钥
   DO_AI_MODEL=anthropic-claude-3.7-sonnet
   ```

2. **编译环境**：
   - Android Studio Iguana 或更高版本
   - JDK 17
   - Android SDK 34+

## 📸 预览

<div align="center">
  <img src="7bdb199e93fc81d845174dc1d2342462.jpg" width="32%" />
  <img src="a82e465241634f1846392585a570d6c9.png" width="32%" />
  <img src="c79cb960a98c068f5c0fba8546616c13.jpg" width="32%" />
</div>

*(AIbomm 预览图)*

## 📄 开源协议

MIT License
