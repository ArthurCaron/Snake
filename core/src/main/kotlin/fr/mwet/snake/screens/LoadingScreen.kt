package fr.mwet.snake.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import fr.mwet.snake.DI
import fr.mwet.snake.Game
import fr.mwet.snake.assets.AssetHandler
import fr.mwet.snake.assets.Progress
import fr.mwet.snake.utils.*
import ktx.app.KtxScreen
import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry
import ktx.graphics.use


@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class LoadingScreen(
    private val assetHandler: AssetHandler,
    private val batch: SpriteBatch,
    private val gameCamera: OrthographicCamera,
) : KtxScreen, DisposableRegistry by DisposableContainer() {
    private val progressBar = ProgressBar(barWidth = 6f, barHeight = 0.5f)
    private val loadingText = LoadingText(textWidth = 3f, textHeight = 3f * 16 / 200f)

    override fun render(delta: Float) {
        val progress = assetHandler.loadAssets()
        if (progress == Progress(1f)) {
            DI.finishInitAfterAssetsAreLoaded()
            Game.setScreen<MainMenuScreen>()
        } else {
            batch.use(gameCamera) {
                progressBar.render(batch, progress)
                loadingText.render(batch)
            }
        }
    }
}

class ProgressBar(barWidth: Float, barHeight: Float) :
    DisposableRegistry by DisposableContainer() {
    private val position = DI.vectorPool.obtain((WORLD_WIDTH - barWidth) * 0.5f, (WORLD_HEIGHT - barHeight) * 0.5f)
    private val fullSize = DI.vectorPool.obtain(barWidth, barHeight)
    private var size = DI.vectorPool.obtain(0, 0)

    val texture = run {
        // Create loading segment part, use Pixmap to generate the texture
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888).apply {
            setColor(Color.WHITE)
            fill()
        }
        Texture(pixmap).alsoRegister().also { pixmap.dispose() }
    }

    fun render(batch: SpriteBatch, progress: Progress) {
        batch.resetColor(0.2f)
        batch.draw(texture, position, fullSize)

        batch.setColor(0.4f, 0.4f, 1f, 1f)
        batch.draw(texture, position, size.move(progress.value * fullSize.x, fullSize.y))
    }
}

class LoadingText(textWidth: Float, textHeight: Float) : DisposableRegistry by DisposableContainer() {
    private val position = DI.vectorPool.obtain((WORLD_WIDTH - textWidth) * 0.5f, (WORLD_HEIGHT - textHeight) * 0.5f)
    private val size = DI.vectorPool.obtain(textWidth, textHeight)

    val texture = Texture(Gdx.files.internal("LoadingAssets.png")).alsoRegister().apply {
        setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
    }

    fun render(batch: SpriteBatch) {
        batch.resetColor()
        batch.draw(texture, position, size)
    }
}
