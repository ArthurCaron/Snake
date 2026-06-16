package fr.mwet.snake.inputs.game

import fr.mwet.snake.game.Direction

interface GameCommand {
    fun execute(targetActor: TargetActor)
}

interface TargetActor {
    fun setDirection(direction: Direction)
}

class GoUp : GameCommand {
    override fun execute(targetActor: TargetActor) {
        targetActor.setDirection(Direction.UP)
    }
}

class GoRight : GameCommand {
    override fun execute(targetActor: TargetActor) {
        targetActor.setDirection(Direction.RIGHT)
    }
}

class GoDown : GameCommand {
    override fun execute(targetActor: TargetActor) {
        targetActor.setDirection(Direction.DOWN)
    }
}

class GoLeft : GameCommand {
    override fun execute(targetActor: TargetActor) {
        targetActor.setDirection(Direction.LEFT)
    }
}
