package fr.mwet.snake.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import fr.mwet.snake.DI
import fr.mwet.snake.GameViewport
import fr.mwet.snake.Game
import fr.mwet.snake.StageViewport
import fr.mwet.snake.assets.AssetHandler
import fr.mwet.snake.utils.SoundHelper
import fr.mwet.snake.utils.WORLD_HEIGHT
import fr.mwet.snake.utils.WORLD_WIDTH
import ktx.app.KtxScreen
import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry
import ktx.graphics.use
import ktx.scene2d.image
import ktx.scene2d.scene2d

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class MainMenuScreen : KtxScreen, DisposableRegistry by DisposableContainer() {
    private val game = DI.inject<Game>()
    private val stage = DI.inject<Stage>()
    private val batch = DI.inject<SpriteBatch>()
    private val gameViewport = DI.inject<GameViewport>()
    private val stageViewport = DI.inject<StageViewport>()
    private val gameCamera = DI.inject<OrthographicCamera>()
    private val assetHandler = DI.inject<AssetHandler>()
    private val soundHelper = DI.inject<SoundHelper>()

    private val gameTitle: Image by lazy {
        scene2d.image(assetHandler.gameTitle) {
            width = gameViewport.screenWidth * 0.95f
            height = width * height / width
            setOrigin(Align.center)
        }
    }

    private val playBtn: ImageButton by lazy {
        ImageButton(
            TextureRegionDrawable(assetHandler.playBtn),
            TextureRegionDrawable(assetHandler.playBtnDown)
        ).apply {
            val playBtnRatio = width / height
            val playBtnWidth = gameViewport.screenWidth * 0.95f * 0.5f
            val playBtnHeight = playBtnWidth / playBtnRatio
            imageCell.width(playBtnWidth).height(playBtnHeight)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent, x: Float, y: Float) {
                    super.clicked(event, x, y)
                    soundHelper.playSwitch()
                    game.setScreen<GameScreen>()
                }
            })
        }
    }

    override fun show() {
        stage.clear()

        // Game title
        stage.addActor(gameTitle)
        val graphicsWidth: Float = Gdx.graphics.width.toFloat()
        val graphicsHeight: Float = Gdx.graphics.height.toFloat()

        gameTitle.setPosition(
            (graphicsWidth - gameTitle.width) * 0.5f,
            (graphicsHeight - gameTitle.height) * 0.5f + gameTitle.width * 0.25f
        )
        gameTitle.setScale(0.001f)

        // GameTitle transition
        gameTitle.addAction(
            Actions.delay(0.5f, Actions.scaleTo(1f, 1f, 1.25f, Interpolation.elasticOut))
        )

        // Show Play button with floating animation
        stage.addActor(playBtn)
        playBtn.setPosition(
            (graphicsWidth - playBtn.width) * 0.5f,
            (graphicsHeight - gameTitle.height) * 0.5f - playBtn.height
        )
        val moveY = gameTitle.height * 0.1f
        playBtn.setColor(1f, 1f, 1f, 0f)
        playBtn.addAction(Actions.delay(0.75f, Actions.fadeIn(0.35f)))
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
            it.draw(assetHandler.background, 0f, 0f, WORLD_WIDTH, WORLD_HEIGHT)
        }

        // Draw stage actors
        stageViewport.apply(true)
        stage.act(delta)
        stage.draw()
    }
}
