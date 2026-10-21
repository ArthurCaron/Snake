package fr.mwet.tools.translationpreview

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import java.nio.file.Path

fun main(args: Array<String>) {
    val language = args.getOrElse(0) { "fr" }
    val table = Path.of(args.getOrElse(1) { "sourceAssets/i18n/translations.csv" })
    val config = Lwjgl3ApplicationConfiguration().apply {
        setTitle("Snake translations: $language")
        setWindowedMode(PreviewMetrics.WINDOW_WIDTH, PreviewMetrics.WINDOW_HEIGHT)
    }
    Lwjgl3Application(TranslationPreview(TranslationEdits(table, language)), config)
}
