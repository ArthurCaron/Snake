package fr.mwet.snake.save.settings

import java.util.Locale

enum class Language(val tag: String, private val fixedLocale: Locale?) {
    System("system", null),
    English("en", Locale.ENGLISH),
    French("fr", Locale.FRENCH);

    fun locale(): Locale = fixedLocale ?: Locale.getDefault()

    companion object {
        fun fromTag(tag: String): Language = entries.firstOrNull { it.tag == tag } ?: System
    }
}
