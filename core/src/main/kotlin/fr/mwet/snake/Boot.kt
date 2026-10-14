package fr.mwet.snake

import com.badlogic.gdx.Application.ApplicationType.WebGL
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import fr.mwet.snake.events.GameEventBus
import fr.mwet.snake.events.GameEventBusImpl
import fr.mwet.snake.events.MenuEventBus
import fr.mwet.snake.events.MenuEventBusImpl
import fr.mwet.snake.inputs.InputRouter
import fr.mwet.snake.save.metadata.MetadataRepository
import fr.mwet.snake.save.serialization.FileTextStore
import fr.mwet.snake.save.serialization.JsonStore
import fr.mwet.snake.save.serialization.PreferencesTextStore
import fr.mwet.snake.save.serialization.TextStore
import fr.mwet.snake.save.settings.SettingsRepository
import fr.mwet.snake.utils.WORLD_HEIGHT
import fr.mwet.snake.utils.WORLD_WIDTH
import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry

class Boot : DisposableRegistry by DisposableContainer() {
    val gameEvents: GameEventBus = GameEventBusImpl()
    val menuEvents: MenuEventBus = MenuEventBusImpl()

    private val jsonStore = JsonStore(textStore())
    val settingsRepository = SettingsRepository(jsonStore)
    val metadataRepository = MetadataRepository(jsonStore)

    val input = InputRouter()
    val batch = SpriteBatch().alsoRegister()
    val gameViewport = GameViewport(OrthographicCamera(WORLD_WIDTH, WORLD_HEIGHT))

    val assetLoading = AssetLoading().alsoRegister()
}

private fun textStore(): TextStore = if (Gdx.app.type == WebGL) {
    PreferencesTextStore(Gdx.app.getPreferences("fr.mwet.snek.save_data"))
} else {
    FileTextStore(Gdx.files.local("save_data"))
}
