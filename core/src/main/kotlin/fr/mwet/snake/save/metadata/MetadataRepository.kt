package fr.mwet.snake.save.metadata

import fr.mwet.snake.save.metadata.serializable.DefaultHighScores
import fr.mwet.snake.save.metadata.serializable.HighScoreSave
import fr.mwet.snake.save.metadata.serializable.HighScoresSave
import fr.mwet.snake.save.serialization.JsonStore

private const val ROOT_FOLDER = "metadata"
private const val HIGH_SCORE_FILE = "${ROOT_FOLDER}/highscores.json"

class MetadataRepository(private val jsonStore: JsonStore) {
    fun load(): Metadata {
        val highScoresSave = jsonStore.readOrCreate(HIGH_SCORE_FILE) { DefaultHighScores.defaultHighScores() }
        val metadata = Metadata(
            HighScores(
                highScores = highScoresSave.highScores.map { it.toHighScore() }
            )
        )
        save(metadata)

        return metadata
    }

    fun save(metadata: Metadata) {
        jsonStore.write(HIGH_SCORE_FILE, metadata.highScores.toSerializable())
    }

    private fun HighScoreSave.toHighScore() = HighScore(name, score)

    private fun HighScores.toSerializable() =
        HighScoresSave(DefaultHighScores.defaultHighScores().version, highScores.map { it.toSerializable() })

    private fun HighScore.toSerializable() = HighScoreSave(name, score)
}
