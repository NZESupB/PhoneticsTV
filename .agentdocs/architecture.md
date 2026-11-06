# 架构与技术约束

修改代码前必读。本文记录跨文件的结构性约定，具体实现细节看对应源码。

## 模块划分

```
com.example.phonetics
├─ MainActivity              创建 AssetOrTtsPlayer，经 LocalPronunciationPlayer 注入；onStop 停止、onDestroy 释放
├─ LocalPronunciationPlayer  CompositionLocal，UI 取播放器的唯一入口
├─ data/                     音标模型与 48 音数据，纯 Kotlin 无 Android 依赖
├─ audio/                    PronunciationPlayer 接口 + AssetOrTtsPlayer 实现
├─ quiz/                     出题器（纯 Kotlin）+ 练习状态机 ViewModel
└─ ui/                       theme / navigation / browse / detail / quiz
```

依赖方向单向：`ui → quiz → data`，`ui → audio → data`。`data` 与 `quiz`（除 ViewModel 外）不依赖 Android 框架，便于纯 JVM 单测。

## 数据组织

`Phoneme` 的 `id` 是 ASCII 短名（如 `i_long`、`th_voiceless`），只用于录音文件名与导航参数；`symbol` 是显示用的 IPA 符号（如 `iː`、`θ`）。

`ExampleWord.phonemes` 存**完整音素序列**而不只是目标音，这让界面能拼出 `/ʃiːp/`，也让出题能排除"单词里本来就含该音"的干扰项。因此两条不变量必须成立：

1. 示例词的每个音素符号都存在于目录中；
2. 每个示例词确实包含它所属的音标。

两者由 `PhonemeCatalogTest` 守护。新增示例词时必须同时满足，否则出题会出现无正确答案或一题多解。

## 发音来源策略

`AssetOrTtsPlayer` 的查找顺序：

1. `assets/audio/phonemes/<id>.<ext>`
2. `assets/audio/words/<文本>.<ext>`
3. 设备 TTS（`Locale.UK`）朗读示例单词

`playAsset` 返回 `false` 表示录音没播成（找不到或损坏），调用方回退到朗读而不是报错停住。失败与"正在朗读哪个词"都通过 `PlayerStatus` 传到界面并用文字说明，不做静默降级。

`PlayerStatus.Playing` 依赖 MediaPlayer 的完成回调和 TTS 的 `onDone` 回到 `Idle`；如果回调丢失，状态会停在 `Playing`（只影响提示文案，不影响功能）。

## 内置音标录音

`assets/audio/phonemes/<Phoneme.id>.mp3` 内置 48 个音标的发音录音（约 2.1 MB），播放时优先于 TTS。
录制来源与授权状态见 `app/src/main/assets/audio/README.md`；未取得授权前仅限本地开发演示。

录音缺失不会报错（回退朗读示例词），因此由 `PhonemeAudioAssetsTest` 在构建期守住：
每个音标都要有 mp3、不得有孤儿文件、文件必须是可解码的 MP3、总体积不超过 8 MB。

## 4K 适配

关键事实：**4K 电视不会让应用"像素翻倍"**。DisplayManager 通常保留 1920×1080 的逻辑分辨率，
把 density 从 320 提到 640（实测 `init=3840x2160 640dpi base=1920x1080 320dpi`）。
所以 dp/sp 数值不需要按像素放大，只要全部用 dp/sp（不用 px）就会随 density 正确缩放。

真正的风险是**逻辑宽度不同的设备**（960dp / 1280dp / 1920dp）：同一个 30sp 标题在 960dp 屏上占三分之一宽。
因此：

- 所有尺寸集中在 `res/values/dimens.xml`，不在代码里写死；
- 字号分三档：基准 / `<name>_compact`（逻辑宽 < 1000dp）/ `<name>_large`（>= 1500dp）；
- `ui/theme/TvTypography.kt` 按 `LocalConfiguration.screenWidthDp` 选档，通过 `LocalTvTypography` 下发；
- 三档资源表用显式 `R.dimen` 映射而不是反射，写错在编译期就会报错；
- 启动时会打印 `TvTypography: screenWidthDp=… tier=…`，排查显示问题时先看这一行。

## 出题规则

- 听音辨标：干扰音标的 `symbol` 不得出现在题目单词的 `phonemes` 里，否则用户听到的确实是它。
- 看标选词：干扰词不得含目标音的 `symbol`。
- 每题 4 个选项、恰好 1 个正确项；同一轮不重复出同一个音标。
- 干扰项优先取同一 `PhonemeGroup` 的音（更具区分度）。

## TV 交互约定

- 只用 `androidx.tv.material3`：`Card` / `Button` / `TabRow` 自带焦点放大与边框，混入 `compose.material3` 会破坏焦点视觉一致性。
- 焦点放大 1.08–1.1 倍，焦点边框 3dp 白色或主色；正误反馈同时用颜色 + 符号（✔/✘）+ 文案，不单靠颜色。
- 进入页面必须主动请求初始焦点（`requestFocusSafely`），否则遥控器用户第一下方向键可能无反应。
- 会随状态重建焦点的页面（答题页换题）必须用 `FocusRequester` 在新题出现后把焦点放回第一个选项。
- 过扫描安全边距见 `ui/theme/Theme.kt` 的 `TvInsets`（48dp × 27dp）。
- 按键：`MEDIA_PLAY_PAUSE` / `MEDIA_PLAY` 全局重听当前题音频；返回键交给 Navigation 默认处理。

## 版本约束

Compose BOM 固定为 `2026.02.00`（Compose 1.10.3），与 `tv-material 1.1.0`（依赖 foundation 1.10.3）一致，且 `minCompileSdk=35`、最低 AGP 8.6.0。升级到 Compose 1.11+ 会要求 compileSdk 37 与 AGP 9.x，需要同时调整工具链。

## 构建与发布

发布链路只有一条：推送到 GitHub（`main` 或 `v*` tag）→ Actions 中的 `Android Release` 工作流 → 产物。

- 门禁：`ktlintCheck`、`testDebugUnitTest`、`lintDebug` 与本地 `AGENTS.md` 的验证命令一致，任一失败不产出 APK。
- 打包：`splits { abi { include("armeabi-v7a", "arm64-v8a") } }`，产出 `app-armeabi-v7a-release.apk` 与 `app-arm64-v8a-release.apk`，不生成 universal APK。`defaultConfig` 里**不要**再加 `ndk.abiFilters`——与 `splits.abi` 同时存在会直接让配置阶段失败。
- 签名：`app/build.gradle.kts` 在仓库根 `keystore.properties` 存在时才创建 `release` 签名配置；缺失时 Release 输出 `*-release-unsigned.apk`。CI 用 Secrets 生成该文件，未配置 `KEYSTORE_BASE64` 时用 `keytool` 生成一次性密钥并打 warning（仅可测试安装，签名每次变化）。
- 产物：`actions/upload-artifact` 上传 `phonetics-tv-release-apks`；tag 构建额外用 `softprops/action-gh-release` 挂到 Release。CI 里 `apksigner verify` 会校验两个 APK 的签名，名不对或未签名即失败。
