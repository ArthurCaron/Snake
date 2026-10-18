package fr.mwet.snake.save.metadata.serializable

import fr.mwet.snake.utils.NO_VERSION

const val CURRENT_VERSION = NO_VERSION

class HighScoreSave() {
    var version: Int = CURRENT_VERSION
    var score: Int = 0

    constructor(version: Int, score: Int) : this() {
        this.version = version
        this.score = score
    }
}
