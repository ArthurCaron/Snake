package fr.mwet.snake.save.metadata

import fr.mwet.snake.save.metadata.serializable.CURRENT_VERSION
import fr.mwet.snake.save.metadata.serializable.HighScoreSave
import fr.mwet.snake.save.metadata.serializable.HighScoresSave
import fr.mwet.snake.save.serialization.JsonStore
import fr.mwet.snake.tuning.GameplayTuning.HIGH_SCORES_KEPT

private const val ROOT_FOLDER = "metadata"
private const val HIGH_SCORES_FILE = "${ROOT_FOLDER}/highscores.json"

data class Metadata(val highScores: List<HighScore>) {
    val highestScore: HighScore get() = highScores.firstOrNull() ?: HighScore(0)
}

data class HighScore(val score: Int)

class MetadataRepository(private val jsonStore: JsonStore) {
    var metadata = load()
        private set

    fun submitHighScore(score: Int): Boolean {
        if (score <= 0) return false
        val isNewHighScore = score > metadata.highestScore.score
        val highScores = (metadata.highScores + HighScore(score))
            .sortedByDescending { it.score }
            .take(HIGH_SCORES_KEPT)
        save(metadata.copy(highScores = highScores))
        return isNewHighScore
    }

    private fun load(): Metadata {
        val highScoresSave = jsonStore.readOrCreate(HIGH_SCORES_FILE) { HighScoresSave(CURRENT_VERSION, emptyList()) }
        val metadata = Metadata(
            highScoresSave.highScores
                .map { HighScore(it.score) }
                .sortedByDescending { it.score }
                .take(HIGH_SCORES_KEPT)
        )
        save(metadata)
        return metadata
    }

    private fun save(metadata: Metadata) {
        this.metadata = metadata
        jsonStore.write(HIGH_SCORES_FILE, metadata.toSerializable())
    }

    private fun Metadata.toSerializable() = HighScoresSave(CURRENT_VERSION, highScores.map { HighScoreSave(it.score) })
}
