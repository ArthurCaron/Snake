package fr.mwet.snake

import fr.mwet.snake.assets.SoundPlayer
import fr.mwet.snake.game.GameWorld
import fr.mwet.snake.inputs.GameCommandContext
import fr.mwet.snake.inputs.GameInputProcessor
import fr.mwet.snake.inputs.GeneralInputProcessor
import fr.mwet.snake.inputs.SwipeInputProcessor
import fr.mwet.snake.screens.game.GameScreen
import fr.mwet.snake.screens.menu.MainMenuScreen
import fr.mwet.snake.screens.scores.ScoresScreen
import fr.mwet.snake.screens.settings.SettingsScreen
import fr.mwet.snake.ui.UiKit
import fr.mwet.snake.ui.UiStyles

class GameContext(boot: Boot, assets: LoadedAssets, navigator: Navigator) {
    private val styles = UiStyles(assets.fonts, assets.textures)
    private val kit = UiKit(boot.batch, boot.gameViewport, assets.textures, styles, boot.input)
    private val soundPlayer = SoundPlayer(assets.sounds, assets.music, boot.settingsRepository)
    private val world = GameWorld(boot.gameEvents)
    private val gameCommandContext = GameCommandContext(world, boot.gameEvents)
    private val gameInput = GameInputProcessor(boot.settingsRepository, gameCommandContext)
    private val swipeInput = SwipeInputProcessor(gameCommandContext)
    private val generalInput = GeneralInputProcessor(boot.settingsRepository, boot.menuEvents)

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

    val scoresScreen = ScoresScreen(kit, boot.metadataRepository, navigator)
    val settingsScreen = SettingsScreen(kit, boot.settingsRepository, boot.menuEvents, navigator)

    init {
        boot.gameEvents.listen(soundPlayer)
        boot.menuEvents.listen(soundPlayer)
        boot.menuEvents.listen(mainMenuScreen)
        boot.gameEvents.listen(gameScreen)
    }
}
