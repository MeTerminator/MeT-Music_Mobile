package top.met6.music.mobile.storage

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties

actual typealias PlatformContext = Unit

class JvmPlatformStorage : PlatformStorage {
    private val baseDir = File(System.getProperty("user.home"), ".met-music")
    private val prefsFile = File(baseDir, "settings.properties")
    private val properties = Properties()

    init {
        if (!baseDir.exists()) {
            baseDir.mkdirs()
        }
        if (prefsFile.exists()) {
            try {
                FileInputStream(prefsFile).use { properties.load(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveProperties() {
        try {
            FileOutputStream(prefsFile).use { properties.store(it, null) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun saveText(key: String, value: String) {
        properties.setProperty(key, value)
        saveProperties()
    }

    override fun getText(key: String): String? {
        return properties.getProperty(key)
    }

    override fun clearText(key: String) {
        properties.remove(key)
        saveProperties()
    }

    private fun getCacheDir(category: String): File {
        val dir = File(baseDir, "cache/$category")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    override suspend fun saveCacheFile(category: String, name: String, data: ByteArray) {
        val file = File(getCacheDir(category), name)
        file.writeBytes(data)
    }

    override suspend fun getCacheFile(category: String, name: String): ByteArray? {
        val file = File(getCacheDir(category), name)
        return if (file.exists()) file.readBytes() else null
    }

    override suspend fun getCacheUrl(category: String, name: String): String? {
        val file = File(getCacheDir(category), name)
        return if (file.exists()) file.toURI().toString() else null
    }

    override suspend fun clearCache(category: String) {
        val dir = getCacheDir(category)
        dir.deleteRecursively()
    }

    override suspend fun getCacheSize(category: String): Long {
        val dir = getCacheDir(category)
        return getFolderSize(dir)
    }

    private fun getFolderSize(file: File): Long {
        if (!file.exists()) return 0L
        if (file.isFile) return file.length()
        var size = 0L
        val files = file.listFiles() ?: return 0L
        for (f in files) {
            size += getFolderSize(f)
        }
        return size
    }

    override suspend fun listCacheFiles(category: String): List<String> {
        val dir = getCacheDir(category)
        val files = dir.listFiles() ?: return emptyList()
        return files.filter { it.isFile }.map { it.name }
    }

    override suspend fun deleteCacheFile(category: String, name: String): Boolean {
        val file = File(getCacheDir(category), name)
        return if (file.exists()) file.delete() else false
    }

    override suspend fun getCacheFileSize(category: String, name: String): Long {
        val file = File(getCacheDir(category), name)
        return if (file.exists()) file.length() else 0L
    }
}

actual fun getCurrentTimeMs(): Long = System.currentTimeMillis()


actual fun getPlatformStorage(context: PlatformContext): PlatformStorage {
    return JvmPlatformStorage()
}
