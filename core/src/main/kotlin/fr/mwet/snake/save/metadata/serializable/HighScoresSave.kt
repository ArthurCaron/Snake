package fr.mwet.snake.save.metadata.serializable

import fr.mwet.snake.utils.NO_VERSION

const val CURRENT_VERSION = NO_VERSION

class HighScoresSave() {
    var version: Int = CURRENT_VERSION
    var highScores: ArrayList<HighScoreSave> = arrayListOf()

    constructor(version: Int, highScores: List<HighScoreSave>) : this() {
        this.version = version
        this.highScores = ArrayList(highScores)
    }
}

class HighScoreSave() {
    var score: Int = 0

    constructor(score: Int) : this() {
        this.score = score
    }
}
