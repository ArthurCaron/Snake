package fr.mwet.snake.inputs.general

import fr.mwet.snake.save.fileFormats.GeneralKeymapping
import ktx.app.KtxInputAdapter

class GeneralInputProcessor(commandToKeycode: List<GeneralKeymapping>) : KtxInputAdapter {
    private val keycodeMapping: Map<Int, GeneralCommand> = commandToKeycode.associate { it.key to it.action }

    override fun keyDown(keycode: Int): Boolean {
        keycodeMapping[keycode]?.let {
            it.execute()
            return true
        }
        return false
    }
}
