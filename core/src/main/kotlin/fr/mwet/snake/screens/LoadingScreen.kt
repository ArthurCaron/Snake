package fr.mwet.snake.screens

import fr.mwet.snake.DI
import fr.mwet.snake.Game
import fr.mwet.snake.assets.AssetHandler
import fr.mwet.snake.assets.Progress
import ktx.app.KtxScreen
import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class LoadingScreen : KtxScreen, DisposableRegistry by DisposableContainer() {
    private val assetHandler = DI.inject<AssetHandler>()
    private val game = DI.inject<Game>()

    override fun render(delta: Float) {
        val progress = assetHandler.loadAssets()
        if (progress == Progress(100f)) {
            game.setScreen<MainMenuScreen>()
        }
    }
}
