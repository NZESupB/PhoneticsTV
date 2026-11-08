# 发音音频资源

## 现状

`phonemes/` 目录已内置 **48 个音标的发音录音**（每个文件 1–4 秒，MP3），
播放音标时优先使用这些录音，界面显示「正在播放音标 /ʃ/」。

`words/` 目录内置 **121 个示例单词的录音**（MP3），由 `tools/generate-word-audio.sh` 预生成，
点示例词时优先播放，界面显示「正在播放单词 sheep」；不再依赖设备是否装了 TTS 引擎或英式语音包。
只有录音缺失或损坏时，才会联网获取发音（见「查找顺序」）；联网也失败时回退到设备 TTS，界面标注「设备朗读示例词：sheep」。

## 音频来源与许可

`phonemes/*.mp3` 提取自新东方「国际音标表」专题页
（https://www.xdf.cn/zhuanti/bd-phonetic-alphabet-card/index.html）的公开网页资源。
该站点未提供明确的转载许可，**当前仅用于本地开发与演示**。
如需对外发布、上架应用商店或商用，必须自行取得授权，或替换为有明确授权的音源
（例如自行录制，或使用 CC 授权的 IPA 发音库），替换方式见下文的命名规则——文件名保持不变即可直接生效。

`words/*.mp3` 的来源逐词登记在仓库根的 `tools/word-audio-sources.tsv`（单词、来源、授权、作者、出处），当前构成：

| 来源 | 数量 | 授权 | 说明 |
| --- | --- | --- | --- |
| `wikimedia` | 52 | CC BY 3.0 US / CC BY 4.0 / CC BY-SA 4.0 / Public domain | Wikimedia Commons 英音真人录音，多数来自 Association Shtooka；对外分发时须按清单署名，CC BY-SA 文件需同协议分享 |
| `youdao` | 69 | 未取得单独授权 | 有道词典英音的非官方接口；本项目为开源非商用学习项目，已决定直接使用 |

需要换成官方授权音源时：删除清单中 `source=youdao` 的 mp3，
设置 `AZURE_TTS_KEY` / `AZURE_TTS_REGION` 后重新运行脚本（默认用 Azure `en-GB-SoniaNeural` 补齐）。

## 音素文件命名

文件名使用 `Phoneme.id`（ASCII，见 `data/PhonemeCatalog.kt`），扩展名支持 `mp3`、`ogg`、`m4a`、`wav`。

| 分组 | id |
| --- | --- |
| 单元音 12 | `i_long` `i_short` `e` `ae` `er_long` `schwa` `u_short` `u_long` `u_short_round` `aw_long` `o_short` `a_long` |
| 双元音 8 | `ei` `ai` `oi` `ou` `au` `ie` `ea` `ue` |
| 爆破音 6 | `p` `b` `t` `d` `k` `g` |
| 摩擦音 10 | `f` `v` `th_voiceless` `th_voiced` `s` `z` `sh` `zh` `h` `r` |
| 破擦音 6 | `ch` `j_voiced` `tr` `dr` `ts` `dz` |
| 鼻音 3 | `m` `n` `ng` |
| 舌侧音 1 | `l` |
| 半元音 2 | `w` `j` |

例如 `/ʃ/` 的录音文件是 `audio/phonemes/sh.mp3`。

## 单词文件命名

文件名是示例单词的拼写（与 `ExampleWord.text` 完全一致，当前全为小写），例如 `audio/words/sheep.mp3`。
完整单词列表见 `data/PhonemeCatalog.kt` 中每个音标的 `examples`。

## 生成单词录音

```bash
tools/generate-word-audio.sh                             # 补齐源默认 Azure，需要 AZURE_TTS_KEY / AZURE_TTS_REGION
WORD_AUDIO_FALLBACK=youdao tools/generate-word-audio.sh  # 补齐源改用有道（非官方接口，需要 ffmpeg）
```

- 优先取 Wikimedia Commons 的英音真人录音（经 Free Dictionary API 定位），带冠词的朗读（如 "a sheep"）会被跳过；
  缺失的词由补齐源生成。
- 已存在的 mp3 会跳过，可重复执行；要换源，先删除对应文件再运行。
- 新增或修改示例词后必须重跑；`WordAudioAssetsTest` 会检查每个词都有录音、文件是有效 MP3、来源已登记、总体积小于 3 MB。

## 查找顺序

音标：

1. `assets/audio/phonemes/<音标 id>.<ext>`
2. 设备 TTS 朗读示例单词

示例单词：

1. `assets/audio/words/<单词拼写>.<ext>`
2. 在线发音缓存 `cacheDir/audio/words/`（之前下载过的词）
3. 在线下载（`OnlineWordAudio`，默认有道英音；界面显示「正在获取在线发音」，成功后显示「正在播放在线发音」）
4. 设备 TTS 朗读

播放失败会回退到下一级，并在界面显示原因，不会静默无声。
在线一级只用来补位内置录音缺失的词（比如新增示例词后还没重跑生成脚本），正常情况下 121 个词都会命中第 1 级。

## 为什么不直接用 TTS 朗读单个音素

TTS 合成孤立音素时经常失真（例如把 `/tʃ/` 读成字母名 "t"），所以没有录音时应用朗读的是**示例单词**，
并在界面上标注「设备朗读示例词：xxx」，让用户清楚听到的内容是什么。

## 新增/替换音频的建议

- 采样率 44.1 kHz 或 48 kHz，单声道，音量归一（避免忽大忽小）
- 音素录音建议 0.8–2 秒，前后各留 50 ms 静音
- 单词录音前后各留 100 ms 静音
- 保持单文件小于 100 KB，48 个音素整体不超过 3 MB（当前约 2.1 MB）
