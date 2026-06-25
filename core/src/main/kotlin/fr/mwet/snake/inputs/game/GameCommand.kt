package fr.mwet.snake.inputs.game

import fr.mwet.snake.Game
import fr.mwet.snake.screens.MainMenuScreen
import fr.mwet.snake.utils.Direction

enum class GameActionId(val gameCommand: GameCommand) {
    Null(NullGameCommand),
    GoBackToMainMenu(GoBackToMainMenuCommand),
    Pause(PauseCommand),
    GoUp(GoUpCommand),
    GoRight(GoRightCommand),
    GoDown(GoDownCommand),
    GoLeft(GoLeftCommand);

    companion object {
        fun from(gameCommand: GameCommand) = when (gameCommand) {
            NullGameCommand -> Null
            GoBackToMainMenuCommand -> GoBackToMainMenu
            PauseCommand -> Pause
            GoUpCommand -> GoUp
            GoRightCommand -> GoRight
            GoDownCommand -> GoDown
            GoLeftCommand -> GoLeft
        }
    }
}

sealed interface GameCommand {
    fun execute(targetActor: TargetActor)
}

interface TargetActor {
    fun setDirection(newDirection: Direction)
}

object NullGameCommand : GameCommand {
    override fun execute(targetActor: TargetActor) {}
}

object GoBackToMainMenuCommand : GameCommand {
    override fun execute(targetActor: TargetActor) {
        Game.setScreen<MainMenuScreen>()
    }
}

object PauseCommand : GameCommand {
    override fun execute(targetActor: TargetActor) {
        Game.setScreen<MainMenuScreen>()
    }
}

object GoUpCommand : GameCommand {
    override fun execute(targetActor: TargetActor) {
        targetActor.setDirection(Direction.UP)
    }
}

object GoRightCommand : GameCommand {
    override fun execute(targetActor: TargetActor) {
        targetActor.setDirection(Direction.RIGHT)
    }
}

object GoDownCommand : GameCommand {
    override fun execute(targetActor: TargetActor) {
        targetActor.setDirection(Direction.DOWN)
    }
}

object GoLeftCommand : GameCommand {
    override fun execute(targetActor: TargetActor) {
        targetActor.setDirection(Direction.LEFT)
    }
}
