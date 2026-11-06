# 发音音频资源

## 现状

`phonemes/` 目录已内置 **48 个音标的发音录音**（每个文件 1–4 秒，MP3），
播放音标时优先使用这些录音，界面显示「正在播放音标 /ʃ/」。

`words/` 目录为空：示例单词没有录音时用设备 TTS（英式英语）朗读，界面会标注「设备朗读示例词：sheep」。
把 `words/<单词小写>.mp3` 放进来即自动优先于 TTS，无需改代码。

## 音频来源与许可

`phonemes/*.mp3` 提取自新东方「国际音标表」专题页
（https://www.xdf.cn/zhuanti/bd-phonetic-alphabet-card/index.html）的公开网页资源。
该站点未提供明确的转载许可，**当前仅用于本地开发与演示**。
如需对外发布、上架应用商店或商用，必须自行取得授权，或替换为有明确授权的音源
（例如自行录制，或使用 CC 授权的 IPA 发音库），替换方式见下文的命名规则——文件名保持不变即可直接生效。

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

文件名是示例单词的小写拼写，例如 `audio/words/sheep.mp3`。
完整单词列表见 `data/PhonemeCatalog.kt` 中每个音标的 `examples`。

## 查找顺序

1. `assets/audio/phonemes/<音标 id>.<ext>`
2. `assets/audio/words/<单词拼写>.<ext>`
3. 设备 TTS 朗读示例单词

播放失败会回退到下一级，并在界面显示原因，不会静默无声。

## 为什么不直接用 TTS 朗读单个音素

TTS 合成孤立音素时经常失真（例如把 `/tʃ/` 读成字母名 "t"），所以没有录音时应用朗读的是**示例单词**，
并在界面上标注「设备朗读示例词：xxx」，让用户清楚听到的内容是什么。

## 新增/替换音频的建议

- 采样率 44.1 kHz 或 48 kHz，单声道，音量归一（避免忽大忽小）
- 音素录音建议 0.8–2 秒，前后各留 50 ms 静音
- 单词录音前后各留 100 ms 静音
- 保持单文件小于 100 KB，48 个音素整体不超过 3 MB（当前约 2.1 MB）
