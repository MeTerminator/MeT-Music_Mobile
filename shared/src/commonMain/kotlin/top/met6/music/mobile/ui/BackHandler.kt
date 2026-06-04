package top.met6.music.mobile.ui

import androidx.compose.runtime.Composable

@Composable
expect fun BindBackHandler(enabled: Boolean, onBack: () -> Unit)
