package fr.mwet.snake.save.metadata

import fr.mwet.snake.save.metadata.serializable.CURRENT_VERSION
import fr.mwet.snake.save.metadata.serializable.HighScoreSave
import fr.mwet.snake.save.serialization.JsonStore

private const val ROOT_FOLDER = "metadata"
private const val HIGH_SCORE_FILE = "${ROOT_FOLDER}/highscore.json"

data class Metadata(val highScore: HighScore)
class HighScore(val score: Int)

class MetadataRepository(private val jsonStore: JsonStore) {
    var metadata = load()
        private set

    fun submitHighScore(score: Int): Boolean {
        if (score <= metadata.highScore.score) return false
        save(Metadata(HighScore(score)))
        return true
    }

    private fun load(): Metadata {
        val highScoreSave = jsonStore.readOrCreate(HIGH_SCORE_FILE) { HighScoreSave(0, 0) }
        val metadata = Metadata(HighScore(highScoreSave.score))
        save(metadata)
        return metadata
    }

    private fun save(metadata: Metadata) {
        this.metadata = metadata
        jsonStore.write(HIGH_SCORE_FILE, metadata.highScore.toSerializable())
    }

    private fun HighScore.toSerializable() = HighScoreSave(CURRENT_VERSION, score)
}
