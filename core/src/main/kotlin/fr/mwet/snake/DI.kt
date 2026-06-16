package fr.mwet.snake

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys.*
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
import fr.mwet.snake.events.GameEventBus
import fr.mwet.snake.events.MenuEventBus
import fr.mwet.snake.game.GameWorld
import fr.mwet.snake.inputs.game.*
import fr.mwet.snake.inputs.general.GeneralInputProcessor
import fr.mwet.snake.inputs.general.GoBackToMainMenu
import fr.mwet.snake.inputs.general.StartGame
import fr.mwet.snake.save.Jsonifier
import fr.mwet.snake.save.fileFormats.*
import fr.mwet.snake.save.fromJson
import fr.mwet.snake.save.toJson
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
        // Event Bus
        val gameEventBus = withBindSingleton<GameEventBus> { GameEventBus() }
        val menuEventBus = withBindSingleton<MenuEventBus> { MenuEventBus() }

        // Save system
        val jsonifier = withBindSingleton<Jsonifier> { Jsonifier() }
        val highscoresFile = Highscores(
            highscores = listOf(
                Highscore(name = "Test", score = 1000), Highscore(name = "Test2", score = 2000)
            )
        ).toJson()

        val keymappingsFile = Keymappings(
            game = listOf(
                GameKeymapping(GoUp(), UP),
                GameKeymapping(GoUp(), W),
                GameKeymapping(GoRight(), RIGHT),
                GameKeymapping(GoRight(), D),
                GameKeymapping(GoDown(), DOWN),
                GameKeymapping(GoDown(), S),
                GameKeymapping(GoLeft(), LEFT),
                GameKeymapping(GoLeft(), A),
            ), general = listOf(
                GeneralKeymapping(GoBackToMainMenu(), ESCAPE),
                GeneralKeymapping(StartGame(), ENTER)
            )
        ).toJson()

        // Should get them from file
        val highscores = highscoresFile.fromJson()
        val keymapping = withBindSingleton<Keymappings> { keymappingsFile.fromJson() }

        // Assets
        val assetManager = withBindSingleton<AssetManager> { AssetManager() }
        val assetHandler = withBindSingleton<AssetHandler> { AssetHandler(assetManager) }
        val i18NHandler = withBindSingleton<I18NHandler> { I18NHandler(assetManager) }
        assetHandler.listenToAssetsLoaded(i18NHandler)
        bindSingleton<MusicHandler> { MusicHandler(assetManager) }
        val soundHandler = withBindSingleton<SoundHandler> { SoundHandler(assetManager) }
        val soundPlayer = withBindSingleton<SoundPlayer> { SoundPlayer(soundHandler) }
        gameEventBus.listen(soundPlayer)
        menuEventBus.listen(soundPlayer)
        bindSingleton<TextureHandler> { TextureHandler(assetManager) }

        // Input Handlers
        val inputMultiplexer = withBindSingleton<InputMultiplexer> { InputMultiplexer() }.also {
            Gdx.input.inputProcessor = it
        }
        inputMultiplexer.addProcessor(withBindSingleton<GeneralInputProcessor> {
            GeneralInputProcessor(keymapping.general)
        })

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
        // Save system
        val keymapping = inject<Keymappings>()

        // Assets
        val assetHandler = inject<AssetHandler>()
        val i18NHandler = inject<I18NHandler>()
        val musicHandler = inject<MusicHandler>()
        val soundPlayer = inject<SoundPlayer>()
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

        // Event Bus
        val gameEventBus = inject<GameEventBus>()
        val menuEventBus = inject<MenuEventBus>()

        // Game World
        val gameWorld = withBindSingleton<GameWorld> { GameWorld(gameEventBus) }
        gameEventBus.listen(gameWorld)
        val gameInputProcessor = withBindSingleton<GameInputProcessor> {
            GameInputProcessor(keymapping.game, gameWorld)
        }
        inputMultiplexer.addProcessor(gameInputProcessor)

        // Screens
        val mainMenuScreen = withBindSingleton {
            MainMenuScreen(
                textureHandler = textureHandler,
                menuEventBus = menuEventBus,
                stage = stage,
                batch = spriteBatch,
                gameViewport = gameViewport,
                stageViewport = stageViewport,
                gameCamera = camera,
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

        menuEventBus.listen(mainMenuScreen)
        gameEventBus.listen(gameScreen)

        Game.addScreen(mainMenuScreen)
        Game.addScreen(settingsScreen)
        Game.addScreen(gameScreen)
    }
}

inline fun <reified Type : Any> withBindSingleton(provider: () -> Type): Type {
    bindSingleton(provider())
    return inject<Type>()
}

class GameViewport(camera: OrthographicCamera) : FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera)

class StageViewport : ScreenViewport()
