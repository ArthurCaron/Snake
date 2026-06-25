package fr.mwet.snake.inputs.game

import fr.mwet.snake.events.GameEventBus
import fr.mwet.snake.game.GameWorld
import fr.mwet.snake.save.settings.GameKeymapping
import ktx.app.KtxInputAdapter

class GameInputProcessor(
    gameKeymappings: List<GameKeymapping>,
    gameEventBus: GameEventBus,
    gameWorld: GameWorld
) : KtxInputAdapter {
    private val keycodeMapping: Map<Int, List<GameCommand>> =
        gameKeymappings
            .flatMap { keymapping -> keymapping.keys.map { key -> key to keymapping.action } }
            .groupBy({ it.first }, { it.second })
    private val context = GameCommandContext(
        targetActor = gameWorld.snake,
        gameEventBus = gameEventBus,
    )

    override fun keyDown(keycode: Int): Boolean {
        keycodeMapping[keycode]?.let { commands ->
            commands.forEach { it.execute(context) }
            return true
        }
        return false
    }
}
