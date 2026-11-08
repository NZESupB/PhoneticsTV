# .agentdocs 索引

面向 AI 代理的项目内部文档。修改前先看与任务相关的条目。

## 产品文档

`product/phonetics-tv.md` - 英语音标 TV 应用的功能范围、音标体系选择与练习规则

## 架构文档

`architecture.md` - 模块划分、数据组织、发音来源策略、TV 交互约定，修改任何代码前必读

## 当前任务文档

（无）

## 全局重要记忆

- 音标数据唯一来源是 `data/PhonemeCatalog.kt`；示例词的 `phonemes` 序列既是界面音标显示的来源，也是练习出题排除干扰项的依据，改动必须跑 `PhonemeCatalogTest`。
- 所有发音统一走 `audio/PronunciationPlayer`；禁止在界面层直接使用 `MediaPlayer` / `TextToSpeech`。
- 缺少录音时朗读示例单词，不合成单音素（TTS 读孤立音素会失真）。
- 48 个音标录音已内置在 `assets/audio/phonemes/`，由 `PhonemeAudioAssetsTest` 守护完整性；来源与授权见 `assets/audio/README.md`。
- 121 个示例词录音已内置在 `assets/audio/words/`，由 `tools/generate-word-audio.sh` 预生成，`WordAudioAssetsTest` 守护；新增或修改示例词后必须重跑脚本。逐词来源登记在 `tools/word-audio-sources.tsv`（Wikimedia 来源分发时需按清单署名）。内置录音缺失的词由 `OnlineWordAudio` 联网获取并缓存（默认有道英音），失败再回退设备 TTS；不要把云 TTS 的 Key 打进 APK。
- 4K TV 的逻辑分辨率通常仍是 1920×1080（density 640），不要按像素放大 dp；字号必须走 `LocalTvTypography` 的三档，不要写死 sp。
- UI 组件使用 `androidx.tv.material3`，不要混入 `androidx.compose.material3`（两套主题与焦点样式不兼容）。
- Compose 依赖固定在 BOM 2026.02.00（Compose 1.10.3）：升级到 1.11+ 会要求 compileSdk 37 与 AGP 9.x。
- 验证命令见根目录 `AGENTS.md`。发版流程：改 `versionCode`/`versionName` → 推 `main` 等 CI 绿 → 打 `v<versionName>` tag 推送，Release 会挂上两个 APK；`versionCode` 必须单调递增，否则覆盖安装会失败。
- 构建与发布走 GitHub Actions（`.github/workflows/android-release.yml`），`splits.abi` 只包含 `arm64-v8a` 与 `armeabi-v7a`：引入带 so 的 native 依赖时要确认两种 ABI 都有产物，否则老盒子或 64 位设备装不上。
- Release 签名材料只从仓库根 `keystore.properties` 读取（已 gitignore），CI 由 Secrets `KEYSTORE_BASE64` / `KEYSTORE_PASSWORD` / `KEY_ALIAS` / `KEY_PASSWORD` 生成；不要把 keystore 或密码写进仓库。正式密钥已配置：本机备份在 `keystore/release.jks`（别名 `phonetics`，密码在同目录 `keystore.properties`，两者均被 gitignore），证书 SHA-256 为 `5ce616db…b13d`——换密钥会导致已安装应用无法覆盖升级。
