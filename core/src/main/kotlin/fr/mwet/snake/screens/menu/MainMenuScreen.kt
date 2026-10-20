package fr.mwet.snake.screens.menu

import fr.mwet.snake.Navigator
import fr.mwet.snake.events.MenuEvent
import fr.mwet.snake.events.MenuEvent.*
import fr.mwet.snake.events.MenuEventBus
import fr.mwet.snake.events.MenuEventListener
import fr.mwet.snake.inputs.GeneralCommandContext
import fr.mwet.snake.inputs.GeneralInputProcessor
import fr.mwet.snake.render.Background
import fr.mwet.snake.render.WorldLayer
import fr.mwet.snake.save.metadata.MetadataRepository
import fr.mwet.snake.save.settings.SettingsRepository
import fr.mwet.snake.ui.UiKit
import fr.mwet.snake.ui.UiLayer
import fr.mwet.snake.utils.WORLD_HEIGHT
import fr.mwet.snake.utils.WORLD_WIDTH
import ktx.app.KtxScreen

class MainMenuScreen(
    private val kit: UiKit,
    menuEventBus: MenuEventBus,
    private val metadataRepository: MetadataRepository,
    settingsRepository: SettingsRepository,
    navigator: Navigator,
) : MenuEventListener, KtxScreen {
    private val worldLayer = WorldLayer(kit.batch, kit.gameViewport)
    private val uiLayer = UiLayer(kit.batch)
    private val background = Background(kit.textures)
    private val ui = MainMenuUi(kit, uiLayer.stage, menuEventBus, navigator, settingsRepository)
    private val menuInput = GeneralInputProcessor(
        kit.settingsRepository, GeneralCommandContext(
            onStartGame = {
                menuEventBus.emit(PlayGameClicked)
                true
            },
        )
    )

    override fun show() {
        kit.input.add(menuInput, uiLayer.stage)
        ui.onShow(metadataRepository.metadata.highestScore.score)
    }

    override fun hide() {
        kit.input.remove(menuInput, uiLayer.stage)
        ui.onHide()
    }

    override fun resize(width: Int, height: Int) {
        worldLayer.resize(width, height)
        uiLayer.resize(width, height)
        ui.layout(boardWidth = minOf(width.toFloat(), height * WORLD_WIDTH / WORLD_HEIGHT) * uiLayer.unitsPerPixel)
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
