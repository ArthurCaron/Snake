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

    fun restoreDefaults(defaults: KeymappingsSave) {
        version = if (version == NO_VERSION) defaults.version else version

        val gameMap = game.associate { it.action to it.keys }.toMutableMap()
        defaults.game.forEach {
            if (gameMap.doesNotContainKey(it.action)) {
                gameMap[it.action] = it.keys
            }
        }
        game = ArrayList(gameMap.map { GameKeymappingSave(it.key, it.value) })

        val generalMap = general.associate { it.action to it.keys }.toMutableMap()
        defaults.general.forEach {
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
    fun defaultGame(keysArePhysical: Boolean) = arrayListOf(
        GameKeymappingSave(GameActionId.GoBackToMainMenu, arrayListOf(Input.Keys.ESCAPE, Input.Keys.BACK)),
        GameKeymappingSave(GameActionId.Pause, arrayListOf(Input.Keys.SPACE)),
        GameKeymappingSave(GameActionId.GoUp, letterKeys(keysArePhysical, qwerty = Input.Keys.W, azerty = Input.Keys.Z) + Input.Keys.UP),
        GameKeymappingSave(GameActionId.GoRight, arrayListOf(Input.Keys.D, Input.Keys.RIGHT)),
        GameKeymappingSave(GameActionId.GoDown, arrayListOf(Input.Keys.S, Input.Keys.DOWN)),
        GameKeymappingSave(GameActionId.GoLeft, letterKeys(keysArePhysical, qwerty = Input.Keys.A, azerty = Input.Keys.Q) + Input.Keys.LEFT),
    )

    fun defaultGeneral() = arrayListOf(
        GeneralKeymappingSave(GeneralActionId.StartGame, arrayListOf(Input.Keys.ENTER)),
    )

    fun defaultKeymappings(keysArePhysical: Boolean) = KeymappingsSave(
        version = 0,
        game = defaultGame(keysArePhysical),
        general = defaultGeneral(),
    )

    private fun letterKeys(keysArePhysical: Boolean, qwerty: Int, azerty: Int): List<Int> =
        if (keysArePhysical) listOf(qwerty) else listOf(qwerty, azerty)
}
