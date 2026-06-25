package fr.mwet.snake.save.serialization

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.TimeUtils

class FileTextStore(private val root: FileHandle) : TextStore {
    override fun read(path: String): String? {
        val file = root.child(path)
        if (!file.exists()) return null

        return runCatching {
            file.readString("UTF-8")
        }.getOrElse {
            backupCorruptedFile(file)
            null
        }
    }

    override fun write(path: String, text: String) {
        val file = root.child(path)
        file.parent().mkdirs()
        val tmp = file.sibling("${file.name()}.tmp")
        tmp.writeString(text, false, "UTF-8")
        tmp.moveTo(file)
    }

    override fun exists(path: String) = root.child(path).exists()

    override fun delete(path: String) = root.child(path).delete()

    fun backupCorruptedFile(file: FileHandle) {
        val backup = file.sibling("corrupted_${TimeUtils.millis()}_${file.name()}")
        file.copyTo(backup)
        file.delete()
    }
}
