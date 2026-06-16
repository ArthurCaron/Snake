package fr.mwet.snake.inputs.game

import fr.mwet.snake.game.Direction

class GoLeft : GameCommand {
    override fun execute(targetActor: TargetActor) {
        targetActor.setDirection(Direction.LEFT)
    }
}
