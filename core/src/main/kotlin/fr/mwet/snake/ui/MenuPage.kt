package fr.mwet.snake.ui

import com.badlogic.gdx.utils.Disposable
import fr.mwet.snake.generated.assets.I18N
import fr.mwet.snake.inputs.GeneralCommandContext
import fr.mwet.snake.inputs.GeneralInputProcessor
import fr.mwet.snake.render.Background
import fr.mwet.snake.render.WorldLayer
import ktx.scene2d.KTableWidget
import ktx.scene2d.actors
import ktx.scene2d.label
import ktx.scene2d.table

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

    var content: KTableWidget
        private set

    init {
        with(kit.widgets) {
            uiLayer.stage.actors {
                table {
                    setFillParent(true)
                    table {
                        background(UiStyle.PANEL)
                        pad(32f)
                        defaults().space(24f)
                        label(title, UiStyle.TITLE) { it.row() }
                        content = table { it.row() }
                        content.defaults().space(12f)
                        menuButton(I18N.back(), onClick = onBack)
                    }
                }
            }
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
    }

    fun render(delta: Float) {
        worldLayer.render(boardBackground, delta)
        uiLayer.render(delta)
    }

    override fun dispose() {
        uiLayer.dispose()
    }
}
