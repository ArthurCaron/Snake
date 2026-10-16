package fr.mwet.snake.save.serialization

internal class InMemoryTextStore : TextStore {
    private val files = mutableMapOf<String, String>()

    override fun read(path: String) = files[path]

    override fun write(path: String, text: String) {
        files[path] = text
    }

    override fun exists(path: String) = path in files
    override fun delete(path: String) = files.remove(path) != null
    override fun backupCorrupted(path: String) = files.remove(path) != null
}
