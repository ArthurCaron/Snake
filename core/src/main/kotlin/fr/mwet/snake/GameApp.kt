package fr.mwet.snake

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import fr.mwet.snake.assets.loadTranslations
import fr.mwet.snake.events.MenuEvent.LoadingFinished
import fr.mwet.snake.inputs.KeyNames
import fr.mwet.snake.inputs.LibGdxKeyNames
import fr.mwet.snake.save.settings.Language
import fr.mwet.snake.screens.appearance.AppearanceScreen
import fr.mwet.snake.screens.options.OptionPage
import fr.mwet.snake.screens.options.OptionsScreen
import fr.mwet.snake.screens.controls.ControlsScreen
import fr.mwet.snake.screens.devoptions.DevOptionsScreen
import fr.mwet.snake.screens.font.FontScreen
import fr.mwet.snake.screens.game.GameScreen
import fr.mwet.snake.screens.language.LanguageScreen
import fr.mwet.snake.screens.loading.LoadingScreen
import fr.mwet.snake.screens.menu.MainMenuScreen
import fr.mwet.snake.screens.settings.SettingsScreen
import fr.mwet.snake.transition.ScreenTransitions
import fr.mwet.snake.tuning.FontTheme
import fr.mwet.snake.utils.SplashWorker
import ktx.app.KtxGame
import ktx.app.KtxScreen

class GameApp(
    private val splashWorker: SplashWorker? = null,
    private val keyNames: KeyNames = LibGdxKeyNames,
) : KtxGame<KtxScreen>(), Navigator, LanguageSwitcher, FontSwitcher {
    private lateinit var boot: Boot
    private lateinit var assets: LoadedAssets
    private var transitions: ScreenTransitions? = null

    override fun create() {
        splashWorker?.closeSplashScreen()
        boot = Boot()
        addScreen(LoadingScreen(boot.assetLoading, boot.batch, boot.gameViewport, onLoaded = ::onAssetsLoaded))
        setScreen<LoadingScreen>()
    }

    private fun onAssetsLoaded(assets: LoadedAssets) {
        this.assets = assets
        transitions = ScreenTransitions(boot.batch, assets.textures.rectangleTexture(1, 1, Color.WHITE), boot.settingsRepository)
        loadTranslations(boot.settingsRepository.language.locale())
        val context = newContext()
        registerScreens(context)
        context.menuEvents.emit(LoadingFinished)
        setScreen<MainMenuScreen>()
        removeScreen<LoadingScreen>()?.dispose()
    }

    override fun applyLanguage(language: Language) {
        boot.settingsRepository.setLanguage(language)
        // The new screens replace the shown one in place: the language changes from the main menu and from the settings.
        val shown = shownScreen.javaClass
        rebuildScreens(prepare = { loadTranslations(language.locale()) }, show = { setScreen(shown) })
    }

    override fun applyFontTheme(theme: FontTheme) {
        boot.settingsRepository.setFontTheme(theme)
        rebuildScreens(show = { setScreen<FontScreen>() })
    }

    private fun rebuildScreens(prepare: () -> Unit = {}, show: () -> Unit) {
        Gdx.app.postRunnable {
            prepare()
            val oldScreens = unregisterScreens()
            registerScreens(newContext())
            show()
            oldScreens.forEach { it.dispose() }
        }
    }

    private fun newContext() = GameContext(boot, assets, keyNames, navigator = this, languages = this, fontSwitcher = this)

    override fun goToMainMenu() = navigate { setScreen<MainMenuScreen>() }

    override fun goToGame() = navigate { setScreen<GameScreen>() }

    override fun goToSettings() = navigate { setScreen<SettingsScreen>() }

    override fun goToControls() = navigate { setScreen<ControlsScreen>() }

    override fun goToLanguage() = navigate { setScreen<LanguageScreen>() }

    override fun goToFont() = navigate { setScreen<FontScreen>() }

    override fun goToAppearance() = navigate { setScreen<AppearanceScreen>() }

    override fun goToOptions(page: OptionPage) = navigate {
        getScreen<OptionsScreen>().configure(page)
        setScreen<OptionsScreen>()
    }

    override fun previewTransition() { transitions?.start {} }

    override fun goToDevOptions() = navigate { setScreen<DevOptionsScreen>() }

    private fun navigate(switch: () -> Unit) {
        transitions?.start(switch) ?: switch()
    }

    override fun render() {
        super.render()
        transitions?.render(Gdx.graphics.deltaTime)
    }

    override fun dispose() {
        super.dispose()
        boot.dispose()
    }

    private fun registerScreens(context: GameContext) {
        addScreen(context.mainMenuScreen)
        addScreen(context.gameScreen)
        addScreen(context.settingsScreen)
        addScreen(context.controlsScreen)
        addScreen(context.languageScreen)
        addScreen(context.fontScreen)
        addScreen(context.devOptionsScreen)
        addScreen(context.appearanceScreen)
        addScreen(context.optionsScreen)
    }

    private fun unregisterScreens(): List<KtxScreen> = listOfNotNull(
        removeScreen<MainMenuScreen>(),
        removeScreen<GameScreen>(),
        removeScreen<SettingsScreen>(),
        removeScreen<ControlsScreen>(),
        removeScreen<LanguageScreen>(),
        removeScreen<FontScreen>(),
        removeScreen<DevOptionsScreen>(),
        removeScreen<AppearanceScreen>(),
        removeScreen<OptionsScreen>(),
    )
}
