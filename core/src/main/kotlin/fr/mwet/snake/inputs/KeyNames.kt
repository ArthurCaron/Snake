package fr.mwet.snake.inputs

import com.badlogic.gdx.Input

interface KeyNames {
    fun nameOf(keycode: Int): String?
}

object LibGdxKeyNames : KeyNames {
    override fun nameOf(keycode: Int): String? = Input.Keys.toString(keycode)
}
