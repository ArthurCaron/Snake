package fr.mwet.snake.save.serialization

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonWriter

class JsonStore(val textStore: TextStore) {
    val json = Json().apply {
        ignoreUnknownFields = true
        setOutputType(JsonWriter.OutputType.json)
        setUsePrototypes(false)
    }

    inline fun <reified T : Any> readOrNull(path: String): T? {
        val text = textStore.read(path) ?: return null

        return runCatching {
            json.fromJson(T::class.java, text)
        }.getOrElse {
            Gdx.app.error("JsonStore", "Failed to load $path. Backing up corrupted save.", it)
            textStore.backupCorrupted(path)
            null
        }
    }

    inline fun <reified T : Any> readOrCreate(path: String, defaultValue: () -> T): T {
        return readOrNull(path) ?: defaultValue().also { write(path, it) }
    }

    fun write(path: String, value: Any) {
        textStore.write(path, json.prettyPrint(value))
    }

    fun exists(path: String): Boolean = textStore.exists(path)

    fun delete(path: String): Boolean = textStore.delete(path)
}
