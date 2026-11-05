package fr.mwet.snake.render

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import fr.mwet.snake.save.settings.SettingsRepository
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.game.GameState
import fr.mwet.snake.game.GameWorld
import fr.mwet.snake.tuning.GameplayTuning.WORLD_HEIGHT
import fr.mwet.snake.tuning.GameplayTuning.WORLD_WIDTH
import fr.mwet.snake.tuning.Palette
import fr.mwet.snake.utils.resetColor


class BoardRenderer(
    textureHandler: TextureHandler,
    private val world: GameWorld,
    private val background: Background,
    private val settings: SettingsRepository,
) : WorldDrawable {
    private val gridCell = textureHandler.game.skin.gridCell
    private val food = FoodRenderer(textureHandler, world.food, settings)
    private val snake = SnakeRenderer(textureHandler, world.snake, world.food, settings)
    private val disintegratingSnake = DisintegratingSnakeRenderer(textureHandler, world.snake, settings)
    private val lastFood = Vector2()
    private var appearance = settings.appearance

    fun reset() {
        food.reset()
        snake.reset()
        disintegratingSnake.reset()
        lastFood.set(world.food.position)
    }

    fun disintegrate() {
        disintegratingSnake.disintegrate(snake.colors.at(0))
    }

    override fun draw(batch: SpriteBatch, delta: Float) {
        if (appearance !== settings.appearance) {
            appearance = settings.appearance
            snake.colors.reset()
        }
        if (world.food.position != lastFood) onFoodEaten()
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

    private fun onFoodEaten() {
        lastFood.set(world.food.position)
        if (settings.appearance.takesFruitColor) snake.colors.change(food.color)
        food.nextFruit()
    }

    private fun drawGrid(batch: SpriteBatch) {
        batch.color = Palette.gridLine
        for (x in 0..<WORLD_WIDTH.toInt()) {
            for (y in 0..<WORLD_HEIGHT.toInt()) {
                batch.draw(gridCell, x.toFloat(), y.toFloat(), 1f, 1f)
            }
        }
        batch.resetColor()
    }
}
