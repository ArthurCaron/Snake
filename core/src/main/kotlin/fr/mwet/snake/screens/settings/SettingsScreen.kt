package fr.mwet.snake.screens.settings

import com.badlogic.gdx.Application.ApplicationType.Android
import com.badlogic.gdx.Gdx
import fr.mwet.snake.Navigator
import fr.mwet.snake.events.MenuEvent.*
import fr.mwet.snake.events.MenuEventBus
import fr.mwet.snake.generated.assets.I18N
import fr.mwet.snake.save.settings.SettingsRepository
import fr.mwet.snake.ui.MenuPage
import fr.mwet.snake.ui.UiKit
import ktx.app.KtxScreen

class SettingsScreen(
    kit: UiKit,
    private val settingsRepository: SettingsRepository,
    private val menuEvents: MenuEventBus,
    navigator: Navigator,
) : KtxScreen {
    private val page = MenuPage(kit, I18N.settings(), onBack = navigator::goToMainMenu)
    private val soundButton = with(kit.widgets) { page.content.menuButton("", onClick = ::toggleSound) { it.fillX().row() } }
    private val musicButton = with(kit.widgets) { page.content.menuButton("", onClick = ::toggleMusic) { it.fillX().row() } }

    init {
        with(kit.widgets) {
            if (Gdx.app.type != Android) {
                page.content.menuButton(I18N.controls(), onClick = navigator::goToControls) { it.fillX().row() }
            }
            page.content.menuButton(I18N.language(), onClick = navigator::goToLanguage) { it.fillX().row() }
            page.content.menuButton(I18N.font(), onClick = navigator::goToFont) { it.fillX().row() }
        }
    }

    private fun toggleSound() {
        settingsRepository.toggleSound()
        refreshAudioButtons()
        menuEvents.emit(SoundToggled)
    }

    private fun toggleMusic() {
        settingsRepository.toggleMusic()
        refreshAudioButtons()
        menuEvents.emit(MusicToggled)
    }

    override fun show() {
        page.show()
        refreshAudioButtons()
    }

    override fun hide() = page.hide()

    override fun resize(width: Int, height: Int) = page.resize(width, height)

    override fun render(delta: Float) = page.render(delta)

    override fun dispose() = page.dispose()

    private fun refreshAudioButtons() {
        soundButton.setText(if (settingsRepository.soundOn) I18N.soundOn() else I18N.soundOff())
        musicButton.setText(if (settingsRepository.musicOn) I18N.musicOn() else I18N.musicOff())
    }
}
