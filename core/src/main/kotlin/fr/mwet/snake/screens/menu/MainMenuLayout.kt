package fr.mwet.snake.screens.menu

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.badlogic.gdx.utils.Align
import fr.mwet.snake.ui.UiMetrics.MENU_ACTIONS_SCALE
import fr.mwet.snake.ui.UiMetrics.MENU_TITLE_GAP

internal class MainMenuLayout(private val title: Actor, private val actions: Table) : Table() {
    init {
        setFillParent(true)
        // Keep the original column's dimensions so its title retains the same position.
        add(title).padBottom(MENU_TITLE_GAP).row()
        add(object : Widget() {
            init { touchable = Touchable.disabled }
            override fun getPrefWidth() = this@MainMenuLayout.actions.prefWidth
            override fun getPrefHeight() = this@MainMenuLayout.actions.prefHeight
        })
        actions.setTransform(true)
        addActor(actions)
    }

    override fun layout() {
        super.layout()
        actions.setSize(actions.prefWidth, actions.prefHeight)
        actions.setOrigin(Align.center)
        actions.setScale(MENU_ACTIONS_SCALE)
        val screenBottom = -(parent?.y ?: 0f)
        actions.setPosition((width - actions.width) / 2, (title.y + screenBottom - actions.height) / 2)
    }
}
