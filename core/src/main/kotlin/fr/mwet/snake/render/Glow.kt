package fr.mwet.snake.render

import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch

inline fun SpriteBatch.additive(draw: () -> Unit) {
    setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE)
    draw()
    setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
}
