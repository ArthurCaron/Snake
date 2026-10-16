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
private const val TITLE_WIDTH_RATIO = 0.70f

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
    private val bestScoreLabel = Label("", styles.info)
    private val soundButton = smallButton("") {
        settingsRepository.toggleSound()
        refreshAudioButtons()
        menuEventBus.emit(SoundToggled)
    }
    private val musicButton = smallButton("") {
        settingsRepository.toggleMusic()
        refreshAudioButtons()
        menuEventBus.emit(MusicToggled)
    }
    private val secondaryButtons = Table().apply {
        defaults().space(MARGIN)
        add(smallButton(I18N.highscore()) { navigator.goToScores() })
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

    private fun smallButton(text: String, onClick: () -> Unit) = TextButton(text, styles.smallButton).apply {
        pad(8f, 24f, 8f, 24f)
        onClick { onClick() }
    }

    private fun updateBestScoreLabel(bestScore: Int) {
        bestScoreLabel.setText(I18N.best(bestScore))
        bestScoreLabel.pack()
    }

}

private class MainMenuTitle(styles: Styles) {
    val actor = Label("Snek", styles.title).apply {
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
