package fr.mwet.tools.translationpreview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TextMatcherTest {
    private val matcher = TextMatcher(mapOf("best" to "Best: {0}", "play" to "Play!", "quote" to "L''écran {0}"))

    @Test
    fun `matches a plain line`() {
        assertEquals("play", matcher.match("Play!")?.key)
    }

    @Test
    fun `matches a formatted line and keeps its arguments`() {
        val shown = matcher.match("Best: 42")!!
        assertEquals("best", shown.key)
        assertEquals("Best (42)", display("Best ({0})", shown.arguments))
    }

    @Test
    fun `reads doubled quotes as one quote in a formatted line`() {
        assertEquals("quote", matcher.match("L'écran 2")?.key)
    }

    @Test
    fun `ignores a text that is not a translation`() {
        assertNull(matcher.match("42"))
    }
}
