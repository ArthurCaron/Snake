package fr.mwet.tools.assetcataloggenerator.reader

import java.nio.file.Files
import java.nio.file.Path

private val KEY_SEPARATORS = charArrayOf('=', ':', ' ', '\t', '\u000c')
private val IDENTIFIER = Regex("[A-Za-z_][A-Za-z0-9_]*")

fun readTranslationKeys(propertiesFile: Path): List<String> {
    val keys = mutableListOf<String>()
    var continuesPreviousLine = false
    Files.readAllLines(propertiesFile).forEach { line ->
        val trimmed = line.trimStart()
        if (continuesPreviousLine) {
            continuesPreviousLine = trimmed.endsWithOddBackslashes()
            return@forEach
        }
        if (trimmed.isEmpty() || trimmed.first() == '#' || trimmed.first() == '!') return@forEach

        val key = trimmed.substringBefore(KEY_SEPARATORS)
        require(key.matches(IDENTIFIER)) { "Translation key is not a valid identifier: \"$key\" in $propertiesFile" }
        require(key !in keys) { "Duplicate translation key: \"$key\" in $propertiesFile" }
        keys += key
        continuesPreviousLine = trimmed.endsWithOddBackslashes()
    }
    return keys
}

private fun String.substringBefore(separators: CharArray): String {
    val index = indexOfAny(separators)
    return if (index < 0) this else substring(0, index)
}

private fun String.endsWithOddBackslashes(): Boolean = takeLastWhile { it == '\\' }.length % 2 == 1
