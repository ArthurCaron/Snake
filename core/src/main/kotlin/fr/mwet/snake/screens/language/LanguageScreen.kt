package fr.mwet.snake.screens.language

import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import fr.mwet.snake.LanguageSwitcher
import fr.mwet.snake.Navigator
import fr.mwet.snake.generated.assets.I18N
import fr.mwet.snake.save.settings.Language
import fr.mwet.snake.save.settings.SettingsRepository
import fr.mwet.snake.ui.MenuPage
import fr.mwet.snake.ui.UiKit
import fr.mwet.snake.ui.menuButton
import ktx.app.KtxScreen

class LanguageScreen(
    kit: UiKit,
    private val settingsRepository: SettingsRepository,
    private val languages: LanguageSwitcher,
    navigator: Navigator,
) : KtxScreen {
    private val page = MenuPage(kit, I18N.language(), onBack = navigator::goToSettings)
    private val buttons: Map<Language, TextButton> = Language.entries.associateWith { language ->
        page.content.menuButton("", onClick = { choose(language) }) { it.fillX().row() }
    }

    override fun show() {
        page.show()
        buttons.forEach { (language, button) ->
            val name = name(language)
            button.setText(if (language == settingsRepository.language) "> $name <" else name)
        }
    }

    override fun hide() = page.hide()

    override fun resize(width: Int, height: Int) = page.resize(width, height)

    override fun render(delta: Float) = page.render(delta)

    override fun dispose() = page.dispose()

    private fun choose(language: Language) {
        if (language != settingsRepository.language) languages.applyLanguage(language)
    }

    private fun name(language: Language) = when (language) {
        Language.System -> I18N.languageSystem()
        Language.English -> "English"
        Language.French -> "Français"
    }
}
