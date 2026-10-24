package fr.mwet.snake.screens.menu

import com.badlogic.gdx.math.Interpolation.elasticOut
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Align
import fr.mwet.snake.Navigator
import fr.mwet.snake.events.MenuEvent.*
import fr.mwet.snake.events.MenuEventBus
import fr.mwet.snake.generated.assets.I18N
import fr.mwet.snake.save.settings.SettingsRepository
import fr.mwet.snake.tuning.AnimationTuning.PLAY_BUTTON_CLICK_DELAY
import fr.mwet.snake.tuning.AnimationTuning.PLAY_BUTTON_HOVER_IN_DURATION
import fr.mwet.snake.tuning.AnimationTuning.PLAY_BUTTON_HOVER_OUT_DURATION
import fr.mwet.snake.tuning.AnimationTuning.PLAY_BUTTON_HOVER_SCALE
import fr.mwet.snake.ui.UiStyle
import fr.mwet.snake.ui.menuButton
import ktx.actors.onEnter
import ktx.actors.onExit
import ktx.scene2d.actor
import ktx.scene2d.actors
import ktx.scene2d.label
import ktx.scene2d.scene2d
import ktx.scene2d.table

private const val MARGIN = 24f
private const val TITLE_WIDTH_RATIO = 0.70f

class MainMenuUi(
    stage: Stage,
    private val menuEventBus: MenuEventBus,
    navigator: Navigator,
    private val settingsRepository: SettingsRepository,
) {
    private val title = MainMenuTitle()
    private val playButton = PlayButton(menuEventBus, navigator)
    private var bestScoreLabel: Label
    private var soundButton: TextButton
    private var musicButton: TextButton

    init {
        stage.actors {
            table {
                setFillParent(true)
                actor(title.actor) { it.padBottom(48f).row() }
                actor(playButton.actor) { it.padBottom(MARGIN).row() }
                bestScoreLabel = label("") { it.padBottom(32f).row() }
                table {
                    defaults().space(MARGIN)
                    menuButton(I18N.highscore(), onClick = navigator::goToScores)
                    menuButton(I18N.settings(), onClick = navigator::goToSettings)
                }
            }
            table {
                setFillParent(true)
                top().right().pad(MARGIN)
                defaults().right().space(MARGIN)
                soundButton = menuButton("", onClick = ::toggleSound) { it.row() }
                musicButton = menuButton("", onClick = ::toggleMusic)
            }
        }
    }

    fun onShow(bestScore: Int) {
        title.startAnimation()
        playButton.startAnimation()
        refreshAudioButtons()
        updateBestScoreLabel(bestScore)
    }

    fun onHide() {
        title.resetAnimation()
        playButton.resetAnimation()
    }

    fun layout(boardWidth: Float) {
        title.fit(boardWidth)
    }

    fun startGame() {
        playButton.startGame()
    }

    private fun toggleSound() {
        settingsRepository.toggleSound()
        refreshAudioButtons()
        menuEventBus.emit(SoundToggled)
    }

    private fun toggleMusic() {
        settingsRepository.toggleMusic()
        refreshAudioButtons()
        menuEventBus.emit(MusicToggled)
    }

    private fun refreshAudioButtons() {
        soundButton.setText(if (settingsRepository.soundOn) I18N.soundOn() else I18N.soundOff())
        musicButton.setText(if (settingsRepository.musicOn) I18N.musicOn() else I18N.musicOff())
    }

    private fun updateBestScoreLabel(bestScore: Int) {
        bestScoreLabel.setText(I18N.best(bestScore))
        bestScoreLabel.pack()
    }
}

private class MainMenuTitle {
    val actor = scene2d.label("Snek", UiStyle.LOGO) {
        setAlignment(Align.center)
        setOrigin(Align.center)
    }

    fun fit(boardWidth: Float) {
        actor.setFontScale(1f)
        val naturalWidth = actor.prefWidth
        if (naturalWidth > 0f) {
            actor.setFontScale(boardWidth * TITLE_WIDTH_RATIO / naturalWidth)
        }
        actor.invalidateHierarchy()
    }

    fun startAnimation() {
    }

    fun resetAnimation() {
        actor.clearActions()
    }
}

private class PlayButton(menuEventBus: MenuEventBus, private val navigator: Navigator) {
    private var clicked = false
    private var hoverAction: Action? = null

    val actor = menuButton(I18N.play(), onClick = { menuEventBus.emit(PlayGameClicked) }, style = UiStyle.BIG) {
        pad(0f)
        setOrigin(Align.center)

        val replaceHoverAction = { action: Action ->
            hoverAction?.let { removeAction(it) }
            hoverAction = action
            addAction(action)
        }

        onEnter { replaceHoverAction(scaleTo(PLAY_BUTTON_HOVER_SCALE, PLAY_BUTTON_HOVER_SCALE, PLAY_BUTTON_HOVER_IN_DURATION, elasticOut)) }
        onExit {
            if (clicked.not()) {
                replaceHoverAction(scaleTo(1f, 1f, PLAY_BUTTON_HOVER_OUT_DURATION, elasticOut))
            }
        }
    }

    fun startGame() {
        if (clicked) return
        clicked = true

        actor.addAction(
            sequence(
                delay(PLAY_BUTTON_CLICK_DELAY),
                Actions.run { navigator.goToGame() },
            ),
        )
    }

    fun startAnimation() {
    }

    fun resetAnimation() {
        actor.clearActions()
        clicked = false
        hoverAction = null
        actor.setScale(1f)
        actor.setColor(1f, 1f, 1f, 1f)
    }
}
