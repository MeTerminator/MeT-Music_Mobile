package top.met6.music.mobile.player

import top.met6.music.mobile.storage.PlatformContext

actual class MediaSessionController actual constructor(context: PlatformContext) {
    actual fun updateMetadata(title: String, artist: String, coverUrl: String, durationMs: Long) {}
    actual fun updatePlaybackState(isPlaying: Boolean, positionMs: Long) {}
    actual fun setCallbacks(
        onPlay: () -> Unit,
        onPause: () -> Unit,
        onNext: () -> Unit,
        onPrev: () -> Unit
    ) {}
    actual fun release() {}
}
