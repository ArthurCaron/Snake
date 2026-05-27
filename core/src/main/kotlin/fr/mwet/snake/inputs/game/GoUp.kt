package fr.mwet.snake.inputs.game

import fr.mwet.snake.entities.Direction

class GoUp : GameCommand {
    override fun execute(targetActor: TargetActor) {
        targetActor.setDirection(Direction.UP)
    }
}
