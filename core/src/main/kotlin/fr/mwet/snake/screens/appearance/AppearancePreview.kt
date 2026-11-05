package fr.mwet.snake.screens.appearance

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import fr.mwet.snake.game.Food
import fr.mwet.snake.game.Snake
import fr.mwet.snake.render.FoodRenderer
import fr.mwet.snake.render.SnakeRenderer
import fr.mwet.snake.ui.UiKit
import fr.mwet.snake.ui.UiMetrics
import fr.mwet.snake.utils.Direction

// Use the game renderers so this preview stays faithful to the game.
class AppearancePreview(private val kit: UiKit) : Widget() {
    private val food = Food(Vector2(6f, 1f))
    private val snake = Snake().apply {
        arrangeAlong(listOf(Vector2(4f, 1f), Vector2(3f, 1f), Vector2(2f, 1f), Vector2(2f, 0f), Vector2(1f, 0f)), Direction.RIGHT)
    }
    private val snakeRenderer = SnakeRenderer(kit.textures, snake, food, kit.settingsRepository)
    private val foodRenderer = FoodRenderer(kit.textures, food, kit.settingsRepository)
    private val savedTransform = Matrix4()
    private val transform = Matrix4()
    private var version = -1
    private var delta = 0f

    override fun getPrefWidth() = UiMetrics.APPEARANCE_PREVIEW_WIDTH
    override fun getPrefHeight() = UiMetrics.APPEARANCE_PREVIEW_HEIGHT

    override fun act(delta: Float) {
        super.act(delta)
        this.delta = delta
        if (version != kit.settingsRepository.version) {
            version = kit.settingsRepository.version
            snakeRenderer.reset()
            if (kit.settingsRepository.appearance.takesFruitColor) snakeRenderer.colors.change(foodRenderer.color)
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        val sprites = batch as SpriteBatch
        val cell = minOf(width / 9f, height / 4f)
        savedTransform.set(batch.transformMatrix)
        transform.set(savedTransform).translate(x + (width - 8f * cell) / 2, y + cell, 0f).scale(cell, cell, 1f)
        batch.transformMatrix = transform
        foodRenderer.render(sprites, delta)
        snakeRenderer.render(sprites, delta)
        batch.transformMatrix = savedTransform
    }
}
