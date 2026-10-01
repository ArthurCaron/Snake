package fr.mwet.snake.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color.WHITE
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
import fr.mwet.snake.assets.I18N
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.assets.toDrawable
import fr.mwet.snake.events.MenuEvent
import fr.mwet.snake.events.MenuEventBus
import fr.mwet.snake.events.MenuEventListener
import fr.mwet.snake.render.Palette
import fr.mwet.snake.save.metadata.BestScore
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


@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class MainMenuScreen(
    menuEventBus: MenuEventBus,
    fontHandler: FontHandler,
    textureHandler: TextureHandler,
    private val batch: SpriteBatch,
    private val gameViewport: GameViewport,
    private val gameCamera: OrthographicCamera,
    private val bestScore: BestScore,
) : MenuEventListener, KtxScreen, DisposableRegistry by DisposableContainer() {
    private val stageViewport = StageViewport().also {
        Game.addViewport(it)
    }
    private val stage = stage(batch, stageViewport)
    private val backgroundTexture =
        textureHandler.rectangleWithBorderTexture(width = WORLD_WIDTH, height = WORLD_HEIGHT, fill = Palette.background)
    private val styles = Styles(fontHandler, textureHandler)
    private val mainMenuTitle = MainMenuTitle(styles)
    private val playButton = PlayButton(styles, menuEventBus)
    private val bestScoreLabel = Label("", styles.info)

    override fun show() {
        DI.registerGeneralInputProcessor()
        DI.registerInputProcessor(stage)

        mainMenuTitle.startAnimation()
        playButton.startAnimation()
        updateBestScoreLabel()
    }

    private fun updateBestScoreLabel() {
        bestScoreLabel.setText(I18N.best(bestScore.value))
        bestScoreLabel.pack()
    }

    override fun hide() {
        DI.unRegisterGeneralInputProcessor()
        DI.unRegisterInputProcessor(stage)

        mainMenuTitle.resetAnimation()
        playButton.resetAnimation()
    }

    override fun resize(width: Int, height: Int) {
        stageViewport.update(width, height, true)
        val stageWidth = stageViewport.worldWidth
        val stageHeight = stageViewport.worldHeight
        val boardWidth = minOf(width.toFloat(), height * WORLD_WIDTH / WORLD_HEIGHT) * stageViewport.unitsPerPixel

        stage.clear()
        stage.addActor(mainMenuTitle.actor)
        stage.addActor(playButton.actor)
        stage.addActor(bestScoreLabel)

        mainMenuTitle.resetPosition(stageWidth, stageHeight, boardWidth)
        playButton.resetPosition(stageWidth, stageHeight)
        bestScoreLabel.setPosition(
            (stageWidth - bestScoreLabel.width) * 0.5f,
            playButton.actor.y - bestScoreLabel.height - 32f,
        )
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

private class MainMenuTitle(styles: Styles) {
    val actor = Label("Snek", styles.title).apply {
        setAlignment(Align.center)
        setOrigin(Align.center)
    }

    fun resetPosition(stageWidth: Float, stageHeight: Float, boardWidth: Float) {
        actor.setFontScale(1f)
        actor.pack()

        val targetWidth = boardWidth * 0.70f
        if (actor.width > 0f) {
            actor.setFontScale(targetWidth / actor.width)
        }

        actor.pack()
        actor.setOrigin(Align.center)
        actor.setPosition(
            (stageWidth - actor.width) * 0.5f,
            (stageHeight - actor.height) * 0.75f,
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

    val actor = TextButton(I18N.play(), styles.playButton).apply {
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

    fun resetPosition(stageWidth: Float, stageHeight: Float) {
        actor.setPosition(
            (stageWidth - actor.width) * 0.5f,
            (stageHeight - actor.height) * 0.25f
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
        actor.setColor(1f, 1f, 1f, 1f)
    }
}

private class Styles(fontHandler: FontHandler, textureHandler: TextureHandler) {
    val title = Label.LabelStyle(fontHandler.titleFont, Palette.text)
    val info = Label.LabelStyle(fontHandler.uiFont, Palette.text)

    val playButton = TextButton.TextButtonStyle().apply {
        up = textureHandler.rectangleWithBorderTexture(
            width = 256,
            height = 96,
            border = 4,
            fill = Palette.background,
            borderColor = Palette.border,
        ).toDrawable()

        down = textureHandler.rectangleWithBorderTexture(
            width = 256,
            height = 96,
            border = 4,
            fill = Palette.accent,
            borderColor = Palette.border,
        ).toDrawable()

        font = fontHandler.buttonFont
        fontColor = Palette.text
        downFontColor = Palette.text
    }
}
