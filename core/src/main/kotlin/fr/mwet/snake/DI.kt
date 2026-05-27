package fr.mwet.snake

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.ScreenViewport
import fr.mwet.snake.DI.bindSingleton
import fr.mwet.snake.DI.inject
import fr.mwet.snake.assets.*
import fr.mwet.snake.entities.GameWorld
import fr.mwet.snake.utils.WORLD_HEIGHT
import fr.mwet.snake.utils.WORLD_WIDTH
import ktx.actors.stage
import ktx.inject.Context
import ktx.inject.register

object DI : Context() {
    fun initialize() =
        register {
            val assetManager = withBindSingleton<AssetManager> { AssetManager() }
            val assetHandler = withBindSingleton<AssetHandler> { AssetHandler(assetManager) }
            val i18NHandler = withBindSingleton<I18NHandler> { I18NHandler(assetManager) }
            assetHandler.listenToAssetsLoaded(i18NHandler)
            val musicHandler = withBindSingleton<MusicHandler> { MusicHandler(assetManager) }
            val soundHandler = withBindSingleton<SoundHandler> { SoundHandler(assetManager) }
            val textureHandler = withBindSingleton<TextureHandler> { TextureHandler(assetManager) }

            val spriteBatch = withBindSingleton<SpriteBatch> { SpriteBatch() }

            val camera = withBindSingleton<OrthographicCamera> { OrthographicCamera(WORLD_WIDTH, WORLD_HEIGHT) }

            val gameViewport = withBindSingleton<GameViewport> { GameViewport(camera) }

            val stageViewport = withBindSingleton<StageViewport> { StageViewport() }

            val stage = withBindSingleton<Stage> { stage(spriteBatch, stageViewport) }

            val inputMultiplexer = withBindSingleton<InputMultiplexer> { InputMultiplexer() }.also {
                it.addProcessor(stage)
                Gdx.input.inputProcessor = it
            }

            val game = withBindSingleton<Game> { Game(gameViewport, stageViewport) }
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

            val gameWorld = withBindSingleton<GameWorld> { GameWorld() }
        }
}

inline fun <reified Type : Any> withBindSingleton(provider: () -> Type): Type {
    bindSingleton(provider())
    return inject<Type>()
}

class GameViewport(camera: OrthographicCamera) : FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera)

class StageViewport : ScreenViewport()
