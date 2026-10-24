package fr.mwet.snake.screens.font

import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import fr.mwet.snake.FontSwitcher
import fr.mwet.snake.Navigator
import fr.mwet.snake.assets.FontHandler
import fr.mwet.snake.assets.FontRole
import fr.mwet.snake.tuning.FontTheme
import fr.mwet.snake.generated.assets.I18N
import fr.mwet.snake.save.settings.SettingsRepository
import fr.mwet.snake.ui.MenuPage
import fr.mwet.snake.ui.UiKit
import fr.mwet.snake.ui.menuButton
import ktx.app.KtxScreen

class FontScreen(
    kit: UiKit,
    fonts: FontHandler,
    private val settingsRepository: SettingsRepository,
    private val switcher: FontSwitcher,
    navigator: Navigator,
) : KtxScreen {
    private val page = MenuPage(kit, I18N.font(), onBack = navigator::goToSettings)
    private val buttons: Map<FontTheme, TextButton> = FontTheme.entries.associateWith { theme ->
        page.content.menuButton(theme.displayName, onClick = { choose(theme) }) {
            style = TextButton.TextButtonStyle(style).apply { font = fonts.font(theme, FontRole.Small) }
            it.fillX().row()
        }
    }

    override fun show() {
        page.show()
        buttons.forEach { (theme, button) ->
            button.setText(if (theme == settingsRepository.fontTheme) "> ${theme.displayName} <" else theme.displayName)
        }
    }

    override fun hide() = page.hide()

    override fun resize(width: Int, height: Int) = page.resize(width, height)

    override fun render(delta: Float) = page.render(delta)

    override fun dispose() = page.dispose()

    private fun choose(theme: FontTheme) {
        if (theme != settingsRepository.fontTheme) switcher.applyFontTheme(theme)
    }
}
