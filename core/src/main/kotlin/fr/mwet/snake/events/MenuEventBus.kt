package fr.mwet.snake.events

class MenuEventBus {
    private val listeners = mutableListOf<MenuEventListener>()

    fun listen(listener: MenuEventListener) {
        listeners.add(listener)
    }

    fun emit(menuEvent: MenuEvent) {
        listeners.forEach { it.onEvent(menuEvent) }
    }
}
