package fr.mwet.snake.inputs.game

import fr.mwet.snake.game.GameWorld
import fr.mwet.snake.save.settings.GameKeymapping
import ktx.app.KtxInputAdapter

class GameInputProcessor(
    gameKeymappings: List<GameKeymapping>,
    private val gameWorld: GameWorld
) : KtxInputAdapter {
    private val keycodeMapping: Map<Int, GameCommand> =
        gameKeymappings.flatMap { it.keys.map { key -> key to it.action } }.toMap()

    override fun keyDown(keycode: Int): Boolean {
        keycodeMapping[keycode]?.let {
            it.execute(gameWorld.snake)
            return true
        }
        return false
    }
}
