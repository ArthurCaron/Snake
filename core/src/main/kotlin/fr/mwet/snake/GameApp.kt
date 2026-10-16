package fr.mwet.snake

import fr.mwet.snake.events.MenuEvent.LoadingFinished
import com.badlogic.gdx.Gdx
import fr.mwet.snake.assets.loadTranslations
import fr.mwet.snake.inputs.KeyNames
import fr.mwet.snake.inputs.LibGdxKeyNames
import fr.mwet.snake.save.settings.Language
import fr.mwet.snake.screens.controls.ControlsScreen
import fr.mwet.snake.screens.game.GameScreen
import fr.mwet.snake.screens.language.LanguageScreen
import fr.mwet.snake.screens.loading.LoadingScreen
import fr.mwet.snake.screens.menu.MainMenuScreen
import fr.mwet.snake.screens.scores.ScoresScreen
import fr.mwet.snake.screens.settings.SettingsScreen
import fr.mwet.snake.utils.SplashWorker
import ktx.app.KtxGame
import ktx.app.KtxScreen

class GameApp(
    private val splashWorker: SplashWorker? = null,
    private val keyNames: KeyNames = LibGdxKeyNames,
) : KtxGame<KtxScreen>(), Navigator, LanguageSwitcher {
    private lateinit var boot: Boot
    private lateinit var assets: LoadedAssets

    override fun create() {
        splashWorker?.closeSplashScreen()
        boot = Boot()
        addScreen(LoadingScreen(boot.assetLoading, boot.batch, boot.gameViewport, onLoaded = ::onAssetsLoaded))
        setScreen<LoadingScreen>()
    }

    private fun onAssetsLoaded(assets: LoadedAssets) {
        this.assets = assets
        loadTranslations(boot.settingsRepository.language.locale())
        val context = GameContext(boot, assets, keyNames, navigator = this, languages = this)
        registerScreens(context)
        context.menuEvents.emit(LoadingFinished)
        goToMainMenu()
        removeScreen<LoadingScreen>()?.dispose()
    }

    override fun applyLanguage(language: Language) {
        boot.settingsRepository.setLanguage(language)
        Gdx.app.postRunnable {
            loadTranslations(language.locale())
            val oldScreens = unregisterScreens()
            registerScreens(GameContext(boot, assets, keyNames, navigator = this, languages = this))
            goToLanguage()
            oldScreens.forEach { it.dispose() }
        }
    }

    override fun goToMainMenu() = setScreen<MainMenuScreen>()

    override fun goToGame() = setScreen<GameScreen>()

    override fun goToScores() = setScreen<ScoresScreen>()

    override fun goToSettings() = setScreen<SettingsScreen>()

    override fun goToControls() = setScreen<ControlsScreen>()

    override fun goToLanguage() = setScreen<LanguageScreen>()

    override fun dispose() {
        super.dispose()
        boot.dispose()
    }

    private fun registerScreens(context: GameContext) {
        addScreen(context.mainMenuScreen)
        addScreen(context.gameScreen)
        addScreen(context.scoresScreen)
        addScreen(context.settingsScreen)
        addScreen(context.controlsScreen)
        addScreen(context.languageScreen)
    }

    private fun unregisterScreens(): List<KtxScreen> = listOfNotNull(
        removeScreen<MainMenuScreen>(),
        removeScreen<GameScreen>(),
        removeScreen<ScoresScreen>(),
        removeScreen<SettingsScreen>(),
        removeScreen<ControlsScreen>(),
        removeScreen<LanguageScreen>(),
    )
}
