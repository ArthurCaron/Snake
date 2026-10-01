package fr.mwet.snake.save.settings

class AudioSettings(private val settingsRepository: SettingsRepository, private var settings: Settings) {
    val soundOn: Boolean get() = settings.userPreferences.soundOn
    val musicOn: Boolean get() = settings.userPreferences.musicOn

    fun toggleSound() {
        settings = settings.copy(userPreferences = settings.userPreferences.copy(soundOn = !soundOn))
        settingsRepository.save(settings)
    }

    fun toggleMusic() {
        settings = settings.copy(userPreferences = settings.userPreferences.copy(musicOn = !musicOn))
        settingsRepository.save(settings)
    }
}
