package fr.mwet.snake.events

class GameEventBus(private val listeners: List<GameEventListener>) {
    fun emit(gameEvent: GameEvent) {
        listeners.forEach { it.onEvent(gameEvent) }
    }
}
