package fr.mwet.snake.inputs.game

import fr.mwet.snake.game.GameWorld
import fr.mwet.snake.save.fileFormats.GameKeymapping
import ktx.app.KtxInputAdapter

class GameInputProcessor(
    commandToKeycode: List<GameKeymapping>,
    private val gameWorld: GameWorld
) : KtxInputAdapter {
    private val keycodeMapping: Map<Int, GameCommand> = commandToKeycode.associate { it.key to it.action }

    override fun keyDown(keycode: Int): Boolean {
        keycodeMapping[keycode]?.let {
            it.execute(gameWorld.snake)
            return true
        }
        return false
    }
}
