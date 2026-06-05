package top.met6.music.mobile.utils

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image

import org.jetbrains.skia.Bitmap
import androidx.compose.ui.graphics.Color

actual fun ByteArray.toImageBitmap(): ImageBitmap {
    val skiaImage = Image.makeFromEncoded(this)
    return skiaImage.toComposeImageBitmap()
}

actual fun ByteArray.extractDominantColor(): Color {
    return try {
        val skiaImage = Image.makeFromEncoded(this)
        val bitmap = Bitmap()
        val gridSize = 5
        bitmap.allocN32Pixels(gridSize, gridSize)
        val srcX = (skiaImage.width - gridSize) / 2
        val srcY = (skiaImage.height - gridSize) / 2
        val success = skiaImage.readPixels(bitmap, srcX, srcY)
        if (success) {
            var r = 0L
            var g = 0L
            var b = 0L
            var count = 0
            for (x in 0 until gridSize) {
                for (y in 0 until gridSize) {
                    val colorInt = bitmap.getColor(x, y)
                    val alpha = (colorInt shr 24) and 0xFF
                    if (alpha > 0) {
                        r += (colorInt shr 16) and 0xFF
                        g += (colorInt shr 8) and 0xFF
                        b += colorInt and 0xFF
                        count++
                    }
                }
            }
            if (count > 0) {
                val avgR = (r / count).toInt()
                val avgG = (g / count).toInt()
                val avgB = (b / count).toInt()
                Color((0xFF shl 24) or (avgR shl 16) or (avgG shl 8) or avgB)
            } else {
                Color(0xFF1E1E24)
            }
        } else {
            Color(0xFF1E1E24)
        }
    } catch (e: Exception) {
        Color(0xFF1E1E24)
    }
}
