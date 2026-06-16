package fr.mwet.snake.inputs.game

import fr.mwet.snake.game.Direction

interface GameCommand {
    fun execute(targetActor: TargetActor)
}

interface TargetActor {
    fun setDirection(direction: Direction)
}
