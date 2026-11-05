package fr.mwet.snake.screens.menu

import fr.mwet.snake.LanguageSwitcher
import fr.mwet.snake.Navigator
import fr.mwet.snake.events.MenuEvent
import fr.mwet.snake.events.MenuEvent.*
import fr.mwet.snake.events.MenuEventBus
import fr.mwet.snake.events.MenuEventListener
import fr.mwet.snake.inputs.DebugInputProcessor
import fr.mwet.snake.inputs.GeneralCommandContext
import fr.mwet.snake.inputs.GeneralInputProcessor
import fr.mwet.snake.render.WorldLayer
import fr.mwet.snake.save.metadata.MetadataRepository
import fr.mwet.snake.save.settings.SettingsRepository
import fr.mwet.snake.ui.UiKit
import fr.mwet.snake.ui.UiLayer
import ktx.app.KtxScreen

class MainMenuScreen(
    private val kit: UiKit,
    menuEventBus: MenuEventBus,
    private val metadataRepository: MetadataRepository,
    settingsRepository: SettingsRepository,
    languages: LanguageSwitcher,
    navigator: Navigator,
) : MenuEventListener, KtxScreen {
    private val worldLayer = WorldLayer(kit.batch, kit.gameViewport)
    private val uiLayer = UiLayer(kit.batch)
    private val ui = MainMenuUi(uiLayer.board, menuEventBus, navigator, settingsRepository, languages, kit.widgets)
    private val menuInput = GeneralInputProcessor(
        kit.settingsRepository, GeneralCommandContext(
            onStartGame = {
                kit.widgets.activateButtonAction { menuEventBus.emit(PlayGameClicked) }
                true
            },
        )
    )

    private val debugInput = DebugInputProcessor(navigator::goToDevOptions)

    override fun show() {
        kit.input.addFirst(debugInput)
        kit.input.add(menuInput, uiLayer.stage)
        ui.onShow(metadataRepository.metadata.highestScore.score)
    }

    override fun hide() {
        kit.input.remove(debugInput, menuInput, uiLayer.stage)
        ui.onHide()
    }

    override fun resize(width: Int, height: Int) {
        worldLayer.resize(width, height)
        uiLayer.resize(width, height)
        uiLayer.fitBoard(kit.gameViewport)
        ui.layout(boardWidth = uiLayer.board.width)
    }

    override fun render(delta: Float) {
        worldLayer.render(kit.backdrop, delta)
        uiLayer.render(delta)
    }

    override fun dispose() {
        uiLayer.dispose()
    }

    override fun onEvent(event: MenuEvent) {
        when (event) {
            PlayGameClicked -> ui.startGame()
            LoadingFinished, SoundToggled, MusicToggled, ButtonActivated -> {}
        }
    }
}
