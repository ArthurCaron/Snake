package fr.mwet.snake.save.metadata

import fr.mwet.snake.save.serialization.JsonStore
import fr.mwet.snake.save.serialization.TextStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MetadataRepositoryTest {
    private val textStore = InMemoryTextStore()
    private val sut = MetadataRepository(JsonStore(textStore))

    @Test
    fun `keeps the ten best scores in order and flags only a new best as a high score`() {
        (1..11).forEach { sut.submitHighScore(it * 10) }

        assertEquals((110 downTo 20 step 10).toList(), sut.metadata.highScores.map { it.score })
        assertFalse(sut.submitHighScore(15))
        assertFalse(sut.submitHighScore(0))
        assertFalse(sut.submitHighScore(55))
        assertTrue(sut.submitHighScore(200))
        assertEquals(200, sut.metadata.highestScore.score)
        assertEquals(listOf(200, 110, 100, 90, 80, 70, 60, 55, 50, 40).map { HighScore(it) }, sut.metadata.highScores)
    }
}

private class InMemoryTextStore : TextStore {
    private val files = mutableMapOf<String, String>()
    override fun read(path: String) = files[path]
    override fun write(path: String, text: String) {
        files[path] = text
    }

    override fun exists(path: String) = path in files
    override fun delete(path: String) = files.remove(path) != null
    override fun backupCorrupted(path: String) = files.remove(path) != null
}
