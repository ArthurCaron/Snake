package fr.mwet.snake.inputs

import fr.mwet.snake.save.settings.SettingsRepository
import ktx.app.KtxInputAdapter

class GameInputProcessor(
    private val settingsRepository: SettingsRepository,
    private val context: GameCommandContext
) : KtxInputAdapter {
    private var mappingVersion = -1
    private var keycodeMapping: Map<Int, List<GameCommand>> = emptyMap()

    override fun keyDown(keycode: Int): Boolean {
        if (mappingVersion != settingsRepository.version) {
            updateKeycodeMapping()
        }

        keycodeMapping[keycode]?.let { commands ->
            commands.forEach { it.execute(context) }
            return true
        }

        return false
    }

    private fun updateKeycodeMapping() {
        keycodeMapping = settingsRepository.settings.keymappings.game
            .flatMap { keymapping -> keymapping.keys.map { key -> key to keymapping.action } }
            .groupBy({ it.first }, { it.second })
        mappingVersion = settingsRepository.version
    }
}
