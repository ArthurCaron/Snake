package fr.mwet.snake

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.ScreenUtils
import fr.mwet.snake.screens.GameScreen
import fr.mwet.snake.screens.LoadingScreen
import fr.mwet.snake.screens.MainMenuScreen
import fr.mwet.snake.screens.SettingsScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class Game : KtxGame<KtxScreen>(), DisposableRegistry by DisposableContainer() {
    private val gameViewport by lazy { DI.inject<GameViewport>() }
    private val stageViewport by lazy { DI.inject<StageViewport>() }

    override fun create() {
        addScreen(LoadingScreen().alsoRegister())
        addScreen(MainMenuScreen().alsoRegister())
        addScreen(SettingsScreen().alsoRegister())
        addScreen(GameScreen().alsoRegister())
        setScreen<LoadingScreen>()
    }

    override fun render() {
        ScreenUtils.clear(Color.BLACK)
        currentScreen.render(Gdx.graphics.deltaTime)
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        gameViewport.update(width, height, true)
        stageViewport.update(width, height, true)
    }
}
