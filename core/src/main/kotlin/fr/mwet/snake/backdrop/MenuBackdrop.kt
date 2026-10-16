package fr.mwet.snake.backdrop

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.render.Background
import fr.mwet.snake.render.WorldDrawable
import fr.mwet.snake.save.settings.SettingsRepository

// Draws the style chosen in the settings. Each style is created once and keeps its own state.
// Only the active style receives draw(), so the other styles stay frozen until they show again.
class MenuBackdrop(textures: TextureHandler, private val settingsRepository: SettingsRepository) : WorldDrawable {
    private val plain = Background(textures)
    private val driftingGrid = DriftingGrid(textures)
    private val demoSnake = DemoSnake(textures)
    private val floatingFruits = FloatingFruits(textures)
    private val fireflies = Fireflies(textures)
    private val diagonalStripes = DiagonalStripes(textures)
    private val waves = Waves(textures)
    private val ripples = Ripples(textures)
    private val gameOfLife = GameOfLife(textures)
    private val wanderingSnakes = WanderingSnakes(textures)

    override fun draw(batch: SpriteBatch, delta: Float) {
        when (settingsRepository.backdrop) {
            BackdropStyle.Plain -> plain.draw(batch, delta)
            BackdropStyle.DriftingGrid -> driftingGrid.draw(batch, delta)
            BackdropStyle.DemoSnake -> demoSnake.draw(batch, delta)
            BackdropStyle.FloatingFruits -> floatingFruits.draw(batch, delta)
            BackdropStyle.Fireflies -> fireflies.draw(batch, delta)
            BackdropStyle.DiagonalStripes -> diagonalStripes.draw(batch, delta)
            BackdropStyle.Waves -> waves.draw(batch, delta)
            BackdropStyle.Ripples -> ripples.draw(batch, delta)
            BackdropStyle.GameOfLife -> gameOfLife.draw(batch, delta)
            BackdropStyle.WanderingSnakes -> wanderingSnakes.draw(batch, delta)
        }
    }
}
