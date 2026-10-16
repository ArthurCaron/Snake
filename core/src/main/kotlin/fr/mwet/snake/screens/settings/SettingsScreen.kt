package fr.mwet.snake.screens.settings

import com.badlogic.gdx.Application.ApplicationType.Android
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import fr.mwet.snake.Navigator
import fr.mwet.snake.backdrop.BackdropStyle
import fr.mwet.snake.events.MenuEvent.MusicToggled
import fr.mwet.snake.events.MenuEvent.SoundToggled
import fr.mwet.snake.events.MenuEventBus
import fr.mwet.snake.generated.assets.I18N
import fr.mwet.snake.render.GroundStyle
import fr.mwet.snake.save.settings.SettingsRepository
import fr.mwet.snake.transition.TransitionStyle
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
    private val soundButton: TextButton
    private val musicButton: TextButton
    private val transitionButton: TextButton
    private val backdropButton: TextButton
    private val groundButton: TextButton

    init {
        with(kit.widgets) {
            soundButton = page.content.menuButton("", onClick = ::toggleSound) { it.fillX().row() }
            musicButton = page.content.menuButton("", onClick = ::toggleMusic) { it.fillX().row() }
            if (Gdx.app.type != Android) {
                page.content.menuButton(I18N.controls(), onClick = navigator::goToControls) { it.fillX().row() }
            }
            page.content.menuButton(I18N.language(), onClick = navigator::goToLanguage) { it.fillX().row() }
            page.content.menuButton(I18N.font(), onClick = navigator::goToFont) { it.fillX().row() }
            transitionButton = page.content.menuButton("", onClick = ::nextTransition) { it.fillX().row() }
            backdropButton = page.content.menuButton("", onClick = ::nextBackdrop) { it.fillX().row() }
            groundButton = page.content.menuButton("", onClick = ::nextGround) { it.fillX().row() }
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

    private fun nextTransition() {
        settingsRepository.setTransition(settingsRepository.transition.next())
        refreshTransitionButton()
    }

    private fun refreshTransitionButton() {
        val name = when (settingsRepository.transition) {
            TransitionStyle.None -> I18N.transitionNone()
            TransitionStyle.Fade -> I18N.transitionFade()
            TransitionStyle.Slide -> I18N.transitionSlide()
            TransitionStyle.SnakeWipe -> I18N.transitionSnake()
        }
        transitionButton.setText(I18N.transition(name))
    }

    private fun nextBackdrop() {
        settingsRepository.setBackdrop(settingsRepository.backdrop.next())
        refreshBackdropButton()
    }

    private fun nextGround() {
        settingsRepository.setGround(settingsRepository.ground.next())
        refreshGroundButton()
    }

    private fun refreshGroundButton() {
        val name = when (settingsRepository.ground) {
            GroundStyle.Checker -> I18N.groundChecker()
            GroundStyle.Plain -> I18N.groundPlain()
            GroundStyle.Grain -> I18N.groundGrain()
            GroundStyle.Stripes -> I18N.groundStripes()
            GroundStyle.Tiles -> I18N.groundTiles()
            GroundStyle.Scales -> I18N.groundScales()
            GroundStyle.Dunes -> I18N.groundDunes()
            GroundStyle.Hex -> I18N.groundHex()
        }
        groundButton.setText(I18N.ground(name))
    }

    private fun refreshBackdropButton() {
        val name = when (settingsRepository.backdrop) {
            BackdropStyle.Plain -> I18N.backdropPlain()
            BackdropStyle.DriftingGrid -> I18N.backdropGrid()
            BackdropStyle.DemoSnake -> I18N.backdropDemo()
            BackdropStyle.FloatingFruits -> I18N.backdropFruits()
            BackdropStyle.Fireflies -> I18N.backdropFireflies()
            BackdropStyle.DiagonalStripes -> I18N.backdropStripes()
            BackdropStyle.Waves -> I18N.backdropWaves()
            BackdropStyle.Ripples -> I18N.backdropRipples()
            BackdropStyle.GameOfLife -> I18N.backdropLife()
            BackdropStyle.WanderingSnakes -> I18N.backdropWanderers()
        }
        backdropButton.setText(I18N.backdrop(name))
    }

    override fun show() {
        page.show()
        refreshAudioButtons()
        refreshTransitionButton()
        refreshBackdropButton()
        refreshGroundButton()
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
