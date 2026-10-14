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
import fr.mwet.snake.assets.FontHandler
import fr.mwet.snake.assets.I18N
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.assets.toDrawable
import fr.mwet.snake.events.MenuEvent.*
import fr.mwet.snake.events.MenuEventBus
import fr.mwet.snake.render.Palette
import fr.mwet.snake.save.settings.SettingsRepository
import ktx.actors.onClick
import ktx.actors.onEnter
import ktx.actors.onExit

private const val MARGIN = 24f
private const val BEST_SCORE_GAP = 32f

class MainMenuUi(
    fontHandler: FontHandler,
    textureHandler: TextureHandler,
    stage: Stage,
    menuEventBus: MenuEventBus,
    navigator: Navigator,
    private val settingsRepository: SettingsRepository,
) {
    private val styles = Styles(fontHandler, textureHandler)
    private val title = MainMenuTitle(styles)
    private val playButton = PlayButton(styles, menuEventBus, navigator)
    private val soundButton = TextButton("", styles.smallButton).apply {
        pad(8f, 24f, 8f, 24f)
        onClick {
            settingsRepository.toggleSound()
            refreshAudioButtons()
            menuEventBus.emit(SoundToggled)
        }
    }
    private val musicButton = TextButton("", styles.smallButton).apply {
        pad(8f, 24f, 8f, 24f)
        onClick {
            settingsRepository.toggleMusic()
            refreshAudioButtons()
            menuEventBus.emit(MusicToggled)
        }
    }
    private val bestScoreLabel = Label("", styles.info)
    private var stageWidth = 0f
    private var stageHeight = 0f

    init {
        stage.addActor(title.actor)
        stage.addActor(playButton.actor)
        stage.addActor(soundButton)
        stage.addActor(musicButton)
        stage.addActor(bestScoreLabel)
    }

    fun onShow(bestScore: Int) {
        title.startAnimation()
        playButton.startAnimation()
        refreshAudioButtons()
        updateBestScoreLabel(bestScore)
        layoutBestScore()
    }

    fun onHide() {
        title.resetAnimation()
        playButton.resetAnimation()
    }

    fun layout(stageWidth: Float, stageHeight: Float, boardWidth: Float) {
        this.stageWidth = stageWidth
        this.stageHeight = stageHeight
        title.resetPosition(stageWidth, stageHeight, boardWidth)
        playButton.resetPosition(stageWidth, stageHeight)
        positionAudioButtons()
        layoutBestScore()
    }

    fun startGame() {
        playButton.startGame()
    }

    private fun refreshAudioButtons() {
        soundButton.setText(if (settingsRepository.soundOn) I18N.soundOn() else I18N.soundOff())
        soundButton.pack()
        musicButton.setText(if (settingsRepository.musicOn) I18N.musicOn() else I18N.musicOff())
        musicButton.pack()
        positionAudioButtons()
    }

    private fun positionAudioButtons() {
        soundButton.setPosition(stageWidth - soundButton.width - MARGIN, stageHeight - soundButton.height - MARGIN)
        musicButton.setPosition(stageWidth - musicButton.width - MARGIN, soundButton.y - musicButton.height - MARGIN)
    }

    private fun updateBestScoreLabel(bestScore: Int) {
        bestScoreLabel.setText(I18N.best(bestScore))
        bestScoreLabel.pack()
    }

    private fun layoutBestScore() {
        bestScoreLabel.setPosition(
            (stageWidth - bestScoreLabel.width) * 0.5f,
            playButton.actor.y - bestScoreLabel.height - BEST_SCORE_GAP,
        )
    }
}

private class MainMenuTitle(styles: Styles) {
    val actor = Label("Snek", styles.title).apply {
        setAlignment(Align.center)
        setOrigin(Align.center)
    }

    fun resetPosition(stageWidth: Float, stageHeight: Float, boardWidth: Float) {
        actor.setFontScale(1f)
        actor.pack()

        val targetWidth = boardWidth * 0.70f
        if (actor.width > 0f) {
            actor.setFontScale(targetWidth / actor.width)
        }

        actor.pack()
        actor.setOrigin(Align.center)
        actor.setPosition(
            (stageWidth - actor.width) * 0.5f,
            (stageHeight - actor.height) * 0.75f,
        )
    }

    fun startAnimation() {
    }

    fun resetAnimation() {
        actor.clearActions()
    }
}

private class PlayButton(styles: Styles, menuEventBus: MenuEventBus, private val navigator: Navigator) {
    private var clicked = false
    private var hoverAction: Action? = null

    val actor = TextButton(I18N.play(), styles.playButton).apply {
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

    fun resetPosition(stageWidth: Float, stageHeight: Float) {
        actor.setPosition(
            (stageWidth - actor.width) * 0.5f,
            (stageHeight - actor.height) * 0.25f
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

private class Styles(fontHandler: FontHandler, textureHandler: TextureHandler) {
    val title = Label.LabelStyle(fontHandler.titleFont, Palette.text)
    val info = Label.LabelStyle(fontHandler.uiFont, Palette.text)

    val playButton = TextButton.TextButtonStyle().apply {
        up = textureHandler.rectangleWithBorderTexture(
            width = 256,
            height = 96,
            border = 4,
            fill = Palette.background,
            borderColor = Palette.border,
        ).toDrawable()

        down = textureHandler.rectangleWithBorderTexture(
            width = 256,
            height = 96,
            border = 4,
            fill = Palette.accent,
            borderColor = Palette.border,
        ).toDrawable()

        font = fontHandler.buttonFont
        fontColor = Palette.text
        downFontColor = Palette.text
    }

    val smallButton = TextButton.TextButtonStyle(playButton).apply {
        font = fontHandler.uiFont
    }
}
