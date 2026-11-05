package fr.mwet.snake.ui

import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.scenes.scene2d.ui.Cell
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import fr.mwet.snake.assets.toDrawable
import fr.mwet.snake.tuning.Palette
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
import ktx.scene2d.actor

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

    val content: KTableWidget = scene2d.table { defaults().space(PAGE_CONTENT_SPACE) }
    val header: KTableWidget = scene2d.table { defaults().space(PAGE_CONTENT_SPACE) }
    private val scroll = ScrollPane(content, ScrollPane.ScrollPaneStyle().apply {
        vScrollKnob = kit.textures.rectangleTexture(6, 24, Palette.highlight).toDrawable()
    }).apply {
        setScrollingDisabled(true, false)
        setOverscroll(false, false)
        setFadeScrollBars(false)
    }
    private var scrollCell: Cell<*>
    private var titleLabel: Label
    private var titleFitWidth = -1f
    var title: String
        get() = titleLabel.text.toString()
        set(value) { titleLabel.setText(value); titleFitWidth = -1f }

    init {
        with(kit.widgets) {
            uiLayer.board.addActor(scene2d.table {
                setFillParent(true)
                table {
                    background(UiStyle.PANEL)
                    pad(PANEL_PAD)
                    defaults().space(PAGE_SPACE)
                    titleLabel = label(title, UiStyle.TITLE) { it.row() }
                    actor(header) { it.space(0f).row() }
                    actor(scroll) { scrollCell = it; it.row() }
                    menuButton(I18N.back(), onClick = onBack)
                }
            })
        }
    }

    fun show() {
        kit.input.add(menuInput, uiLayer.stage)
        uiLayer.stage.scrollFocus = scroll
    }

    fun hide() {
        kit.input.remove(menuInput, uiLayer.stage)
        uiLayer.stage.unfocusAll()
    }

    fun scrollToTop() { scroll.scrollY = 0f; scroll.updateVisualScroll() }

    fun resize(width: Int, height: Int) {
        worldLayer.resize(width, height)
        uiLayer.resize(width, height)
        uiLayer.fitBoard(kit.gameViewport)
    }

    fun render(delta: Float) {
        val maxWidth = (uiLayer.board.width - PANEL_PAD * 4).coerceAtLeast(0f)
        if (titleFitWidth != maxWidth) {
            titleFitWidth = maxWidth
            titleLabel.setFontScale(1f)
            if (titleLabel.prefWidth > maxWidth) titleLabel.setFontScale(maxWidth / titleLabel.prefWidth)
        }
        val maxHeight = (uiLayer.board.height - titleLabel.prefHeight - header.prefHeight - PANEL_PAD * 2 - PAGE_SPACE * 3 - UiMetrics.UNIT * 24).coerceAtLeast(0f)
        if (scrollCell.maxHeight != maxHeight || scrollCell.maxWidth != maxWidth) {
            scrollCell.maxHeight(maxHeight).maxWidth(maxWidth)
            content.invalidateHierarchy()
        }
        worldLayer.render(kit.backdrop, delta)
        uiLayer.render(delta)
    }

    override fun dispose() {
        uiLayer.dispose()
    }
}
