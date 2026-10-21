package fr.mwet.snake.assets

import fr.mwet.snake.events.GameEvent
import fr.mwet.snake.events.GameEvent.*
import fr.mwet.snake.events.GameEventListener
import fr.mwet.snake.events.MenuEvent
import fr.mwet.snake.events.MenuEventListener
import fr.mwet.snake.save.settings.SettingsRepository
import fr.mwet.snake.tuning.AudioTuning.DEFAULT_MUSIC_VOLUME
import fr.mwet.snake.tuning.AudioTuning.DEFAULT_SOUND_VOLUME

class SoundPlayer(
    private val soundHandler: SoundHandler,
    private val musicHandler: MusicHandler,
    private val settingsRepository: SettingsRepository,
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
            Paused, Resumed -> playPauseToggled()
        }
    }

    override fun onEvent(event: MenuEvent) {
        when (event) {
            MenuEvent.LoadingFinished -> {
                musicHandler.music.isLooping = true
                musicHandler.music.volume = DEFAULT_MUSIC_VOLUME
                updateMusicPlayback()
            }

            MenuEvent.PlayGameClicked -> {
                playButtonPressed()
            }

            MenuEvent.SoundToggled -> {
                playButtonPressed()
            }

            MenuEvent.MusicToggled -> {
                playButtonPressed()
                updateMusicPlayback()
            }
        }
    }

    private fun updateMusicPlayback() {
        val music = musicHandler.music

        if (settingsRepository.musicOn) {
            if (!music.isPlaying) music.play()
        } else {
            music.pause()
        }
    }

    fun playButtonPressed() {
        if (!settingsRepository.soundOn) return
        buttonPressedId = soundHandler.buttonPressedSound.play()
    }

    fun playMove() {
        if (!settingsRepository.soundOn) return
        soundHandler.moveSound.stop()
        moveId = soundHandler.moveSound.play(DEFAULT_SOUND_VOLUME)
    }

    fun playEat() {
        if (!settingsRepository.soundOn) return
        eatId = soundHandler.eatSound.play()
    }

    fun playLost() {
        if (!settingsRepository.soundOn) return
        soundHandler.lostSound.play()
    }

    fun playWon() {
        if (!settingsRepository.soundOn) return
        soundHandler.wonSound.play()
    }

    fun playPauseToggled() {
        if (!settingsRepository.soundOn) return
        soundHandler.pauseSound.play()
    }
}
