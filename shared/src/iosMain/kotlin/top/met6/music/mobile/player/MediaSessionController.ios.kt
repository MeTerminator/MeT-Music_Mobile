@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
package top.met6.music.mobile.player

import platform.MediaPlayer.*
import platform.Foundation.*
import platform.UIKit.UIImage
import kotlinx.cinterop.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.call.body
import top.met6.music.mobile.api.httpClient
import top.met6.music.mobile.storage.PlatformContext

actual class MediaSessionController actual constructor(context: PlatformContext) {
    private var onPlayCallback: (() -> Unit)? = null
    private var onPauseCallback: (() -> Unit)? = null
    private var onNextCallback: (() -> Unit)? = null
    private var onPrevCallback: (() -> Unit)? = null

    private var currentTitle = ""
    private var currentArtist = ""
    private var currentDuration = 0L
    private var currentArtwork: MPMediaItemArtwork? = null

    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        val commandCenter = MPRemoteCommandCenter.sharedCommandCenter()
        
        commandCenter.playCommand.addTargetWithHandler { _ ->
            onPlayCallback?.invoke()
            MPRemoteCommandHandlerStatusSuccess
        }
        
        commandCenter.pauseCommand.addTargetWithHandler { _ ->
            onPauseCallback?.invoke()
            MPRemoteCommandHandlerStatusSuccess
        }
        
        commandCenter.nextTrackCommand.addTargetWithHandler { _ ->
            onNextCallback?.invoke()
            MPRemoteCommandHandlerStatusSuccess
        }
        
        commandCenter.previousTrackCommand.addTargetWithHandler { _ ->
            onPrevCallback?.invoke()
            MPRemoteCommandHandlerStatusSuccess
        }
    }

    actual fun updateMetadata(title: String, artist: String, coverUrl: String, durationMs: Long) {
        currentTitle = title
        currentArtist = artist
        currentDuration = durationMs
        currentArtwork = null
        updateNowPlaying(false, 0L)

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
                    val uiImage = bytes.usePinned { pinned ->
                        val nsData = NSData.dataWithBytes(pinned.addressOf(0), length = bytes.size.toULong())
                        UIImage.imageWithData(nsData)
                    }
                    if (uiImage != null) {
                        currentArtwork = MPMediaItemArtwork(boundsSize = uiImage.size, requestHandler = { _ -> uiImage })
                        updateNowPlaying(false, 0L)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    actual fun updatePlaybackState(isPlaying: Boolean, positionMs: Long) {
        updateNowPlaying(isPlaying, positionMs)
    }

    private fun updateNowPlaying(isPlaying: Boolean, positionMs: Long) {
        val infoCenter = MPNowPlayingInfoCenter.defaultCenter()
        val info = mutableMapOf<Any?, Any?>()
        
        info[MPMediaItemPropertyTitle] = currentTitle
        info[MPMediaItemPropertyArtist] = currentArtist
        info[MPMediaItemPropertyPlaybackDuration] = currentDuration / 1000.0
        info[MPNowPlayingInfoPropertyElapsedPlaybackTime] = positionMs / 1000.0
        info[MPNowPlayingInfoPropertyPlaybackRate] = if (isPlaying) 1.0 else 0.0
        
        currentArtwork?.let {
            info[MPMediaItemPropertyArtwork] = it
        }
        
        infoCenter.nowPlayingInfo = info.toMap()
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
        MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo = null
    }
}
