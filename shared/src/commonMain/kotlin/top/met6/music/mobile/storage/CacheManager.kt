package top.met6.music.mobile.storage

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.call.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import top.met6.music.mobile.api.httpClient
import top.met6.music.mobile.models.Playlist
import top.met6.music.mobile.models.Song

class CacheManager(private val storage: PlatformStorage) {
    private val downloadScope = CoroutineScope(Dispatchers.Default)
    private val activeDownloadsMutex = Mutex()
    private val activeDownloads = mutableSetOf<String>()

    suspend fun savePlaylistsToCache(uid: String, playlists: List<Playlist>) {
        try {
            val json = Json.encodeToString(playlists)
            storage.saveCacheFile("playlists", "playlists_$uid.json", json.encodeToByteArray())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getPlaylistsFromCache(uid: String): List<Playlist>? {
        val bytes = storage.getCacheFile("playlists", "playlists_$uid.json") ?: return null
        return try {
            val json = bytes.decodeToString()
            Json.decodeFromString<List<Playlist>>(json)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun savePlaylistTracksToCache(playlistId: String, songs: List<Song>) {
        try {
            val json = Json.encodeToString(songs)
            storage.saveCacheFile("playlists", "tracks_$playlistId.json", json.encodeToByteArray())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getPlaylistTracksFromCache(playlistId: String): List<Song>? {
        val bytes = storage.getCacheFile("playlists", "tracks_$playlistId.json") ?: return null
        return try {
            val json = bytes.decodeToString()
            Json.decodeFromString<List<Song>>(json)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getCoverImageUrl(songId: String, remoteUrl: String): String {
        if (remoteUrl.isEmpty()) return ""
        val cachedUrl = storage.getCacheUrl("covers", songId)
        if (cachedUrl != null) {
            return cachedUrl
        }
        
        try {
            val response: HttpResponse = httpClient.get(remoteUrl)
            val bytes = response.body<ByteArray>()
            storage.saveCacheFile("covers", songId, bytes)
            return storage.getCacheUrl("covers", songId) ?: remoteUrl
        } catch (e: Exception) {
            e.printStackTrace()
            return remoteUrl
        }
    }

    suspend fun getCoverImageBytes(songId: String, remoteUrl: String): ByteArray? {
        if (remoteUrl.isEmpty()) return null
        val secureUrl = if (remoteUrl.startsWith("http://")) {
            remoteUrl.replace("http://", "https://")
        } else {
            remoteUrl
        }
        val cachedBytes = storage.getCacheFile("covers", songId)
        if (cachedBytes != null) {
            return cachedBytes
        }
        try {
            val response: HttpResponse = httpClient.get(secureUrl)
            val bytes = response.body<ByteArray>()
            storage.saveCacheFile("covers", songId, bytes)
            return bytes
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    suspend fun getCachedSongUrl(songId: String): String? {
        return storage.getCacheUrl("songs", songId)
    }

    suspend fun getSongAudioUrl(songId: String, remoteUrl: String): String {
        if (remoteUrl.isEmpty()) return ""
        val cachedUrl = storage.getCacheUrl("songs", songId)
        if (cachedUrl != null) {
            return cachedUrl
        }
        
        activeDownloadsMutex.withLock {
            if (!activeDownloads.contains(songId)) {
                activeDownloads.add(songId)
                downloadScope.launch {
                    try {
                        val response: HttpResponse = httpClient.get(remoteUrl)
                        val bytes = response.body<ByteArray>()
                        storage.saveCacheFile("songs", songId, bytes)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        activeDownloadsMutex.withLock {
                            activeDownloads.remove(songId)
                        }
                    }
                }
            }
        }
        
        return remoteUrl
    }

    suspend fun getCacheSizeMb(category: String): Double {
        val bytes = storage.getCacheSize(category)
        return bytes.toDouble() / (1024.0 * 1024.0)
    }

    suspend fun clearCacheCategory(category: String) {
        storage.clearCache(category)
    }
}
