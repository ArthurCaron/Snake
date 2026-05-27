package fr.mwet.snake.entities

enum class Direction(val directionX: Int, val directionY: Int) {
    UP(0, 1),
    RIGHT(1, 0),
    DOWN(0, -1),
    LEFT(-1, 0);

    fun isOpposite(direction: Direction): Boolean {
        if (this == DOWN && direction == UP) return true
        if (this == UP && direction == DOWN) return true
        if (this == RIGHT && direction == LEFT) return true
        if (this == LEFT && direction == RIGHT) return true
        return false
    }
}
