package fr.mwet.snake.inputs.general

import fr.mwet.snake.save.settings.GeneralKeymapping
import ktx.app.KtxInputAdapter

class GeneralInputProcessor(generalKeymappings: List<GeneralKeymapping>) : KtxInputAdapter {
    private val keycodeMapping: Map<Int, GeneralCommand> =
        generalKeymappings.flatMap { it.keys.map { key -> key to it.action } }.toMap()

    override fun keyDown(keycode: Int): Boolean {
        keycodeMapping[keycode]?.let {
            it.execute()
            return true
        }
        return false
    }
}
