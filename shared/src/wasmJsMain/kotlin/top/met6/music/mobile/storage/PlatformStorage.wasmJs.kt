package top.met6.music.mobile.storage

import top.met6.music.mobile.utils.toBase64
import top.met6.music.mobile.utils.fromBase64
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual typealias PlatformContext = Unit

@JsFun("(key, value) => window.metMusicStorage.saveText(key, value)")
private external fun saveTextJs(key: String, value: String)

@JsFun("(key) => window.metMusicStorage.getText(key)")
private external fun getTextJs(key: String): String?

@JsFun("(key) => window.metMusicStorage.clearText(key)")
private external fun clearTextJs(key: String)

@JsFun("(category, name, base64, callback) => { window.metMusicStorage.saveCacheFile(category, name, base64).then(() => callback(true), () => callback(false)); }")
private external fun saveCacheFileJs(category: String, name: String, base64: String, callback: (Boolean) -> Unit)

@JsFun("(category, name, callback) => { window.metMusicStorage.getCacheFile(category, name).then(res => callback(res), () => callback(null)); }")
private external fun getCacheFileJs(category: String, name: String, callback: (String?) -> Unit)

@JsFun("(category, name, callback) => { window.metMusicStorage.getCacheUrl(category, name).then(res => callback(res), () => callback(null)); }")
private external fun getCacheUrlJs(category: String, name: String, callback: (String?) -> Unit)

@JsFun("(category, callback) => { window.metMusicStorage.clearCache(category).then(() => callback(), () => callback()); }")
private external fun clearCacheJs(category: String, callback: () -> Unit)

@JsFun("(category, callback) => { window.metMusicStorage.getCacheSize(category).then(res => callback(res), () => callback(0)); }")
private external fun getCacheSizeJs(category: String, callback: (Double) -> Unit)

class WasmPlatformStorage : PlatformStorage {
    override fun saveText(key: String, value: String) {
        saveTextJs(key, value)
    }

    override fun getText(key: String): String? {
        return getTextJs(key)
    }

    override fun clearText(key: String) {
        clearTextJs(key)
    }

    override suspend fun saveCacheFile(category: String, name: String, data: ByteArray) = suspendCancellableCoroutine<Unit> { cont ->
        saveCacheFileJs(category, name, data.toBase64()) {
            if (cont.isActive) cont.resume(Unit)
        }
    }

    override suspend fun getCacheFile(category: String, name: String): ByteArray? = suspendCancellableCoroutine { cont ->
        getCacheFileJs(category, name) { base64 ->
            if (cont.isActive) {
                if (base64 != null) {
                    cont.resume(base64.fromBase64())
                } else {
                    cont.resume(null)
                }
            }
        }
    }

    override suspend fun getCacheUrl(category: String, name: String): String? = suspendCancellableCoroutine { cont ->
        getCacheUrlJs(category, name) { url ->
            if (cont.isActive) cont.resume(url)
        }
    }

    override suspend fun clearCache(category: String) = suspendCancellableCoroutine<Unit> { cont ->
        clearCacheJs(category) {
            if (cont.isActive) cont.resume(Unit)
        }
    }

    override suspend fun getCacheSize(category: String): Long = suspendCancellableCoroutine { cont ->
        getCacheSizeJs(category) { size ->
            if (cont.isActive) cont.resume(size.toLong())
        }
    }
}

actual fun getPlatformStorage(context: PlatformContext): PlatformStorage {
    return WasmPlatformStorage()
}
