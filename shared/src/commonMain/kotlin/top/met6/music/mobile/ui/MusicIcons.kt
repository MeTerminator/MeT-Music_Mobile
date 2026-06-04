package top.met6.music.mobile.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object MusicIcons {
    val Play: ImageVector by lazy {
        ImageVector.Builder(
            name = "Play",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(fill = SolidColor(Color.White)) {
            moveTo(8f, 5f)
            lineTo(19f, 12f)
            lineTo(8f, 19f)
            close()
        }.build()
    }

    val Pause: ImageVector by lazy {
        ImageVector.Builder(
            name = "Pause",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(fill = SolidColor(Color.White)) {
            // Left bar
            moveTo(6f, 19f)
            horizontalLineTo(10f)
            verticalLineTo(5f)
            horizontalLineTo(6f)
            close()
            // Right bar
            moveTo(14f, 5f)
            verticalLineTo(19f)
            horizontalLineTo(18f)
            verticalLineTo(5f)
            close()
        }.build()
    }

    val SkipNext: ImageVector by lazy {
        ImageVector.Builder(
            name = "SkipNext",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(fill = SolidColor(Color.White)) {
            moveTo(6f, 18f)
            lineTo(14.5f, 12f)
            lineTo(6f, 6f)
            close()
            moveTo(16f, 6f)
            horizontalLineTo(18f)
            verticalLineTo(18f)
            horizontalLineTo(16f)
            close()
        }.build()
    }

    val SkipPrevious: ImageVector by lazy {
        ImageVector.Builder(
            name = "SkipPrevious",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(fill = SolidColor(Color.White)) {
            moveTo(6f, 6f)
            horizontalLineTo(8f)
            verticalLineTo(18f)
            horizontalLineTo(6f)
            close()
            moveTo(9.5f, 12f)
            lineTo(18f, 18f)
            verticalLineTo(6f)
            close()
        }.build()
    }

    val Shuffle: ImageVector by lazy {
        ImageVector.Builder(
            name = "Shuffle",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(fill = SolidColor(Color.White)) {
            moveTo(10.59f, 9.17f)
            lineTo(5.41f, 4f)
            lineTo(4f, 5.41f)
            lineTo(9.17f, 10.58f)
            lineTo(10.59f, 9.17f)
            close()
            moveTo(14.5f, 4f)
            lineTo(16.54f, 6.04f)
            lineTo(4f, 18.59f)
            lineTo(5.41f, 20f)
            lineTo(17.96f, 7.46f)
            lineTo(20f, 9.5f)
            verticalLineTo(4f)
            horizontalLineTo(14.5f)
            close()
            moveTo(14.83f, 13.41f)
            lineTo(13.42f, 14.82f)
            lineTo(16.55f, 17.95f)
            lineTo(14.5f, 20f)
            horizontalLineTo(20f)
            verticalLineTo(14.5f)
            lineTo(17.96f, 16.54f)
            lineTo(14.83f, 13.41f)
            close()
        }.build()
    }

    val Repeat: ImageVector by lazy {
        ImageVector.Builder(
            name = "Repeat",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(fill = SolidColor(Color.White)) {
            moveTo(7f, 7f)
            horizontalLineTo(17f)
            verticalLineTo(10f)
            lineTo(21f, 6f)
            lineTo(17f, 2f)
            verticalLineTo(5f)
            horizontalLineTo(5f)
            verticalLineTo(11f)
            horizontalLineTo(7f)
            verticalLineTo(7f)
            close()
            moveTo(17f, 17f)
            horizontalLineTo(7f)
            verticalLineTo(14f)
            lineTo(3f, 18f)
            lineTo(7f, 22f)
            verticalLineTo(19f)
            horizontalLineTo(19f)
            verticalLineTo(13f)
            horizontalLineTo(17f)
            verticalLineTo(17f)
            close()
        }.build()
    }

    val RepeatOne: ImageVector by lazy {
        ImageVector.Builder(
            name = "RepeatOne",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(fill = SolidColor(Color.White)) {
            moveTo(7f, 7f)
            horizontalLineTo(17f)
            verticalLineTo(10f)
            lineTo(21f, 6f)
            lineTo(17f, 2f)
            verticalLineTo(5f)
            horizontalLineTo(5f)
            verticalLineTo(11f)
            horizontalLineTo(7f)
            verticalLineTo(7f)
            close()
            moveTo(17f, 17f)
            horizontalLineTo(7f)
            verticalLineTo(14f)
            lineTo(3f, 18f)
            lineTo(7f, 22f)
            verticalLineTo(19f)
            horizontalLineTo(19f)
            verticalLineTo(13f)
            horizontalLineTo(17f)
            verticalLineTo(17f)
            close()
            moveTo(13f, 15f)
            verticalLineTo(9f)
            horizontalLineTo(12f)
            lineTo(10f, 10.5f)
            verticalLineTo(11.75f)
            lineTo(12f, 10.25f)
            verticalLineTo(15f)
            horizontalLineTo(13f)
            close()
        }.build()
    }
}
