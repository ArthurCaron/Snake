package fr.mwet.snake.render

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import com.badlogic.gdx.math.Vector2
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.save.settings.SettingsRepository
import fr.mwet.snake.game.Segment
import fr.mwet.snake.game.Snake
import fr.mwet.snake.tuning.GameplayTuning.WORLD_HEIGHT
import fr.mwet.snake.tuning.GameplayTuning.WORLD_WIDTH
import fr.mwet.snake.tuning.RenderTuning.Snake.MARK_SIZE
import fr.mwet.snake.tuning.RenderTuning.Snake.GLOW_SIZE
import fr.mwet.snake.utils.VectorPool
import fr.mwet.snake.utils.resetColor
import kotlin.random.Random

class DisintegratingSnakeRenderer(textureHandler: TextureHandler, val snake: Snake, private val settings: SettingsRepository) {
    private var eTime = 0f
    private val vectors = VectorPool()

    private val skin = textureHandler.skin
    private val bodyParts = mutableListOf<DisintegratingSegment>()
    private val color = Color()

    fun disintegrate(snakeColor: Color) {
        color.set(snakeColor)
        bodyParts.add(DisintegratingSegment(vectors.copy(snake.head.position), skin.snakeHeadDead, skin.snakeHeadDeadDetail))
        bodyParts.add(DisintegratingSegment(vectors.copy(snake.tail.position), skin.snakeTail, skin.snakeTailDetail))

        var segment: Segment? = snake.head.next
        while (segment != null) {
            if (segment != snake.tail) {
                bodyParts.add(DisintegratingSegment(vectors.copy(segment.position), skin.snakeBodyStraight, skin.snakeBodyStraightDetail))
            }
            segment = segment.next
        }
    }

    fun render(batch: SpriteBatch, delta: Float) {
        eTime += delta

        bodyParts.forEach {
            it.position.x += (it.tx - it.position.x) * delta * 2f
            it.position.y += (it.ty - it.position.y) * delta * 2f
        }
        if (settings.appearance.snakeGlow) {
            batch.additive {
                batch.setColor(color.r, color.g, color.b, settings.appearance.theme.snakeGlowAlpha)
                bodyParts.forEach { part -> batch.draw(skin.glow, part.position.x + (1 - GLOW_SIZE) / 2, part.position.y + (1 - GLOW_SIZE) / 2, GLOW_SIZE, GLOW_SIZE) }
            }
        }
        batch.color = color
        bodyParts.forEach { draw(batch, it, it.texture) }
        val mark = settings.appearance.markShape.region(skin)
        if (mark != null) {
            batch.color = settings.appearance.marks
            val size = MARK_SIZE
            bodyParts.forEach { part ->
                batch.draw(mark, part.position.x + (1 - size) / 2, part.position.y + (1 - size) / 2,
                    size / 2, size / 2, size, size, 1f, 1f, part.angle)
            }
        }
        batch.resetColor()
        bodyParts.forEach { draw(batch, it, it.detail) }
    }

    private fun draw(batch: SpriteBatch, part: DisintegratingSegment, texture: AtlasRegion) {
        batch.draw(
            texture,
            part.position.x,
            part.position.y,
            0.5f,
            0.5f,
            1f,
            1f,
            1f,
            1f,
            part.angle
        )
    }

    fun reset() {
        eTime = 0f
        bodyParts.forEach { vectors.free(it.position) }
        bodyParts.clear()
    }
}

data class DisintegratingSegment(var position: Vector2, val texture: AtlasRegion, val detail: AtlasRegion) {
    val tx: Float = Random.nextInt(0, WORLD_WIDTH.toInt() - 1).toFloat()
    val ty: Float = Random.nextInt(0, WORLD_HEIGHT.toInt() - 1).toFloat()
    val angle: Float = Random.nextInt(0, 360).toFloat()
}
