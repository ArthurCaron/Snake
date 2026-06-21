package fr.mwet.snake

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys.*
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.ScreenViewport
import fr.mwet.snake.DI.bindSingleton
import fr.mwet.snake.DI.inject
import fr.mwet.snake.assets.*
import fr.mwet.snake.events.GameEventBus
import fr.mwet.snake.events.GameEventBusImpl
import fr.mwet.snake.events.MenuEventBus
import fr.mwet.snake.events.MenuEventBusImpl
import fr.mwet.snake.game.GameWorld
import fr.mwet.snake.inputs.game.*
import fr.mwet.snake.inputs.general.GeneralInputProcessor
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
import ktx.inject.Context
import ktx.inject.register

object DI : Context() {
    fun initBeforeAssetsAreLoaded() = register {
        // Event Bus
        val gameEventBus = withBindSingleton<GameEventBus> { GameEventBusImpl() }
        val menuEventBus = withBindSingleton<MenuEventBus> { MenuEventBusImpl() }

        // Save system
        val jsonifier = withBindSingleton<Jsonifier> { Jsonifier() }
        val highscoresFile = Highscores(
            highscores = listOf(
                Highscore(name = "Test", score = 1000), Highscore(name = "Test2", score = 2000)
            )
        ).toJson()

        val keymappingsFile = Keymappings(
            game = listOf(
                GameKeymapping(GoBackToMainMenu(), ESCAPE),
                GameKeymapping(GoUp(), UP),
                GameKeymapping(GoUp(), W),
                GameKeymapping(GoRight(), RIGHT),
                GameKeymapping(GoRight(), D),
                GameKeymapping(GoDown(), DOWN),
                GameKeymapping(GoDown(), S),
                GameKeymapping(GoLeft(), LEFT),
                GameKeymapping(GoLeft(), A),
            ), general = listOf(
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
        bindSingleton<InputMultiplexer> {
            InputMultiplexer().also { Gdx.input.inputProcessor = it }
        }
        bindSingleton<GeneralInputProcessor> {
            GeneralInputProcessor(keymapping.general)
        }

        // Cameras
        val camera = withBindSingleton<OrthographicCamera> { OrthographicCamera(WORLD_WIDTH, WORLD_HEIGHT) }

        // Viewports
        val gameViewport = withBindSingleton<GameViewport> { GameViewport(camera) }
        Game.addViewport(gameViewport)

        // Batches
        val spriteBatch = withBindSingleton<SpriteBatch> { SpriteBatch() }

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
        val textureHandler = inject<TextureHandler>()

        // Input Handlers
        val inputMultiplexer = inject<InputMultiplexer>()

        // Cameras
        val camera = inject<OrthographicCamera>()

        // Viewports
        val gameViewport = inject<GameViewport>()

        // Batches
        val spriteBatch = inject<SpriteBatch>()

        // Event Bus
        val gameEventBus = inject<GameEventBus>()
        val menuEventBus = inject<MenuEventBus>()

        // Game World
        val gameWorld = withBindSingleton<GameWorld> { GameWorld(gameEventBus) }
        gameEventBus.listen(gameWorld)
        bindSingleton<GameInputProcessor> {
            GameInputProcessor(keymapping.game, gameWorld)
        }

        // Screens
        val mainMenuScreen = withBindSingleton {
            MainMenuScreen(
                menuEventBus = menuEventBus,
                textureHandler = textureHandler,
                inputMultiplexer = inputMultiplexer,
                batch = spriteBatch,
                gameViewport = gameViewport,
                gameCamera = camera,
            )
        }
        val settingsScreen = withBindSingleton { SettingsScreen() }
        val gameScreen = withBindSingleton {
            GameScreen(
                textureHandler = textureHandler,
                inputMultiplexer = inputMultiplexer,
                batch = spriteBatch,
                gameViewport = gameViewport,
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

    fun registerGameInputProcessor() {
        val gameInputProcessor = inject<GameInputProcessor>()
        val inputMultiplexer = inject<InputMultiplexer>()
        inputMultiplexer.addProcessor(gameInputProcessor)
    }

    fun unRegisterGameInputProcessor() {
        val gameInputProcessor = inject<GameInputProcessor>()
        val inputMultiplexer = inject<InputMultiplexer>()
        inputMultiplexer.removeProcessor(gameInputProcessor)
    }

    fun registerGeneralInputProcessor() {
        val generalInputProcessor = inject<GeneralInputProcessor>()
        val inputMultiplexer = inject<InputMultiplexer>()
        inputMultiplexer.addProcessor(generalInputProcessor)
    }

    fun unRegisterGeneralInputProcessor() {
        val generalInputProcessor = inject<GeneralInputProcessor>()
        val inputMultiplexer = inject<InputMultiplexer>()
        inputMultiplexer.removeProcessor(generalInputProcessor)
    }
}

inline fun <reified Type : Any> withBindSingleton(provider: () -> Type): Type {
    bindSingleton(provider())
    return inject<Type>()
}

class GameViewport(camera: OrthographicCamera) : FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera)

class StageViewport : ScreenViewport()
