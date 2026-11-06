# AGENTS.md

## 项目

英语音标学习 Android TV 应用（Kotlin + Compose for TV）。音标数据在 `app/src/main/java/com/example/phonetics/data/PhonemeCatalog.kt`，发音来源规范在 `app/src/main/assets/audio/README.md`。

## 环境

- JDK 17（macOS：`export JAVA_HOME=$(/usr/libexec/java_home -v 17)`）
- Android SDK 平台 35（`local.properties` 中 `sdk.dir` 指向 SDK 根目录）
- Gradle 通过 `./gradlew` 使用，版本见 `gradle/wrapper/gradle-wrapper.properties`

## 验证命令（提交前必须全部通过）

```bash
./gradlew :app:ktlintCheck :app:lintDebug :app:testDebugUnitTest :app:assembleDebug
```

- 格式问题用 `./gradlew :app:ktlintFormat` 自动修复。
- `lint` 必须 0 error；warning 允许存在，但新增 warning 需在 PR 说明原因。
- CI（`.github/workflows/android-release.yml`）跑同一组门禁，并额外 `assembleRelease` 产出 ABI 拆分的 `app-arm64-v8a-release.apk` 与 `app-armeabi-v7a-release.apk`；改了这两条命令或 ABI 范围要同步 README 的「构建产物」小节。
- 改动音标数据、出题逻辑或练习状态机时，必须同步补充或更新 `app/src/test/java` 下的单元测试。

## 测试约定

- 单元测试使用 JUnit4，纯 JVM，不依赖 Android 运行时（不引 Robolectric）。
- 涉及随机的逻辑（出题器）必须支持注入种子，测试里断言唯一正确答案、范围限定与可复现性。
- 数据完整性由 `PhonemeCatalogTest` 守护：48 个音、分组数量、id 唯一且为 ASCII、示例词音素只使用目录内符号、每个示例词确实包含所属音。新增或修改示例词后先跑该测试。

## 约束

- 不要在播放器之外实现发音逻辑；所有发音统一走 `PronunciationPlayer`。
- 缺少录音时朗读示例单词而不是合成单个音素（原因见 `assets/audio/README.md`）。
- UI 文字使用中文；新增界面需保证遥控器方向键可达、确认键可操作、焦点有明显视觉反馈。
