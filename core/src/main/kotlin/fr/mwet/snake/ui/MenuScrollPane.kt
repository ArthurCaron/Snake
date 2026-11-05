package fr.mwet.snake.ui

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane

// Reserve the scrollbar width before the first layout, when ScrollPane does not yet know whether it will scroll.
class MenuScrollPane(content: Actor, style: ScrollPaneStyle) : ScrollPane(content, style) {
    override fun getPrefWidth(): Float {
        val reserved = maxOf(style.vScroll?.minWidth ?: 0f, style.vScrollKnob?.minWidth ?: 0f)
        return super.getPrefWidth() + if (isScrollY) 0f else reserved
    }
}
