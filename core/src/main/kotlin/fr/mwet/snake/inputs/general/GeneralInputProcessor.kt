package fr.mwet.snake.inputs.general

import com.badlogic.gdx.Input.Keys.ESCAPE
import ktx.app.KtxInputAdapter

class GeneralInputProcessor : KtxInputAdapter {
    private val keycodeMapping = GeneralKeycodeMapping()

    override fun keyDown(keycode: Int): Boolean {
        keycodeMapping.get(keycode)?.let {
            it.execute()
            return true
        }
        return false
    }
}

class GeneralKeycodeMapping {
    private val keycodeMapping = mutableMapOf<Int, GeneralCommand>()

    fun get(keycode: Int) = keycodeMapping[keycode]

    // TODO: get from file
    init {
        keycodeMapping[ESCAPE] = GoBackToMainMenu()
    }
}
