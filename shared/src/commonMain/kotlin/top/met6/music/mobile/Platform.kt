package top.met6.music.mobile

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform