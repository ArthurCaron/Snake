package fr.mwet.snake.inputs.game

import fr.mwet.snake.game.GameWorld
import fr.mwet.snake.save.settings.GameKeymapping
import ktx.app.KtxInputAdapter

class GameInputProcessor(
    gameKeymappings: List<GameKeymapping>,
    private val gameWorld: GameWorld
) : KtxInputAdapter {
    private val keycodeMapping: Map<Int, List<GameCommand>> =
        gameKeymappings
            .flatMap { keymapping -> keymapping.keys.map { key -> key to keymapping.action } }
            .groupBy({ it.first }, { it.second })

    override fun keyDown(keycode: Int): Boolean {
        keycodeMapping[keycode]?.let { commands ->
            commands.forEach { it.execute(gameWorld.snake) }
            return true
        }
        return false
    }
}
