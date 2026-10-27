package fr.mwet.snake.game

import fr.mwet.snake.events.GameEvent
import fr.mwet.snake.events.GameEventBus
import fr.mwet.snake.events.GameEventListener
import fr.mwet.snake.tuning.GameplayTuning.START_LENGTH
import fr.mwet.snake.tuning.GameplayTuning.WORLD_HEIGHT
import fr.mwet.snake.tuning.GameplayTuning.WORLD_WIDTH
import kotlin.test.Test
import kotlin.test.assertEquals

class GameWorldTest {
    @Test
    fun `emits pause and resume events only when the state changes`() {
        val events = mutableListOf<GameEvent>()
        val world = GameWorld(object : GameEventBus {
            override fun listen(listener: GameEventListener) = Unit
            override fun emit(gameEvent: GameEvent) {
                events.add(gameEvent)
            }
        })

        world.resume()
        world.pause()
        world.pause()
        world.resume()
        world.resume()

        assertEquals(listOf(GameEvent.Paused, GameEvent.Resumed), events)
        assertEquals(GameState.Ready, world.state)
    }

    @Test
    fun `keeps the earned speed after a pause and resets it for a new game`() {
        val world = GameWorld(object : GameEventBus {
            override fun listen(listener: GameEventListener) = Unit
            override fun emit(gameEvent: GameEvent) = Unit
        })
        world.newGame()
        world.start()
        world.ticker.speedUp(by = 5f, upTo = 12f)

        world.pause()
        world.resume()
        world.start()

        assertEquals(UpdateResult.DoUpdate, world.ticker.update(0.11f))

        world.newGame()
        world.start()

        assertEquals(UpdateResult.KeepWaiting, world.ticker.update(0.11f))
    }

    @Test
    fun `an almost won game is won with the maximum score after one tick`() {
        val world = GameWorld(object : GameEventBus {
            override fun listen(listener: GameEventListener) = Unit
            override fun emit(gameEvent: GameEvent) = Unit
        })
        world.newGame(SnakeStart.almostWon())
        world.start()

        world.update(1f)

        assertEquals(GameState.Won, world.state)
        assertEquals((WORLD_WIDTH * WORLD_HEIGHT).toInt() - START_LENGTH, world.score)
    }
}
