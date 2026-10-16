package fr.mwet.snake.render

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.HdpiUtils
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import fr.mwet.snake.GameViewport
import fr.mwet.snake.tuning.GameplayTuning.WORLD_HEIGHT
import fr.mwet.snake.tuning.GameplayTuning.WORLD_WIDTH
import ktx.graphics.use
import kotlin.math.roundToInt

interface WorldDrawable {
    fun draw(batch: SpriteBatch, delta: Float)
}

class WorldLayer(private val batch: SpriteBatch, private val viewport: GameViewport) {
    private val fullScreen = Matrix4()

    fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    fun render(drawable: WorldDrawable, delta: Float) {
        val screenWidth = Gdx.graphics.width
        val screenHeight = Gdx.graphics.height
        val unitsPerPixel = viewport.worldWidth / viewport.screenWidth
        fullScreen.setToOrtho2D(
            -viewport.screenX * unitsPerPixel,
            -viewport.screenY * unitsPerPixel,
            screenWidth * unitsPerPixel,
            screenHeight * unitsPerPixel,
        )
        HdpiUtils.glViewport(0, 0, screenWidth, screenHeight)
        batch.use(fullScreen) { drawable.draw(it, delta) }
    }
}

private val inverseProjection = Matrix4()
private val corner = Vector3()

// The part of the world that the batch shows, read back from its projection. Inside WorldLayer.render it is the whole
// screen: the board, plus the bands that FitViewport leaves around it.
fun SpriteBatch.visibleWorld(out: Rectangle): Rectangle {
    inverseProjection.set(projectionMatrix).inv()
    corner.set(-1f, -1f, 0f).prj(inverseProjection)
    val left = corner.x
    val bottom = corner.y
    corner.set(1f, 1f, 0f).prj(inverseProjection)
    return out.set(left, bottom, corner.x - left, corner.y - bottom)
}

// Clips the next draws to the board, so that the bands around it keep only the ground. Call endBoardClip after them.
fun SpriteBatch.clipToBoard() {
    flush()
    corner.set(0f, 0f, 0f).prj(projectionMatrix)
    val left = toScreen(corner.x, Gdx.graphics.width)
    val bottom = toScreen(corner.y, Gdx.graphics.height)
    corner.set(WORLD_WIDTH, WORLD_HEIGHT, 0f).prj(projectionMatrix)
    val right = toScreen(corner.x, Gdx.graphics.width)
    val top = toScreen(corner.y, Gdx.graphics.height)
    Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST)
    HdpiUtils.glScissor(left, bottom, right - left, top - bottom)
}

fun SpriteBatch.endBoardClip() {
    flush()
    Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST)
}

// The projection puts the screen between -1 and 1: this converts back to pixels.
private fun toScreen(projected: Float, screenSize: Int): Int = ((projected + 1f) / 2f * screenSize).roundToInt()
