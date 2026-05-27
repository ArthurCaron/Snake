package fr.mwet.snake.inputs.game

import fr.mwet.snake.entities.Direction

interface GameCommand {
    fun execute(targetActor: TargetActor)
}

interface TargetActor {
    fun setDirection(direction: Direction)
}
