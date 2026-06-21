package fr.mwet.snake.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Interpolation.elasticOut
import com.badlogic.gdx.math.Interpolation.linear
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import fr.mwet.snake.DI
import fr.mwet.snake.Game
import fr.mwet.snake.GameViewport
import fr.mwet.snake.StageViewport
import fr.mwet.snake.assets.TextureHandler
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
import ktx.scene2d.image
import ktx.scene2d.scene2d

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class MainMenuScreen(
    menuEventBus: MenuEventBus,
    textureHandler: TextureHandler,
    inputMultiplexer: InputMultiplexer,
    private val batch: SpriteBatch,
    private val gameViewport: GameViewport,
    private val gameCamera: OrthographicCamera,
) : MenuEventListener, KtxScreen, DisposableRegistry by DisposableContainer() {
    private val stageViewport = StageViewport().also {
        Game.addViewport(it)
    }
    private val stage = stage(batch, stageViewport).also {
        inputMultiplexer.addProcessor(it)
    }
    private val backgroundTexture = textureHandler.background
    private val mainMenuTitle = MainMenuTitle(textureHandler, gameViewport)
    private val playButton = PlayButton(textureHandler, gameViewport, menuEventBus)

    override fun show() {
        DI.registerGeneralInputProcessor()

        mainMenuTitle.startAnimation()
        playButton.startAnimation()
    }

    override fun hide() {
        DI.unRegisterGeneralInputProcessor()

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
            it.draw(backgroundTexture, 0f, 0f, WORLD_WIDTH, WORLD_HEIGHT)
        }

        stageViewport.apply(true)
        stage.act(delta)
        stage.draw()
    }

    override fun onEvent(event: MenuEvent) {
        when (event) {
            MenuEvent.PlayGameClicked -> {
                playButton.startGame()
            }
        }
    }
}

private class MainMenuTitle(
    textureHandler: TextureHandler,
    private val gameViewport: GameViewport,
) {
    val actor: Image = scene2d.image(textureHandler.gameTitle) {
        val defaultRatio = height / width
        width = gameViewport.screenWidth * 0.70f
        height = width * defaultRatio
        setOrigin(Align.center)
    }

    fun resetPosition() {
        actor.setPosition(
            (Gdx.graphics.width - actor.width) * 0.5f,
            (Gdx.graphics.height - actor.height) * 0.75f
        )
    }

    fun startAnimation() {
        actor.setScale(0.001f)
        actor.addAction(
            sequence(
                delay(0.5f),
                scaleTo(1f, 1f, 1.25f, elasticOut),
            )
        )
    }

    fun resetAnimation() {
        actor.clearActions()
    }
}

private class PlayButton(
    textureHandler: TextureHandler,
    private val gameViewport: GameViewport,
    menuEventBus: MenuEventBus,
) {
    private var clicked = false
    private val up = TextureRegionDrawable(textureHandler.playBtn)
    private val down = TextureRegionDrawable(textureHandler.playBtnDown)

    private var hoverAction: Action? = null
    val actor: Image = scene2d.image(up) {
        val defaultRatio = height / width
        width = gameViewport.screenWidth * 0.95f * 0.5f
        height = width * defaultRatio
        setOrigin(Align.center)
    }.apply {
        val replaceHoverAction = { action: Action ->
            hoverAction?.let { removeAction(it) }
            hoverAction = action
            addAction(action)
        }

        onClick { menuEventBus.emit(MenuEvent.PlayGameClicked) }
        onEnter { replaceHoverAction(scaleTo(1.25f, 1.25f, 0.3f, elasticOut)) }
        onExit {
            if (!clicked) {
                replaceHoverAction(scaleTo(1f, 1f, 0.5f, elasticOut))
            }
        }
    }

    fun startGame() {
        if (clicked) return

        clicked = true
        actor.drawable = down

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
        actor.setColor(1f, 1f, 1f, 0f)
        actor.addAction(
            parallel(
                delay(0.75f, fadeIn(0.35f)),
                forever(
                    sequence(
                        moveBy(0f, 5f, 1f, linear),
                        moveBy(0f, -5f, 1f, linear)
                    )
                )
            )
        )
    }

    fun resetAnimation() {
        actor.clearActions()
        clicked = false
        hoverAction = null
        actor.drawable = up
        actor.setScale(1f)
        actor.setColor(1f, 1f, 1f, 0f)
    }
}
