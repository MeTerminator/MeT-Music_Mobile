package top.met6.music.mobile.lyric

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import top.met6.music.mobile.storage.PlatformContext

actual fun checkOverlayPermission(context: PlatformContext): Boolean {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
        Settings.canDrawOverlays(context.context)
    } else {
        true
    }
}

actual fun requestOverlayPermission(context: PlatformContext) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.context.packageName}")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.context.startActivity(intent)
    }
}

actual fun startFloatingLyricService(context: PlatformContext) {
    val intent = Intent(context.context, FloatingLyricService::class.java)
    context.context.startService(intent)
}

actual fun stopFloatingLyricService(context: PlatformContext) {
    val intent = Intent(context.context, FloatingLyricService::class.java)
    context.context.stopService(intent)
}

actual fun isAndroid(): Boolean = true
