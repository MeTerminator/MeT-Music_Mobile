package top.met6.music.mobile.lyric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import top.met6.music.mobile.models.LyricResponse

class LyricParserTest {

    @Test
    fun testParseQrc() {
        val qrcData = """<?xml version="1.0" encoding="utf-8"?>
<QrcInfos>
<QrcHeadInfo SaveTime="1730644424" Version="100"/>
<LyricInfo LyricCount="1">
<Lyric_1 LyricType="1" LyricContent="[ti:潮声回响]
[ar:洛天依]
[al:]
[by:krc转qrc工具]
[offset:0]
[168,257]洛(168,0)天(168,51)依 (219,51)- (270,0)潮(270,51)声(321,52)回(373,0)响(373,52)
[23501,4848]绞(23501,304)断(23805,357)的(24162,432)弦 (24594,795)能(25695,255)否(25950,465)重(26415,254)新(26669,357)演(27026,458)奏(27484,865)
"/>
</LyricInfo>
</QrcInfos>"""

        val response = LyricResponse(qrc = qrcData)
        val lines = LyricParser.parse(response)

        println("Parsed lines count: ${'$'}{lines.size}")
        for (line in lines) {
            println("Line: startTime=${'$'}{line.startTimeMs}, endTime=${'$'}{line.endTimeMs}, text='${'$'}{line.text}'")
            for (syllable in line.syllables) {
                println("  Syllable: text='${'$'}{syllable.text}', start=${'$'}{syllable.timeOffsetMs}, duration=${'$'}{syllable.durationMs}")
            }
        }

        assertTrue(lines.isNotEmpty())
        assertEquals(2, lines.size)

        val firstLine = lines[0]
        assertEquals(168L, firstLine.startTimeMs)
        assertEquals(425L, firstLine.endTimeMs)
        assertEquals("洛天依 - 潮声回响", firstLine.text)

        val secondLine = lines[1]
        assertEquals(23501L, secondLine.startTimeMs)
        assertEquals(28349L, secondLine.endTimeMs)
        assertEquals("绞断的弦 能否重新演奏", secondLine.text)
    }
}
