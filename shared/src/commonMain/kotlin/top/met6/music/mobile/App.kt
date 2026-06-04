package top.met6.music.mobile

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import top.met6.music.mobile.state.AppState
import top.met6.music.mobile.storage.PlatformContext
import top.met6.music.mobile.ui.AppNavigation

@Composable
fun App(context: PlatformContext) {
    LaunchedEffect(Unit) {
        AppState.init(context)
    }

    MaterialTheme {
        AppNavigation()
    }
}