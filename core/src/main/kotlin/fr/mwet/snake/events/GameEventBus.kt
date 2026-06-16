package fr.mwet.snake.events

class GameEventBus {
    private val listeners = mutableListOf<GameEventListener>()

    fun listen(listener: GameEventListener) {
        listeners.add(listener)
    }

    fun emit(gameEvent: GameEvent) {
        listeners.forEach { it.onEvent(gameEvent) }
    }
}
