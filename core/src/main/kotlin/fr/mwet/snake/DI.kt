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
import fr.mwet.snake.screens.GameScreen
import fr.mwet.snake.screens.LoadingScreen
import fr.mwet.snake.screens.MainMenuScreen
import fr.mwet.snake.screens.SettingsScreen
import fr.mwet.snake.utils.WORLD_HEIGHT
import fr.mwet.snake.utils.WORLD_WIDTH
import ktx.actors.stage
import ktx.inject.Context
import ktx.inject.register

object DI : Context() {
    fun initialize() =
        register {
            // Assets
            val assetManager = withBindSingleton<AssetManager> { AssetManager() }
            val assetHandler = withBindSingleton<AssetHandler> { AssetHandler(assetManager) }
            val i18NHandler = withBindSingleton<I18NHandler> { I18NHandler(assetManager) }
            assetHandler.listenToAssetsLoaded(i18NHandler)
            val musicHandler = withBindSingleton<MusicHandler> { MusicHandler(assetManager) }
            val soundHandler = withBindSingleton<SoundHandler> { SoundHandler(assetManager) }
            val textureHandler = withBindSingleton<TextureHandler> { TextureHandler(assetManager) }

            // Batches
            val spriteBatch = withBindSingleton<SpriteBatch> { SpriteBatch() }

            // Cameras
            val camera = withBindSingleton<OrthographicCamera> { OrthographicCamera(WORLD_WIDTH, WORLD_HEIGHT) }

            // Viewports
            val gameViewport = withBindSingleton<GameViewport> { GameViewport(camera) }
            val stageViewport = withBindSingleton<StageViewport> { StageViewport() }

            // Stages
            val stage = withBindSingleton<Stage> { stage(spriteBatch, stageViewport) }

            // Input Handlers
            val inputMultiplexer = withBindSingleton<InputMultiplexer> { InputMultiplexer() }.also {
                it.addProcessor(stage)
                Gdx.input.inputProcessor = it
            }

            // Game
            val game = withBindSingleton<Game> { Game(gameViewport, stageViewport) }

            val gameWorld = withBindSingleton<GameWorld> { GameWorld() }

            val loadingScreen = withBindSingleton {
                LoadingScreen(
                    assetHandler = assetHandler,
                    batch = spriteBatch,
                    gameCamera = camera,
                    game = game,
                )
            }
            val mainMenuScreen = withBindSingleton {
                MainMenuScreen(
                    textureHandler = textureHandler,
                    soundHandler = soundHandler,
                    stage = stage,
                    batch = spriteBatch,
                    gameViewport = gameViewport,
                    stageViewport = stageViewport,
                    gameCamera = camera,
                    game = game,
                )
            }
            val settingsScreen = withBindSingleton { SettingsScreen() }
            val gameScreen = withBindSingleton {
                GameScreen(
                    textureHandler = textureHandler,
                    stage = stage,
                    batch = spriteBatch,
                    gameViewport = gameViewport,
                    stageViewport = stageViewport,
                    gameCamera = camera,
                    gameWorld = gameWorld,
                )
            }

            game.addScreen(loadingScreen)
            game.addScreen(mainMenuScreen)
            game.addScreen(settingsScreen)
            game.addScreen(gameScreen)
            game.setScreen<LoadingScreen>()
        }
}

inline fun <reified Type : Any> withBindSingleton(provider: () -> Type): Type {
    bindSingleton(provider())
    return inject<Type>()
}

class GameViewport(camera: OrthographicCamera) : FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera)

class StageViewport : ScreenViewport()
