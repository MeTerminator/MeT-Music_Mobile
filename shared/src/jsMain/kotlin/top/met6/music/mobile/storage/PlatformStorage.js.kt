package top.met6.music.mobile.storage

import top.met6.music.mobile.utils.toBase64
import top.met6.music.mobile.utils.fromBase64
import kotlinx.coroutines.await

actual typealias PlatformContext = Unit

class JsPlatformStorage : PlatformStorage {
    override fun saveText(key: String, value: String) {
        val storage = js("window.metMusicStorage")
        storage.saveText(key, value)
    }

    override fun getText(key: String): String? {
        val storage = js("window.metMusicStorage")
        return storage.getText(key) as? String
    }

    override fun clearText(key: String) {
        val storage = js("window.metMusicStorage")
        storage.clearText(key)
    }

    override suspend fun saveCacheFile(category: String, name: String, data: ByteArray) {
        val storage = js("window.metMusicStorage")
        val base64 = data.toBase64()
        val promise = storage.saveCacheFile(category, name, base64) as kotlin.js.Promise<Unit>
        promise.await()
    }

    override suspend fun getCacheFile(category: String, name: String): ByteArray? {
        val storage = js("window.metMusicStorage")
        val promise = storage.getCacheFile(category, name) as kotlin.js.Promise<String?>
        val base64 = promise.await() ?: return null
        return base64.fromBase64()
    }

    override suspend fun getCacheUrl(category: String, name: String): String? {
        val storage = js("window.metMusicStorage")
        val promise = storage.getCacheUrl(category, name) as kotlin.js.Promise<String?>
        return promise.await()
    }

    override suspend fun clearCache(category: String) {
        val storage = js("window.metMusicStorage")
        val promise = storage.clearCache(category) as kotlin.js.Promise<Unit>
        promise.await()
    }

    override suspend fun getCacheSize(category: String): Long {
        val storage = js("window.metMusicStorage")
        val promise = storage.getCacheSize(category) as kotlin.js.Promise<Double>
        return promise.await().toLong()
    }

    override suspend fun listCacheFiles(category: String): List<String> = emptyList()
    override suspend fun deleteCacheFile(category: String, name: String): Boolean = false
    override suspend fun getCacheFileSize(category: String, name: String): Long = 0L
}

actual fun getCurrentTimeMs(): Long = kotlin.js.Date.now().toLong()


actual fun getPlatformStorage(context: PlatformContext): PlatformStorage {
    return JsPlatformStorage()
}
