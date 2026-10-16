package fr.mwet.snake.transition

import com.badlogic.gdx.graphics.g2d.SpriteBatch

// One style of screen transition. ScreenTransitions runs it in two phases: out, the switch of screen, then in.
interface ScreenTransition {
    val outSeconds: Float
    val inSeconds: Float

    // Called once, before the out phase: the frame buffer still holds the old screen.
    fun begin() {}

    // t goes from 0 to 1 in each phase. coming is false before the switch, true after. The batch draws in screen pixels.
    fun draw(batch: SpriteBatch, t: Float, coming: Boolean, width: Float, height: Float)

    fun end() {}
}
