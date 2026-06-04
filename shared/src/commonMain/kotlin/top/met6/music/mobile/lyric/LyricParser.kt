package top.met6.music.mobile.lyric

import top.met6.music.mobile.models.LyricResponse

data class Syllable(
    val text: String,
    val timeOffsetMs: Long,
    val durationMs: Long
)

data class LyricLine(
    val startTimeMs: Long,
    val endTimeMs: Long,
    val text: String,
    val syllables: List<Syllable> = emptyList(),
    var translation: String? = null,
    var romalrc: String? = null
)

object LyricParser {

    fun parse(response: LyricResponse): List<LyricLine> {
        val qrcString = response.qrc
        if (!qrcString.isNullOrEmpty()) {
            val qrcLines = parseQrc(qrcString)
            if (qrcLines.isNotEmpty()) {
                val translationLrc = response.qrctrans ?: response.lrctrans
                if (!translationLrc.isNullOrEmpty()) {
                    alignTranslations(qrcLines, parseLrc(translationLrc))
                }
                val romaLrc = response.qrcroma
                if (!romaLrc.isNullOrEmpty()) {
                    alignRoma(qrcLines, parseLrc(romaLrc))
                }
                return qrcLines
            }
        }

        val lrcString = response.lrc
        if (!lrcString.isNullOrEmpty()) {
            val lrcLines = parseLrc(lrcString)
            val translationLrc = response.lrctrans
            if (!translationLrc.isNullOrEmpty()) {
                alignTranslations(lrcLines, parseLrc(translationLrc))
            }
            return lrcLines
        }

        return emptyList()
    }

    private fun decodeXmlEntities(text: String): String {
        return text.replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&#39;", "'")
    }

    private fun parseLrc(lrcText: String): List<LyricLine> {
        val lines = lrcText.split("\n", "\r\n")
        val result = mutableListOf<LyricLine>()
        val regex = Regex("""^\[(\d+):(\d+)(?:\.(\d+))?\](.*)$""")

        for (line in lines) {
            val match = regex.matchEntire(line.trim()) ?: continue
            val min = match.groupValues[1].toLong()
            val sec = match.groupValues[2].toLong()
            val msStr = match.groupValues[3]
            val ms = if (msStr.isEmpty()) 0L else {
                val padded = msStr.padEnd(3, '0').substring(0, 3)
                padded.toLong()
            }
            val startTimeMs = min * 60 * 1000 + sec * 1000 + ms
            val text = decodeXmlEntities(match.groupValues[4].trim())
            if (text.isNotEmpty() && !text.contains("纯音乐，请您欣赏")) {
                result.add(LyricLine(
                    startTimeMs = startTimeMs,
                    endTimeMs = startTimeMs + 3000L,
                    text = text
                ))
            }
        }

        for (i in 0 until result.size - 1) {
            val current = result[i]
            val next = result[i + 1]
            result[i] = current.copy(endTimeMs = next.startTimeMs)
        }
        return result
    }

    private fun parseQrc(qrcText: String): List<LyricLine> {
        val lines = qrcText.split("\n", "\r\n")
        val result = mutableListOf<LyricLine>()
        val lineTimeRegex = Regex("""^\[(\d+),(\d+)\]""")
        val syllableRegex = Regex("""([^{()]*)[{()](\d+),(\d+)[})]""")

        for (line in lines) {
            val match = lineTimeRegex.find(line) ?: continue
            val startTimeMs = match.groupValues[1].toLong()
            val durationMs = match.groupValues[2].toLong()
            val endTimeMs = startTimeMs + durationMs
            
            val content = line.substring(match.value.length).trim()
            if (content.isEmpty()) continue

            val syllables = mutableListOf<Syllable>()
            val matches = syllableRegex.findAll(content)
            val fullTextBuilder = StringBuilder()

            for (sMatch in matches) {
                val text = decodeXmlEntities(sMatch.groupValues[1])
                val offset = sMatch.groupValues[2].toLong()
                val duration = sMatch.groupValues[3].toLong()
                
                syllables.add(Syllable(text, offset, duration))
                fullTextBuilder.append(text)
            }

            val displayText = if (syllables.isEmpty()) decodeXmlEntities(content) else fullTextBuilder.toString()
            if (displayText.isNotEmpty()) {
                result.add(LyricLine(
                    startTimeMs = startTimeMs,
                    endTimeMs = endTimeMs,
                    text = displayText,
                    syllables = syllables
                ))
            }
        }
        return result
    }

    private fun alignTranslations(base: List<LyricLine>, translations: List<LyricLine>) {
        for (line in base) {
            val match = translations.firstOrNull { 
                kotlin.math.abs(it.startTimeMs - line.startTimeMs) < 150 
            }
            if (match != null && match.text != "//" && match.text.isNotEmpty()) {
                line.translation = match.text
            }
        }
    }

    private fun alignRoma(base: List<LyricLine>, roma: List<LyricLine>) {
        for (line in base) {
            val match = roma.firstOrNull { 
                kotlin.math.abs(it.startTimeMs - line.startTimeMs) < 150 
            }
            if (match != null && match.text != "//" && match.text.isNotEmpty()) {
                line.romalrc = match.text
            }
        }
    }
}
