package fr.mwet.snake.save.settings.serializable

import com.badlogic.gdx.Input
import fr.mwet.snake.inputs.GameActionId
import fr.mwet.snake.inputs.GeneralActionId
import fr.mwet.snake.utils.NO_VERSION
import fr.mwet.snake.utils.doesNotContainKey

class KeymappingsSave() {
    var version: Int = NO_VERSION
    var game: ArrayList<GameKeymappingSave> = arrayListOf()
    var general: ArrayList<GeneralKeymappingSave> = arrayListOf()

    constructor(version: Int, game: List<GameKeymappingSave>, general: List<GeneralKeymappingSave>) : this() {
        this.version = version
        this.game = ArrayList(game)
        this.general = ArrayList(general)
    }

    fun restoreDefaults() {
        version = if (version == NO_VERSION) DefaultKeymappings.defaultKeymappings().version else version

        val gameMap = game.associate { it.action to it.keys }.toMutableMap()
        DefaultKeymappings.defaultKeymappings().game.forEach {
            if (gameMap.doesNotContainKey(it.action)) {
                gameMap[it.action] = it.keys
            }
        }
        game = ArrayList(gameMap.map { GameKeymappingSave(it.key, it.value) })

        val generalMap = general.associate { it.action to it.keys }.toMutableMap()
        DefaultKeymappings.defaultKeymappings().general.forEach {
            if (generalMap.doesNotContainKey(it.action)) {
                generalMap[it.action] = it.keys
            }
        }
        general = ArrayList(generalMap.map { GeneralKeymappingSave(it.key, it.value) })
    }
}

class GameKeymappingSave() {
    var action: GameActionId = GameActionId.Null
    var keys: ArrayList<Int> = arrayListOf()

    constructor(action: GameActionId, keys: List<Int>) : this() {
        this.action = action
        this.keys = ArrayList(keys)
    }
}

class GeneralKeymappingSave() {
    var action: GeneralActionId = GeneralActionId.Null
    var keys: ArrayList<Int> = arrayListOf()

    constructor(action: GeneralActionId, keys: List<Int>) : this() {
        this.action = action
        this.keys = ArrayList(keys)
    }
}

object DefaultKeymappings {
    fun defaultGame() = arrayListOf(
        GameKeymappingSave(GameActionId.GoBackToMainMenu, arrayListOf(Input.Keys.ESCAPE)),
        GameKeymappingSave(GameActionId.Pause, arrayListOf(Input.Keys.SPACE)),
        GameKeymappingSave(GameActionId.GoUp, arrayListOf(Input.Keys.Z, Input.Keys.W, Input.Keys.UP)),
        GameKeymappingSave(GameActionId.GoRight, arrayListOf(Input.Keys.D, Input.Keys.RIGHT)),
        GameKeymappingSave(GameActionId.GoDown, arrayListOf(Input.Keys.S, Input.Keys.DOWN)),
        GameKeymappingSave(GameActionId.GoLeft, arrayListOf(Input.Keys.Q, Input.Keys.A, Input.Keys.LEFT)),
    )

    fun defaultGeneral() = arrayListOf(
        GeneralKeymappingSave(GeneralActionId.StartGame, arrayListOf(Input.Keys.ENTER)),
    )

    fun defaultKeymappings() = KeymappingsSave(
        version = 0,
        game = defaultGame(),
        general = defaultGeneral(),
    )
}
