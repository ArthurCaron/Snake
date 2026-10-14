package fr.mwet.snake

import fr.mwet.snake.assets.SoundPlayer
import fr.mwet.snake.game.GameWorld
import fr.mwet.snake.inputs.GameCommandContext
import fr.mwet.snake.inputs.GameInputProcessor
import fr.mwet.snake.inputs.GeneralInputProcessor
import fr.mwet.snake.inputs.SwipeInputProcessor
import fr.mwet.snake.screens.game.GameScreen
import fr.mwet.snake.screens.menu.MainMenuScreen
import fr.mwet.snake.ui.GameUiStyles

class GameContext(boot: Boot, assets: LoadedAssets, navigator: Navigator) {
    private val styles = GameUiStyles(assets.fonts, assets.textures)
    private val soundPlayer = SoundPlayer(assets.sounds, assets.music, boot.settingsRepository)
    private val world = GameWorld(boot.gameEvents)
    private val gameCommandContext = GameCommandContext(world, boot.gameEvents)
    private val gameInput = GameInputProcessor(boot.settingsRepository.settings.keymappings.game, gameCommandContext)
    private val swipeInput = SwipeInputProcessor(gameCommandContext)
    private val generalInput =
        GeneralInputProcessor(boot.settingsRepository.settings.keymappings.general, boot.menuEvents)

    val mainMenuScreen = MainMenuScreen(
        menuEventBus = boot.menuEvents,
        fontHandler = assets.fonts,
        textureHandler = assets.textures,
        batch = boot.batch,
        gameViewport = boot.gameViewport,
        metadataRepository = boot.metadataRepository,
        settingsRepository = boot.settingsRepository,
        input = boot.input,
        menuInput = generalInput,
        navigator = navigator,
    )

    val gameScreen = GameScreen(
        textureHandler = assets.textures,
        batch = boot.batch,
        gameViewport = boot.gameViewport,
        world = world,
        styles = styles,
        metadataRepository = boot.metadataRepository,
        input = boot.input,
        gameInput = gameInput,
        swipeInput = swipeInput,
        navigator = navigator,
    )

    init {
        boot.gameEvents.listen(soundPlayer)
        boot.menuEvents.listen(soundPlayer)
        boot.menuEvents.listen(mainMenuScreen)
        boot.gameEvents.listen(gameScreen)
    }
}
