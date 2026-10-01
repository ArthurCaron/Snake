package fr.mwet.snake.game

class Ticker(private val initialTicksPerSecond: Float) {
    private var ticksPerSecond = initialTicksPerSecond
    private val secondsPerTick get() = 1f / ticksPerSecond
    private var secondsSinceLastTick: Float = 0f

    fun reset() {
        ticksPerSecond = initialTicksPerSecond
        secondsSinceLastTick = 0f
    }

    fun speedUp(by: Float, upTo: Float) {
        ticksPerSecond = minOf(ticksPerSecond + by, upTo)
    }

    fun update(delta: Float): UpdateResult {
        secondsSinceLastTick += delta
        if (secondsSinceLastTick < secondsPerTick) return UpdateResult.KeepWaiting

        secondsSinceLastTick -= secondsPerTick
        if (secondsSinceLastTick >= secondsPerTick) secondsSinceLastTick = 0f
        return UpdateResult.DoUpdate
    }
}

sealed interface UpdateResult {
    object KeepWaiting : UpdateResult
    object DoUpdate : UpdateResult
}
