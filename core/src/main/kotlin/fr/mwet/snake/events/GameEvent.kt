package fr.mwet.snake.events

sealed interface GameEvent {
    data object SnakeMoved : GameEvent
    data object FoodEaten : GameEvent
    data object GameOver : GameEvent
}
