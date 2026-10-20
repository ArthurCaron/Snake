package fr.mwet.snake.screens.controls

import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Cell
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import fr.mwet.snake.Navigator
import fr.mwet.snake.generated.assets.I18N
import fr.mwet.snake.inputs.GameActionId
import fr.mwet.snake.inputs.GeneralActionId
import fr.mwet.snake.inputs.KeyNames
import fr.mwet.snake.save.settings.Keymappings
import fr.mwet.snake.save.settings.SettingsRepository
import fr.mwet.snake.save.settings.addGameKey
import fr.mwet.snake.save.settings.addGeneralKey
import fr.mwet.snake.save.settings.removeGameKey
import fr.mwet.snake.save.settings.removeGeneralKey
import fr.mwet.snake.ui.MenuPage
import fr.mwet.snake.ui.UiKit
import fr.mwet.snake.ui.textButton
import ktx.app.KtxScreen

private const val NO_KEY = "-"
private const val ADD_KEY = "+"
private const val REMOVE_KEY = "x"
private const val REMOVE_KEY_GAP = 8f

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

    private val removeKeyWidth = REMOVE_KEY_GAP + removeKeyButton {}.prefWidth
    private val resetButton = kit.styles.textButton(I18N.resetControls()) {
        stopCapture()
        settingsRepository.resetKeymappings()
        refresh()
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
    }

    private fun bind(key: Int) {
        val row = capturing ?: return
        settingsRepository.updateKeymappings { row.addKey(it, key) }
    }

    private fun remove(row: ActionRow, key: Int) {
        settingsRepository.updateKeymappings { row.removeKey(it, key) }
        refresh()
    }

    private fun refresh() {
        val keymappings = settingsRepository.settings.keymappings
        val keyColumns = rows.maxOf { maxOf(1, it.keys(keymappings).size) }
        val content = page.content
        content.clearChildren()
        rows.forEach { row ->
            val keys = row.keys(keymappings)
            content.add(row.titleLabel).left().padRight(12f)
            when {
                row == capturing -> content.add(Label(I18N.pressAKey(), kit.styles.label)).left().colspan(keyColumns)
                keys.isEmpty() -> content.add(Label(NO_KEY, kit.styles.label)).left().colspan(keyColumns)
                else -> {
                    keys.forEach { key -> keyFrame(row, key).let { content.add(it).left().minWidth(it.prefWidth + removeKeyWidth) } }
                    repeat(keyColumns - keys.size) { content.add() }
                }
            }
            content.add(row.addButton)
            content.row()
        }
        content.add(resetButton).colspan(keyColumns + 2).padTop(12f)
    }

    private fun removeKeyButton(onClick: () -> Unit) = kit.styles.textButton(REMOVE_KEY, onClick).apply { pad(0f, 8f, 0f, 8f) }

    private fun keyFrame(row: ActionRow, key: Int): Table {
        val removeButton = removeKeyButton { remove(row, key) }
        return Table().apply {
            background = kit.styles.panel
            touchable = Touchable.enabled
            pad(4f, 12f, 4f, 12f)
            add(Label(keyNames.nameOf(key) ?: "?", kit.styles.label))
            addListener(RevealOnHover(add(), removeButton))
        }
    }

    private fun gameRow(id: GameActionId, title: String) = ActionRow(
        title = title,
        keys = { keymappings -> keymappings.game.firstOrNull { it.action == id.gameCommand }?.keys.orEmpty() },
        addKey = { keymappings, key -> keymappings.addGameKey(id.gameCommand, key) },
        removeKey = { keymappings, key -> keymappings.removeGameKey(id.gameCommand, key) },
    )

    private fun generalRow(id: GeneralActionId, title: String) = ActionRow(
        title = title,
        keys = { keymappings -> keymappings.general.firstOrNull { it.action == id.generalCommand }?.keys.orEmpty() },
        addKey = { keymappings, key -> keymappings.addGeneralKey(id.generalCommand, key) },
        removeKey = { keymappings, key -> keymappings.removeGeneralKey(id.generalCommand, key) },
    )

    private inner class ActionRow(
        title: String,
        val keys: (Keymappings) -> List<Int>,
        val addKey: (Keymappings, Int) -> Keymappings,
        val removeKey: (Keymappings, Int) -> Keymappings,
    ) {
        val titleLabel = Label(title, kit.styles.label)
        val addButton = kit.styles.textButton(ADD_KEY) { startCapture(this) }
    }

    private inner class KeyCapture : InputAdapter() {
        override fun keyDown(keycode: Int): Boolean {
            if (keycode != Input.Keys.ESCAPE && keycode != Input.Keys.BACK) bind(keycode)
            stopCapture()
            refresh()
            return true
        }
    }
}

private class RevealOnHover(private val cell: Cell<Actor>, private val actor: Actor) : InputListener() {
    override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
        if (pointer != -1 || cell.actor != null) return
        cell.setActor(actor).padLeft(REMOVE_KEY_GAP)
    }

    override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
        if (pointer != -1 || toActor?.isDescendantOf(event.listenerActor) == true) return
        cell.clearActor()
        cell.padLeft(0f)
    }
}
