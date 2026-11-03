package fr.mwet.snake.ui

import com.badlogic.gdx.utils.Disposable
import fr.mwet.snake.generated.assets.I18N
import fr.mwet.snake.inputs.GeneralCommandContext
import fr.mwet.snake.inputs.GeneralInputProcessor
import fr.mwet.snake.render.WorldLayer
import fr.mwet.snake.ui.UiMetrics.PAGE_CONTENT_SPACE
import fr.mwet.snake.ui.UiMetrics.PAGE_SPACE
import fr.mwet.snake.ui.UiMetrics.PANEL_PAD
import ktx.scene2d.KTableWidget
import ktx.scene2d.label
import ktx.scene2d.scene2d
import ktx.scene2d.table

class MenuPage(private val kit: UiKit, title: String, private val onBack: () -> Unit) : Disposable {
    private val worldLayer = WorldLayer(kit.batch, kit.gameViewport)
    private val uiLayer = UiLayer(kit.batch)
    private val menuInput = GeneralInputProcessor(
        kit.settingsRepository, GeneralCommandContext(
            onBack = {
                onBack()
                true
            },
        )
    )

    var content: KTableWidget
        private set

    init {
        with(kit.widgets) {
            uiLayer.board.addActor(scene2d.table {
                setFillParent(true)
                table {
                    background(UiStyle.PANEL)
                    pad(PANEL_PAD)
                    defaults().space(PAGE_SPACE)
                    label(title, UiStyle.TITLE) { it.row() }
                    content = table { it.row() }
                    content.defaults().space(PAGE_CONTENT_SPACE)
                    menuButton(I18N.back(), onClick = onBack)
                }
            })
        }
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
        uiLayer.fitBoard(kit.gameViewport)
    }

    fun render(delta: Float) {
        worldLayer.render(kit.backdrop, delta)
        uiLayer.render(delta)
    }

    override fun dispose() {
        uiLayer.dispose()
    }
}
