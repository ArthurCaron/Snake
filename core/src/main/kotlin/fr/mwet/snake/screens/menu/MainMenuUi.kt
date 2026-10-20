package fr.mwet.snake.screens.menu

import com.badlogic.gdx.math.Interpolation.elasticOut
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Align
import fr.mwet.snake.Navigator
import fr.mwet.snake.events.MenuEvent.*
import fr.mwet.snake.events.MenuEventBus
import fr.mwet.snake.generated.assets.I18N
import fr.mwet.snake.save.settings.SettingsRepository
import fr.mwet.snake.ui.UiKit
import fr.mwet.snake.ui.UiStyles
import fr.mwet.snake.ui.textButton
import ktx.actors.onClick
import ktx.actors.onEnter
import ktx.actors.onExit

private const val MARGIN = 24f
private const val TITLE_WIDTH_RATIO = 0.70f

class MainMenuUi(
    kit: UiKit,
    stage: Stage,
    menuEventBus: MenuEventBus,
    navigator: Navigator,
    private val settingsRepository: SettingsRepository,
) {
    private val title = MainMenuTitle(kit.styles)
    private val playButton = PlayButton(kit.styles, menuEventBus, navigator)
    private val bestScoreLabel = Label("", kit.styles.label)
    private val soundButton = kit.styles.button.textButton("") {
        settingsRepository.toggleSound()
        refreshAudioButtons()
        menuEventBus.emit(SoundToggled)
    }
    private val musicButton = kit.styles.button.textButton("") {
        settingsRepository.toggleMusic()
        refreshAudioButtons()
        menuEventBus.emit(MusicToggled)
    }
    private val secondaryButtons = Table().apply {
        defaults().space(MARGIN)
        add(kit.styles.button.textButton(I18N.highscore()) { navigator.goToScores() })
        add(kit.styles.button.textButton(I18N.settings()) { navigator.goToSettings() })
    }

    init {
        stage.addActor(Table().apply {
            setFillParent(true)
            add(title.actor).padBottom(48f).row()
            add(playButton.actor).padBottom(MARGIN).row()
            add(bestScoreLabel).padBottom(32f).row()
            add(secondaryButtons)
        })
        stage.addActor(Table().apply {
            setFillParent(true)
            top().right().pad(MARGIN)
            defaults().right().space(MARGIN)
            add(soundButton).row()
            add(musicButton)
        })
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

    private fun refreshAudioButtons() {
        soundButton.setText(if (settingsRepository.soundOn) I18N.soundOn() else I18N.soundOff())
        musicButton.setText(if (settingsRepository.musicOn) I18N.musicOn() else I18N.musicOff())
    }

    private fun updateBestScoreLabel(bestScore: Int) {
        bestScoreLabel.setText(I18N.best(bestScore))
        bestScoreLabel.pack()
    }

}

private class MainMenuTitle(uiStyles: UiStyles) {
    val actor = Label("Snek", uiStyles.mainTitle).apply {
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

private class PlayButton(
    uiStyles: UiStyles,
    menuEventBus: MenuEventBus,
    private val navigator: Navigator
) {
    private var clicked = false
    private var hoverAction: Action? = null

    val actor = TextButton(I18N.play(), uiStyles.playButton).apply {
        setOrigin(Align.center)

        val replaceHoverAction = { action: Action ->
            hoverAction?.let { removeAction(it) }
            hoverAction = action
            addAction(action)
        }

        onClick { menuEventBus.emit(PlayGameClicked) }
        onEnter { replaceHoverAction(scaleTo(1.25f, 1.25f, 0.3f, elasticOut)) }
        onExit {
            if (clicked.not()) {
                replaceHoverAction(scaleTo(1f, 1f, 0.5f, elasticOut))
            }
        }
    }

    fun startGame() {
        if (clicked) return
        clicked = true

        actor.addAction(
            sequence(
                delay(0.3f),
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
