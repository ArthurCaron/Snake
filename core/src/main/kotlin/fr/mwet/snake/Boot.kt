package fr.mwet.snake

import com.badlogic.gdx.Application.ApplicationType.Desktop
import com.badlogic.gdx.Application.ApplicationType.WebGL
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import fr.mwet.snake.inputs.InputRouter
import fr.mwet.snake.save.metadata.MetadataRepository
import fr.mwet.snake.save.serialization.FileTextStore
import fr.mwet.snake.save.serialization.JsonStore
import fr.mwet.snake.save.serialization.PreferencesTextStore
import fr.mwet.snake.save.serialization.TextStore
import fr.mwet.snake.save.settings.SettingsRepository
import fr.mwet.snake.tuning.GameplayTuning.WORLD_HEIGHT
import fr.mwet.snake.tuning.GameplayTuning.WORLD_WIDTH
import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry

class Boot : DisposableRegistry by DisposableContainer() {
    private val jsonStore = JsonStore(textStore())
    val settingsRepository = SettingsRepository(jsonStore, keysArePhysical = Gdx.app.type == Desktop)
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
