package fr.rakambda.rakambdatoolbox.terminal.titlenotifier

import org.junit.Assert.assertEquals
import org.junit.Test

class TerminalTitleNotifierSettingsTest {
    @Test
    fun `parses one pattern per line, trimmed, without blanks nor duplicates`() {
        val text = "  ✳ \n\nWaiting\r\n   \nWaiting\nDone"

        assertEquals(listOf("✳", "Waiting", "Done"), TerminalTitleNotifierSettings.parsePatterns(text))
    }

    @Test
    fun `parses empty text to no pattern`() {
        assertEquals(emptyList<String>(), TerminalTitleNotifierSettings.parsePatterns(""))
    }
}
