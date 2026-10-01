package fr.mwet.snake.assets

import fr.mwet.snake.events.GameEvent
import fr.mwet.snake.events.GameEvent.*
import fr.mwet.snake.events.GameEventListener
import fr.mwet.snake.events.MenuEvent
import fr.mwet.snake.events.MenuEventListener
import fr.mwet.snake.save.settings.AudioSettings

private const val MUSIC_VOLUME = 0.5f
private const val MOVE_VOLUME = 0.25f

class SoundPlayer(
    private val soundHandler: SoundHandler,
    private val musicHandler: MusicHandler,
    private val audioSettings: AudioSettings,
) : MenuEventListener, GameEventListener {
    private var buttonPressedId: Long = 0
    private var moveId: Long = 0
    private var eatId: Long = 0

    override fun onEvent(event: GameEvent) {
        when (event) {
            SnakeMoved -> playMove()
            FoodEaten -> playEat()
            Lost -> playLost()
            Won -> playWon()
            Pause -> playPause()
            GoBackToMainMenu -> {}
        }
    }

    override fun onEvent(event: MenuEvent) {
        when (event) {
            MenuEvent.PlayGameClicked -> {
                playButtonPressed()
            }

            MenuEvent.SoundToggled -> {
                playButtonPressed()
            }

            MenuEvent.MusicToggled -> {
                playButtonPressed()

                val music = musicHandler.music
                if (audioSettings.musicOn) {
                    music.isLooping = true
                    music.volume = MUSIC_VOLUME
                    if (!music.isPlaying) music.play()
                } else {
                    music.pause()
                }
            }
        }
    }

    fun playButtonPressed() {
        if (!audioSettings.soundOn) return
        buttonPressedId = soundHandler.buttonPressedSound.play()
    }

    fun playMove() {
        if (!audioSettings.soundOn) return
        soundHandler.moveSound.stop()
        moveId = soundHandler.moveSound.play(MOVE_VOLUME)
    }

    fun playEat() {
        if (!audioSettings.soundOn) return
        eatId = soundHandler.eatSound.play()
    }

    fun playLost() {
        if (!audioSettings.soundOn) return
        soundHandler.lostSound.play()
    }

    fun playWon() {
        if (!audioSettings.soundOn) return
        soundHandler.wonSound.play()
    }

    fun playPause() {
        if (!audioSettings.soundOn) return
        soundHandler.pauseSound.play()
    }
}
