package top.met6.music.mobile.ui

import androidx.compose.runtime.Composable
import androidx.activity.compose.BackHandler

@Composable
actual fun BindBackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled, onBack)
}
