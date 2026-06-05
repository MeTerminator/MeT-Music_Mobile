package top.met6.music.mobile.utils

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color

actual fun ByteArray.toImageBitmap(): ImageBitmap {
    val bmp = BitmapFactory.decodeByteArray(this, 0, this.size)
        ?: throw IllegalArgumentException("Could not decode image bytes")
    return bmp.asImageBitmap()
}

actual fun ByteArray.extractDominantColor(): Color {
    return try {
        val bmp = BitmapFactory.decodeByteArray(this, 0, this.size) ?: return Color(0xFF1E1E24)
        val scaledBmp = Bitmap.createScaledBitmap(bmp, 1, 1, true)
        val pixel = scaledBmp.getPixel(0, 0)
        scaledBmp.recycle()
        bmp.recycle()
        Color(pixel)
    } catch (e: Exception) {
        Color(0xFF1E1E24)
    }
}
