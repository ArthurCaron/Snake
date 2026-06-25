package fr.mwet.snake.save.serialization

interface TextStore {
    fun read(path: String): String?
    fun write(path: String, text: String)
    fun exists(path: String): Boolean
    fun delete(path: String): Boolean
}
