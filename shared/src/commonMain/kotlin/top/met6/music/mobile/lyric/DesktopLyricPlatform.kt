package top.met6.music.mobile.lyric

import top.met6.music.mobile.storage.PlatformContext

expect fun checkOverlayPermission(context: PlatformContext): Boolean
expect fun requestOverlayPermission(context: PlatformContext)
expect fun startFloatingLyricService(context: PlatformContext)
expect fun stopFloatingLyricService(context: PlatformContext)
expect fun isAndroid(): Boolean
