package fr.mwet.snake.events

sealed interface MenuEvent {
    data object LoadingFinished : MenuEvent
    data object PlayGameClicked : MenuEvent
    data object SoundToggled : MenuEvent
    data object MusicToggled : MenuEvent
}
