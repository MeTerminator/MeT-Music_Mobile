package top.met6.music.mobile.player

import top.met6.music.mobile.storage.PlatformContext

expect class MediaSessionController(context: PlatformContext) {
    fun updateMetadata(title: String, artist: String, coverUrl: String, durationMs: Long)
    fun updatePlaybackState(isPlaying: Boolean, positionMs: Long)
    fun setCallbacks(
        onPlay: () -> Unit,
        onPause: () -> Unit,
        onNext: () -> Unit,
        onPrev: () -> Unit
    )
    fun release()
}
