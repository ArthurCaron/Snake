package fr.mwet.snake.render

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import com.badlogic.gdx.math.Rectangle
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.save.settings.SettingsRepository
import fr.mwet.snake.tuning.GameplayTuning.WORLD_HEIGHT
import fr.mwet.snake.tuning.GameplayTuning.WORLD_WIDTH
import fr.mwet.snake.tuning.Palette
import fr.mwet.snake.utils.resetColor
import kotlin.math.ceil
import kotlin.math.floor
import fr.mwet.snake.tuning.RenderTuning.Background.TILE_CELLS

private val TILES_X = ceil(WORLD_WIDTH / TILE_CELLS).toInt()
private val TILES_Y = ceil(WORLD_HEIGHT / TILE_CELLS).toInt()

class Background(textureHandler: TextureHandler, private val settingsRepository: SettingsRepository) : WorldDrawable {
    private val skin = textureHandler.skin
    private val frame = skin.boardFrame
    private val visible = Rectangle()

    override fun draw(batch: SpriteBatch, delta: Float) {
        batch.visibleWorld(visible)
        val ground = ground()
        batch.color = Palette.boardSurround
        for (x in floor(visible.x / TILE_CELLS).toInt() until ceil((visible.x + visible.width) / TILE_CELLS).toInt()) {
            for (y in floor(visible.y / TILE_CELLS).toInt() until ceil((visible.y + visible.height) / TILE_CELLS).toInt()) {
                batch.draw(ground, x * TILE_CELLS, y * TILE_CELLS, TILE_CELLS, TILE_CELLS)
            }
        }
        batch.resetColor()
        for (x in 0 until TILES_X) {
            for (y in 0 until TILES_Y) {
                val left = x * TILE_CELLS
                val bottom = y * TILE_CELLS
                drawCropped(batch, ground, left, bottom, minOf(TILE_CELLS, WORLD_WIDTH - left), minOf(TILE_CELLS, WORLD_HEIGHT - bottom))
            }
        }
        batch.draw(frame, 0f, 0f, WORLD_WIDTH, WORLD_HEIGHT)
    }

    private fun ground(): AtlasRegion = when (settingsRepository.ground) {
        GroundStyle.Checker -> skin.ground
        GroundStyle.Plain -> skin.groundPlain
        GroundStyle.Grain -> skin.groundGrain
        GroundStyle.Stripes -> skin.groundStripes
        GroundStyle.Tiles -> skin.groundTiles
        GroundStyle.Scales -> skin.groundScales
        GroundStyle.Dunes -> skin.groundDunes
        GroundStyle.Hex -> skin.groundHex
    }

    private fun drawCropped(batch: SpriteBatch, ground: AtlasRegion, left: Float, bottom: Float, width: Float, height: Float) {
        val right = ground.u + (ground.u2 - ground.u) * width / TILE_CELLS
        val top = ground.v2 + (ground.v - ground.v2) * height / TILE_CELLS
        batch.draw(ground.texture, left, bottom, width, height, ground.u, ground.v2, right, top)
    }
}
