package fr.mwet.snake.save.fileFormats

class Highscores() {
    var highscores: MutableList<Highscore> = mutableListOf()

    constructor(highscores: List<Highscore>) : this() { this.highscores = highscores.toMutableList() }
}

class Highscore() {
    var name: String = ""
    var score: Int = 0

    constructor(name: String, score: Int) : this() { this.name = name; this.score = score }
}

@JvmInline
value class StringHighscores(val highscores: String)
