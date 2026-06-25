package fr.mwet.snake.save.game

import fr.mwet.snake.save.serialization.JsonStore

private const val ROOT_FOLDER = "game"

class GameSaveRepository(private val jsonStore: JsonStore)
