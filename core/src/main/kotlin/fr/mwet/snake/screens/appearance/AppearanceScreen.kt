package fr.mwet.snake.screens.appearance

import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import fr.mwet.snake.Navigator
import fr.mwet.snake.generated.assets.I18N
import fr.mwet.snake.screens.options.OptionPage
import fr.mwet.snake.ui.MenuPage
import fr.mwet.snake.ui.UiKit
import fr.mwet.snake.ui.SelectionBadge
import fr.mwet.snake.ui.UiMetrics
import fr.mwet.snake.screens.options.symbolName
import fr.mwet.snake.screens.options.themeName
import ktx.app.KtxScreen
import ktx.scene2d.actor

class AppearanceScreen(private val kit: UiKit, navigator: Navigator) : KtxScreen {
    private val settings = kit.settingsRepository
    private val page = MenuPage(kit, I18N.appearance(), onBack = navigator::goToSettings)
    private val toggles = mutableListOf<Pair<TextButton, () -> String>>()
    private val bodyBadge = SelectionBadge(kit.textures)
    private val marksBadge = SelectionBadge(kit.textures)
    private val symbolBadge = SelectionBadge(kit.textures)
    private var themeButton: TextButton
    private var symbolButton: TextButton

    init {
        with(kit.widgets) {
            page.header.actor(AppearancePreview(kit)) { it.fillX().row() }
            page.header.menuButton(I18N.previewButton(), onClick = {}) { it.fillX().row() }
            themeButton = page.content.menuButton("", onClick = { navigator.goToOptions(OptionPage.Theme) }) { it.fillX().row() }
            page.content.menuButton(I18N.snakeBodyColor(), onClick = { navigator.goToOptions(OptionPage.BodyColor) }) {
                labelCell.expandX()
                add(bodyBadge).size(UiMetrics.CHOICE_BADGE_SIZE).padLeft(UiMetrics.PAGE_CONTENT_SPACE)
                it.fillX().row()
            }
            page.content.menuButton(I18N.snakeMarksColor(), onClick = { navigator.goToOptions(OptionPage.MarksColor) }) {
                labelCell.expandX()
                add(marksBadge).size(UiMetrics.CHOICE_BADGE_SIZE).padLeft(UiMetrics.PAGE_CONTENT_SPACE)
                it.fillX().row()
            }
            symbolButton = page.content.menuButton("", onClick = { navigator.goToOptions(OptionPage.Symbol) }) {
                labelCell.expandX()
                add(symbolBadge).size(UiMetrics.CHOICE_BADGE_SIZE).padLeft(UiMetrics.PAGE_CONTENT_SPACE)
                it.fillX().row()
            }
            toggle(I18N.fruitColor(), { settings.appearance.takesFruitColor }) { settings.setAppearance(settings.appearance.copy(takesFruitColor = !it)) }
            toggle(I18N.snakeGlow(), { settings.appearance.snakeGlow }) { settings.setAppearance(settings.appearance.copy(snakeGlow = !it)) }
            toggle(I18N.fruitGlow(), { settings.appearance.fruitGlow }) { settings.setAppearance(settings.appearance.copy(fruitGlow = !it)) }
            toggle(I18N.menuGlow(), { settings.appearance.menuGlow }) { settings.setAppearance(settings.appearance.copy(menuGlow = !it)) }
            page.content.menuButton(I18N.resetAppearance(), onClick = {
                settings.setAppearance(settings.appearance.theme.defaultAppearance())
                refresh()
            }) { it.fillX().row() }
        }
    }

    private fun toggle(name: String, value: () -> Boolean, change: (Boolean) -> Unit) {
        val button = with(kit.widgets) {
            page.content.menuButton("", onClick = { change(value()); refresh() }) { it.fillX().row() }
        }
        toggles += button to { I18N.optionState(name, if (value()) I18N.enabled() else I18N.disabled()) }
    }

    private fun refresh() {
        val look = settings.appearance
        toggles.forEach { (button, label) -> button.setText(label()) }
        themeButton.setText(I18N.optionState(I18N.visualTheme(), themeName(look.theme)))
        symbolButton.setText(I18N.optionState(I18N.snakeSymbol(), symbolName(look.markShape)))
        bodyBadge.showColor(look.body)
        marksBadge.showColor(look.marks)
        symbolBadge.showSymbol(look.markShape.region(kit.textures.skin), look.marks)
    }

    override fun show() { page.show(); refresh() }
    override fun hide() = page.hide()
    override fun resize(width: Int, height: Int) = page.resize(width, height)
    override fun render(delta: Float) = page.render(delta)
    override fun dispose() = page.dispose()
}
