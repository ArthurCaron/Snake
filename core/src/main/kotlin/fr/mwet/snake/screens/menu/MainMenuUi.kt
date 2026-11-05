package fr.mwet.snake.screens.menu

import com.badlogic.gdx.math.Interpolation.elasticOut
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.Align
import fr.mwet.snake.LanguageSwitcher
import fr.mwet.snake.Navigator
import fr.mwet.snake.events.MenuEvent.*
import fr.mwet.snake.events.MenuEventBus
import fr.mwet.snake.generated.assets.I18N
import fr.mwet.snake.save.settings.Language
import fr.mwet.snake.save.settings.SettingsRepository
import fr.mwet.snake.tuning.AnimationTuning.PLAY_BUTTON_CLICK_DELAY
import fr.mwet.snake.tuning.AnimationTuning.PLAY_BUTTON_HOVER_IN_DURATION
import fr.mwet.snake.tuning.AnimationTuning.PLAY_BUTTON_HOVER_OUT_DURATION
import fr.mwet.snake.tuning.AnimationTuning.PLAY_BUTTON_HOVER_SCALE
import fr.mwet.snake.tuning.FontTuning.TITLE_TEXT
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
import ktx.scene2d.KTableWidget
import ktx.scene2d.label
import ktx.scene2d.scene2d
import ktx.scene2d.table


class MainMenuUi(
    board: Group,
    private val menuEventBus: MenuEventBus,
    navigator: Navigator,
    private val settingsRepository: SettingsRepository,
    languages: LanguageSwitcher,
    widgets: Widgets,
) {
    private val title = MainMenuTitle()
    private val playButton = PlayButton(menuEventBus, navigator, widgets)
    private var bestScoreLabel: Label
    private var soundButton: ImageButton
    private var musicButton: ImageButton
    private val flags = LanguageFlags(settingsRepository, languages, widgets)

    init {
        with(widgets) {
            board.addActor(scene2d.table {
                setFillParent(true)
                actor(title.actor) { it.padBottom(MENU_TITLE_GAP).row() }
                actor(playButton.actor) { it.padBottom(SCREEN_MARGIN).row() }
                bestScoreLabel = label("") { it.padBottom(MENU_BEST_SCORE_GAP).row() }
                menuButton(I18N.settings(), onClick = navigator::goToSettings) { it.row() }
            })
            board.addActor(scene2d.table {
                setFillParent(true)
                top().right().pad(SCREEN_MARGIN)
                defaults().right().space(SCREEN_MARGIN)
                soundButton = iconButton(UiStyle.SOUND_ON, onClick = ::toggleSound) { it.row() }
                musicButton = iconButton(UiStyle.MUSIC_ON, onClick = ::toggleMusic)
            })
            board.addActor(flags.actor)
        }
    }

    fun onShow(bestScore: Int) {
        title.startAnimation()
        playButton.startAnimation()
        refreshAudioButtons()
        updateBestScoreLabel(bestScore)
        flags.close()
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

// The flag of the current language, at the top left of the board. A click opens the flags of the other languages below it, and a
// click on one of them switches to that language.
private class LanguageFlags(
    settingsRepository: SettingsRepository,
    private val languages: LanguageSwitcher,
    widgets: Widgets,
) {
    private lateinit var choices: KTableWidget

    val actor = with(widgets) {
        scene2d.table {
            setFillParent(true)
            top().left().pad(SCREEN_MARGIN)
            defaults().left()
            flagButton(UiStyle.flag(settingsRepository.language.resolved()), onClick = ::toggle) { it.row() }
            choices = table {
                defaults().left().padTop(SCREEN_MARGIN)
                for (language in Language.entries) {
                    if (language == Language.System || language == settingsRepository.language.resolved()) continue
                    flagButton(UiStyle.flag(language), onClick = { choose(language) }) { it.row() }
                }
                isVisible = false
            }
        }
    }

    fun close() {
        choices.isVisible = false
    }

    private fun toggle() {
        choices.isVisible = !choices.isVisible
    }

    private fun choose(language: Language) {
        close()
        languages.applyLanguage(language)
    }
}

// The game title in the title font, as wide as MENU_TITLE_WIDTH_RATIO of the board.
private class MainMenuTitle {
    val actor = scene2d.label(TITLE_TEXT, UiStyle.GAME_TITLE) {
        setOrigin(Align.center)
    }

    fun fit(boardWidth: Float) {
        actor.setFontScale(1f)
        actor.setFontScale(boardWidth * MENU_TITLE_WIDTH_RATIO / actor.prefWidth)
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
