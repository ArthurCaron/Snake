package fr.mwet.snake.events

sealed interface GameEvent {
    data object SnakeMoved : GameEvent
    data object FoodEaten : GameEvent
    data object Lost : GameEvent
    data object Won : GameEvent
    data object Pause : GameEvent
    data object GoBackToMainMenu : GameEvent
}
