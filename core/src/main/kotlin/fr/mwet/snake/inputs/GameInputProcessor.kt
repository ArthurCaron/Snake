package fr.mwet.snake.inputs

import fr.mwet.snake.save.settings.GameKeymapping
import ktx.app.KtxInputAdapter

class GameInputProcessor(
    gameKeymappings: List<GameKeymapping>,
    private val context: GameCommandContext
) : KtxInputAdapter {
    private val keycodeMapping: Map<Int, List<GameCommand>> =
        gameKeymappings
            .flatMap { keymapping -> keymapping.keys.map { key -> key to keymapping.action } }
            .groupBy({ it.first }, { it.second })

    override fun keyDown(keycode: Int): Boolean {
        keycodeMapping[keycode]?.let { commands ->
            commands.forEach { it.execute(context) }
            return true
        }
        return false
    }
}
