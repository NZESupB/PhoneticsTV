package com.example.phonetics.data

/**
 * MP3 以 ID3 标签或 MPEG 帧同步字（0xFF 0xEx/0xFx）开头，可据此排除下载到的 HTML/JSON 错误页。
 */
internal fun hasMp3Header(bytes: ByteArray): Boolean {
    if (bytes.size < 3) return false
    val hasId3 = bytes[0] == 'I'.code.toByte() && bytes[1] == 'D'.code.toByte() && bytes[2] == '3'.code.toByte()
    val first = bytes[0].toInt() and 0xFF
    val second = bytes[1].toInt() and 0xFF
    val hasFrameSync = first == 0xFF && (second and 0xE0) == 0xE0
    return hasId3 || hasFrameSync
}
