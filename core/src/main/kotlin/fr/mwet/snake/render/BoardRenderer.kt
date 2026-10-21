package fr.mwet.snake.render

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.game.GameState
import fr.mwet.snake.game.GameWorld
import fr.mwet.snake.tuning.GameplayTuning.WORLD_HEIGHT
import fr.mwet.snake.tuning.GameplayTuning.WORLD_WIDTH
import fr.mwet.snake.utils.resetColor

private const val GRID_ALPHA = 0.05f

class BoardRenderer(textureHandler: TextureHandler, private val world: GameWorld) : WorldDrawable {
    private val background = Background(textureHandler)
    private val gridCell = textureHandler.gridCell
    private val food = FoodRenderer(textureHandler, world.food)
    private val snake = SnakeRenderer(textureHandler, world.snake)
    private val disintegratingSnake = DisintegratingSnakeRenderer(textureHandler, world.snake)

    fun reset() {
        food.reset()
        snake.reset()
        disintegratingSnake.reset()
    }

    fun disintegrate() {
        disintegratingSnake.disintegrate()
    }

    override fun draw(batch: SpriteBatch, delta: Float) {
        background.draw(batch, delta)
        drawGrid(batch)
        when (world.state) {
            GameState.Lost -> disintegratingSnake.render(batch, delta)
            GameState.Won -> snake.render(batch, delta)
            GameState.Ready, GameState.Playing, GameState.Paused -> {
                food.render(batch, delta)
                snake.render(batch, delta)
            }
        }
    }

    private fun drawGrid(batch: SpriteBatch) {
        batch.setColor(1f, 1f, 1f, GRID_ALPHA)
        for (x in 0..<WORLD_WIDTH.toInt()) {
            for (y in 0..<WORLD_HEIGHT.toInt()) {
                batch.draw(gridCell, x.toFloat(), y.toFloat(), 1f, 1f)
            }
        }
        batch.resetColor()
    }
}
