package fr.mwet.tools.translationpreview

import fr.mwet.tools.assetcataloggenerator.reader.TranslationRow
import fr.mwet.tools.assetcataloggenerator.reader.readTranslationTable
import java.nio.file.Files
import java.nio.file.Path

class TranslationEdits(private val file: Path, val language: String) {
    private val table = readTranslationTable(file)
    private val column = table.languages.indexOf(language)
    private val values: MutableMap<String, String>

    init {
        require(column >= 0) { "No \"$language\" column in $file, found ${table.languages}" }
        values = table.rows.associateTo(LinkedHashMap()) { it.key to it.values[column] }
    }

    val keys: List<String> get() = table.rows.map { it.key }

    operator fun get(key: String): String = values.getValue(key)

    operator fun set(key: String, value: String) {
        values[key] = value
    }

    fun save() {
        val rows = table.rows.map { row -> row.copy(values = row.values.toMutableList().also { it[column] = values.getValue(row.key) }) }
        Files.writeString(file, renderTranslationTable(table.languages, rows))
    }
}

fun renderTranslationTable(languages: List<String>, rows: List<TranslationRow>): String = buildString {
    appendLine((listOf("key") + languages).joinToString(",") { it.csvCell() })
    rows.forEach { row -> appendLine((listOf(row.key) + row.values).joinToString(",") { it.csvCell() }) }
}

private fun String.csvCell(): String =
    if (any { it == ',' || it == '"' || it == '\n' || it == '\r' }) "\"" + replace("\"", "\"\"") + "\"" else this
