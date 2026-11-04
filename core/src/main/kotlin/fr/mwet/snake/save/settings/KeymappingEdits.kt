package fr.mwet.snake.save.settings

import fr.mwet.snake.inputs.GameCommand
import fr.mwet.snake.inputs.GeneralCommand

// Each action has KEYS_PER_ACTION slots. Putting a key in a slot takes it away from every other action.
const val KEYS_PER_ACTION = 2

fun Keymappings.setGameKey(action: GameCommand, slot: Int, key: Int) = Keymappings(
    game = game.map { if (it.action == action) it.copy(keys = it.keys.withKeyAt(slot, key)) else it.copy(keys = it.keys - key) },
    general = general.map { it.copy(keys = it.keys - key) },
)

fun Keymappings.removeGameKey(action: GameCommand, slot: Int) = Keymappings(
    game = game.map { if (it.action == action) it.copy(keys = it.keys.withoutKeyAt(slot)) else it },
    general = general,
)

fun Keymappings.setGeneralKey(action: GeneralCommand, slot: Int, key: Int) = Keymappings(
    game = game.map { it.copy(keys = it.keys - key) },
    general = general.map { if (it.action == action) it.copy(keys = it.keys.withKeyAt(slot, key)) else it.copy(keys = it.keys - key) },
)

fun Keymappings.removeGeneralKey(action: GeneralCommand, slot: Int) = Keymappings(
    game = game,
    general = general.map { if (it.action == action) it.copy(keys = it.keys.withoutKeyAt(slot)) else it },
)

// A slot past the last key appends the key. If the key is already in the other slot, it stays only once.
private fun List<Int>.withKeyAt(slot: Int, key: Int): List<Int> =
    (if (slot < size) mapIndexed { index, old -> if (index == slot) key else old } else this + key).distinct()

private fun List<Int>.withoutKeyAt(slot: Int): List<Int> = filterIndexed { index, _ -> index != slot }
