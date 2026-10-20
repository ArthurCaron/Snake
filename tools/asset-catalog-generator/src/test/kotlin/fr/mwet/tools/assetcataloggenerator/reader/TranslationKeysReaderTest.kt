package fr.mwet.tools.assetcataloggenerator.reader

import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TranslationKeysReaderTest {
    private val file = Files.createTempFile("translations", ".properties")

    @AfterTest
    fun deleteFile() {
        Files.deleteIfExists(file)
    }

    @Test
    fun `reads the keys in file order, skipping comments, blank lines and continuation lines`() {
        Files.writeString(
            file,
            """
            # comment
            ! other comment
            play=Play!

              score : Score: {0}
            long=first line \
                second line \\\
                third line
            pressAKey Press a key
            empty=
            """.trimIndent(),
        )

        assertEquals(listOf("play", "score", "long", "pressAKey", "empty"), readTranslationKeys(file))
    }
}
