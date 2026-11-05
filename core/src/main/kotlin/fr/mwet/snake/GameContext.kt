package fr.mwet.snake

import fr.mwet.snake.assets.SoundPlayer
import fr.mwet.snake.backdrop.MenuBackdrop
import fr.mwet.snake.events.GameEventBus
import fr.mwet.snake.events.GameEventBusImpl
import fr.mwet.snake.events.MenuEvent.ButtonActivated
import fr.mwet.snake.events.MenuEventBus
import fr.mwet.snake.events.MenuEventBusImpl
import fr.mwet.snake.game.GameWorld
import fr.mwet.snake.inputs.GameCommandContext
import fr.mwet.snake.inputs.GameInputProcessor
import fr.mwet.snake.inputs.KeyNames
import fr.mwet.snake.inputs.TouchInputProcessor
import fr.mwet.snake.screens.appearance.AdminAppearanceScreen
import fr.mwet.snake.screens.appearance.AppearanceScreen
import fr.mwet.snake.screens.options.OptionsScreen
import fr.mwet.snake.screens.controls.ControlsScreen
import fr.mwet.snake.screens.devoptions.DevOptionsScreen
import fr.mwet.snake.screens.font.FontScreen
import fr.mwet.snake.screens.game.GameScreen
import fr.mwet.snake.screens.language.LanguageScreen
import fr.mwet.snake.screens.menu.MainMenuScreen
import fr.mwet.snake.screens.settings.SettingsScreen
import fr.mwet.snake.ui.UiKit
import fr.mwet.snake.ui.Widgets
import fr.mwet.snake.render.Background
import fr.mwet.snake.ui.createUiSkin
import ktx.scene2d.Scene2DSkin

class GameContext(
    boot: Boot,
    assets: LoadedAssets,
    keyNames: KeyNames,
    navigator: Navigator,
    languages: LanguageSwitcher,
    fontSwitcher: FontSwitcher,
) {
    init {
        assets.textures.selectTheme(boot.settingsRepository.appearance.theme)
        fr.mwet.snake.tuning.Palette.applyTheme(boot.settingsRepository.appearance.theme)
        Scene2DSkin.defaultSkin = createUiSkin(assets.fonts, boot.settingsRepository.fontTheme, assets.textures, boot.settingsRepository)
    }

    private val gameEvents: GameEventBus = GameEventBusImpl()
    val menuEvents: MenuEventBus = MenuEventBusImpl()
    private val widgets = Widgets(boot.settingsRepository.appearance.theme) { menuEvents.emit(ButtonActivated) }
    private val background = Background(assets.textures, boot.settingsRepository)
    private val backdrop = MenuBackdrop(assets.textures, boot.settingsRepository, background)
    private val kit = UiKit(boot.batch, boot.gameViewport, assets.textures, boot.input, boot.settingsRepository, widgets, backdrop)
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
        languages = languages,
        navigator = navigator,
    )

    val gameScreen = GameScreen(
        kit = kit,
        world = world,
        background = background,
        metadataRepository = boot.metadataRepository,
        gameInput = gameInput,
        touchInput = touchInput,
        navigator = navigator,
    )

    val settingsScreen = SettingsScreen(kit, boot.settingsRepository, menuEvents, navigator)
    val controlsScreen = ControlsScreen(kit, boot.settingsRepository, keyNames, navigator)
    val languageScreen = LanguageScreen(kit, boot.settingsRepository, languages, navigator)
    val fontScreen = FontScreen(kit, assets.fonts, boot.settingsRepository, fontSwitcher, navigator)
    val adminAppearanceScreen = AdminAppearanceScreen(kit, navigator)
    val appearanceScreen = AppearanceScreen(kit, navigator)
    val optionsScreen = OptionsScreen(kit, navigator)
    val devOptionsScreen = DevOptionsScreen(kit, navigator)

    init {
        gameEvents.listen(soundPlayer)
        menuEvents.listen(soundPlayer)
        menuEvents.listen(mainMenuScreen)
    }
}
