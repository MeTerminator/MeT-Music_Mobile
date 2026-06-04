package top.met6.music.mobile.utils

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

actual fun ByteArray.toImageBitmap(): ImageBitmap {
    val bmp = BitmapFactory.decodeByteArray(this, 0, this.size)
        ?: throw IllegalArgumentException("Could not decode image bytes")
    return bmp.asImageBitmap()
}
