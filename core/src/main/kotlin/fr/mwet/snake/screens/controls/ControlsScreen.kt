package fr.mwet.snake.screens.controls

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Stack
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Align
import fr.mwet.snake.Navigator
import fr.mwet.snake.generated.assets.I18N
import fr.mwet.snake.inputs.BackCommand
import fr.mwet.snake.inputs.GameActionId
import fr.mwet.snake.inputs.GeneralActionId
import fr.mwet.snake.inputs.KeyNames
import fr.mwet.snake.save.settings.KEYS_PER_ACTION
import fr.mwet.snake.save.settings.Keymappings
import fr.mwet.snake.save.settings.SettingsRepository
import fr.mwet.snake.save.settings.removeGameKey
import fr.mwet.snake.save.settings.removeGeneralKey
import fr.mwet.snake.save.settings.setGameKey
import fr.mwet.snake.save.settings.setGeneralKey
import fr.mwet.snake.ui.MenuPage
import fr.mwet.snake.ui.UiKit
import fr.mwet.snake.ui.UiMetrics.CONTROLS_LABEL_GAP
import fr.mwet.snake.ui.UiMetrics.CONTROLS_RESET_GAP
import fr.mwet.snake.ui.UiMetrics.KEY_COLUMN_GAP
import fr.mwet.snake.ui.UiStyle
import ktx.app.KtxScreen
import ktx.scene2d.actor
import ktx.scene2d.label
import ktx.scene2d.scene2d
import ktx.scene2d.table

private const val NO_KEY = "-"
private const val CAPTURING = "?"
private const val MODIFIED = "*"

// A table with one row per action and two columns of keys. A click on a key waits for a new key: the key replaces it,
// Backspace removes it, Esc cancels.
class ControlsScreen(
    private val kit: UiKit,
    private val settingsRepository: SettingsRepository,
    private val keyNames: KeyNames,
    navigator: Navigator,
) : KtxScreen {
    private val page = MenuPage(kit, I18N.controls(), onBack = navigator::goToSettings)
    private val actionRows = listOf(
        gameRow(GameActionId.GoUp, I18N.actionGoUp()),
        gameRow(GameActionId.GoLeft, I18N.actionGoLeft()),
        gameRow(GameActionId.GoDown, I18N.actionGoDown()),
        gameRow(GameActionId.GoRight, I18N.actionGoRight()),
        generalRow(GeneralActionId.Pause, I18N.actionPause()),
        generalRow(GeneralActionId.Back, I18N.actionBack()),
        generalRow(GeneralActionId.StartGame, I18N.actionStartGame()),
    )
    private val keyCapture = KeyCapture()
    private var capturing: ActionRow? = null
    private var capturingSlot = 0

    // Both hints share one place, as high as the longer one: the table does not move when the hint changes.
    private val clickHint = hintLabel(I18N.controlsHint())
    private val captureHint = hintLabel(I18N.pressAKey())

    init {
        page.content.apply {
            actionRows.forEach { row ->
                // The star keeps its place while hidden, so the columns do not move when an action changes.
                actor(scene2d.table { actor(row.title); actor(row.star) }) { it.left().padRight(CONTROLS_LABEL_GAP) }
                row.slots.forEachIndexed { slot, button ->
                    // uniformX gives both columns the width of the longest key name.
                    actor(button) { it.uniformX().fillX().padLeft(if (slot == 0) 0f else KEY_COLUMN_GAP) }
                }
                row()
            }
            actor(Stack(clickHint, captureHint)) { it.colspan(KEYS_PER_ACTION + 1).growX().padTop(CONTROLS_RESET_GAP).row() }
            actor(kit.widgets.menuButton(I18N.resetControls(), onClick = ::resetControls)) { it.colspan(KEYS_PER_ACTION + 1) }
        }
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

    private fun resetControls() {
        stopCapture()
        settingsRepository.resetKeymappings()
        refresh()
    }

    private fun startCapture(row: ActionRow, slot: Int) {
        if (capturing == null) kit.input.addFirst(keyCapture)
        capturing = row
        capturingSlot = slot
        refresh()
    }

    private fun stopCapture() {
        if (capturing == null) return
        kit.input.remove(keyCapture)
        capturing = null
    }

    private fun refresh() {
        val keymappings = settingsRepository.settings.keymappings
        val defaults = settingsRepository.defaultKeymappings()
        actionRows.forEach { row ->
            val keys = row.keys(keymappings)
            row.star.isVisible = keys != row.keys(defaults)
            row.slots.forEachIndexed { slot, button ->
                val isCapturing = row == capturing && slot == capturingSlot
                button.isChecked = isCapturing
                button.setText(if (isCapturing) CAPTURING else keys.getOrNull(slot)?.let { keyNames.nameOf(it) } ?: NO_KEY)
            }
        }
        clickHint.isVisible = capturing == null
        captureHint.isVisible = capturing != null
    }

    private fun hintLabel(text: String) = scene2d.label(text) {
        wrap = true
        setAlignment(Align.center)
    }

    private fun gameRow(id: GameActionId, name: String) = ActionRow(
        name = name,
        keys = { keymappings -> keymappings.game.firstOrNull { it.action == id.gameCommand }?.keys.orEmpty() },
        setKey = { keymappings, slot, key -> keymappings.setGameKey(id.gameCommand, slot, key) },
        removeKey = { keymappings, slot -> keymappings.removeGameKey(id.gameCommand, slot) },
    )

    private fun generalRow(id: GeneralActionId, name: String) = ActionRow(
        name = name,
        keys = { keymappings -> keymappings.general.firstOrNull { it.action == id.generalCommand }?.keys.orEmpty() },
        setKey = { keymappings, slot, key -> keymappings.setGeneralKey(id.generalCommand, slot, key) },
        removeKey = { keymappings, slot -> keymappings.removeGeneralKey(id.generalCommand, slot) },
    )

    private inner class ActionRow(
        val name: String,
        val keys: (Keymappings) -> List<Int>,
        val setKey: (Keymappings, Int, Int) -> Keymappings,
        val removeKey: (Keymappings, Int) -> Keymappings,
    ) {
        val title: Label = scene2d.label(name)
        val star: Label = scene2d.label(MODIFIED)
        val slots: List<TextButton> = List(KEYS_PER_ACTION) { slot ->
            kit.widgets.menuButton(NO_KEY, onClick = { startCapture(this, slot) }, style = UiStyle.KEY)
        }
    }

    private inner class KeyCapture : InputAdapter() {
        override fun keyDown(keycode: Int): Boolean {
            val row = capturing ?: return false
            val slot = capturingSlot
            val backKeys = settingsRepository.settings.keymappings.general
                .firstOrNull { it.action == BackCommand }?.keys.orEmpty()
            when {
                keycode == Keys.BACKSPACE -> settingsRepository.updateKeymappings { row.removeKey(it, slot) }
                keycode == Keys.ESCAPE || keycode in backKeys -> {}
                else -> settingsRepository.updateKeymappings { row.setKey(it, slot, keycode) }
            }
            stopCapture()
            refresh()
            return true
        }
    }
}
