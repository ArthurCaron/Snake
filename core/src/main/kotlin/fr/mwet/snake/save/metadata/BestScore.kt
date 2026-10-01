package fr.mwet.snake.save.metadata

class BestScore(private val metadataRepository: MetadataRepository, metadata: Metadata) {
    var value: Int = metadata.highScores.highScores.maxOfOrNull { it.score } ?: 0
        private set

    fun submit(score: Int): Boolean {
        if (score <= value) return false
        value = score
        metadataRepository.save(Metadata(HighScores(listOf(HighScore(name = "", score = score)))))
        return true
    }
}
