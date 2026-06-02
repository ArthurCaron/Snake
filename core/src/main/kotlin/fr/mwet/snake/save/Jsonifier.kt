package fr.mwet.snake.save

import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonWriter
import fr.mwet.snake.DI
import fr.mwet.snake.save.fileFormats.Highscores
import fr.mwet.snake.save.fileFormats.Keymappings
import fr.mwet.snake.save.fileFormats.StringHighscores
import fr.mwet.snake.save.fileFormats.StringKeymappings

// https://libgdx.com/wiki/utils/reading-and-writing-json
class Jsonifier {
    val json = Json().apply {
        setOutputType(JsonWriter.OutputType.json)
        setUsePrototypes(false)
    }

    fun <T> prettyPrint(obj: T): String = json.prettyPrint(obj)

    inline fun <reified T> fromJson(value: String): T = json.fromJson(T::class.java, value)
}

fun Highscores.toJson(): StringHighscores = DI.inject<Jsonifier>().prettyPrint(this).let(::StringHighscores)

fun Keymappings.toJson(): StringKeymappings = DI.inject<Jsonifier>().prettyPrint(this).let(::StringKeymappings)

fun StringHighscores.fromJson(): Highscores = DI.inject<Jsonifier>().fromJson(this.highscores)

fun StringKeymappings.fromJson(): Keymappings = DI.inject<Jsonifier>().fromJson(this.keymappings)
