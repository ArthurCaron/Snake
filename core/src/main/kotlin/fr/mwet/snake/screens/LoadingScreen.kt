package fr.mwet.snake.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import fr.mwet.snake.AssetLoading
import fr.mwet.snake.GameViewport
import fr.mwet.snake.LoadedAssets
import fr.mwet.snake.Progress
import fr.mwet.snake.render.Palette
import fr.mwet.snake.utils.*
import ktx.app.KtxScreen
import com.badlogic.gdx.utils.Disposable
import ktx.graphics.use


class LoadingScreen(
    private val assetLoading: AssetLoading,
    private val batch: SpriteBatch,
    private val gameViewport: GameViewport,
    private val onLoaded: (LoadedAssets) -> Unit,
) : KtxScreen {
    private val progressBar = ProgressBar(barWidth = 6f, barHeight = 0.5f)
    private val loadingText = LoadingText(textWidth = 3f, textHeight = 3f * 16 / 200f)

    override fun render(delta: Float) {
        val assets = assetLoading.update()
        if (assets != null) {
            onLoaded(assets)
            return
        }

        gameViewport.apply()
        batch.use(gameViewport.camera) {
            progressBar.render(it, assetLoading.progress)
            loadingText.render(it)
        }
    }

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, true)
    }

    override fun dispose() {
        progressBar.dispose()
        loadingText.dispose()
    }
}

class ProgressBar(barWidth: Float, barHeight: Float) : Disposable {
    private val position = Vector2((WORLD_WIDTH - barWidth) * 0.5f, (WORLD_HEIGHT - barHeight) * 0.5f)
    private val fullSize = Vector2(barWidth, barHeight)
    private val size = Vector2()

    val texture = run {
        // Create loading segment part, use Pixmap to generate the texture
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888).apply {
            setColor(Palette.text)
            fill()
        }
        Texture(pixmap).also { pixmap.dispose() }
    }

    fun render(batch: SpriteBatch, progress: Progress) {
        batch.resetColor(0.2f)
        batch.draw(texture, position, fullSize)

        batch.setColor(0.4f, 0.4f, 1f, 1f)
        batch.draw(texture, position, size.move(progress.value * fullSize.x, fullSize.y))
    }

    override fun dispose() {
        texture.dispose()
    }
}

class LoadingText(textWidth: Float, textHeight: Float) : Disposable {
    private val position = Vector2((WORLD_WIDTH - textWidth) * 0.5f, (WORLD_HEIGHT - textHeight) * 0.5f)
    private val size = Vector2(textWidth, textHeight)

    val texture = Texture(Gdx.files.internal("LoadingAssets.png")).apply {
        setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
    }

    fun render(batch: SpriteBatch) {
        batch.resetColor()
        batch.draw(texture, position, size)
    }

    override fun dispose() {
        texture.dispose()
    }
}
