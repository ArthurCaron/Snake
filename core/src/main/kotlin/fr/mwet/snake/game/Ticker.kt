package fr.mwet.snake.game

class Ticker {
    private val ticksPerSecond = 5f
    private val secondsPerTick = 1f / ticksPerSecond
    private var secondsSinceLastTick: Float = 0f

    fun reset() {
        secondsSinceLastTick = 0f
    }

    fun update(delta: Float): UpdateResult {
        secondsSinceLastTick += delta
        if (secondsSinceLastTick < secondsPerTick) return UpdateResult.KeepWaiting

        secondsSinceLastTick -= secondsPerTick
        return UpdateResult.DoUpdate
    }
}

sealed interface UpdateResult {
    object KeepWaiting : UpdateResult
    object DoUpdate : UpdateResult
}
