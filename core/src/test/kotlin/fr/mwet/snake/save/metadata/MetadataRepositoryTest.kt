package fr.mwet.snake.save.metadata

import fr.mwet.snake.save.serialization.InMemoryTextStore
import fr.mwet.snake.save.serialization.JsonStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MetadataRepositoryTest {
    private val textStore = InMemoryTextStore()
    private val sut = MetadataRepository(JsonStore(textStore))

    @Test
    fun `keeps only the best score and flags only a new best as a high score`() {
        (1..11).forEach { sut.submitHighScore(it * 10) }

        assertEquals(listOf(HighScore(110)), sut.metadata.highScores)
        assertFalse(sut.submitHighScore(15))
        assertFalse(sut.submitHighScore(0))
        assertTrue(sut.submitHighScore(200))
        assertEquals(listOf(HighScore(200)), sut.metadata.highScores)
    }
}
