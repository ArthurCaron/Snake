package fr.mwet.snake.backdrop

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.render.Background
import fr.mwet.snake.render.WorldDrawable
import fr.mwet.snake.render.clipToBoard
import fr.mwet.snake.render.endBoardClip
import fr.mwet.snake.save.settings.SettingsRepository

// Draws the style chosen in the settings. Each style is created once and keeps its own state.
// Only the active style receives draw(), so the other styles stay frozen until they show again.
// The bands around the board show the plain ground: the style draws only inside the board.
class MenuBackdrop(
    textures: TextureHandler,
    private val settingsRepository: SettingsRepository,
    private val plain: Background,
) : WorldDrawable {
    private val driftingGrid = DriftingGrid(textures, plain)
    private val demoSnake = DemoSnake(textures, plain)
    private val floatingFruits = FloatingFruits(textures, plain)
    private val fireflies = Fireflies(textures, plain)
    private val diagonalStripes = DiagonalStripes(textures, plain)
    private val waves = Waves(textures, plain)
    private val ripples = Ripples(textures, plain)
    private val gameOfLife = GameOfLife(textures, plain)
    private val wanderingSnakes = WanderingSnakes(textures, plain)

    override fun draw(batch: SpriteBatch, delta: Float) {
        plain.draw(batch, delta)
        if (settingsRepository.backdrop == BackdropStyle.Plain) return
        batch.clipToBoard()
        when (settingsRepository.backdrop) {
            BackdropStyle.Plain -> {}
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
        batch.endBoardClip()
    }
}
