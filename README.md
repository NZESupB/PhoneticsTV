# 英语音标学习（Android TV）

英式 DJ 音标（48 音）学习应用，面向 Android TV：遥控器方向键 / 确认键全程可达，界面按 10-foot 规范设计。

## 技术栈

| 项 | 选择 |
| --- | --- |
| 语言 | Kotlin |
| UI | Jetpack Compose for TV（`androidx.tv:tv-material` 1.1.0） |
| compileSdk / targetSdk / minSdk | 35 / 35 / 23 |
| AGP / Gradle / Kotlin | 8.13.2 / 8.14 / 2.3.21 |
| 音标体系 | 英式 DJ 48 音（20 元音 + 28 辅音） |
| 发音来源 | 内置 48 个音标录音与 121 个示例词录音（assets，构建前预生成）；示例词缺少录音时联网获取并缓存，再不行用设备 TTS（英式英语）朗读 |
| 分辨率适配 | 1920×1080dp 基准，字号按逻辑宽度分三档（<1000dp / 1000–1500dp / ≥1500dp） |

## 快速开始

```bash
# 需要 JDK 17 与 Android SDK 35
export JAVA_HOME=$(/usr/libexec/java_home -v 17)          # macOS
echo "sdk.dir=$HOME/Library/Android/sdk" > local.properties

./gradlew :app:assembleDebug        # 构建
./gradlew :app:installDebug         # 安装到已连接的 TV / 模拟器
adb shell am start -n com.example.phonetics/.MainActivity
```

## 功能

- **音标列表**：元音 / 辅音两个标签，按发音方式分组分行展示；焦点落到卡片时顶部预览同步显示发音要领与示例词。
- **发音播放**：进入详情自动播放；详情页可逐个朗读示例词；播放来源（录音 / 设备朗读）在界面如实标注。
- **练习**：听音辨标、看标选词，范围可选元音 / 辅音 / 全部，每轮 10 题；作答后锁定并给出正误反馈，轮末展示正确率与错题回顾（错题可按确认键重听）。
- **遥控器交互**：方向键移动焦点、确认键操作、播放键重听、返回键回退；进入页面自动设置初始焦点。

## 目录结构

```
app/src/main/
├─ AndroidManifest.xml              TV 清单项（leanback、banner、横屏）
├─ assets/audio/README.md           录音命名规范、来源与授权（放置录音即生效）
├─ res/                             banner、图标、主题、字符串
└─ java/com/example/phonetics/
   ├─ MainActivity.kt               播放器容器与 CompositionLocal 注入
   ├─ data/                         音标模型与 48 音数据（PhonemeCatalog）
   ├─ audio/                        PronunciationPlayer 接口 + 录音/TTS 实现
   ├─ quiz/                         出题器、练习状态机 ViewModel
   └─ ui/                           主题、导航、首页、详情、练习（设置/答题/结果）
```

## 验证

```bash
./gradlew :app:ktlintCheck :app:lintDebug :app:testDebugUnitTest :app:assembleDebug
```

单元测试覆盖音标数据完整性（数量、分组、示例词音素一致性）、内置录音完整性（音标与示例词各一套）、出题规则（唯一正确答案、范围限定、可复现）与练习状态机（作答锁定、判分、统计、重开）。

示例词录音由 `tools/generate-word-audio.sh` 生成，新增或修改示例词后需重跑，用法见 `app/src/main/assets/audio/README.md`。

## 已知限制

- 内置的 48 个音标录音提取自公开网页资源，**未取得明确授权**；本仓库为公开的学习演示项目、不用于商用，需要对外分发或上架时应替换为有授权的音源（见 `app/src/main/assets/audio/README.md`）。
- 内置的 121 个示例词录音中，52 个是 Wikimedia Commons 的 CC / 公有领域录音（分发时需按 `tools/word-audio-sources.tsv` 署名），69 个来自有道词典的非官方接口；运行时的在线发音默认也走该接口，接口变动时这部分会回退到设备 TTS。
- 已在 Android TV API 34 模拟器（4K AVD，3840×2160）实测 D-pad 全流程；未在真实电视盒子上验证。

## 构建产物（GitHub Actions）

`.github/workflows/android-release.yml` 在 `push`（`main` / `v*` tag）、PR 与手动触发时构建
按 ABI 拆分的 **签名 Release APK**，产物名固定为：

| 文件 | 适用设备 |
| --- | --- |
| `app-arm64-v8a-release.apk` | 近年的 64 位 Android TV / 盒子 / 手机 |
| `app-armeabi-v7a-release.apk` | 老款 32 位盒子（Amlogic / Rockchip 等） |

下载位置：仓库 **Actions → Android Release → 最新运行 → Artifacts**（`phonetics-tv-release-apks`）。
推送 `v*` tag（如 `git tag v1.0 && git push origin v1.0`）时，两个 APK 会同时挂到 GitHub Release。

CI 的质量门禁与本地一致：`ktlintCheck`、`testDebugUnitTest`、`lintDebug` 任一失败即中断，不会产出 APK。

### 签名

`app/build.gradle.kts` 从仓库根的 `keystore.properties` 读取签名材料（该文件已 gitignore）。
在仓库 **Settings → Secrets and variables → Actions** 配置以下 Secrets 即使用正式密钥：

| Secret | 内容 |
| --- | --- |
| `KEYSTORE_BASE64` | `base64 -i release.jks \| pbcopy` 的结果（macOS）或 `base64 -w0 release.jks` |
| `KEYSTORE_PASSWORD` | keystore 密码 |
| `KEY_ALIAS` | 密钥别名 |
| `KEY_PASSWORD` | 密钥密码（与 keystore 密码相同时可省略） |

未配置 `KEYSTORE_BASE64` 时，CI 用 `keytool` 生成一次性密钥并给出 warning：APK 可直接安装测试，
但签名每次构建都会变，**不能用于覆盖安装升级或上架**。

### 本地签名（可选）

本机签名材料放在 `keystore/release.jks` + 仓库根 `keystore.properties`（两者都已在 `.gitignore` 中），
格式为 `storeFile=keystore/release.jks`、`storePassword`、`keyAlias`、`keyPassword`。
存在这两个文件时，本地 `./gradlew :app:assembleRelease` 产出的 APK 与 CI 使用同一证书，
可互相覆盖安装；缺失时输出 `*-release-unsigned.apk`。

改签名密钥意味着已安装的应用无法再覆盖升级（Android 校验签名），务必保留 `keystore/release.jks` 的备份。
