package fr.mwet.snake.screens.controls

import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.scenes.scene2d.ui.Label
import fr.mwet.snake.Navigator
import fr.mwet.snake.assets.I18N
import fr.mwet.snake.inputs.GameActionId
import fr.mwet.snake.inputs.GeneralActionId
import fr.mwet.snake.inputs.KeyNames
import fr.mwet.snake.save.settings.Keymappings
import fr.mwet.snake.save.settings.SettingsRepository
import fr.mwet.snake.save.settings.rebindGame
import fr.mwet.snake.save.settings.rebindGeneral
import fr.mwet.snake.ui.MenuPage
import fr.mwet.snake.ui.UiKit
import fr.mwet.snake.ui.textButton
import ktx.app.KtxScreen

private const val NO_KEY = "-"
private const val KEYS_COLUMN_MIN_WIDTH = 240f

class ControlsScreen(
    private val kit: UiKit,
    private val settingsRepository: SettingsRepository,
    private val keyNames: KeyNames,
    navigator: Navigator,
) : KtxScreen {
    private val page = MenuPage(kit, I18N.controls(), onBack = navigator::goToSettings)
    private val rows = listOf(
        gameRow(GameActionId.GoUp, I18N.actionGoUp()),
        gameRow(GameActionId.GoRight, I18N.actionGoRight()),
        gameRow(GameActionId.GoDown, I18N.actionGoDown()),
        gameRow(GameActionId.GoLeft, I18N.actionGoLeft()),
        gameRow(GameActionId.Pause, I18N.actionPause()),
        gameRow(GameActionId.GoBackToMainMenu, I18N.actionBack()),
        generalRow(GeneralActionId.StartGame, I18N.actionStartGame()),
    )
    private val keyCapture = KeyCapture()
    private var capturing: ActionRow? = null

    init {
        val content = page.content
        rows.forEach { row ->
            content.add(Label(row.title, kit.styles.label)).left().padRight(24f)
            content.add(row.keysLabel).left().minWidth(KEYS_COLUMN_MIN_WIDTH)
            content.add(kit.styles.textButton(I18N.change()) { startCapture(row) })
            content.row()
        }
        content.add(kit.styles.textButton(I18N.resetControls()) {
            stopCapture()
            settingsRepository.resetKeymappings()
            refresh()
        }).colspan(3).padTop(12f)
    }

    override fun show() {
        page.show()
        refresh()
    }

    override fun hide() {
        stopCapture()
        page.hide()
    }

    override fun resize(width: Int, height: Int) = page.resize(width, height)

    override fun render(delta: Float) = page.render(delta)

    override fun dispose() = page.dispose()

    private fun startCapture(row: ActionRow) {
        if (capturing == null) kit.input.addFirst(keyCapture)
        capturing = row
        refresh()
    }

    private fun stopCapture() {
        if (capturing == null) return
        kit.input.remove(keyCapture)
        capturing = null
        refresh()
    }

    private fun bind(key: Int) {
        val row = capturing ?: return
        settingsRepository.updateKeymappings { row.rebind(it, key) }
        stopCapture()
    }

    private fun refresh() {
        val keymappings = settingsRepository.settings.keymappings
        rows.forEach { row ->
            row.keysLabel.setText(if (row == capturing) I18N.pressAKey() else keyNames(row.keys(keymappings)))
        }
    }

    private fun keyNames(keys: List<Int>) =
        if (keys.isEmpty()) NO_KEY else keys.joinToString(", ") { keyNames.nameOf(it) ?: "?" }

    private fun gameRow(id: GameActionId, title: String) = ActionRow(
        title = title,
        keys = { keymappings -> keymappings.game.firstOrNull { it.action == id.gameCommand }?.keys.orEmpty() },
        rebind = { keymappings, key -> keymappings.rebindGame(id.gameCommand, key) },
    )

    private fun generalRow(id: GeneralActionId, title: String) = ActionRow(
        title = title,
        keys = { keymappings -> keymappings.general.firstOrNull { it.action == id.generalCommand }?.keys.orEmpty() },
        rebind = { keymappings, key -> keymappings.rebindGeneral(id.generalCommand, key) },
    )

    private inner class ActionRow(
        val title: String,
        val keys: (Keymappings) -> List<Int>,
        val rebind: (Keymappings, Int) -> Keymappings,
    ) {
        val keysLabel = Label("", kit.styles.label)
    }

    private inner class KeyCapture : InputAdapter() {
        override fun keyDown(keycode: Int): Boolean {
            if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) stopCapture() else bind(keycode)
            return true
        }
    }
}
