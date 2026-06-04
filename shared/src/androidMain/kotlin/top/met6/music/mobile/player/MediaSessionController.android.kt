package top.met6.music.mobile.player

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import top.met6.music.mobile.storage.PlatformContext

import android.graphics.BitmapFactory
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.call.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import top.met6.music.mobile.api.httpClient

actual class MediaSessionController actual constructor(context: PlatformContext) {
    private val mediaSession: MediaSessionCompat = MediaSessionCompat(context.context, "MeTMusicSession")

    private var onPlayCallback: (() -> Unit)? = null
    private var onPauseCallback: (() -> Unit)? = null
    private var onNextCallback: (() -> Unit)? = null
    private var onPrevCallback: (() -> Unit)? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() { onPlayCallback?.invoke() }
            override fun onPause() { onPauseCallback?.invoke() }
            override fun onSkipToNext() { onNextCallback?.invoke() }
            override fun onSkipToPrevious() { onPrevCallback?.invoke() }
        })
        mediaSession.isActive = true
    }

    actual fun updateMetadata(title: String, artist: String, coverUrl: String, durationMs: Long) {
        val metadataBuilder = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, durationMs)
        
        mediaSession.setMetadata(metadataBuilder.build())

        if (coverUrl.isNotEmpty()) {
            scope.launch {
                try {
                    val secureUrl = if (coverUrl.startsWith("http://")) {
                        coverUrl.replace("http://", "https://")
                    } else {
                        coverUrl
                    }
                    val response: HttpResponse = httpClient.get(secureUrl)
                    val bytes = response.body<ByteArray>()
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    if (bitmap != null) {
                        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
                        mediaSession.setMetadata(metadataBuilder.build())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    actual fun updatePlaybackState(isPlaying: Boolean, positionMs: Long) {
        val state = if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
        val playbackState = PlaybackStateCompat.Builder()
            .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
            .setState(state, positionMs, 1.0f)
            .build()
        mediaSession.setPlaybackState(playbackState)
    }

    actual fun setCallbacks(
        onPlay: () -> Unit,
        onPause: () -> Unit,
        onNext: () -> Unit,
        onPrev: () -> Unit
    ) {
        onPlayCallback = onPlay
        onPauseCallback = onPause
        onNextCallback = onNext
        onPrevCallback = onPrev
    }

    actual fun release() {
        mediaSession.isActive = false
        mediaSession.release()
    }
}
