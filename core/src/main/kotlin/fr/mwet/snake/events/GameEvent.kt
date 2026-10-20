package fr.mwet.snake.events

sealed interface GameEvent {
    data object SnakeMoved : GameEvent
    data object FoodEaten : GameEvent
    data object Lost : GameEvent
    data object Won : GameEvent
    data object Paused : GameEvent
    data object Resumed : GameEvent
}
