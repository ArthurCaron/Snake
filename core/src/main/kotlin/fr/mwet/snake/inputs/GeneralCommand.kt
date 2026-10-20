package fr.mwet.snake.inputs

enum class GeneralActionId(val generalCommand: GeneralCommand) {
    Null(NullGeneralCommand),
    StartGame(StartGameCommand),
    Pause(PauseCommand),
    Back(BackCommand);

    companion object {
        fun from(generalCommand: GeneralCommand) = when (generalCommand) {
            NullGeneralCommand -> Null
            StartGameCommand -> StartGame
            PauseCommand -> Pause
            BackCommand -> Back
        }
    }
}

sealed interface GeneralCommand {
    fun execute(generalCommandContext: GeneralCommandContext): Boolean
}

class GeneralCommandContext(
    val onStartGame: (() -> Boolean)? = null,
    val onBack: (() -> Boolean)? = null,
    val onPause: (() -> Boolean)? = null,
)

object NullGeneralCommand : GeneralCommand {
    override fun execute(generalCommandContext: GeneralCommandContext) = false
}

object StartGameCommand : GeneralCommand {
    override fun execute(generalCommandContext: GeneralCommandContext) = generalCommandContext.onStartGame?.invoke() ?: false
}

object BackCommand : GeneralCommand {
    override fun execute(generalCommandContext: GeneralCommandContext) = generalCommandContext.onBack?.invoke() ?: false
}

object PauseCommand : GeneralCommand {
    override fun execute(generalCommandContext: GeneralCommandContext) = generalCommandContext.onPause?.invoke() ?: false
}
