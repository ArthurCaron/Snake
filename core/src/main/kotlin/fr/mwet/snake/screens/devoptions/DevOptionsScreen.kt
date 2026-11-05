package fr.mwet.snake.screens.devoptions

import fr.mwet.snake.Navigator
import fr.mwet.snake.generated.assets.I18N
import fr.mwet.snake.screens.options.OptionPage
import fr.mwet.snake.screens.options.transitionName
import fr.mwet.snake.screens.options.backdropName
import fr.mwet.snake.screens.options.groundName
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import fr.mwet.snake.ui.MenuPage
import fr.mwet.snake.ui.UiKit
import ktx.app.KtxScreen

class DevOptionsScreen(kit: UiKit, navigator: Navigator) : KtxScreen {
    private val page = MenuPage(kit, I18N.devOptions(), onBack = navigator::goToMainMenu)

    private val buttons = mutableListOf<Pair<TextButton, () -> String>>()

    init {
        with(kit.widgets) {
            buttons += page.content.menuButton(I18N.font(), onClick = navigator::goToFont) { it.fillX().row() } to { I18N.optionState(I18N.font(), kit.settingsRepository.fontTheme.displayName) }
            buttons += page.content.menuButton(I18N.transitionTitle(), onClick = { navigator.goToOptions(OptionPage.Transition) }) { it.fillX().row() } to { I18N.transition(transitionName(kit.settingsRepository.transition)) }
            buttons += page.content.menuButton(I18N.backdropTitle(), onClick = { navigator.goToOptions(OptionPage.Backdrop) }) { it.fillX().row() } to { I18N.backdrop(backdropName(kit.settingsRepository.backdrop)) }
            buttons += page.content.menuButton(I18N.groundTitle(), onClick = { navigator.goToOptions(OptionPage.Ground) }) { it.fillX().row() } to { I18N.ground(groundName(kit.settingsRepository.ground)) }
        }
    }

    override fun show() { page.show(); buttons.forEach { (button, label) -> button.setText(label()) } }
    override fun hide() = page.hide()
    override fun resize(width: Int, height: Int) = page.resize(width, height)
    override fun render(delta: Float) = page.render(delta)
    override fun dispose() = page.dispose()
}
