package top.met6.music.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App(top.met6.music.mobile.storage.PlatformContext(applicationContext))
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App(top.met6.music.mobile.storage.PlatformContext(androidx.compose.ui.platform.LocalContext.current.applicationContext))
}