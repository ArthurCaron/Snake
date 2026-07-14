package fr.mwet.tools.assetcataloggenerator

import java.util.*

/**
 * Turns an asset name/path segment into a clean variable name (and valid Kotlin identifier) for generated code.
 * "gridCell" -> GridCell
 * "snake--head" -> SnakeHead
 * "Fredoka_Condensed-Regular" -> FredokaCondensedRegular
 * "3-hit-sound" -> Asset3HitSound
 * "class" -> ClassAsset
 * "---" -> Asset
 */
fun String.toCleanVariableName(): String {
    /*
     * Splits the string anywhere there is something that is not an ASCII letter or digit.
     * "gridCell" -> ["gridCell"]
     * "snake--head" -> ["snake", "", "head"]
     * "Fredoka_Condensed-Regular" -> ["Fredoka", "Condensed", "Regular"]
     * "3-hit-sound" -> ["3", "hit", "sound"]
     * "class" -> ["class"]
     * "---" -> []
     */
    return splitByBlocksOfLettersOrDigits()
        /* Removes empty parts, useful if the name has repeated separators
         * ...
         * ["snake", "", "head"] -> ["snake", "head"]
         * ...
         */
        .filter { it.isNotBlank() }
        /* Rejoins the parts without separators, while uppercasing the first letter of each name, creating PascalCase
         * ["gridCell"] -> GridCell
         * ["snake", "", "head"] -> SnakeHead
         * ["Fredoka", "Condensed", "Regular"] -> FredokaCondensedRegular
         * ["3", "hit", "sound"] -> 3HitSound
         * ["class"] -> Class
         * [] -> ""
         */
        .joinToString("") { it.uppercaseFirstLetter() }
        /* If the original string had no usable characters, the generated identifier becomes Asset
         * ...
         * "" -> Asset
         * ...
         */
        .ifBlank { "Asset" }
        /* Adds Asset in front of variables that start with a digit
         * ...
         * 3HitSound -> Asset3HitSound
         * ...
         */
        .let { if (it.first().isDigit()) "Asset$it" else it }
        /* Adds Asset after variables that are part of the Kotlin language
         * ...
         * Class -> ClassAsset
         * ...
         */
        .let { if (it in kotlinKeywords) "${it}Asset" else it }
}

private fun String.splitByBlocksOfLettersOrDigits(): List<String> = split(Regex("[^A-Za-z0-9]+"))

private fun String.uppercaseFirstLetter(): String = replaceFirstChar { char ->
    if (char.isLowerCase()) char.titlecase(Locale.ROOT) else char.toString()
}

private val kotlinKeywords = setOf(
    "As",
    "Break",
    "Class",
    "Continue",
    "Do",
    "Else",
    "False",
    "For",
    "Fun",
    "If",
    "In",
    "Interface",
    "Is",
    "Null",
    "Object",
    "Package",
    "Return",
    "Super",
    "This",
    "Throw",
    "True",
    "Try",
    "Typealias",
    "Val",
    "Var",
    "When",
    "While",
)
