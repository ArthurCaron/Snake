package fr.mwet.tools.translationpreview

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.Layout
import java.lang.reflect.Modifier
import java.util.Collections
import java.util.IdentityHashMap
import fr.mwet.tools.translationpreview.PreviewTuning.SEARCH_DEPTH
import fr.mwet.tools.translationpreview.PreviewTuning.SETTLE_SECONDS
import fr.mwet.tools.translationpreview.PreviewTuning.SETTLE_STEPS

private const val GAME_PACKAGE = "fr.mwet.snake"

class Occurrence(val screen: String, val stage: Stage, val label: Label, val key: String, val arguments: List<String>) {
    private val position = Vector2()

    val block: Actor get() = label.parent as? TextButton ?: label

    val overflows: Boolean
        get() {
            if (!label.wrap && label.prefWidth > label.width + 0.5f) return true
            label.localToStageCoordinates(position.set(0f, 0f))
            return position.x < 0f || position.x + maxOf(label.width, label.prefWidth) > stage.width
        }

    fun show(text: String) {
        label.setText(text)
        relayout(stage)
    }
}

fun findOccurrences(screen: String, stages: List<Stage>, matcher: TextMatcher): List<Occurrence> =
    stages.flatMap { stage ->
        repeat(SETTLE_STEPS) { stage.act(SETTLE_SECONDS) }
        relayout(stage)
        stage.root.labels().filter { it.isShown() }.mapNotNull { label ->
            matcher.match(label.text.toString())?.let { Occurrence(screen, stage, label, it.key, it.arguments) }
        }
    }

fun relayout(stage: Stage) {
    stage.root.children.forEach { (it as? Layout)?.validate() }
}

private fun Actor.isShown(): Boolean = generateSequence(this) { it.parent }.all { it.isVisible }

private fun Group.labels(): List<Label> = children.flatMap { child ->
    when (child) {
        is Label -> listOf(child)
        is Group -> child.labels()
        else -> emptyList()
    }
}

fun findStages(owner: Any): List<Stage> {
    val stages = mutableListOf<Stage>()
    val visited = Collections.newSetFromMap(IdentityHashMap<Any, Boolean>())

    fun visit(value: Any, depth: Int) {
        if (depth > SEARCH_DEPTH || !visited.add(value)) return
        if (value is Stage) {
            stages += value
            return
        }
        if (!value.javaClass.name.startsWith(GAME_PACKAGE)) return
        generateSequence<Class<*>>(value.javaClass) { it.superclass }.forEach { type ->
            type.declaredFields.filterNot { Modifier.isStatic(it.modifiers) }.forEach { field ->
                field.isAccessible = true
                field.get(value)?.let { visit(it, depth + 1) }
            }
        }
    }

    visit(owner, 0)
    return stages
}
