package fr.mwet.tools.assetcataloggenerator.reader

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TranslationTableReaderTest {
    @Test
    fun `reads quoted cells with commas, doubled quotes and line breaks`() {
        val table = parseTranslationTable(
            "key,en,fr\r\n" +
                "play,Play!,Jouer !\n" +
                "tapToStart,\"Tap, swipe\",\"Touche, glisse\"\n" +
                "quote,\"Say \"\"hi\"\"\",\"Dis \"\"salut\"\"\"\n" +
                "lines,\"one\ntwo\",\"un\ndeux\"\n",
            "test.csv",
        )

        assertEquals(listOf("en", "fr"), table.languages)
        assertEquals(
            listOf(
                TranslationRow("play", listOf("Play!", "Jouer !")),
                TranslationRow("tapToStart", listOf("Tap, swipe", "Touche, glisse")),
                TranslationRow("quote", listOf("Say \"hi\"", "Dis \"salut\"")),
                TranslationRow("lines", listOf("one\ntwo", "un\ndeux")),
            ),
            table.rows,
        )
    }

    @Test
    fun `fails on a missing translation`() {
        val error = assertFailsWith<IllegalArgumentException> {
            parseTranslationTable("key,en,fr\nplay,Play!,\n", "test.csv")
        }

        assertEquals("Missing \"fr\" translation for \"play\" at line 2 of test.csv", error.message)
    }

    @Test
    fun `fails on a duplicate key`() {
        assertFailsWith<IllegalArgumentException> {
            parseTranslationTable("key,en\nplay,Play!\nplay,Again\n", "test.csv")
        }
    }

    @Test
    fun `fails on a key that is not an identifier`() {
        assertFailsWith<IllegalArgumentException> {
            parseTranslationTable("key,en\nplay now,Play!\n", "test.csv")
        }
    }
}
