package top.met6.music.mobile.state

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import top.met6.music.mobile.api.ApiClient
import top.met6.music.mobile.models.Playlist
import top.met6.music.mobile.models.Song
import top.met6.music.mobile.player.AudioPlayer
import top.met6.music.mobile.player.MediaSessionController
import top.met6.music.mobile.player.createAudioPlayer
import top.met6.music.mobile.lyric.LyricLine
import top.met6.music.mobile.lyric.LyricParser
import top.met6.music.mobile.storage.CacheManager
import top.met6.music.mobile.storage.PlatformContext
import top.met6.music.mobile.storage.PlatformStorage
import top.met6.music.mobile.storage.getPlatformStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

sealed class Screen {
    object Login : Screen()
    object Playlists : Screen()
    data class PlaylistDetail(val playlist: Playlist) : Screen()
    object Settings : Screen()
    object CacheManagerDetail : Screen()
}

enum class PlaybackMode {
    SEQUENCE,
    LOOP_LIST,
    SHUFFLE,
    LOOP_SINGLE
}

object AppState {
    lateinit var storage: CacheManager
    lateinit var player: AudioPlayer
    lateinit var platformStorage: PlatformStorage
    lateinit var mediaSessionController: MediaSessionController
    private val scope = CoroutineScope(Dispatchers.Main)
    private var isInitialized = false

    // --- Navigation ---
    val currentScreen = mutableStateOf<Screen>(Screen.Login)
    val navigationStack = mutableListOf<Screen>()

    fun navigateTo(screen: Screen) {
        navigationStack.add(currentScreen.value)
        currentScreen.value = screen
    }

    fun navigateBack(): Boolean {
        if (navigationStack.isNotEmpty()) {
            currentScreen.value = navigationStack.removeAt(navigationStack.size - 1)
            return true
        }
        return false
    }

    // --- User state ---
    val isLoggedIn = mutableStateOf(false)
    val qqId = mutableStateOf("")
    val nickname = mutableStateOf("未登录")
    val avatarUrl = mutableStateOf("")
    val playlists = mutableStateOf<List<Playlist>>(emptyList())
    val userPlaylistsLoading = mutableStateOf(false)

    // --- Playback Queue ---
    val currentQueue = mutableStateOf<List<Song>>(emptyList())
    val currentIndex = mutableStateOf(-1)
    val currentSong = mutableStateOf<Song?>(null)
    val savedPlaybackPositionMs = mutableStateOf(0L)
    val playbackMode = mutableStateOf(PlaybackMode.SEQUENCE)
    
    // --- Audio Details & Lyrics ---
    val lyricLines = mutableStateOf<List<LyricLine>>(emptyList())
    val currentLyricIndex = mutableStateOf(-1)
    val isFullScreenPlayer = mutableStateOf(false)
    val soundQuality = mutableStateOf("hq") // web, hq, sq, rs, dts, q360v1, q360v2, qai

    // --- Cache sizes ---
    val playlistCacheSize = mutableStateOf(0.0)
    val coverCacheSize = mutableStateOf(0.0)
    val songCacheSize = mutableStateOf(0.0)
    val lyricCacheSize = mutableStateOf(0.0)
    val currentThemeColor = mutableStateOf(Color(0xFF1E1E24))
    val lyricOffsetMs = mutableStateOf(0L)
    val lyricLeadMs = mutableStateOf(300L)
    val lyricFontSize = mutableStateOf(20)
    val translationFontSize = mutableStateOf(14)
    val useSpotifyFont = mutableStateOf(false)

    fun savePlaybackState() {
        try {
            val queue = currentQueue.value
            val index = currentIndex.value
            val pos = player.currentPositionMs.value
            
            platformStorage.saveText("playback_queue", Json.encodeToString(queue))
            platformStorage.saveText("playback_index", index.toString())
            val posToSave = if (pos > 0) pos else savedPlaybackPositionMs.value
            platformStorage.saveText("playback_position", posToSave.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadSavedPlaybackState() {
        val savedQueueJson = platformStorage.getText("playback_queue")
        val savedIndexStr = platformStorage.getText("playback_index")
        val savedPosStr = platformStorage.getText("playback_position")
        
        if (!savedQueueJson.isNullOrEmpty() && !savedIndexStr.isNullOrEmpty()) {
            try {
                val queue = Json.decodeFromString<List<Song>>(savedQueueJson)
                val index = savedIndexStr.toInt()
                if (index in queue.indices) {
                    currentQueue.value = queue
                    currentIndex.value = index
                    val song = queue[index]
                    currentSong.value = song
                    
                    val pos = savedPosStr?.toLongOrNull() ?: 0L
                    savedPlaybackPositionMs.value = pos
                    
                    // Pre-load lyrics for saved song
                    scope.launch {
                        try {
                            var lyricResponse = storage.getLyricFromCache(song.id)
                            if (lyricResponse == null) {
                                lyricResponse = ApiClient.getSongLyric(song.id)
                                storage.saveLyricToCache(song.id, lyricResponse)
                            }
                            lyricLines.value = LyricParser.parse(lyricResponse)
                            updateLyricIndex(pos)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
        savedPlaybackPositionMs.value = positionMs
        platformStorage.saveText("playback_position", positionMs.toString())
        updateLyricIndex(positionMs)
    }

    fun init(context: PlatformContext) {
        if (isInitialized) return
        isInitialized = true

        val storageImpl = getPlatformStorage(context)
        platformStorage = storageImpl
        storage = CacheManager(storageImpl)
        
        mediaSessionController = MediaSessionController(context)
        mediaSessionController.setCallbacks(
            onPlay = { resumeOrPlay() },
            onPause = { player.pause() },
            onNext = { nextSong() },
            onPrev = { prevSong() }
        )

        player = createAudioPlayer(context)
        player.onPlaybackComplete = {
            scope.launch {
                nextSong(isAutoPlay = true)
            }
        }

        // Read saved configs
        val savedQq = platformStorage.getText("qq_id")
        val savedQuality = platformStorage.getText("sound_quality")
        val savedMode = platformStorage.getText("playback_mode")
        val supportedQualities = setOf("web", "hq", "sq", "rs", "dts", "q360v1", "q360v2", "qai")
        if (!savedQuality.isNullOrEmpty() && supportedQualities.contains(savedQuality)) {
            soundQuality.value = savedQuality
        } else {
            soundQuality.value = "hq"
        }
        if (!savedMode.isNullOrEmpty()) {
            try {
                playbackMode.value = PlaybackMode.valueOf(savedMode)
            } catch (e: Exception) {}
        }

        val savedOffset = platformStorage.getText("lyric_offset")
        if (!savedOffset.isNullOrEmpty()) {
            lyricOffsetMs.value = savedOffset.toLongOrNull() ?: 0L
        } else {
            lyricOffsetMs.value = 0L
        }

        val savedLead = platformStorage.getText("lyric_lead")
        if (!savedLead.isNullOrEmpty()) {
            lyricLeadMs.value = savedLead.toLongOrNull() ?: 300L
        } else {
            lyricLeadMs.value = 300L
        }

        val savedLyricSize = platformStorage.getText("lyric_font_size")
        if (!savedLyricSize.isNullOrEmpty()) {
            lyricFontSize.value = savedLyricSize.toIntOrNull() ?: 20
        } else {
            lyricFontSize.value = 20
        }

        val savedTranslationSize = platformStorage.getText("translation_font_size")
        if (!savedTranslationSize.isNullOrEmpty()) {
            translationFontSize.value = savedTranslationSize.toIntOrNull() ?: 14
        } else {
            translationFontSize.value = 14
        }

        val savedSpotifyFont = platformStorage.getText("use_spotify_font")
        if (!savedSpotifyFont.isNullOrEmpty()) {
            useSpotifyFont.value = savedSpotifyFont.toBoolean()
        } else {
            useSpotifyFont.value = false
        }

        // Restore last playback state
        loadSavedPlaybackState()

        if (!savedQq.isNullOrEmpty()) {
            qqId.value = savedQq
            isLoggedIn.value = true
            currentScreen.value = Screen.Playlists
            loadUserPlaylists(savedQq)
        }

        // Start progress & lyrics tracker
        var ticks = 0
        var wasPlaying = false
        scope.launch {
            while (true) {
                val isPlaying = player.isPlaying.value
                if (isPlaying) {
                    val pos = player.currentPositionMs.value
                    updateLyricIndex(pos)
                    mediaSessionController.updatePlaybackState(true, pos)
                    
                    ticks++
                    if (ticks >= 150) { // Persist progress to disk approx every 4.5 seconds
                        platformStorage.saveText("playback_position", pos.toString())
                        ticks = 0
                    }
                } else if (wasPlaying) {
                    // Transitioned from playing to paused/stopped: write status immediately
                    platformStorage.saveText("playback_position", player.currentPositionMs.value.toString())
                    mediaSessionController.updatePlaybackState(false, player.currentPositionMs.value)
                }
                wasPlaying = isPlaying
                kotlinx.coroutines.delay(30)
            }
        }
        updateCacheSizes()
    }

    fun login(qqNumber: String, onResult: (Boolean, String) -> Unit) {
        if (qqNumber.trim().isEmpty()) {
            onResult(false, "请输入QQ号")
            return
        }
        userPlaylistsLoading.value = true
        scope.launch {
            try {
                // Fetch from network
                val response = ApiClient.getUserPlaylists(qqNumber)
                if (response.playlist.isNotEmpty()) {
                    // Success!
                    storage.savePlaylistsToCache(qqNumber, response.playlist)
                    
                    platformStorage.saveText("qq_id", qqNumber)
                    platformStorage.saveText("nickname", response.username ?: qqNumber)
                    platformStorage.saveText("avatar_url", response.avatarUrl ?: "")

                    qqId.value = qqNumber
                    nickname.value = response.username ?: qqNumber
                    avatarUrl.value = response.avatarUrl ?: ""
                    playlists.value = response.playlist
                    isLoggedIn.value = true
                    
                    userPlaylistsLoading.value = false
                    onResult(true, "登录成功")
                    navigateTo(Screen.Playlists)
                    updateCacheSizes()
                } else {
                    userPlaylistsLoading.value = false
                    onResult(false, "没有找到该用户的歌单")
                }
            } catch (e: Exception) {
                // Fallback to cache if available
                val cached = storage.getPlaylistsFromCache(qqNumber)
                if (cached != null) {
                    qqId.value = qqNumber
                    nickname.value = platformStorage.getText("nickname") ?: qqNumber
                    avatarUrl.value = platformStorage.getText("avatar_url") ?: ""
                    playlists.value = cached
                    isLoggedIn.value = true

                    userPlaylistsLoading.value = false
                    onResult(true, "网络已断开，已载入本地缓存歌单")
                    navigateTo(Screen.Playlists)
                } else {
                    userPlaylistsLoading.value = false
                    onResult(false, "登录失败，请检查网络或QQ号: ${e.message}")
                }
            }
        }
    }

    fun logout() {
        platformStorage.clearText("qq_id")
        platformStorage.clearText("nickname")
        platformStorage.clearText("avatar_url")
        
        isLoggedIn.value = false
        qqId.value = ""
        nickname.value = "未登录"
        avatarUrl.value = ""
        playlists.value = emptyList()
        currentQueue.value = emptyList()
        currentIndex.value = -1
        currentSong.value = null
        player.stop()

        navigationStack.clear()
        currentScreen.value = Screen.Login
    }

    fun loadUserPlaylists(qq: String) {
        userPlaylistsLoading.value = true
        scope.launch {
            try {
                val response = ApiClient.getUserPlaylists(qq)
                storage.savePlaylistsToCache(qq, response.playlist)
                playlists.value = response.playlist
                nickname.value = response.username ?: qq
                avatarUrl.value = response.avatarUrl ?: ""
            } catch (e: Exception) {
                // Load from cache
                storage.getPlaylistsFromCache(qq)?.let {
                    playlists.value = it
                }
            } finally {
                userPlaylistsLoading.value = false
            }
        }
    }
    fun setSoundQualityLevel(quality: String) {
        soundQuality.value = quality
        platformStorage.saveText("sound_quality", quality)
        
        // If playing, reload the song with new quality
        currentSong.value?.let { playSong(it) }
    }

    fun setPlaybackMode(mode: PlaybackMode) {
        playbackMode.value = mode
        platformStorage.saveText("playback_mode", mode.name)
    }

    fun setLyricOffset(offsetMs: Long) {
        lyricOffsetMs.value = offsetMs
        platformStorage.saveText("lyric_offset", offsetMs.toString())
    }

    fun setLyricLead(leadMs: Long) {
        lyricLeadMs.value = leadMs
        platformStorage.saveText("lyric_lead", leadMs.toString())
    }

    fun setLyricFontSize(size: Int) {
        lyricFontSize.value = size
        platformStorage.saveText("lyric_font_size", size.toString())
    }

    fun setTranslationFontSize(size: Int) {
        translationFontSize.value = size
        platformStorage.saveText("translation_font_size", size.toString())
    }

    fun setUseSpotifyFont(enabled: Boolean) {
        useSpotifyFont.value = enabled
        platformStorage.saveText("use_spotify_font", enabled.toString())
    }

    fun updateCacheSizes() {
        scope.launch {
            playlistCacheSize.value = storage.getCacheSizeMb("playlists")
            coverCacheSize.value = storage.getCacheSizeMb("covers")
            songCacheSize.value = storage.getCacheSizeMb("songs")
            lyricCacheSize.value = storage.getCacheSizeMb("lyrics")
        }
    }

    fun clearCacheCategory(category: String) {
        scope.launch {
            storage.clearCacheCategory(category)
            if (category == "songs") {
                storage.clearCacheCategory("songs_metadata")
            }
            updateCacheSizes()
            if (category == "playlists" && isLoggedIn.value) {
                loadUserPlaylists(qqId.value)
            }
        }
    }

    // --- Audio Playback Functions ---
 
    fun playPlaylist(songsList: List<Song>, startIndex: Int) {
        if (songsList.isEmpty()) return
        savedPlaybackPositionMs.value = 0L
        currentQueue.value = songsList
        playSongAtIndex(startIndex)
    }
 
    fun playSongAtIndex(index: Int) {
        val queue = currentQueue.value
        if (index < 0 || index >= queue.size) return
        if (index != currentIndex.value) {
            savedPlaybackPositionMs.value = 0L
        }
        currentIndex.value = index
        val song = queue[index]
        currentSong.value = song
        playSong(song)
    }
 
    fun nextSong(isAutoPlay: Boolean = false) {
        val queue = currentQueue.value
        if (queue.isEmpty()) return
        savedPlaybackPositionMs.value = 0L
        
        var nextIdx = currentIndex.value
        when (playbackMode.value) {
            PlaybackMode.SEQUENCE -> {
                nextIdx++
                if (nextIdx >= queue.size) {
                    if (isAutoPlay) {
                        player.stop()
                        return
                    }
                    nextIdx = 0
                }
            }
            PlaybackMode.LOOP_LIST -> {
                nextIdx = (nextIdx + 1) % queue.size
            }
            PlaybackMode.LOOP_SINGLE -> {
                if (!isAutoPlay) {
                    nextIdx = (nextIdx + 1) % queue.size
                }
            }
            PlaybackMode.SHUFFLE -> {
                if (queue.size > 1) {
                    var rand = queue.indices.random()
                    while (rand == currentIndex.value) {
                        rand = queue.indices.random()
                    }
                    nextIdx = rand
                } else {
                    nextIdx = 0
                }
            }
        }
        playSongAtIndex(nextIdx)
    }
 
    fun prevSong() {
        val queue = currentQueue.value
        if (queue.isEmpty()) return
        savedPlaybackPositionMs.value = 0L
        
        var prevIdx = currentIndex.value
        when (playbackMode.value) {
            PlaybackMode.SHUFFLE -> {
                if (queue.size > 1) {
                    var rand = queue.indices.random()
                    while (rand == currentIndex.value) {
                        rand = queue.indices.random()
                    }
                    prevIdx = rand
                } else {
                    prevIdx = 0
                }
            }
            else -> {
                prevIdx--
                if (prevIdx < 0) prevIdx = queue.size - 1
            }
        }
        playSongAtIndex(prevIdx)
    }
 
    fun resumeOrPlay() {
        val song = currentSong.value
        if (song != null && !player.isReady.value) {
            playSong(song)
        } else {
            player.resume()
        }
    }

    private fun playSong(song: Song) {
        player.stop()
        lyricLines.value = emptyList()
        currentLyricIndex.value = -1
 
        scope.launch {
            try {
                // Check local cache first
                var audioUrl = storage.getCachedSongUrl(song.id)
                
                if (audioUrl == null) {
                    var remoteUrl = storage.getCachedRemoteUrl(song.id, soundQuality.value)
                    if (remoteUrl == null) {
                        // 1. Fetch song url
                        val urlResponse = ApiClient.getSongUrl(song.id, soundQuality.value)
                        val rawUrl = urlResponse.data.firstOrNull()?.url
                        if (rawUrl.isNullOrEmpty()) {
                            nextSong() // skip on error
                            return@launch
                        }
                        remoteUrl = rawUrl
                        storage.saveRemoteUrlToCache(song.id, soundQuality.value, rawUrl)
                    }
     
                    // 2. Resolve cached audio file url
                    audioUrl = storage.getSongAudioUrl(song.id, remoteUrl)
                }
                
                // 3. Load & Parse lyrics
                var lyricResponse = storage.getLyricFromCache(song.id)
                if (lyricResponse == null) {
                    lyricResponse = ApiClient.getSongLyric(song.id)
                    storage.saveLyricToCache(song.id, lyricResponse)
                }
                lyricLines.value = LyricParser.parse(lyricResponse)

                mediaSessionController.updateMetadata(
                    title = song.name,
                    artist = song.artists.joinToString(" & ") { it.name },
                    coverUrl = song.bestCoverUrl,
                    durationMs = song.durationMs
                )
 
                // 4. Update cache size metrics
                updateCacheSizes()
 
                // 5. Play!
                player.play(audioUrl)

                // Save song metadata for cache management
                storage.saveSongMetadata(song, soundQuality.value)

                // Preload the next song
                val next = getNextSong()
                if (next != null) {
                    preloadSong(next)
                }
 
                // 6. Seek to startup position if needed
                val startupPos = savedPlaybackPositionMs.value
                if (startupPos > 0L) {
                    var checks = 0
                    while (!player.isReady.value && checks < 100) {
                        kotlinx.coroutines.delay(50)
                        checks++
                    }
                    player.seekTo(startupPos)
                    savedPlaybackPositionMs.value = 0L // reset
                }
 
                // 7. Save state
                savePlaybackState()
            } catch (e: Exception) {
                e.printStackTrace()
                nextSong() // skip on error
            }
        }
    }

    fun getNextSong(): Song? {
        val queue = currentQueue.value
        if (queue.isEmpty()) return null
        val currentIndexVal = currentIndex.value
        if (currentIndexVal < 0 || currentIndexVal >= queue.size) return null
        
        var nextIdx = currentIndexVal
        when (playbackMode.value) {
            PlaybackMode.SEQUENCE -> {
                nextIdx++
                if (nextIdx >= queue.size) {
                    nextIdx = 0
                }
            }
            PlaybackMode.LOOP_LIST -> {
                nextIdx = (nextIdx + 1) % queue.size
            }
            PlaybackMode.LOOP_SINGLE -> {
                nextIdx = (nextIdx + 1) % queue.size
            }
            PlaybackMode.SHUFFLE -> {
                nextIdx = (nextIdx + 1) % queue.size
            }
        }
        if (nextIdx in queue.indices) {
            return queue[nextIdx]
        }
        return null
    }

    fun preloadSong(song: Song) {
        scope.launch {
            try {
                // 1. Preload Cover Image
                if (song.bestCoverUrl.isNotEmpty()) {
                    storage.getCoverImageUrl(song.id, song.bestCoverUrl)
                }

                // 2. Save Song Metadata
                storage.saveSongMetadata(song, soundQuality.value)

                // 3. Preload Audio Link & File
                val cachedLocalUrl = storage.getCachedSongUrl(song.id)
                if (cachedLocalUrl == null) {
                    var remoteUrl = storage.getCachedRemoteUrl(song.id, soundQuality.value)
                    if (remoteUrl == null) {
                        val urlResponse = ApiClient.getSongUrl(song.id, soundQuality.value)
                        val rawUrl = urlResponse.data.firstOrNull()?.url
                        if (!rawUrl.isNullOrEmpty()) {
                            remoteUrl = rawUrl
                            storage.saveRemoteUrlToCache(song.id, soundQuality.value, rawUrl)
                        }
                    }
                    if (remoteUrl != null) {
                        storage.getSongAudioUrl(song.id, remoteUrl)
                    }
                }

                // 4. Preload Lyrics
                try {
                    var lyricResponse = storage.getLyricFromCache(song.id)
                    if (lyricResponse == null) {
                        lyricResponse = ApiClient.getSongLyric(song.id)
                        storage.saveLyricToCache(song.id, lyricResponse)
                    }
                } catch (e: Exception) {
                    // Ignore lyric preloading errors
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
 
    private fun updateLyricIndex(currentTimeMs: Long) {
        val adjustedTime = currentTimeMs + lyricOffsetMs.value + lyricLeadMs.value // dynamic offset + dynamic early transition lead
        val lines = lyricLines.value
        if (lines.isEmpty()) return
        
        var index = -1
        for (i in lines.indices) {
            val line = lines[i]
            if (adjustedTime >= line.startTimeMs && adjustedTime <= line.endTimeMs) {
                index = i
                break
            } else if (adjustedTime < line.startTimeMs) {
                // If it is before the first line or between lines
                if (i > 0) {
                    index = i - 1
                }
                break
            }
        }
        if (index == -1 && adjustedTime >= lines.last().endTimeMs) {
            index = lines.size - 1
        }
        currentLyricIndex.value = index
    }
}
