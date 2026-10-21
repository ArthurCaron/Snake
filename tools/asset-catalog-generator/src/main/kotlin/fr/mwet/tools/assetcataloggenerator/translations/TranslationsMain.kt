package fr.mwet.tools.assetcataloggenerator.translations

import fr.mwet.tools.assetcataloggenerator.reader.readTranslationTable
import fr.mwet.tools.assetcataloggenerator.renderer.renderTranslationBundle
import java.nio.file.Files
import java.nio.file.Path.of

private const val BUNDLE = "translations"

fun main(args: Array<String>) {
    require(args.size == 4 && args[0] == "--input" && args[2] == "--output") {
        "Usage: --input <translations.csv> --output <runtime i18n directory>"
    }
    val table = readTranslationTable(of(args[1]))
    val outputDir = of(args[3])

    Files.createDirectories(outputDir)
    Files.list(outputDir).use { files ->
        files.filter { it.fileName.toString().startsWith(BUNDLE) && it.fileName.toString().endsWith(".properties") }
            .forEach(Files::delete)
    }

    Files.writeString(outputDir.resolve("$BUNDLE.properties"), renderTranslationBundle(table, 0))
    table.languages.forEachIndexed { language, tag ->
        Files.writeString(outputDir.resolve("${BUNDLE}_$tag.properties"), renderTranslationBundle(table, language))
    }
}
