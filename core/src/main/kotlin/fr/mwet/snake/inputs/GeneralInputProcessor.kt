package fr.mwet.snake.inputs

import fr.mwet.snake.events.MenuEventBus
import fr.mwet.snake.save.settings.SettingsRepository
import ktx.app.KtxInputAdapter

class GeneralInputProcessor(private val settingsRepository: SettingsRepository, menuEventBus: MenuEventBus) :
    KtxInputAdapter {
    private var mappingVersion = -1
    private var keycodeMapping: Map<Int, List<GeneralCommand>> = emptyMap()
    private val context = GeneralCommandContext(menuEventBus = menuEventBus)

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
        keycodeMapping = settingsRepository.settings.keymappings.general
            .flatMap { keymapping -> keymapping.keys.map { key -> key to keymapping.action } }
            .groupBy({ it.first }, { it.second })
        mappingVersion = settingsRepository.version
    }
}
