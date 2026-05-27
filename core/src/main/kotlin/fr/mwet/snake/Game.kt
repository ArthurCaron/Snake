package fr.mwet.snake

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.ScreenUtils
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class Game(
    private val gameViewport: GameViewport,
    private val stageViewport: StageViewport,
) : KtxGame<KtxScreen>(), DisposableRegistry by DisposableContainer() {
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
