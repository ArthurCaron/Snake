package fr.mwet.snake.screens.appearance

import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import fr.mwet.snake.Navigator
import fr.mwet.snake.generated.assets.I18N
import fr.mwet.snake.render.VisualTheme
import fr.mwet.snake.screens.options.OptionPage
import fr.mwet.snake.screens.options.symbolName
import fr.mwet.snake.ui.MenuPage
import fr.mwet.snake.ui.SelectionBadge
import fr.mwet.snake.ui.UiKit
import fr.mwet.snake.ui.UiMetrics
import ktx.app.KtxScreen
import ktx.scene2d.actor

class AppearanceScreen(private val kit: UiKit, navigator: Navigator) : KtxScreen {
    private val settings = kit.settingsRepository
    private val page = MenuPage(kit, I18N.appearance(), onBack = navigator::goToSettings)
    private val bodyBadge = SelectionBadge(kit.textures)
    private val symbolBadge = SelectionBadge(kit.textures)
    private var symbolButton: TextButton
    private var fruitColorButton: TextButton
    private var neonButton: TextButton

    init {
        with(kit.widgets) {
            page.header.actor(AppearancePreview(kit)) { it.fillX().row() }
            page.content.menuButton(I18N.snakeBodyColor(), onClick = { navigator.goToOptions(OptionPage.BodyColor) }) {
                labelCell.expandX()
                add(bodyBadge).size(UiMetrics.CHOICE_BADGE_SIZE).padLeft(UiMetrics.PAGE_CONTENT_SPACE)
                it.fillX().row()
            }
            symbolButton = page.content.menuButton("", onClick = { navigator.goToOptions(OptionPage.Symbol) }) {
                labelCell.expandX()
                add(symbolBadge).size(UiMetrics.CHOICE_BADGE_SIZE).padLeft(UiMetrics.PAGE_CONTENT_SPACE)
                it.fillX().row()
            }
            fruitColorButton = page.content.menuButton("", onClick = {
                settings.setAppearance(settings.appearance.copy(takesFruitColor = !settings.appearance.takesFruitColor))
                refresh()
            }) { it.fillX().row() }
            neonButton = page.content.menuButton("", onClick = {
                navigator.applyVisualTheme(if (settings.appearance.theme == VisualTheme.Neon) VisualTheme.Normal else VisualTheme.Neon)
            }) { it.fillX().row() }
        }
    }

    private fun refresh() {
        val look = settings.appearance
        symbolButton.setText(I18N.optionState(I18N.snakeSymbol(), symbolName(look.markShape)))
        fruitColorButton.setText(I18N.optionState(I18N.fruitColor(), if (look.takesFruitColor) I18N.enabled() else I18N.disabled()))
        neonButton.setText(I18N.optionState(I18N.neon(), if (look.theme == VisualTheme.Neon) I18N.enabled() else I18N.disabled()))
        bodyBadge.showColor(look.body)
        symbolBadge.showSymbol(look.markShape.region(kit.textures.skin), look.marks)
    }

    override fun show() { page.show(); refresh() }
    override fun hide() = page.hide()
    override fun resize(width: Int, height: Int) = page.resize(width, height)
    override fun render(delta: Float) = page.render(delta)
    override fun dispose() = page.dispose()
}
