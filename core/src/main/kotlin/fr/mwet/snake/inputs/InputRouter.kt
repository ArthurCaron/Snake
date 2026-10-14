package fr.mwet.snake.inputs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor

class InputRouter {
    private val multiplexer = InputMultiplexer()

    init {
        Gdx.input.setCatchKey(Input.Keys.BACK, true)
        Gdx.input.inputProcessor = multiplexer
    }

    fun add(vararg processors: InputProcessor) = processors.forEach { multiplexer.addProcessor(it) }

    fun remove(vararg processors: InputProcessor) = processors.forEach { multiplexer.removeProcessor(it) }
}
