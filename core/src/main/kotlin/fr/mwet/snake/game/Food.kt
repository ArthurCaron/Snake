package fr.mwet.snake.game

data class Food(
    var x: Int = 0,
    var y: Int = 0,
) {
    fun reset(availableCells: Set<Cell>) {
        val randomCell = availableCells.randomOrNull()
        x = randomCell?.x ?: 0
        y = randomCell?.y ?: 0
    }
}
