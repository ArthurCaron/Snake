package fr.mwet.snake.render

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.tuning.GameplayTuning.WORLD_HEIGHT
import fr.mwet.snake.tuning.GameplayTuning.WORLD_WIDTH

class Background(textureHandler: TextureHandler) : WorldDrawable {
    private val texture =
        textureHandler.rectangleWithBorderTexture(width = WORLD_WIDTH, height = WORLD_HEIGHT, fill = Palette.background)

    override fun draw(batch: SpriteBatch, delta: Float) {
        batch.draw(texture, 0f, 0f)
    }
}
