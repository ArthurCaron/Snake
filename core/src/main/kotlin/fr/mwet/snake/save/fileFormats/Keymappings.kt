package fr.mwet.snake.save.fileFormats

import fr.mwet.snake.inputs.game.GameCommand
import fr.mwet.snake.inputs.game.TargetActor
import fr.mwet.snake.inputs.general.GeneralCommand

class Keymappings() {
    var game: MutableList<GameKeymapping> = mutableListOf()
    var general: MutableList<GeneralKeymapping> = mutableListOf()

    constructor(game: List<GameKeymapping>, general: List<GeneralKeymapping>) : this() {
        this.game = game.toMutableList()
        this.general = general.toMutableList()
    }
}

class GameKeymapping() {
    var action: GameCommand = NullGameCommand
    var key: Int = -1

    constructor(action: GameCommand, key: Int) : this() {
        this.action = action; this.key = key
    }
}

class GeneralKeymapping() {
    var action: GeneralCommand = NullGeneralCommand
    var key: Int = -1

    constructor(action: GeneralCommand, key: Int) : this() {
        this.action = action; this.key = key
    }
}

object NullGameCommand : GameCommand {
    override fun execute(targetActor: TargetActor) {}
}

object NullGeneralCommand : GeneralCommand {
    override fun execute() {}
}

@JvmInline
value class StringKeymappings(
    val keymappings: String,
)
