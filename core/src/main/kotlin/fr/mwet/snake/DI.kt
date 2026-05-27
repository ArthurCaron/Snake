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
import fr.mwet.snake.inputs.game.GameInputProcessor
import fr.mwet.snake.inputs.general.GeneralInputProcessor
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
    fun initBeforeAssetsAreLoaded() = register {
        // Assets
        val assetManager = withBindSingleton<AssetManager> { AssetManager() }
        val assetHandler = withBindSingleton<AssetHandler> { AssetHandler(assetManager) }
        val i18NHandler = withBindSingleton<I18NHandler> { I18NHandler(assetManager) }
        assetHandler.listenToAssetsLoaded(i18NHandler)
        bindSingleton<MusicHandler> { MusicHandler(assetManager) }
        bindSingleton<SoundHandler> { SoundHandler(assetManager) }
        bindSingleton<TextureHandler> { TextureHandler(assetManager) }

        // Input Handlers
        val inputMultiplexer = withBindSingleton<InputMultiplexer> { InputMultiplexer() }.also {
            Gdx.input.inputProcessor = it
        }
        inputMultiplexer.addProcessor(withBindSingleton<GeneralInputProcessor> { GeneralInputProcessor() })

        // Cameras
        val camera = withBindSingleton<OrthographicCamera> { OrthographicCamera(WORLD_WIDTH, WORLD_HEIGHT) }

        // Viewports
        val gameViewport = withBindSingleton<GameViewport> { GameViewport(camera) }
        Game.addViewport(gameViewport)
        val stageViewport = withBindSingleton<StageViewport> { StageViewport() }
        Game.addViewport(stageViewport)

        // Batches
        val spriteBatch = withBindSingleton<SpriteBatch> { SpriteBatch() }

        // Stages
        val stage = withBindSingleton<Stage> { stage(spriteBatch, stageViewport) }
        inputMultiplexer.addProcessor(stage)

        Game.addScreen(withBindSingleton {
            LoadingScreen(
                assetHandler = assetHandler,
                batch = spriteBatch,
                gameCamera = camera,
            )
        })

        Game.setScreen<LoadingScreen>()
    }

    fun finishInitAfterAssetsAreLoaded() = register {
        // Assets
        val assetHandler = inject<AssetHandler>()
        val i18NHandler = inject<I18NHandler>()
        val musicHandler = inject<MusicHandler>()
        val soundHandler = inject<SoundHandler>()
        val textureHandler = inject<TextureHandler>()

        // Input Handlers
        val inputMultiplexer = inject<InputMultiplexer>()

        // Cameras
        val camera = inject<OrthographicCamera>()

        // Viewports
        val gameViewport = inject<GameViewport>()
        val stageViewport = inject<StageViewport>()

        // Batches
        val spriteBatch = inject<SpriteBatch>()

        // Stages
        val stage = inject<Stage>()

        // Game World
        val gameWorld = withBindSingleton<GameWorld> { GameWorld() }
        inputMultiplexer.addProcessor(withBindSingleton<GameInputProcessor> { GameInputProcessor(gameWorld) })

        // Screens
        Game.addScreen(withBindSingleton {
            MainMenuScreen(
                textureHandler = textureHandler,
                soundHandler = soundHandler,
                stage = stage,
                batch = spriteBatch,
                gameViewport = gameViewport,
                stageViewport = stageViewport,
                gameCamera = camera,
            )
        })
        Game.addScreen(withBindSingleton { SettingsScreen() })
        Game.addScreen(withBindSingleton {
            GameScreen(
                textureHandler = textureHandler,
                stage = stage,
                batch = spriteBatch,
                gameViewport = gameViewport,
                stageViewport = stageViewport,
                gameCamera = camera,
                gameWorld = gameWorld,
            )
        })
    }
}

inline fun <reified Type : Any> withBindSingleton(provider: () -> Type): Type {
    bindSingleton(provider())
    return inject<Type>()
}

class GameViewport(camera: OrthographicCamera) : FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera)

class StageViewport : ScreenViewport()
