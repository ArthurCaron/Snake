package fr.mwet.snake.inputs

import fr.mwet.snake.save.settings.GeneralKeymapping
import ktx.app.KtxInputAdapter

class GeneralInputProcessor(generalKeymappings: List<GeneralKeymapping>) : KtxInputAdapter {
    private val keycodeMapping: Map<Int, List<GeneralCommand>> =
        generalKeymappings
            .flatMap { keymapping -> keymapping.keys.map { key -> key to keymapping.action } }
            .groupBy({ it.first }, { it.second })

    override fun keyDown(keycode: Int): Boolean {
        keycodeMapping[keycode]?.let { commands ->
            commands.forEach { it.execute() }
            return true
        }
        return false
    }
}
