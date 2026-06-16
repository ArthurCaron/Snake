package fr.mwet.snake.events

interface GameEventBus {
    fun listen(listener: GameEventListener)
    fun emit(gameEvent: GameEvent)
}

class GameEventBusImpl : GameEventBus {
    private val listeners = mutableListOf<GameEventListener>()

    override fun listen(listener: GameEventListener) {
        listeners.add(listener)
    }

    override fun emit(gameEvent: GameEvent) {
        listeners.forEach { it.onEvent(gameEvent) }
    }
}
