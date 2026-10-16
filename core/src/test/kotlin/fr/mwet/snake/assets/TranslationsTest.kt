package fr.mwet.snake.assets

import fr.mwet.snake.generated.assets.I18N
import java.io.File
import java.util.Properties
import kotlin.test.Test
import kotlin.test.assertTrue

private val I18N_FOLDER = File("../sourceAssets/i18n")

class TranslationsTest {
    @Test
    fun `every I18N line has a translation in every bundle`() {
        val bundles = I18N_FOLDER.listFiles { file -> file.name.startsWith("translations") && file.extension == "properties" }
            .orEmpty()
        assertTrue(bundles.isNotEmpty(), "No translation bundle found in ${I18N_FOLDER.canonicalPath}")

        val missing = bundles.flatMap { bundle ->
            val keys = Properties().apply { bundle.reader(Charsets.UTF_8).use { load(it) } }.stringPropertyNames()
            I18N.entries.map { it.name }.filterNot { it in keys }.map { "${bundle.name}: $it" }
        }

        assertTrue(missing.isEmpty(), "Missing translations:\n${missing.joinToString("\n")}")
    }
}
