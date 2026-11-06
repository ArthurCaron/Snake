package fr.mwet.snake.inputs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor

class InputRouter {
    private val multiplexer = InputMultiplexer()

    init {
        Gdx.input.setCatchKey(Input.Keys.BACK, true)
        // In a browser, these keys would also scroll the page under the game.
        SCROLL_KEYS.forEach { Gdx.input.setCatchKey(it, true) }
        Gdx.input.inputProcessor = multiplexer
    }

    fun add(vararg processors: InputProcessor) = processors.forEach { multiplexer.addProcessor(it) }

    fun addFirst(processor: InputProcessor) = multiplexer.addProcessor(0, processor)

    fun remove(vararg processors: InputProcessor) = processors.forEach { multiplexer.removeProcessor(it) }
}

private val SCROLL_KEYS = intArrayOf(
    Input.Keys.UP, Input.Keys.DOWN, Input.Keys.LEFT, Input.Keys.RIGHT,
    Input.Keys.SPACE, Input.Keys.PAGE_UP, Input.Keys.PAGE_DOWN, Input.Keys.HOME, Input.Keys.END,
)
