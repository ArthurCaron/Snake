package fr.mwet.snake

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.ScreenViewport
import fr.mwet.snake.entities.GameWorld
import fr.mwet.snake.DI.bindSingleton
import fr.mwet.snake.DI.inject
import fr.mwet.snake.assets.AssetHandler
import fr.mwet.snake.utils.SoundHelper
import fr.mwet.snake.utils.WORLD_HEIGHT
import fr.mwet.snake.utils.WORLD_WIDTH
import ktx.actors.stage
import ktx.inject.Context
import ktx.inject.register

object DI : Context() {
    fun initialize() =
        register {
            val assetManager = withBindSingleton<AssetManager> { AssetManager() }
            bindSingleton<AssetHandler> { AssetHandler(assetManager) }
            val game = withBindSingleton<Game> { Game() }

            // For when I change my mind again
//            val loadingScreen = withBindSingleton { LoadingScreen() }
//            val mainMenuScreen = withBindSingleton { MainMenuScreen() }
//            val settingsScreen = withBindSingleton { SettingsScreen() }
//            val gameScreen = withBindSingleton { GameScreen() }
//
//            game.addScreen(loadingScreen)
//            game.addScreen(mainMenuScreen)
//            game.addScreen(settingsScreen)
//            game.addScreen(gameScreen)
//            game.setScreen<LoadingScreen>()

            val spriteBatch = withBindSingleton<SpriteBatch> { SpriteBatch() }
            val camera = withBindSingleton<OrthographicCamera> { OrthographicCamera(WORLD_WIDTH, WORLD_HEIGHT) }
            bindSingleton<GameViewport> { GameViewport(camera) }
            val stageViewport = withBindSingleton<StageViewport> { StageViewport() }
            val stage = withBindSingleton<Stage> { stage(spriteBatch, stageViewport) }
            withBindSingleton<InputMultiplexer> { InputMultiplexer() }.also {
                it.addProcessor(stage)
                Gdx.input.inputProcessor = it
            }
            bindSingleton<SoundHelper> { SoundHelper() }
            bindSingleton<GameWorld> { GameWorld() }
        }
}

inline fun <reified Type : Any> withBindSingleton(provider: () -> Type): Type {
    bindSingleton(provider())
    return inject<Type>()
}

class GameViewport(camera: OrthographicCamera) : FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera)

class StageViewport : ScreenViewport()
