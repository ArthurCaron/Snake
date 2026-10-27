package fr.mwet.snake.inputs

import com.badlogic.gdx.Input
import ktx.app.KtxInputAdapter

class DebugInputProcessor(private val onStartAlmostWonGame: () -> Unit) : KtxInputAdapter {
    override fun keyDown(keycode: Int): Boolean {
        if (keycode != Input.Keys.F9) return false
        onStartAlmostWonGame()
        return true
    }
}
