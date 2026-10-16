package fr.mwet.snake.ui

import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Disposable
import fr.mwet.snake.assets.I18N
import fr.mwet.snake.render.Background
import fr.mwet.snake.render.WorldLayer
import ktx.actors.onClick

class MenuPage(private val kit: UiKit, title: String, private val onBack: () -> Unit) : Disposable {
    private val worldLayer = WorldLayer(kit.batch, kit.gameViewport)
    private val boardBackground = Background(kit.textures)
    private val uiLayer = UiLayer(kit.batch)
    private val backKeys = BackKeys()

    val content = Table().apply { defaults().space(12f) }

    init {
        val panel = Table().apply {
            background = kit.styles.panel
            pad(32f)
            defaults().space(24f)
            add(Label(title, kit.styles.title)).row()
            add(content).row()
            add(kit.styles.textButton(I18N.back()) { onBack() })
        }
        uiLayer.stage.addActor(Table().apply {
            setFillParent(true)
            add(panel)
        })
    }

    fun show() {
        kit.input.add(backKeys, uiLayer.stage)
    }

    fun hide() {
        kit.input.remove(backKeys, uiLayer.stage)
    }

    fun resize(width: Int, height: Int) {
        worldLayer.resize(width, height)
        uiLayer.resize(width, height)
    }

    fun render(delta: Float) {
        worldLayer.render(boardBackground, delta)
        uiLayer.render(delta)
    }

    override fun dispose() {
        uiLayer.dispose()
    }

    private inner class BackKeys : InputAdapter() {
        override fun keyDown(keycode: Int): Boolean {
            if (keycode != Input.Keys.ESCAPE && keycode != Input.Keys.BACK) return false
            onBack()
            return true
        }
    }
}

fun UiStyles.textButton(text: String, onClick: () -> Unit) = TextButton(text, button).apply {
    pad(8f, 24f, 8f, 24f)
    onClick { onClick() }
}
