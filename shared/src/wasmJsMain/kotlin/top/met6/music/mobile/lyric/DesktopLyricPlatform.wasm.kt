package top.met6.music.mobile.lyric

import top.met6.music.mobile.storage.PlatformContext

actual fun checkOverlayPermission(context: PlatformContext): Boolean = false
actual fun requestOverlayPermission(context: PlatformContext) {}
actual fun startFloatingLyricService(context: PlatformContext) {}
actual fun stopFloatingLyricService(context: PlatformContext) {}
actual fun isAndroid(): Boolean = false
