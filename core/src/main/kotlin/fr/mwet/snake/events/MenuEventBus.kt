package fr.mwet.snake.events

class MenuEventBus(private val listeners: MutableList<MenuEventListener>) {
    fun emit(menuEvent: MenuEvent) {
        listeners.forEach { it.onEvent(menuEvent) }
    }
}
