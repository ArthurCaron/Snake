package fr.mwet.snake.inputs

import com.badlogic.gdx.Gdx
import ktx.app.KtxInputAdapter
import kotlin.math.abs

private const val SWIPE_THRESHOLD_SCREEN_RATIO = 0.05f
private const val NO_POINTER = -1

class SwipeInputProcessor(private val context: GameCommandContext) : KtxInputAdapter {
    private var pointer = NO_POINTER
    private var anchorX = 0
    private var anchorY = 0

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (this.pointer != NO_POINTER) return false
        this.pointer = pointer
        anchorX = screenX
        anchorY = screenY
        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        if (pointer != this.pointer) return false
        val dx = screenX - anchorX
        val dy = screenY - anchorY
        val threshold = minOf(Gdx.graphics.width, Gdx.graphics.height) * SWIPE_THRESHOLD_SCREEN_RATIO
        if (maxOf(abs(dx), abs(dy)) < threshold) return false

        swipeCommand(dx, dy).execute(context)
        anchorX = screenX
        anchorY = screenY
        return true
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (pointer == this.pointer) this.pointer = NO_POINTER
        return false
    }

    override fun touchCancelled(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (pointer == this.pointer) this.pointer = NO_POINTER
        return false
    }

    private fun swipeCommand(dx: Int, dy: Int): GameCommand =
        if (abs(dx) > abs(dy)) {
            if (dx > 0) GoRightCommand else GoLeftCommand
        } else {
            if (dy > 0) GoDownCommand else GoUpCommand
        }
}
