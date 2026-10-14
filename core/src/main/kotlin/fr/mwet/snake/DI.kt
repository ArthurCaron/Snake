package fr.mwet.snake

import com.badlogic.gdx.Application.ApplicationType.WebGL
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor
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
import fr.mwet.snake.inputs.GameCommandContext
import fr.mwet.snake.inputs.GameInputProcessor
import fr.mwet.snake.inputs.GeneralInputProcessor
import fr.mwet.snake.inputs.SwipeInputProcessor
import fr.mwet.snake.save.metadata.MetadataRepository
import fr.mwet.snake.save.serialization.FileTextStore
import fr.mwet.snake.save.serialization.JsonStore
import fr.mwet.snake.save.serialization.PreferencesTextStore
import fr.mwet.snake.save.settings.AudioSettings
import fr.mwet.snake.save.settings.Keymappings
import fr.mwet.snake.save.settings.SettingsRepository
import fr.mwet.snake.screens.GameScreen
import fr.mwet.snake.screens.LoadingScreen
import fr.mwet.snake.screens.MainMenuScreen
import fr.mwet.snake.ui.GameUiStyles
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
        val fileTextStore = withBindSingleton { FileTextStore(Gdx.files.local("save_data")) }
        // Find the file around here: C:\Users\$user\.prefs\fr.mwet.snek.save_data
        val preferencesTextStore =
            withBindSingleton { PreferencesTextStore(Gdx.app.getPreferences("fr.mwet.snek.save_data")) }
        val jsonStore = withBindSingleton<JsonStore> {
            JsonStore(if (Gdx.app.type == WebGL) preferencesTextStore else fileTextStore)
        }
        val settingsRepository = withBindSingleton { SettingsRepository(jsonStore) }
        val metadataRepository = withBindSingleton { MetadataRepository(jsonStore) }

        val settings = withBindSingleton { settingsRepository.load() }
        val keymapping = withBindSingleton<Keymappings> { settings.keymappings }

        // Assets
        val assetManager = withBindSingleton<AssetManager> { AssetManager() }
        assetManager.registerFontLoaders()
        val assetHandler = withBindSingleton<AssetHandler> { AssetHandler(assetManager) }
        val i18NHandler = withBindSingleton<I18NHandler> { I18NHandler(assetManager) }
        assetHandler.listenToAssetsLoaded(i18NHandler)
        val fontHandler = withBindSingleton<FontHandler> { FontHandler(assetManager) }
        val musicHandler = withBindSingleton<MusicHandler> { MusicHandler(assetManager) }
        val soundHandler = withBindSingleton<SoundHandler> { SoundHandler(assetManager) }
        val audioSettings = withBindSingleton { AudioSettings(settingsRepository, settings) }
        val soundPlayer = withBindSingleton<SoundPlayer> { SoundPlayer(soundHandler, musicHandler, audioSettings) }
        gameEventBus.listen(soundPlayer)
        menuEventBus.listen(soundPlayer)
        bindSingleton<TextureHandler> { TextureHandler(assetManager) }

        // Input Handlers
        bindSingleton<InputMultiplexer> {
            Gdx.input.setCatchKey(Input.Keys.BACK, true)
            InputMultiplexer().also { Gdx.input.inputProcessor = it }
        }
        bindSingleton<GeneralInputProcessor> {
            GeneralInputProcessor(keymapping.general, menuEventBus)
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
                menuEventBus = menuEventBus,
            )
        })
        Game.setScreen<LoadingScreen>()
    }

    fun finishInitAfterAssetsAreLoaded() = register {
        // Save system
        val keymapping = inject<Keymappings>()

        // Repositories
        val metadataRepository = inject<MetadataRepository>()

        // Assets
        val fontHandler = inject<FontHandler>()
        val textureHandler = inject<TextureHandler>()

        // Cameras
        val camera = inject<OrthographicCamera>()

        // Viewports
        val gameViewport = inject<GameViewport>()

        // Batches
        val spriteBatch = inject<SpriteBatch>()

        // Event Bus
        val gameEventBus = inject<GameEventBus>()
        val menuEventBus = inject<MenuEventBus>()

        // UI
        val gameUiStyles = withBindSingleton { GameUiStyles(fontHandler, textureHandler) }

        // Game World
        val gameWorld = withBindSingleton<GameWorld> { GameWorld(gameEventBus) }
        val gameCommandContext = GameCommandContext(gameWorld, gameEventBus)
        bindSingleton<GameInputProcessor> { GameInputProcessor(keymapping.game, gameCommandContext) }
        bindSingleton<SwipeInputProcessor> { SwipeInputProcessor(gameCommandContext) }

        // Screens
        val audioSettings = inject<AudioSettings>()
        val mainMenuScreen = withBindSingleton {
            MainMenuScreen(
                menuEventBus = menuEventBus,
                fontHandler = fontHandler,
                textureHandler = textureHandler,
                batch = spriteBatch,
                gameViewport = gameViewport,
                gameCamera = camera,
                metadataRepository = metadataRepository,
                audioSettings = audioSettings,
            )
        }
        val gameScreen = withBindSingleton {
            GameScreen(
                textureHandler = textureHandler,
                batch = spriteBatch,
                gameViewport = gameViewport,
                gameCamera = camera,
                gameWorld = gameWorld,
                styles = gameUiStyles,
                metadataRepository = metadataRepository,
            )
        }

        menuEventBus.listen(mainMenuScreen)
        gameEventBus.listen(gameScreen)

        Game.addScreen(mainMenuScreen)
        Game.addScreen(gameScreen)
    }

    fun registerInputProcessor(inputProcessor: InputProcessor) {
        val inputMultiplexer = inject<InputMultiplexer>()
        inputMultiplexer.addProcessor(inputProcessor)
    }

    fun unRegisterInputProcessor(inputProcessor: InputProcessor) {
        val inputMultiplexer = inject<InputMultiplexer>()
        inputMultiplexer.removeProcessor(inputProcessor)
    }

    fun registerGameInputProcessor() {
        registerInputProcessor(inject<GameInputProcessor>())
        registerInputProcessor(inject<SwipeInputProcessor>())
    }

    fun unRegisterGameInputProcessor() {
        unRegisterInputProcessor(inject<GameInputProcessor>())
        unRegisterInputProcessor(inject<SwipeInputProcessor>())
    }

    fun registerGeneralInputProcessor() = registerInputProcessor(inject<GeneralInputProcessor>())
    fun unRegisterGeneralInputProcessor() = unRegisterInputProcessor(inject<GeneralInputProcessor>())
}

private const val UI_REFERENCE_SHORT_SIDE = 900f

inline fun <reified Type : Any> withBindSingleton(provider: () -> Type): Type {
    bindSingleton(provider())
    return inject<Type>()
}

class GameViewport(camera: OrthographicCamera) : FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera)

class StageViewport : ScreenViewport() {
    override fun update(screenWidth: Int, screenHeight: Int, centerCamera: Boolean) {
        unitsPerPixel = UI_REFERENCE_SHORT_SIDE / maxOf(1, minOf(screenWidth, screenHeight))
        super.update(screenWidth, screenHeight, centerCamera)
    }
}
