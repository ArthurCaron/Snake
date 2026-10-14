package fr.mwet.snake.screens.menu

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import fr.mwet.snake.GameViewport
import fr.mwet.snake.Navigator
import fr.mwet.snake.assets.FontHandler
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.events.MenuEvent
import fr.mwet.snake.events.MenuEvent.*
import fr.mwet.snake.events.MenuEventBus
import fr.mwet.snake.events.MenuEventListener
import fr.mwet.snake.inputs.GeneralInputProcessor
import fr.mwet.snake.inputs.InputRouter
import fr.mwet.snake.render.Background
import fr.mwet.snake.render.WorldLayer
import fr.mwet.snake.save.metadata.MetadataRepository
import fr.mwet.snake.save.settings.SettingsRepository
import fr.mwet.snake.ui.UiLayer
import fr.mwet.snake.utils.WORLD_HEIGHT
import fr.mwet.snake.utils.WORLD_WIDTH
import ktx.app.KtxScreen

class MainMenuScreen(
    menuEventBus: MenuEventBus,
    fontHandler: FontHandler,
    textureHandler: TextureHandler,
    batch: SpriteBatch,
    gameViewport: GameViewport,
    private val metadataRepository: MetadataRepository,
    settingsRepository: SettingsRepository,
    private val input: InputRouter,
    private val menuInput: GeneralInputProcessor,
    navigator: Navigator,
) : MenuEventListener, KtxScreen {
    private val worldLayer = WorldLayer(batch, gameViewport)
    private val uiLayer = UiLayer(batch)
    private val background = Background(textureHandler)
    private val ui = MainMenuUi(fontHandler, textureHandler, uiLayer.stage, menuEventBus, navigator, settingsRepository)

    override fun show() {
        input.add(menuInput, uiLayer.stage)
        ui.onShow(metadataRepository.metadata.highScore.score)
    }

    override fun hide() {
        input.remove(menuInput, uiLayer.stage)
        ui.onHide()
    }

    override fun resize(width: Int, height: Int) {
        worldLayer.resize(width, height)
        uiLayer.resize(width, height)
        val boardWidth = minOf(width.toFloat(), height * WORLD_WIDTH / WORLD_HEIGHT) * uiLayer.unitsPerPixel
        ui.layout(uiLayer.width, uiLayer.height, boardWidth)
    }

    override fun render(delta: Float) {
        worldLayer.render(background, delta)
        uiLayer.render(delta)
    }

    override fun dispose() {
        uiLayer.dispose()
    }

    override fun onEvent(event: MenuEvent) {
        when (event) {
            PlayGameClicked -> ui.startGame()
            LoadingFinished, SoundToggled, MusicToggled -> {}
        }
    }
}
