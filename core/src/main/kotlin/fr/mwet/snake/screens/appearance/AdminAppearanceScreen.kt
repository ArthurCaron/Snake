package fr.mwet.snake.screens.appearance

import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import fr.mwet.snake.Navigator
import fr.mwet.snake.generated.assets.I18N
import fr.mwet.snake.screens.options.OptionPage
import fr.mwet.snake.screens.options.transitionName
import fr.mwet.snake.screens.options.backdropName
import fr.mwet.snake.screens.options.groundName
import fr.mwet.snake.ui.MenuPage
import fr.mwet.snake.ui.SelectionBadge
import fr.mwet.snake.ui.UiKit
import fr.mwet.snake.ui.UiMetrics
import ktx.app.KtxScreen
import ktx.scene2d.actor

class AdminAppearanceScreen(private val kit: UiKit, navigator: Navigator) : KtxScreen {
    private val settings = kit.settingsRepository
    private val page = MenuPage(kit, I18N.appearance(), onBack = navigator::goToDevOptions)
    private val marksBadge = SelectionBadge(kit.textures)
    private val buttons = mutableListOf<Pair<TextButton, () -> String>>()

    init {
        with(kit.widgets) {
            page.header.actor(AppearancePreview(kit)) { it.fillX().row() }
            page.header.menuButton(I18N.previewButton(), onClick = {}) { it.fillX().row() }
            page.content.menuButton(I18N.snakeMarksColor(), onClick = { navigator.goToOptions(OptionPage.MarksColor) }) {
                labelCell.expandX()
                add(marksBadge).size(UiMetrics.CHOICE_BADGE_SIZE).padLeft(UiMetrics.PAGE_CONTENT_SPACE)
                it.fillX().row()
            }
            buttons += page.content.menuButton("", onClick = navigator::goToFont) { it.fillX().row() } to {
                I18N.optionState(I18N.font(), settings.fontTheme.displayName)
            }
            buttons += page.content.menuButton("", onClick = { navigator.goToOptions(OptionPage.Transition) }) { it.fillX().row() } to {
                I18N.transition(transitionName(settings.transition))
            }
            buttons += page.content.menuButton("", onClick = { navigator.goToOptions(OptionPage.Backdrop) }) { it.fillX().row() } to {
                I18N.backdrop(backdropName(settings.backdrop))
            }
            buttons += page.content.menuButton("", onClick = { navigator.goToOptions(OptionPage.Ground) }) { it.fillX().row() } to {
                I18N.ground(groundName(settings.ground))
            }
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
        buttons += button to { I18N.optionState(name, if (value()) I18N.enabled() else I18N.disabled()) }
    }

    private fun refresh() {
        buttons.forEach { (button, label) -> button.setText(label()) }
        marksBadge.showColor(settings.appearance.marks)
    }

    override fun show() { page.show(); refresh() }
    override fun hide() = page.hide()
    override fun resize(width: Int, height: Int) = page.resize(width, height)
    override fun render(delta: Float) = page.render(delta)
    override fun dispose() = page.dispose()
}
