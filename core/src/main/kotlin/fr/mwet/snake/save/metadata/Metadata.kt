package fr.mwet.snake.save.metadata

data class Metadata(val highScores: HighScores)
data class HighScores(val highScores: List<HighScore>)
data class HighScore(val name: String, val score: Int)
