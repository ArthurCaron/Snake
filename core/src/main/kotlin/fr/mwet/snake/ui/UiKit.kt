package fr.mwet.snake.ui

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import fr.mwet.snake.GameViewport
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.inputs.InputRouter
import fr.mwet.snake.save.settings.SettingsRepository

class UiKit(
    val batch: SpriteBatch,
    val gameViewport: GameViewport,
    val textures: TextureHandler,
    val styles: UiStyles,
    val input: InputRouter,
    val settingsRepository: SettingsRepository,
)
