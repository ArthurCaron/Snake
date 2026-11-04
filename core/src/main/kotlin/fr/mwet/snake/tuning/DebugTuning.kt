package fr.mwet.snake.tuning

object DebugTuning {
    val ENABLED = System.getProperty("snake.debug") == "true"

    // Shows the dev options button on the main menu. Set to false for the release build.
    const val SHOW_DEV_OPTIONS = true
}
