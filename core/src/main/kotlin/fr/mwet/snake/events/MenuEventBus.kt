package fr.mwet.snake.events

interface MenuEventBus {
    fun listen(listener: MenuEventListener)
    fun emit(menuEvent: MenuEvent)
}

class MenuEventBusImpl : MenuEventBus {
    private val listeners = mutableListOf<MenuEventListener>()

    override fun listen(listener: MenuEventListener) {
        listeners.add(listener)
    }

    override fun emit(menuEvent: MenuEvent) {
        listeners.forEach { it.onEvent(menuEvent) }
    }
}
