package fr.rakambda.rakambdatoolbox.terminal.titlenotifier

import org.junit.Assert.assertEquals
import org.junit.Test

class TitleMatchTrackerTest {
    private val patterns = listOf("✳", "Waiting")

    @Test
    fun `reports a pattern when it appears`() {
        val tracker = TitleMatchTracker()

        assertEquals(emptySet<String>(), tracker.update(listOf("bash"), patterns))
        assertEquals(setOf("✳"), tracker.update(listOf("✳ Claude Code"), patterns))
    }

    @Test
    fun `does not report a pattern again while it stays present`() {
        val tracker = TitleMatchTracker()
        tracker.update(listOf("✳ Claude Code"), patterns)

        assertEquals(emptySet<String>(), tracker.update(listOf("✳ Claude Code - task"), patterns))
    }

    @Test
    fun `reports a pattern again after it disappeared`() {
        val tracker = TitleMatchTracker()
        tracker.update(listOf("✳ Claude Code"), patterns)
        tracker.update(listOf("⠂ Claude Code"), patterns)

        assertEquals(setOf("✳"), tracker.update(listOf("✳ Claude Code"), patterns))
    }

    @Test
    fun `reports only the newly matched patterns`() {
        val tracker = TitleMatchTracker()
        tracker.update(listOf("✳ Claude Code"), patterns)

        assertEquals(setOf("Waiting"), tracker.update(listOf("✳ Waiting"), patterns))
    }

    @Test
    fun `matches any of the titles`() {
        val tracker = TitleMatchTracker()

        assertEquals(setOf("Waiting"), tracker.update(listOf("Local", "Waiting for input"), patterns))
    }

    @Test
    fun `is case-sensitive`() {
        val tracker = TitleMatchTracker()

        assertEquals(emptySet<String>(), tracker.update(listOf("waiting"), patterns))
    }
}
