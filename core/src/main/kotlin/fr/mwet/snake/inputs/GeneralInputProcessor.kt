package fr.mwet.snake.inputs

import fr.mwet.snake.events.MenuEventBus
import fr.mwet.snake.save.settings.GeneralKeymapping
import ktx.app.KtxInputAdapter

class GeneralInputProcessor(generalKeymappings: List<GeneralKeymapping>, menuEventBus: MenuEventBus) : KtxInputAdapter {
    private val keycodeMapping: Map<Int, List<GeneralCommand>> =
        generalKeymappings
            .flatMap { keymapping -> keymapping.keys.map { key -> key to keymapping.action } }
            .groupBy({ it.first }, { it.second })
    private val context = GeneralCommandContext(
        menuEventBus = menuEventBus
    )

    override fun keyDown(keycode: Int): Boolean {
        keycodeMapping[keycode]?.let { commands ->
            commands.forEach { it.execute(context) }
            return true
        }
        return false
    }
}
