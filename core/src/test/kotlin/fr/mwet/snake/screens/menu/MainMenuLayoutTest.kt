package fr.mwet.snake.screens.menu

import com.badlogic.gdx.Files
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.headless.HeadlessFiles
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import fr.mwet.snake.ui.UiMetrics.MENU_ACTIONS_SCALE
import fr.mwet.snake.ui.UiMetrics.MENU_BEST_SCORE_GAP
import fr.mwet.snake.ui.UiMetrics.MENU_TITLE_GAP
import fr.mwet.snake.ui.UiMetrics.SCREEN_MARGIN
import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.AfterTest
import kotlin.test.assertEquals

class MainMenuLayoutTest {
    private var previousFiles: Files? = null

    @BeforeTest
    fun setUp() { previousFiles = Gdx.files; Gdx.files = HeadlessFiles() }

    @AfterTest
    fun tearDown() { Gdx.files = previousFiles }

    @Test
    fun `only the actions grow and move while the title keeps its original bounds`() {
        for (boardHeight in listOf(1125f, 1406.25f)) {
            for (bottomMargin in listOf(0f, 120f)) {
                val originalTitle = FixedWidget(600f, 180f)
                val original = Table().apply {
                    add(originalTitle).padBottom(MENU_TITLE_GAP).row()
                    add(FixedWidget(320f, 100f)).padBottom(SCREEN_MARGIN).row()
                    add(FixedWidget(200f, 40f)).padBottom(MENU_BEST_SCORE_GAP).row()
                    add(FixedWidget(200f, 70f))
                    setSize(1125f, boardHeight)
                    validate()
                }
                val title = FixedWidget(600f, 180f)
                val actions = Table().apply {
                    add(FixedWidget(320f, 100f)).padBottom(SCREEN_MARGIN).row()
                    add(FixedWidget(200f, 40f)).padBottom(MENU_BEST_SCORE_GAP).row()
                    add(FixedWidget(200f, 70f))
                }
                val layout = MainMenuLayout(title, actions)
                Group().apply {
                    setBounds(0f, bottomMargin, original.width, original.height)
                    addActor(layout)
                }
                layout.validate()

                assertEquals(originalTitle.x, title.x)
                assertEquals(originalTitle.y, title.y)
                assertEquals(originalTitle.width, title.width)
                assertEquals(originalTitle.height, title.height)
                assertEquals(MENU_ACTIONS_SCALE, actions.scaleX)
                assertEquals(MENU_ACTIONS_SCALE, actions.scaleY)
                assertEquals((title.y - bottomMargin) / 2, actions.y + actions.height / 2)
                assertEquals(layout.width / 2, actions.x + actions.width / 2)
            }
        }
    }

    private class FixedWidget(private val preferredWidth: Float, private val preferredHeight: Float) : Widget() {
        override fun getPrefWidth() = preferredWidth
        override fun getPrefHeight() = preferredHeight
    }
}
