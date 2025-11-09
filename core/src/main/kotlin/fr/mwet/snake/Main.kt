package fr.mwet.snake

import com.badlogic.gdx.ApplicationListener
import fr.mwet.snake.utils.SplashWorker
import ktx.assets.disposeSafely

class Main : ApplicationListener {
    private val game by lazy { DI.inject<Game>() }
    var splashWorker: SplashWorker? = null

    override fun resize(width: Int, height: Int) {
        game.resize(width, height)
    }

    override fun create() {
        splashWorker?.closeSplashScreen()
        DI.initialize()
        game.create()
    }

    override fun render() {
        game.render()
    }

    override fun resume() {
        game.resume()
    }

    override fun dispose() {
        DI.disposeSafely()
    }

    override fun pause() {
        game.pause()
    }
}
