package fr.mwet.snake.assets

import fr.mwet.snake.events.GameEvent
import fr.mwet.snake.events.GameEvent.*
import fr.mwet.snake.events.GameEventListener
import fr.mwet.snake.events.MenuEvent
import fr.mwet.snake.events.MenuEventListener

class SoundPlayer(private val soundHandler: SoundHandler) : MenuEventListener, GameEventListener {
    private var switchId: Long = 0
    private var moveId: Long = 0
    private var eatId: Long = 0

    override fun onEvent(event: GameEvent) {
        when (event) {
            SnakeMoved -> playMove()
            FoodEaten -> playEat()
            GameOver, GoBackToMainMenu, Pause -> {}
        }
    }

    override fun onEvent(event: MenuEvent) {
        when (event) {
            MenuEvent.PlayGameClicked -> playSwitch()
        }
    }

    fun playSwitch() {
        switchId = soundHandler.switchSound.play()
    }

    fun playMove() {
        soundHandler.moveSound.stop()
        moveId = soundHandler.moveSound.play()
    }

    fun playEat() {
        eatId = soundHandler.eatSound.play()
    }
}
