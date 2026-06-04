package top.met6.music.mobile.storage

expect class PlatformContext

interface PlatformStorage {
    fun saveText(key: String, value: String)
    fun getText(key: String): String?
    fun clearText(key: String)
    
    suspend fun saveCacheFile(category: String, name: String, data: ByteArray)
    suspend fun getCacheFile(category: String, name: String): ByteArray?
    suspend fun getCacheUrl(category: String, name: String): String?
    suspend fun clearCache(category: String)
    suspend fun getCacheSize(category: String): Long

    suspend fun listCacheFiles(category: String): List<String>
    suspend fun deleteCacheFile(category: String, name: String): Boolean
    suspend fun getCacheFileSize(category: String, name: String): Long
}

expect fun getCurrentTimeMs(): Long

expect fun getPlatformStorage(context: PlatformContext): PlatformStorage
