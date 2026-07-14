package fr.mwet.snake.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Color.WHITE
import com.badlogic.gdx.graphics.Color.valueOf
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Interpolation.elasticOut
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Align
import fr.mwet.snake.DI
import fr.mwet.snake.Game
import fr.mwet.snake.GameViewport
import fr.mwet.snake.StageViewport
import fr.mwet.snake.assets.FontHandler
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.assets.toDrawable
import fr.mwet.snake.events.MenuEvent
import fr.mwet.snake.events.MenuEventBus
import fr.mwet.snake.events.MenuEventListener
import fr.mwet.snake.utils.WORLD_HEIGHT
import fr.mwet.snake.utils.WORLD_WIDTH
import ktx.actors.onClick
import ktx.actors.onEnter
import ktx.actors.onExit
import ktx.actors.stage
import ktx.app.KtxScreen
import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry
import ktx.graphics.use

private val BACKGROUND_COLOR = Color.valueOf("#1f1f1f")

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class MainMenuScreen(
    menuEventBus: MenuEventBus,
    fontHandler: FontHandler,
    textureHandler: TextureHandler,
    private val batch: SpriteBatch,
    private val gameViewport: GameViewport,
    private val gameCamera: OrthographicCamera,
) : MenuEventListener, KtxScreen, DisposableRegistry by DisposableContainer() {
    private val stageViewport = StageViewport().also {
        Game.addViewport(it)
    }
    private val stage = stage(batch, stageViewport)
    private val backgroundTexture =
        textureHandler.rectangleWithBorderTexture(width = WORLD_WIDTH, height = WORLD_HEIGHT, fill = BACKGROUND_COLOR)
    private val styles = Styles(fontHandler, textureHandler)
    private val mainMenuTitle = MainMenuTitle(styles, gameViewport)
    private val playButton = PlayButton(styles, menuEventBus)

    override fun show() {
        DI.registerGeneralInputProcessor()
        DI.registerInputProcessor(stage)

        mainMenuTitle.startAnimation()
        playButton.startAnimation()
    }

    override fun hide() {
        DI.unRegisterGeneralInputProcessor()
        DI.unRegisterInputProcessor(stage)

        mainMenuTitle.resetAnimation()
        playButton.resetAnimation()
    }

    override fun resize(width: Int, height: Int) {
        stage.clear()
        stage.addActor(mainMenuTitle.actor)
        stage.addActor(playButton.actor)

        mainMenuTitle.resetPosition()
        playButton.resetPosition()
    }

    override fun render(delta: Float) {
        gameViewport.apply(true)
        batch.use(gameCamera) {
            it.draw(backgroundTexture, 0f, 0f)
        }

        stageViewport.apply(true)
        stage.act(delta)
        stage.draw()
    }

    override fun onEvent(event: MenuEvent) {
        when (event) {
            MenuEvent.PlayGameClicked -> playButton.startGame()
        }
    }
}

private class MainMenuTitle(styles: Styles, private val gameViewport: GameViewport) {
    val actor = Label("Snek", styles.title).apply {
        setAlignment(Align.center)
        setOrigin(Align.center)
    }

    fun resetPosition() {
        actor.setFontScale(1f)
        actor.pack()

        val targetWidth = gameViewport.screenWidth * 0.70f
        if (actor.width > 0f) {
            actor.setFontScale(targetWidth / actor.width)
        }

        actor.pack()
        actor.setOrigin(Align.center)
        actor.setPosition(
            (Gdx.graphics.width - actor.width) * 0.5f,
            (Gdx.graphics.height - actor.height) * 0.75f,
        )
    }

    fun startAnimation() {
//        actor.setScale(0.001f)
//        actor.addAction(
//            sequence(
//                delay(0.5f),
//                scaleTo(1f, 1f, 1.25f, elasticOut),
//            )
//        )
    }

    fun resetAnimation() {
        actor.clearActions()
    }
}

private class PlayButton(styles: Styles, menuEventBus: MenuEventBus) {
    private var clicked = false
    private var hoverAction: Action? = null

    val actor = TextButton("Play", styles.playButton).apply {
        setOrigin(Align.center)

        val replaceHoverAction = { action: Action ->
            hoverAction?.let { removeAction(it) }
            hoverAction = action
            addAction(action)
        }

        onClick { menuEventBus.emit(MenuEvent.PlayGameClicked) }
        onEnter { replaceHoverAction(scaleTo(1.25f, 1.25f, 0.3f, elasticOut)) }
        onExit {
            if (clicked.not()) {
                replaceHoverAction(scaleTo(1f, 1f, 0.5f, elasticOut))
            }
        }
    }

    fun startGame() {
        if (clicked) return
        clicked = true

        actor.addAction(
            sequence(
                delay(0.3f),
                Actions.run { Game.setScreen<GameScreen>() },
            ),
        )
    }

    fun resetPosition() {
        actor.setPosition(
            (Gdx.graphics.width - actor.width) * 0.5f,
            (Gdx.graphics.height - actor.height) * 0.25f
        )
    }

    fun startAnimation() {
//        actor.setColor(1f, 1f, 1f, 0f)
//        actor.addAction(
//            parallel(
//                delay(0.75f, fadeIn(0.35f)), forever(
//                    sequence(
//                        moveBy(0f, 5f, 1f, linear),
//                        moveBy(0f, -5f, 1f, linear)
//                    )
//                )
//            )
//        )
    }

    fun resetAnimation() {
        actor.clearActions()
        clicked = false
        hoverAction = null
        actor.setScale(1f)
        actor.setColor(1f, 1f, 1f, 0f)
    }
}

private class Styles(fontHandler: FontHandler, textureHandler: TextureHandler) {
    val title = Label.LabelStyle(fontHandler.titleFont, WHITE)

    val playButton = TextButton.TextButtonStyle().apply {
        up = textureHandler.rectangleWithBorderTexture(
            width = 256,
            height = 96,
            border = 4,
            fill = valueOf("1f1f1f"),
            borderColor = WHITE,
        ).toDrawable()

        down = textureHandler.rectangleWithBorderTexture(
            width = 256,
            height = 96,
            border = 4,
            fill = valueOf("25351f"),
            borderColor = WHITE,
        ).toDrawable()

        font = fontHandler.buttonFont
        fontColor = WHITE
        downFontColor = WHITE
    }
}
