package fr.mwet.snake.inputs.game

import fr.mwet.snake.game.Direction

class GoDown : GameCommand {
    override fun execute(targetActor: TargetActor) {
        targetActor.setDirection(Direction.DOWN)
    }
}
