package fr.mwet.snake

import fr.mwet.snake.assets.SoundPlayer
import fr.mwet.snake.events.GameEventBus
import fr.mwet.snake.events.GameEventBusImpl
import fr.mwet.snake.events.MenuEventBus
import fr.mwet.snake.events.MenuEventBusImpl
import fr.mwet.snake.game.GameWorld
import fr.mwet.snake.inputs.GameCommandContext
import fr.mwet.snake.inputs.GameInputProcessor
import fr.mwet.snake.inputs.KeyNames
import fr.mwet.snake.inputs.TouchInputProcessor
import fr.mwet.snake.screens.controls.ControlsScreen
import fr.mwet.snake.screens.game.GameScreen
import fr.mwet.snake.screens.language.LanguageScreen
import fr.mwet.snake.screens.menu.MainMenuScreen
import fr.mwet.snake.screens.scores.ScoresScreen
import fr.mwet.snake.screens.settings.SettingsScreen
import fr.mwet.snake.ui.UiKit
import fr.mwet.snake.ui.createUiSkin
import ktx.scene2d.Scene2DSkin

class GameContext(boot: Boot, assets: LoadedAssets, keyNames: KeyNames, navigator: Navigator, languages: LanguageSwitcher) {
    init {
        Scene2DSkin.defaultSkin = createUiSkin(assets.fonts, assets.textures)
    }

    private val gameEvents: GameEventBus = GameEventBusImpl()
    val menuEvents: MenuEventBus = MenuEventBusImpl()
    private val kit = UiKit(boot.batch, boot.gameViewport, assets.textures, boot.input, boot.settingsRepository)
    private val soundPlayer = SoundPlayer(assets.sounds, assets.music, boot.settingsRepository)
    private val world = GameWorld(gameEvents)
    private val gameCommandContext = GameCommandContext(world)
    private val gameInput = GameInputProcessor(boot.settingsRepository, gameCommandContext)
    private val touchInput = TouchInputProcessor(gameCommandContext)

    val mainMenuScreen = MainMenuScreen(
        kit = kit,
        menuEventBus = menuEvents,
        metadataRepository = boot.metadataRepository,
        settingsRepository = boot.settingsRepository,
        navigator = navigator,
    )

    val gameScreen = GameScreen(
        kit = kit,
        world = world,
        metadataRepository = boot.metadataRepository,
        gameInput = gameInput,
        touchInput = touchInput,
        navigator = navigator,
    )

    val scoresScreen = ScoresScreen(kit, boot.metadataRepository, navigator)
    val settingsScreen = SettingsScreen(kit, boot.settingsRepository, menuEvents, navigator)
    val controlsScreen = ControlsScreen(kit, boot.settingsRepository, keyNames, navigator)
    val languageScreen = LanguageScreen(kit, boot.settingsRepository, languages, navigator)

    init {
        gameEvents.listen(soundPlayer)
        menuEvents.listen(soundPlayer)
        menuEvents.listen(mainMenuScreen)
    }
}
