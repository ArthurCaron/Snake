package fr.mwet.snake.save.serialization

import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonWriter

class JsonStore(val textStore: TextStore) {
    val json = Json().apply {
        ignoreUnknownFields = true
        setOutputType(JsonWriter.OutputType.json)
        setUsePrototypes(false)
    }

    inline fun <reified T : Any> readOrNull(path: String): T? {
        return textStore.read(path)?.let { json.fromJson(T::class.java, it) }
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
