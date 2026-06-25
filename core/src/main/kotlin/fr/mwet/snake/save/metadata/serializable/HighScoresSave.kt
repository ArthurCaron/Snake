package fr.mwet.snake.save.metadata.serializable

import fr.mwet.snake.utils.NO_VERSION

class HighScoresSave() {
    var version: Int = NO_VERSION
    var highScores: ArrayList<HighScoreSave> = arrayListOf()

    constructor(version: Int, highScores: List<HighScoreSave>) : this() {
        this.version = version
        this.highScores = ArrayList(highScores)
    }
}

class HighScoreSave() {
    var name: String = ""
    var score: Int = 0

    constructor(name: String, score: Int) : this() {
        this.name = name
        this.score = score
    }
}

object DefaultHighScores {
    fun defaultHighScores() = HighScoresSave(0, highScores = listOf())
}
