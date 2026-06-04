import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(projects.shared)

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)

    // JavaFX for Media Playback
    val openjfxVersion = "21.0.2"
    val osName = System.getProperty("os.name").lowercase()
    val platform = when {
        osName.contains("win") -> "win"
        osName.contains("mac") -> "mac"
        else -> "linux"
    }
    implementation("org.openjfx:javafx-base:$openjfxVersion:$platform")
    implementation("org.openjfx:javafx-graphics:$openjfxVersion:$platform")
    implementation("org.openjfx:javafx-controls:$openjfxVersion:$platform")
    implementation("org.openjfx:javafx-media:$openjfxVersion:$platform")
}

compose.desktop {
    application {
        mainClass = "top.met6.music.mobile.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "top.met6.music.mobile"
            packageVersion = "1.0.0"
        }
    }
}