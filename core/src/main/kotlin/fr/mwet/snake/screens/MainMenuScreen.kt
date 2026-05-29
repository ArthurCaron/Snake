package fr.mwet.snake.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import fr.mwet.snake.Game
import fr.mwet.snake.GameViewport
import fr.mwet.snake.StageViewport
import fr.mwet.snake.assets.SoundHandler
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.utils.WORLD_HEIGHT
import fr.mwet.snake.utils.WORLD_WIDTH
import ktx.actors.onClick
import ktx.actors.onEnter
import ktx.actors.onExit
import ktx.app.KtxScreen
import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry
import ktx.graphics.use
import ktx.scene2d.image
import ktx.scene2d.scene2d

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class MainMenuScreen(
    private val textureHandler: TextureHandler,
    private val soundHandler: SoundHandler,
    private val stage: Stage,
    private val batch: SpriteBatch,
    private val gameViewport: GameViewport,
    private val stageViewport: StageViewport,
    private val gameCamera: OrthographicCamera,
) : KtxScreen, DisposableRegistry by DisposableContainer() {
    private var clickedPlayBtn = false
    val playBtnDrawable = TextureRegionDrawable(textureHandler.playBtn)
    val playBtnDownDrawable = TextureRegionDrawable(textureHandler.playBtnDown)

    private val gameTitle: Image = scene2d.image(textureHandler.gameTitle) {
        val defaultRatio = height / width
        width = gameViewport.screenWidth * 0.70f
        height = width * defaultRatio
        setOrigin(Align.center)
    }

    private val playBtn: Image = scene2d.image(playBtnDrawable) {
        val defaultRatio = height / width
        width = gameViewport.screenWidth * 0.95f * 0.5f
        height = width * defaultRatio
        setOrigin(Align.center)
    }.apply {
        onClick {
            clickedPlayBtn = true
            soundHandler.playSwitch()
            playBtn.drawable = playBtnDownDrawable
            playBtn.addAction(Actions.delay(0.3f, Actions.run {
                clickedPlayBtn = false
                playBtn.drawable = playBtnDrawable
                Game.setScreen<GameScreen>()
            }))
        }
        onEnter {
            playBtn.addAction(Actions.scaleTo(1.25f, 1.25f, 0.3f, Interpolation.elasticOut))
        }
        onExit {
            if (!clickedPlayBtn) {
                playBtn.addAction(Actions.scaleTo(1f, 1f, 0.5f, Interpolation.elasticOut))
            }
        }
    }

    override fun show() {
        animateGameTitle()
        animatePlayButton()
    }

    override fun resize(width: Int, height: Int) {
        stage.clear()

        stage.addActor(gameTitle)
        positionGameTitle()

        stage.addActor(playBtn)
        positionPlayButton()
    }

    private fun positionGameTitle() {
        gameTitle.setPosition(
            (Gdx.graphics.width - gameTitle.width) * 0.5f,
            (Gdx.graphics.height - gameTitle.height) * 0.75f
        )
    }

    private fun positionPlayButton() {
        playBtn.setPosition(
            (Gdx.graphics.width - playBtn.width) * 0.5f,
            (Gdx.graphics.height - playBtn.height) * 0.25f
        )
    }

    private fun animateGameTitle() {
        gameTitle.setScale(0.001f)
        gameTitle.addAction(
            Actions.delay(0.5f, Actions.scaleTo(1f, 1f, 1.25f, Interpolation.elasticOut))
        )
    }

    private fun animatePlayButton() {
        playBtn.setColor(1f, 1f, 1f, 0f)
        playBtn.addAction(Actions.delay(0.75f, Actions.fadeIn(0.35f)))
        val moveY = gameTitle.height * 0.1f
        playBtn.addAction(
            Actions.forever(
                Actions.sequence(
                    Actions.moveBy(0f, moveY, 1f, Interpolation.linear),
                    Actions.moveBy(0f, -moveY, 1f, Interpolation.linear)
                )
            )
        )
    }

    override fun render(delta: Float) {
        gameViewport.apply(true)
        batch.use(gameCamera) {
            it.draw(textureHandler.background, 0f, 0f, WORLD_WIDTH, WORLD_HEIGHT)
        }

        // Draw stage actors
        stageViewport.apply(true)
        stage.act(delta)
        stage.draw()
    }
}
