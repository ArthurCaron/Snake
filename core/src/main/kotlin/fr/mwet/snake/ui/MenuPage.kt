package fr.mwet.snake.ui

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Disposable
import fr.mwet.snake.generated.assets.I18N
import fr.mwet.snake.inputs.GeneralCommandContext
import fr.mwet.snake.inputs.GeneralInputProcessor
import fr.mwet.snake.render.Background
import fr.mwet.snake.render.WorldLayer

class MenuPage(private val kit: UiKit, title: String, private val onBack: () -> Unit) : Disposable {
    private val worldLayer = WorldLayer(kit.batch, kit.gameViewport)
    private val boardBackground = Background(kit.textures)
    private val uiLayer = UiLayer(kit.batch)
    private val menuInput = GeneralInputProcessor(
        kit.settingsRepository, GeneralCommandContext(
            onBack = {
                onBack()
                true
            },
        )
    )

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
        kit.input.add(menuInput, uiLayer.stage)
    }

    fun hide() {
        kit.input.remove(menuInput, uiLayer.stage)
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
}
