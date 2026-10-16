package fr.mwet.snake

import fr.mwet.snake.save.settings.Language

interface LanguageSwitcher {
    fun applyLanguage(language: Language)
}
