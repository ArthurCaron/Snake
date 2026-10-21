package fr.mwet.tools.translationpreview

private val PLACEHOLDER = Regex("\\{(\\d+)}")

class ShownText(val key: String, val arguments: List<String>)

class TextMatcher(patterns: Map<String, String>) {
    private val regexes = patterns.entries
        .sortedBy { PLACEHOLDER.containsMatchIn(it.value) }
        .map { (key, pattern) -> key to toRegex(pattern) }

    fun match(text: String): ShownText? = regexes.firstNotNullOfOrNull { (key, regex) ->
        regex.matchEntire(text)?.let { ShownText(key, it.groupValues.drop(1)) }
    }
}

fun display(pattern: String, arguments: List<String>): String {
    if (!PLACEHOLDER.containsMatchIn(pattern)) return pattern
    return PLACEHOLDER.replace(pattern.replace("''", "'")) { placeholder ->
        arguments.getOrElse(placeholder.groupValues[1].toInt()) { placeholder.value }
    }
}

private fun toRegex(pattern: String): Regex {
    if (!PLACEHOLDER.containsMatchIn(pattern)) return Regex(Regex.escape(pattern))
    val literal = pattern.replace("''", "'")
    val parts = PLACEHOLDER.split(literal).map { Regex.escape(it) }
    return Regex(parts.joinToString("(.+?)"))
}
