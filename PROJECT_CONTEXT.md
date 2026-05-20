# Agent Android - 项目开发上下文

## 用户信息
- **GitHub**: NaHCO3-Firefly
- **Email**: 1226174871@qq.com
- **仓库**: `git@github.com:NaHCO3-Firefly/agent-android.git`

## 来自用户的追加消息
1. https://opencode.ai/docs/zh-cn/go/ - OpenCode Go 订阅服务，需要在设置中作为提供商预设之一支持
2. OpenCode Go API 端点: `https://opencode.ai/zen/go/v1/chat/completions` (OpenAI 兼容格式)
3. **重要**: API 密钥输入框必须是普通文本框 (`inputType="text"`)，不能是密码框 (`textPassword`)
   原因: scrcpy (Android 屏幕镜像工具) 在聚焦密码框时会黑屏，影响真机调试
4. 提供商预设列表: OpenAI、OpenCode Go、自定义
   - 选择 OpenAI: baseUrl 自动填充 `https://api.openai.com/v1`，模型默认 `gpt-3.5-turbo`
   - 选择 OpenCode Go: baseUrl 自动填充 `https://opencode.ai/zen/go/v1`，模型从 Go 模型列表中选择
   - 选择自定义: 所有字段手动填写
5. **信息推送改为系统原生** (已移除 FCM，改用 NotificationManager)

## 给下一位 AI 的信息
这个项目是一个 AI Agent Android 应用，MVVM 架构。**你需要在上下文消耗完之前完成项目开发。用户会告诉你上下文余额的**

**最重要原则**：每完成一个文件就更新本文件勾选，保持进度准确。
如果上下文即将耗尽，把你的 todo 和未完成清单追加到本文件末尾"给下一位 AI 的信息"中，包括你遇到的困难。

**每次完成一个 todo 后 push 一次到 `git@github.com:NaHCO3-Firefly/agent-android.git`**

## 项目概述
AI Agent Android 应用，最低支持 Android 7 (API 24)，兼容 armeabi-v7a。

## 技术栈
- **语言**: Kotlin
- **UI**: 传统 View/XML 布局，Material Design 3
- **架构**: MVVM (ViewModel + LiveData + Coroutines)
- **数据库**: Room
- **网络**: Retrofit + OkHttp (OpenAI 兼容 API)
- **Markdown**: Markwon
- **图片**: Coil
- **推送**: 系统原生 NotificationManager（已移除 FCM）
- **配置存储**: SharedPreferences (通过 SharedPrefsManager)

## 功能需求
1. 聊天对话界面 - AI Agent 交互
2. OpenAI 兼容 API 对接（可配置 endpoint + api_key）
3. **提供商预设**: OpenAI、OpenCode Go、自定义
   - OpenCode Go: `https://opencode.ai/zen/go/v1/chat/completions`
   - API 密钥输入框必须为普通文本框 (text), 不能是密码框 (textPassword) — scrcpy 兼容
4. 多轮对话上下文
5. Markdown 渲染（代码块等）
6. 对话历史管理（查看、删除、搜索）
7. 流式输出（SSE streaming via OkHttp）
8. 多会话切换
9. 文件/图片上传
10. 系统原生推送通知
11. 设置页面（提供商预设、API 地址、密钥、模型名称等配置）

## 包结构
```
com.firefly.agentandroid
├── App.kt
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt
│   │   ├── dao/
│   │   │   ├── ConversationDao.kt
│   │   │   └── MessageDao.kt
│   │   └── entity/
│   │       ├── Conversation.kt
│   │       └── Message.kt
│   ├── remote/
│   │   ├── ApiService.kt
│   │   ├── SseClient.kt
│   │   ├── dto/
│   │   │   ├── ChatRequest.kt
│   │   │   └── ChatResponse.kt
│   │   └── RetrofitClient.kt
│   └── repository/
│       └── ChatRepository.kt
├── ui/
│   ├── main/
│   │   ├── MainActivity.kt
│   │   ├── MainViewModel.kt
│   │   └── ConversationAdapter.kt
│   ├── chat/
│   │   ├── ChatActivity.kt
│   │   ├── ChatViewModel.kt
│   │   └── MessageAdapter.kt
│   └── settings/
│       └── SettingsActivity.kt
└── util/
    ├── MarkdownRenderer.kt
    └── SharedPrefsManager.kt
```

---

## 已创建文件 ✅

### Gradle 构建系统
- [x] settings.gradle.kts
- [x] build.gradle.kts (root) — 已移除 FCM google-services 插件
- [x] gradle.properties
- [x] gradle/wrapper/gradle-wrapper.properties (Gradle 8.4)
- [x] gradle/wrapper/gradle-wrapper.jar (官方 wrapper JAR)
- [x] gradlew + gradlew.bat (官方 v8.4.0 脚本)
- [x] app/build.gradle.kts — 已移除 Firebase BOM + messaging-ktx
- [x] app/proguard-rules.pro
- [x] AndroidManifest.xml
- [x] res/xml/network_security_config.xml
- [x] .gitignore

### 数据层
- [x] App.kt (系统原生通知 channel，无 FCM)
- [x] data/local/entity/Conversation.kt
- [x] data/local/entity/Message.kt
- [x] data/local/dao/ConversationDao.kt
- [x] data/local/dao/MessageDao.kt
- [x] data/local/AppDatabase.kt

### 网络层
- [x] data/remote/dto/ChatRequest.kt
- [x] data/remote/dto/ChatResponse.kt
- [x] data/remote/ApiService.kt
- [x] data/remote/SseClient.kt
- [x] data/remote/RetrofitClient.kt

### 仓库层
- [x] data/repository/ChatRepository.kt

### UI 层
- [x] ui/main/MainActivity.kt
- [x] ui/main/MainViewModel.kt
- [x] ui/main/ConversationAdapter.kt
- [x] ui/chat/ChatActivity.kt
- [x] ui/chat/ChatViewModel.kt
- [x] ui/chat/MessageAdapter.kt
- [x] ui/settings/SettingsActivity.kt (含提供商预设 Spinner)

### 工具类
- [x] util/MarkdownRenderer.kt (已修复 Coil 初始化 bug)
- [x] util/SharedPrefsManager.kt (含 ProviderPreset 枚举)

### 布局文件
- [x] res/layout/activity_main.xml
- [x] res/layout/activity_chat.xml
- [x] res/layout/activity_settings.xml (含 provider 预设 Spinner)
- [x] res/layout/item_conversation.xml
- [x] res/layout/item_message_user.xml
- [x] res/layout/item_message_ai.xml
- [x] res/layout/item_message_system.xml

### 资源文件
- [x] res/values/strings.xml
- [x] res/values/colors.xml
- [x] res/values/themes.xml
- [x] res/values/dimens.xml
- [x] res/values-zh/strings.xml

### Drawable 图标
- [x] res/drawable/ic_send.xml
- [x] res/drawable/ic_stop.xml
- [x] res/drawable/ic_new_chat.xml
- [x] res/drawable/bg_message_user.xml
- [x] res/drawable/bg_message_ai.xml
- [x] res/drawable/bg_message_system.xml
- [x] res/drawable/bg_input.xml
- [x] res/drawable/ic_launcher_foreground.xml
- [x] res/drawable/ic_launcher_background.xml

### 启动器图标
- [x] res/mipmap-anydpi-v26/ic_launcher.xml (自适应图标)

---

## 已推送状态
- ✅ Git 仓库: `git@github.com:NaHCO3-Firefly/agent-android.git`
- ✅ 分支: main
- ✅ 首次提交: 56 文件, 2643 行
- ✅ FCM 已完全移除，改用系统原生通知
- ✅ MarkdownRenderer Coil bug 已修复

---

## 待完成清单

1. **编译验证** — 当前环境 Java 26 不兼容 AGP 7.4.2 / Kotlin 1.9.22，需在 JDK 17/21 环境编译验证
2. **MainViewModel 搜索功能实现** — `searchConversations` 方法只返回 LiveData，但未切换数据源
3. **会话标题列表不刷新** — MainActivity 在 `searchView.onQueryTextChange` 中直接调用 `viewModel.getAllConversations()` 或 `viewModel.searchConversations()`，但 LiveData 来自 Room，需要正确切换观察
4. **MessageAdapter DiffUtil** — 流式输出时 content 频繁变化，`areContentsTheSame` 只比较 content，频繁更新可能造成性能问题
5. **添加图片/文件上传 UI 和逻辑**
6. **MainActivity 缺少 Settings 入口按钮** — 布局中无设置按钮

---

## 给下一位 AI 的注意事项

1. **MainViewModel 搜索逻辑缺陷**：`conversations` LiveData 在 init 时绑定到 `getAllConversations()`，搜索时无法切换。要么用 `MediatorLiveData` 动态切换数据源，要么让 ViewModel 提供方法返回新的 LiveData 让 Activity 重新 observe。
2. **需要编译环境**：此项目需要 JDK 17 或 21 才能编译（AGP 7.4.2 不支持 Java 26）。在 Android Studio 中打开会自动处理。
3. **API Key scrcpy 兼容性**：`activity_settings.xml` 中 `edit_api_key` 的 `inputType="text"` 已正确设置，不要改成 `textPassword`。
4. **OpenCode Go API 文档**：https://opencode.ai/docs/zh-cn/go/
5. **仓库**：`git@github.com:NaHCO3-Firefly/agent-android.git`，已推送初始提交。
