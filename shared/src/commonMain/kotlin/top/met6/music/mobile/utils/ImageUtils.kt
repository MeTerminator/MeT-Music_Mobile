package top.met6.music.mobile.utils

import androidx.compose.ui.graphics.ImageBitmap

import androidx.compose.ui.graphics.Color

expect fun ByteArray.toImageBitmap(): ImageBitmap
expect fun ByteArray.extractDominantColor(): Color
