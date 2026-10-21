package fr.mwet.snake.screens.menu

import com.badlogic.gdx.math.Interpolation.elasticOut
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
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
import fr.mwet.snake.ui.UiMetrics.MENU_BEST_SCORE_GAP
import fr.mwet.snake.ui.UiMetrics.MENU_TITLE_GAP
import fr.mwet.snake.ui.UiMetrics.MENU_TITLE_WIDTH_RATIO
import fr.mwet.snake.ui.UiMetrics.PLAY_BUTTON_PAD_BOTTOM
import fr.mwet.snake.ui.UiMetrics.PLAY_BUTTON_PAD_SIDE
import fr.mwet.snake.ui.UiMetrics.PLAY_BUTTON_PAD_TOP
import fr.mwet.snake.ui.UiMetrics.SCREEN_MARGIN
import fr.mwet.snake.ui.UiStyle
import fr.mwet.snake.ui.Widgets
import ktx.actors.onEnter
import ktx.actors.onExit
import ktx.scene2d.actor
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.actors
import ktx.scene2d.image
import ktx.scene2d.label
import ktx.scene2d.scene2d
import ktx.scene2d.table


class MainMenuUi(
    stage: Stage,
    private val menuEventBus: MenuEventBus,
    navigator: Navigator,
    private val settingsRepository: SettingsRepository,
    widgets: Widgets,
) {
    private val title = MainMenuTitle()
    private val playButton = PlayButton(menuEventBus, navigator, widgets)
    private var bestScoreLabel: Label
    private var soundButton: ImageButton
    private var musicButton: ImageButton

    init {
        with(widgets) {
            stage.actors {
                table {
                    setFillParent(true)
                    actor(title.actor) { it.padBottom(MENU_TITLE_GAP).row() }
                    actor(playButton.actor) { it.padBottom(SCREEN_MARGIN).row() }
                    bestScoreLabel = label("") { it.padBottom(MENU_BEST_SCORE_GAP).row() }
                    table {
                        defaults().space(SCREEN_MARGIN)
                        menuButton(I18N.highscore(), onClick = navigator::goToScores)
                        menuButton(I18N.settings(), onClick = navigator::goToSettings)
                    }
                }
                table {
                    setFillParent(true)
                    top().right().pad(SCREEN_MARGIN)
                    defaults().right().space(SCREEN_MARGIN)
                    soundButton = iconButton(UiStyle.SOUND_ON, onClick = ::toggleSound) { it.row() }
                    musicButton = iconButton(UiStyle.MUSIC_ON, onClick = ::toggleMusic)
                }
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
        val style = if (settingsRepository.soundOn) UiStyle.SOUND_ON else UiStyle.SOUND_OFF
        soundButton.style = Scene2DSkin.defaultSkin.get(style, ImageButtonStyle::class.java)
        val musicStyle = if (settingsRepository.musicOn) UiStyle.MUSIC_ON else UiStyle.MUSIC_OFF
        musicButton.style = Scene2DSkin.defaultSkin.get(musicStyle, ImageButtonStyle::class.java)
    }

    private fun updateBestScoreLabel(bestScore: Int) {
        bestScoreLabel.setText(I18N.best(bestScore))
        bestScoreLabel.pack()
    }
}

private class MainMenuTitle {
    private val logo = Scene2DSkin.defaultSkin.getDrawable(UiStyle.LOGO) as TextureRegionDrawable

    val actor = scene2d.image(logo) {
        setOrigin(Align.center)
    }

    fun fit(boardWidth: Float) {
        val width = boardWidth * MENU_TITLE_WIDTH_RATIO
        val region = logo.region
        logo.setMinSize(width, width * region.regionHeight / region.regionWidth)
        actor.invalidateHierarchy()
    }

    fun startAnimation() {
    }

    fun resetAnimation() {
        actor.clearActions()
    }
}

private class PlayButton(menuEventBus: MenuEventBus, private val navigator: Navigator, widgets: Widgets) {
    private var clicked = false
    private var hoverAction: Action? = null

    val actor = widgets.menuButton(I18N.play(), onClick = { menuEventBus.emit(PlayGameClicked) }, style = UiStyle.BIG) {
        pad(PLAY_BUTTON_PAD_TOP, PLAY_BUTTON_PAD_SIDE, PLAY_BUTTON_PAD_BOTTOM, PLAY_BUTTON_PAD_SIDE)
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
