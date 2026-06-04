package top.met6.music.mobile.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import io.ktor.client.call.body
import io.ktor.client.request.get
import top.met6.music.mobile.api.httpClient
import top.met6.music.mobile.state.AppState
import top.met6.music.mobile.utils.toImageBitmap

@Composable
fun CachedImage(
    songId: String,
    imageUrl: String,
    modifier: Modifier = Modifier,
    placeholder: Painter? = null,
    contentScale: ContentScale = ContentScale.Crop
) {
    var bitmap by remember(imageUrl) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(imageUrl) {
        if (imageUrl.isNotEmpty()) {
            try {
                val bytes = AppState.storage.getCoverImageBytes(songId, imageUrl)
                if (bytes != null) {
                    bitmap = bytes.toImageBitmap()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val bmp = bitmap
    if (bmp != null) {
        Image(
            bitmap = bmp,
            contentDescription = null,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        if (placeholder != null) {
            Image(
                painter = placeholder,
                contentDescription = null,
                modifier = modifier,
                contentScale = contentScale
            )
        } else {
            Box(modifier = modifier.background(Color.DarkGray))
        }
    }
}
