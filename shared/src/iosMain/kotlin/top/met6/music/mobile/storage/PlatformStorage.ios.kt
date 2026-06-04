@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
package top.met6.music.mobile.storage

import platform.Foundation.*
import kotlinx.cinterop.*
import platform.posix.memcpy

actual typealias PlatformContext = Unit

class IosPlatformStorage : PlatformStorage {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun saveText(key: String, value: String) {
        defaults.setObject(value, key)
    }

    override fun getText(key: String): String? {
        return defaults.stringForKey(key)
    }

    override fun clearText(key: String) {
        defaults.removeObjectForKey(key)
    }

    private fun getCacheDir(category: String): String {
        val fileManager = NSFileManager.defaultManager
        val urls = fileManager.URLsForDirectory(NSCachesDirectory, NSUserDomainMask)
        val cacheUrl = urls.first() as NSURL
        val dirUrl = cacheUrl.URLByAppendingPathComponent("met_music_cache/$category")!!
        val path = dirUrl.path!!
        if (!fileManager.fileExistsAtPath(path)) {
            fileManager.createDirectoryAtPath(path, withIntermediateDirectories = true, attributes = null, error = null)
        }
        return path
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun saveCacheFile(category: String, name: String, data: ByteArray) {
        val path = "${getCacheDir(category)}/$name"
        val nsData = data.toNSData()
        nsData.writeToFile(path, atomically = true)
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun getCacheFile(category: String, name: String): ByteArray? {
        val path = "${getCacheDir(category)}/$name"
        val fileManager = NSFileManager.defaultManager
        if (!fileManager.fileExistsAtPath(path)) return null
        val nsData = NSData.dataWithContentsOfFile(path) ?: return null
        return nsData.toByteArray()
    }

    override suspend fun getCacheUrl(category: String, name: String): String? {
        val path = "${getCacheDir(category)}/$name"
        val fileManager = NSFileManager.defaultManager
        return if (fileManager.fileExistsAtPath(path)) path else null
    }

    override suspend fun clearCache(category: String) {
        val path = getCacheDir(category)
        val fileManager = NSFileManager.defaultManager
        fileManager.removeItemAtPath(path, error = null)
    }

    override suspend fun getCacheSize(category: String): Long {
        val path = getCacheDir(category)
        val fileManager = NSFileManager.defaultManager
        return getFolderSize(path, fileManager)
    }

    private fun getFolderSize(path: String, fileManager: NSFileManager): Long {
        val exists = fileManager.fileExistsAtPath(path)
        if (!exists) return 0L
        
        val attrs = fileManager.attributesOfItemAtPath(path, error = null) ?: return 0L
        val fileType = attrs[NSFileType] as? String
        if (fileType != NSFileTypeDirectory) {
            return (attrs[NSFileSize] as? NSNumber)?.longValue ?: 0L
        }
        
        var size = 0L
        val contents = fileManager.contentsOfDirectoryAtPath(path, error = null) ?: return 0L
        for (item in contents) {
            val subPath = "$path/$item"
            size += getFolderSize(subPath, fileManager)
        }
        return size
    }
}

@OptIn(ExperimentalForeignApi::class)
fun ByteArray.toNSData(): NSData {
    if (isEmpty()) return NSData()
    val pinned = pin()
    return NSData.dataWithBytes(pinned.addressOf(0), length = size.toULong())
}

@OptIn(ExperimentalForeignApi::class)
fun NSData.toByteArray(): ByteArray {
    val len = length.toInt()
    if (len == 0) return ByteArray(0)
    val bytes = ByteArray(len)
    bytes.usePinned { pinned ->
        memcpy(pinned.addressOf(0), this.bytes, length)
    }
    return bytes
}

actual fun getPlatformStorage(context: PlatformContext): PlatformStorage {
    return IosPlatformStorage()
}
