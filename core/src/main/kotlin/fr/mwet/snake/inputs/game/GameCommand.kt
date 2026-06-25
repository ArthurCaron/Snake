package fr.mwet.snake.inputs.game

import fr.mwet.snake.events.GameEvent
import fr.mwet.snake.events.GameEventBus
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
    fun execute(context: GameCommandContext)
}

class GameCommandContext(
    val targetActor: TargetActor,
    val gameEventBus: GameEventBus,
)

interface TargetActor {
    fun setDirection(newDirection: Direction)
}

object NullGameCommand : GameCommand {
    override fun execute(context: GameCommandContext) {}
}

object GoBackToMainMenuCommand : GameCommand {
    override fun execute(context: GameCommandContext) {
        context.gameEventBus.emit(GameEvent.GoBackToMainMenu)
    }
}

object PauseCommand : GameCommand {
    override fun execute(context: GameCommandContext) {
        context.gameEventBus.emit(GameEvent.Pause)
    }
}

object GoUpCommand : GameCommand {
    override fun execute(context: GameCommandContext) {
        context.targetActor.setDirection(Direction.UP)
    }
}

object GoRightCommand : GameCommand {
    override fun execute(context: GameCommandContext) {
        context.targetActor.setDirection(Direction.RIGHT)
    }
}

object GoDownCommand : GameCommand {
    override fun execute(context: GameCommandContext) {
        context.targetActor.setDirection(Direction.DOWN)
    }
}

object GoLeftCommand : GameCommand {
    override fun execute(context: GameCommandContext) {
        context.targetActor.setDirection(Direction.LEFT)
    }
}
