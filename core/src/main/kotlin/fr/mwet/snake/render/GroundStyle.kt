package fr.mwet.snake.render

enum class GroundStyle(val tag: String) {
    Checker("checker"),
    Plain("plain"),
    Grain("grain"),
    Stripes("stripes"),
    Tiles("tiles"),
    Scales("scales"),
    Dunes("dunes"),
    Hex("hex");

    fun next(): GroundStyle = entries[(ordinal + 1) % entries.size]

    companion object {
        fun fromTag(tag: String): GroundStyle = entries.firstOrNull { it.tag == tag } ?: Checker
    }
}
