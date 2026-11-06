package com.example.phonetics.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * 把 dimen 资源当作字号读取。
 *
 * 注意不能用 `dimensionResource(id).toSp()`：dimen 已按当前 density 换算成像素，再转 sp 会被放大两次。
 * 这里直接取原始资源像素值除以 density，再带上 sp 单位，等价于 `sp` 资源按系统字体缩放后的字号。
 */
@Composable
@ReadOnlyComposable
fun spResource(id: Int): TextUnit {
    val res = LocalContext.current.resources
    val density = LocalDensity.current.density
    return (res.getDimension(id) / density).sp
}
