package fr.mwet.snake

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
object Game : KtxGame<KtxScreen>(), DisposableRegistry by DisposableContainer() {
    private val viewports = mutableListOf<Viewport>()

    override fun render() {
        ScreenUtils.clear(Color.BLACK)
        currentScreen.render(Gdx.graphics.deltaTime)
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        viewports.forEach { viewport ->
            viewport.update(width, height, true)
        }
    }

    fun addViewport(viewport: Viewport) {
        viewports.add(viewport)
    }
}
