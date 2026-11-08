#!/usr/bin/env bash
# 为 PhonemeCatalog 中的示例词预生成 app/src/main/assets/audio/words/<word>.mp3。
#
# 音源优先级：
#   1. Wikimedia Commons 的英音真人录音：用 Free Dictionary API（dictionaryapi.dev）定位文件，
#      再从 Commons 取 MP3 转码（dictionaryapi 自身的媒体端点不稳定）。
#      只接受文件名以 "-<单词>.<扩展名>" 结尾的录音，排除 "En-uk-a sheep.ogg" 这类带冠词的朗读。
#   2. 补齐源，由 WORD_AUDIO_FALLBACK 指定：
#      - azure（默认）：Azure 神经网络 TTS en-GB-SoniaNeural，需要环境变量 AZURE_TTS_KEY / AZURE_TTS_REGION；
#      - youdao：有道词典英音，非官方接口（运行时在线发音 OnlineWordAudio 默认也用它）。
#
# 已存在的文件会跳过；要换源，先删除对应 mp3 再运行。
# 每个词的来源、授权与作者写入 tools/word-audio-sources.tsv，署名与发布判断以它为准。
# 依赖：bash、curl、jq、xxd（youdao 补齐源另需 ffmpeg）。密钥只从环境变量读取，不要写进仓库。
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$ROOT/app/src/main/assets/audio/words"
CATALOG="$ROOT/app/src/main/java/com/example/phonetics/data/PhonemeCatalog.kt"
SOURCES="$ROOT/tools/word-audio-sources.tsv"
FALLBACK="${WORD_AUDIO_FALLBACK:-azure}"
USER_AGENT="phonetics-tv-tools/1.0 (word audio generator)"
HEADER=$'word\tsource\tlicense_or_voice\tauthor\torigin'

command -v jq >/dev/null || { echo "缺少 jq" >&2; exit 1; }
case "$FALLBACK" in
  azure)
    if [[ -z "${AZURE_TTS_KEY:-}" || -z "${AZURE_TTS_REGION:-}" ]]; then
      echo "WORD_AUDIO_FALLBACK=azure 需要 AZURE_TTS_KEY 与 AZURE_TTS_REGION" >&2
      exit 1
    fi
    ;;
  youdao) ;;
  *) echo "未知的 WORD_AUDIO_FALLBACK：$FALLBACK（可选 azure / youdao）" >&2; exit 1 ;;
esac

mkdir -p "$OUT"
[[ -f $SOURCES ]] || echo "$HEADER" >"$SOURCES"
words=$(grep -oE 'ExampleWord\("[^"]+"' "$CATALOG" | cut -d'"' -f2 | sort -u)
trap 'rm -f "$OUT"/*.part "$SOURCES.tmp"' EXIT

# 遇到限流（429）与 5xx 自动退避重试。
http() {
  curl -sS -m 30 -A "$USER_AGENT" --retry 5 --retry-delay 5 "$@"
}

# MP3 以 ID3 标签或 MPEG 帧同步字开头；据此拒绝下载到的 HTML/JSON 错误页。
is_mp3() {
  local hex
  hex=$(head -c 3 "$1" | xxd -p)
  [[ ${#hex} -eq 6 ]] || return 1
  [[ $hex == 494433 ]] || [[ $((16#${hex:0:2})) -eq 255 && $((16#${hex:2:2} & 224)) -eq 224 ]]
}

# 输出该词英音录音的 Commons 页面 id；词典没有收录或没有英音时输出为空。
dictionary_page_id() {
  local w=$1 body code origin
  body=$(http -w '\n%{http_code}' "https://api.dictionaryapi.dev/api/v2/entries/en/$w")
  code=${body##*$'\n'}
  body=${body%$'\n'*}
  [[ $code == 404 ]] && return 0
  [[ $code == 200 ]] || { echo "dictionaryapi 返回 HTTP $code：$w" >&2; return 1; }
  origin=$(jq -r '[.[].phonetics[] | select((.audio // "") | test("-uk\\.mp3$"))][0].sourceUrl // empty' <<<"$body")
  [[ $origin == *curid=* ]] && echo "${origin##*curid=}"
  return 0
}

# 输出 "<MP3 地址>\t<授权>\t<作者>"；文件名与单词不完全匹配时输出为空。
commons_entry() {
  local w=$1 id=$2 fields title url mime license author lower file
  fields=$(http -f "https://commons.wikimedia.org/w/api.php?action=query&format=json&pageids=$id&prop=imageinfo&iiprop=url%7Cmime%7Cextmetadata&iiextmetadatafilter=LicenseShortName%7CArtist" |
    jq -r '.query.pages[] | [
        .title,
        (.imageinfo[0].url | split("?")[0]),
        .imageinfo[0].mime,
        (.imageinfo[0].extmetadata.LicenseShortName.value // "unknown"),
        ((.imageinfo[0].extmetadata.Artist.value // "unknown") | gsub("<[^>]*>"; "") | gsub("[\\t\\n]"; " "))
      ] | @tsv')
  IFS=$'\t' read -r title url mime license author <<<"$fields"
  lower=$(tr '[:upper:]' '[:lower:]' <<<"$title")
  [[ $lower =~ -$w\.(ogg|oga|wav|flac|mp3)$ ]] || { echo "跳过 $w：$title 不是单词本身的录音" >&2; return 0; }
  if [[ $mime != audio/mpeg ]]; then
    file=${url##*/}
    url="https://upload.wikimedia.org/wikipedia/commons/transcoded/${url#*/wikipedia/commons/}/$file.mp3"
  fi
  printf '%s\t%s\t%s\n' "$url" "$license" "$author"
}

fetch_azure() {
  http -f -X POST "https://${AZURE_TTS_REGION}.tts.speech.microsoft.com/cognitiveservices/v1" \
    -H "Ocp-Apim-Subscription-Key: ${AZURE_TTS_KEY}" \
    -H "Content-Type: application/ssml+xml" \
    -H "X-Microsoft-OutputFormat: audio-24khz-48kbitrate-mono-mp3" \
    --data "<speak version='1.0' xml:lang='en-GB'><voice name='en-GB-SoniaNeural'>$1</voice></speak>" \
    -o "$2"
}

fetch_youdao() {
  http -f "https://dict.youdao.com/dictvoice?type=1&audio=$1" -o "$2"
  # 有道部分词返回标着 audio/mpeg 的 WAV，转成 MP3 以统一格式与体积。
  if [[ $(head -c 4 "$2") == RIFF ]]; then
    command -v ffmpeg >/dev/null || { echo "$1 是 WAV，转码需要 ffmpeg" >&2; return 1; }
    ffmpeg -v error -y -f wav -i "$2" -ac 1 -b:a 64k -f mp3 "$2.part"
    mv "$2.part" "$2"
  fi
}

# 逐词落盘，中途失败后重跑不会丢失已下载文件的来源记录。
record_source() {
  local w=$1 row=$2
  awk -F'\t' -v w="$w" '$1 != w' "$SOURCES" >"$SOURCES.tmp"
  echo "$row" >>"$SOURCES.tmp"
  mv "$SOURCES.tmp" "$SOURCES"
}

for w in $words; do
  target="$OUT/$w.mp3"
  if [[ -s $target ]]; then
    awk -F'\t' -v w="$w" '$1 == w { found = 1 } END { exit !found }' "$SOURCES" ||
      { echo "已有 $w.mp3 但来源未登记，请删除后重新生成" >&2; exit 1; }
    continue
  fi

  sleep 1 # Commons 对匿名高频请求会限流
  entry=""
  page_id=$(dictionary_page_id "$w")
  [[ -z $page_id ]] || entry=$(commons_entry "$w" "$page_id")

  if [[ -n $entry ]]; then
    IFS=$'\t' read -r url license author <<<"$entry"
    http -f "$url" -o "$target.part"
    row="$w	wikimedia	$license	$author	https://commons.wikimedia.org/w/index.php?curid=$page_id"
  elif [[ $FALLBACK == azure ]]; then
    fetch_azure "$w" "$target.part"
    row="$w	azure	en-GB-SoniaNeural	Microsoft Azure TTS	"
  else
    fetch_youdao "$w" "$target.part"
    row="$w	youdao	unlicensed	unknown	https://dict.youdao.com/dictvoice?type=1&audio=$w"
  fi

  is_mp3 "$target.part" || { echo "下载结果不是 MP3：$w" >&2; exit 1; }
  mv "$target.part" "$target"
  record_source "$w" "$row"
  echo "✔ $w ($(cut -f2 <<<"$row"))"
done

# 清单只保留目录中仍存在的词，并按单词排序。
{
  echo "$HEADER"
  tail -n +2 "$SOURCES" | awk -F'\t' 'NR == FNR { known[$1] = 1; next } $1 in known' <(echo "$words") - | sort
} >"$SOURCES.tmp"
mv "$SOURCES.tmp" "$SOURCES"
echo "完成：$(wc -w <<<"$words" | tr -d ' ') 个词，来源见 ${SOURCES#"$ROOT"/}"
