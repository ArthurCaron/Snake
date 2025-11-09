package fr.mwet.snake.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import fr.mwet.snake.DI
import fr.mwet.snake.Game
import fr.mwet.snake.assets.AssetHandler
import fr.mwet.snake.assets.Progress
import fr.mwet.snake.utils.WORLD_HEIGHT
import fr.mwet.snake.utils.WORLD_WIDTH
import fr.mwet.snake.utils.draw
import fr.mwet.snake.utils.resetColor
import ktx.app.KtxScreen
import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry
import ktx.graphics.use


@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class LoadingScreen : KtxScreen, DisposableRegistry by DisposableContainer() {
    private val assetHandler = DI.inject<AssetHandler>()
    private val game = DI.inject<Game>()
    private val batch = DI.inject<SpriteBatch>()
    private val gameCamera = DI.inject<OrthographicCamera>()

    private lateinit var progressBar: ProgressBar
    private lateinit var loadingText: LoadingText
    private lateinit var loadSegment: Texture
    private lateinit var loadText: Texture

    override fun show() {
        // Create loading segment part, use Pixmap to generate the texture
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.fill()
        loadSegment = Texture(pixmap)
        pixmap.dispose()

        // Precalculated loading bar positions
        val barWidth = 6f
        val barHeight = 0.5f
        progressBar = ProgressBar(
            position = Vector2((WORLD_WIDTH - barWidth) * 0.5f, (WORLD_HEIGHT - barHeight) * 0.5f),
            fullSize = Vector2(barWidth, barHeight),
        )

        // Load a bitmap text "Loading Assets"
        loadText = Texture(Gdx.files.internal("LoadingAssets.png"))
        loadText.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)

        // Calculate text positions to put in on screen in render loop
        val textWidth = 3f
        val textHeight = textWidth * 16 / 200f
        loadingText = LoadingText(
            position = Vector2((WORLD_WIDTH - textWidth) * 0.5f, (WORLD_HEIGHT - textHeight) * 0.5f),
            size = Vector2(textWidth, textHeight),
        )
    }

    override fun render(delta: Float) {
        val progress = assetHandler.loadAssets()
        if (progress == Progress(1f)) {
            game.setScreen<MainMenuScreen>()
        } else {
            batch.use(gameCamera) {
                // Draw the loading bar
                batch.resetColor(0.2f)
                batch.draw(loadSegment, progressBar.position, progressBar.fullSize)

                batch.setColor(0.4f, 0.4f, 1f, 1f)
                batch.draw(loadSegment, progressBar.position, progressBar.computeCurrentSize(progress))

                // Draw the text
                batch.resetColor()
                batch.draw(loadText, loadingText.position, loadingText.size)
            }
        }
    }
}

data class ProgressBar(
    val position: Vector2,
    var fullSize: Vector2,
) {
    fun computeCurrentSize(progress: Progress) = Vector2(progress.value * fullSize.x, fullSize.y)
}

data class LoadingText(
    val position: Vector2,
    val size: Vector2,
)
