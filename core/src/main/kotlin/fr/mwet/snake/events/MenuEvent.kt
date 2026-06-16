package fr.mwet.snake.events

sealed interface MenuEvent {
    data object PlayGameClicked : MenuEvent
}
