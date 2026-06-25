package fr.mwet.snake.save.serialization

import com.badlogic.gdx.Preferences

class PreferencesTextStore(private val root: Preferences) : TextStore {
    override fun read(path: String): String? = root.getString(path, null)

    override fun write(path: String, text: String) {
        root.putString(path, text)
        root.flush()
    }

    override fun exists(path: String) = root.contains(path)

    override fun delete(path: String): Boolean {
        root.remove(path)
        root.flush()
        return true
    }
}
