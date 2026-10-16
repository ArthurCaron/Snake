package fr.mwet.snake.transition

enum class TransitionStyle(val tag: String) {
    None("none"),
    Fade("fade"),
    Slide("slide"),
    SnakeWipe("snake");

    fun next(): TransitionStyle = entries[(ordinal + 1) % entries.size]

    companion object {
        fun fromTag(tag: String): TransitionStyle = entries.firstOrNull { it.tag == tag } ?: None
    }
}
