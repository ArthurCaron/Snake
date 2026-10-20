package fr.mwet.snake.inputs

import fr.mwet.snake.save.settings.SettingsRepository
import ktx.app.KtxInputAdapter

class GeneralInputProcessor(private val settingsRepository: SettingsRepository, private val context: GeneralCommandContext) :
    KtxInputAdapter {
    private var mappingVersion = -1
    private var keycodeMapping: Map<Int, List<GeneralCommand>> = emptyMap()

    override fun keyDown(keycode: Int): Boolean {
        if (mappingVersion != settingsRepository.version) {
            updateKeycodeMapping()
        }

        keycodeMapping[keycode]?.forEach { command ->
            if (command.execute(context)) return true
        }
        return false
    }

    private fun updateKeycodeMapping() {
        keycodeMapping = settingsRepository.settings.keymappings.general
            .flatMap { keymapping -> keymapping.keys.map { key -> key to keymapping.action } }
            .groupBy({ it.first }, { it.second })
        mappingVersion = settingsRepository.version
    }
}
