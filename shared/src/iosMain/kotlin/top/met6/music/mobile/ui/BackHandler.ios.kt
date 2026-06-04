package top.met6.music.mobile.ui

import androidx.compose.runtime.Composable

@Composable
actual fun BindBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No-op for iOS
}
