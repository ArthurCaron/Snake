package fr.mwet.snake.save.settings

import java.util.Locale

enum class Language(val tag: String, private val fixedLocale: Locale?) {
    System("system", null),
    English("en", Locale.ENGLISH),
    French("fr", Locale.FRENCH);

    fun locale(): Locale = fixedLocale ?: Locale.getDefault()

    // The fixed language shown for this one: System shows the device language, or English when the game lacks it.
    fun resolved(): Language =
        if (this != System) this else entries.firstOrNull { it.fixedLocale?.language == locale().language } ?: English

    companion object {
        fun fromTag(tag: String): Language = entries.firstOrNull { it.tag == tag } ?: System
    }
}
