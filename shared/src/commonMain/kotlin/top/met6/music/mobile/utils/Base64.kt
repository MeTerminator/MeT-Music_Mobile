package top.met6.music.mobile.utils

fun ByteArray.toBase64(): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
    val builder = StringBuilder()
    var i = 0
    while (i < size) {
        val b1 = this[i].toInt() and 0xFF
        i++
        if (i < size) {
            val b2 = this[i].toInt() and 0xFF
            i++
            if (i < size) {
                val b3 = this[i].toInt() and 0xFF
                i++
                builder.append(chars[b1 shr 2])
                builder.append(chars[((b1 and 0x03) shl 4) or (b2 shr 4)])
                builder.append(chars[((b2 and 0x0F) shl 2) or (b3 shr 6)])
                builder.append(chars[b3 and 0x3F])
            } else {
                builder.append(chars[b1 shr 2])
                builder.append(chars[((b1 and 0x03) shl 4) or (b2 shr 4)])
                builder.append(chars[(b2 and 0x0F) shl 2])
                builder.append('=')
            }
        } else {
            builder.append(chars[b1 shr 2])
            builder.append(chars[(b1 and 0x03) shl 4])
            builder.append("==")
        }
    }
    return builder.toString()
}

fun String.fromBase64(): ByteArray {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
    val lookup = IntArray(256) { -1 }
    for (idx in chars.indices) {
        lookup[chars[idx].code] = idx
    }
    
    val cleaned = this.replace("=", "").replace("\n", "").replace("\r", "")
    val len = cleaned.length
    val bytesCount = (len * 3) / 4
    val result = ByteArray(bytesCount)
    
    var rIdx = 0
    var i = 0
    while (i < len) {
        val c1 = lookup[cleaned[i].code]
        val c2 = if (i + 1 < len) lookup[cleaned[i + 1].code] else 0
        val c3 = if (i + 2 < len) lookup[cleaned[i + 2].code] else 0
        val c4 = if (i + 3 < len) lookup[cleaned[i + 3].code] else 0
        
        if (rIdx < bytesCount && c1 >= 0 && c2 >= 0) {
            result[rIdx++] = ((c1 shl 2) or (c2 shr 4)).toByte()
        }
        if (rIdx < bytesCount && c2 >= 0 && c3 >= 0) {
            result[rIdx++] = (((c2 and 0x0F) shl 4) or (c3 shr 2)).toByte()
        }
        if (rIdx < bytesCount && c3 >= 0 && c4 >= 0) {
            result[rIdx++] = (((c3 and 0x03) shl 6) or c4).toByte()
        }
        i += 4
    }
    return result
}
