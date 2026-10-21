package fr.mwet.tools.assetcataloggenerator.reader

import java.nio.file.Files
import java.nio.file.Path

private val IDENTIFIER = Regex("[A-Za-z_][A-Za-z0-9_]*")

data class TranslationTable(val languages: List<String>, val rows: List<TranslationRow>)

data class TranslationRow(val key: String, val values: List<String>)

fun readTranslationTable(csvFile: Path): TranslationTable = parseTranslationTable(Files.readString(csvFile), csvFile.toString())

fun parseTranslationTable(csv: String, source: String): TranslationTable {
    val records = parseCsv(csv)
    require(records.isNotEmpty()) { "Empty translation table: $source" }
    val header = records.first()
    require(header.first() == "key" && header.size >= 2) { "The header must be \"key\" followed by one column per language in $source" }
    val languages = header.drop(1)

    val keys = mutableSetOf<String>()
    val rows = records.drop(1).mapIndexed { index, record ->
        val line = index + 2
        val key = record.first()
        require(key.matches(IDENTIFIER)) { "Translation key is not a valid identifier: \"$key\" at line $line of $source" }
        require(keys.add(key)) { "Duplicate translation key: \"$key\" at line $line of $source" }
        require(record.size == header.size) { "Expected ${header.size} cells for \"$key\" at line $line of $source, found ${record.size}" }
        val values = record.drop(1)
        values.forEachIndexed { language, value ->
            require(value.isNotEmpty()) { "Missing \"${languages[language]}\" translation for \"$key\" at line $line of $source" }
        }
        TranslationRow(key, values)
    }
    return TranslationTable(languages, rows)
}

fun parseCsv(csv: String): List<List<String>> {
    val records = mutableListOf<List<String>>()
    var record = mutableListOf<String>()
    val cell = StringBuilder()
    var quoted = false
    var index = 0

    fun endRecord() {
        record += cell.toString()
        cell.clear()
        if (record.size > 1 || record.first().isNotEmpty()) records += record
        record = mutableListOf()
    }

    while (index < csv.length) {
        val char = csv[index]
        when {
            quoted && char == '"' && csv.getOrNull(index + 1) == '"' -> {
                cell.append('"')
                index++
            }
            char == '"' -> quoted = !quoted
            quoted -> cell.append(char)
            char == ',' -> {
                record += cell.toString()
                cell.clear()
            }
            char == '\r' && csv.getOrNull(index + 1) == '\n' -> {}
            char == '\n' -> endRecord()
            else -> cell.append(char)
        }
        index++
    }
    require(!quoted) { "Unclosed quoted cell in the translation table" }
    endRecord()
    return records
}
