package fr.mwet.snake

import fr.mwet.snake.screens.options.OptionPage

interface Navigator {
    fun goToMainMenu()
    fun goToGame()
    fun goToSettings()
    fun goToControls()
    fun goToLanguage()
    fun goToFont()
    fun goToAppearance()
    fun goToOptions(page: OptionPage)
    fun previewTransition()
    fun goToDevOptions()
}
