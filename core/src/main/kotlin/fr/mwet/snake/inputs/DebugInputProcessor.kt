package fr.mwet.snake.inputs

import ktx.app.KtxInputAdapter

// Use the typed character so the shortcut follows the French keyboard layout on desktop and web.
class DebugInputProcessor(private val onActivate: () -> Unit) : KtxInputAdapter {
    override fun keyTyped(character: Char): Boolean {
        if (character != '²') return false
        onActivate()
        return true
    }
}
