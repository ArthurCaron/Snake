package fr.mwet.snake.screens.settings

import fr.mwet.snake.Navigator
import fr.mwet.snake.assets.I18N
import fr.mwet.snake.events.MenuEvent.*
import fr.mwet.snake.events.MenuEventBus
import fr.mwet.snake.save.settings.SettingsRepository
import fr.mwet.snake.ui.MenuPage
import fr.mwet.snake.ui.UiKit
import fr.mwet.snake.ui.textButton
import ktx.app.KtxScreen

class SettingsScreen(
    kit: UiKit,
    private val settingsRepository: SettingsRepository,
    private val menuEvents: MenuEventBus,
    navigator: Navigator,
) : KtxScreen {
    private val page = MenuPage(kit, I18N.settings(), onBack = navigator::goToMainMenu)
    private val soundButton = kit.styles.textButton("") {
        settingsRepository.toggleSound()
        refreshAudioButtons()
        menuEvents.emit(SoundToggled)
    }
    private val musicButton = kit.styles.textButton("") {
        settingsRepository.toggleMusic()
        refreshAudioButtons()
        menuEvents.emit(MusicToggled)
    }

    init {
        page.content.add(soundButton).fillX().row()
        page.content.add(musicButton).fillX().row()
        page.content.add(kit.styles.textButton(I18N.controls()) { navigator.goToControls() }).fillX().row()
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
