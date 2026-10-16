package fr.mwet.snake.backdrop

enum class BackdropStyle(val tag: String) {
    Plain("plain"),
    Fireflies("fireflies"),
    DiagonalStripes("stripes"),
    Waves("waves"),
    DriftingGrid("grid"),
    DemoSnake("demo"),
    FloatingFruits("fruits"),
    Ripples("ripples"),
    GameOfLife("life"),
    WanderingSnakes("wanderers");

    fun next(): BackdropStyle = entries[(ordinal + 1) % entries.size]

    companion object {
        fun fromTag(tag: String): BackdropStyle = entries.firstOrNull { it.tag == tag } ?: DemoSnake
    }
}
