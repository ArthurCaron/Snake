package fr.mwet.snake

import fr.mwet.snake.events.MenuEvent.LoadingFinished

import fr.mwet.snake.screens.controls.ControlsScreen
import fr.mwet.snake.screens.game.GameScreen
import fr.mwet.snake.screens.loading.LoadingScreen
import fr.mwet.snake.screens.menu.MainMenuScreen
import fr.mwet.snake.screens.scores.ScoresScreen
import fr.mwet.snake.screens.settings.SettingsScreen
import fr.mwet.snake.utils.SplashWorker
import ktx.app.KtxGame
import ktx.app.KtxScreen

class SnakeGame(private val splashWorker: SplashWorker? = null) : KtxGame<KtxScreen>(), Navigator {
    private lateinit var boot: Boot

    override fun create() {
        splashWorker?.closeSplashScreen()
        boot = Boot()
        addScreen(LoadingScreen(boot.assetLoading, boot.batch, boot.gameViewport, onLoaded = ::onAssetsLoaded))
        setScreen<LoadingScreen>()
    }

    private fun onAssetsLoaded(assets: LoadedAssets) {
        val context = GameContext(boot, assets, navigator = this)
        addScreen(context.mainMenuScreen)
        addScreen(context.gameScreen)
        addScreen(context.scoresScreen)
        addScreen(context.settingsScreen)
        addScreen(context.controlsScreen)
        boot.menuEvents.emit(LoadingFinished)
        goToMainMenu()
        removeScreen<LoadingScreen>()?.dispose()
    }

    override fun goToMainMenu() = setScreen<MainMenuScreen>()

    override fun goToGame() = setScreen<GameScreen>()

    override fun goToScores() = setScreen<ScoresScreen>()

    override fun goToSettings() = setScreen<SettingsScreen>()

    override fun goToControls() = setScreen<ControlsScreen>()

    override fun dispose() {
        super.dispose()
        boot.dispose()
    }
}
