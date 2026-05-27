package fr.mwet.snake.inputs.game

import com.badlogic.gdx.Input.Keys.*
import fr.mwet.snake.entities.GameWorld
import ktx.app.KtxInputAdapter

class GameInputProcessor(private val gameWorld: GameWorld) : KtxInputAdapter {
    private val keycodeMapping = GameKeycodeMapping()

    override fun keyDown(keycode: Int): Boolean {
        keycodeMapping.get(keycode)?.let {
            it.execute(gameWorld.snake)
            return true
        }
        return false
    }
}

class GameKeycodeMapping {
    private val keycodeMapping = mutableMapOf<Int, GameCommand>()

    fun get(keycode: Int) = keycodeMapping[keycode]

    // TODO: get from file
    init {
        keycodeMapping[UP] = GoUp()
        keycodeMapping[W] = GoUp()
        keycodeMapping[RIGHT] = GoRight()
        keycodeMapping[D] = GoRight()
        keycodeMapping[DOWN] = GoDown()
        keycodeMapping[S] = GoDown()
        keycodeMapping[LEFT] = GoLeft()
        keycodeMapping[A] = GoLeft()
    }
}
