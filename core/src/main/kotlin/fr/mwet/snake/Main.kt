package fr.mwet.snake

import com.badlogic.gdx.ApplicationListener
import fr.mwet.snake.utils.SplashWorker
import ktx.assets.disposeSafely

class Main : ApplicationListener {
    var splashWorker: SplashWorker? = null

    override fun resize(width: Int, height: Int) {
        Game.resize(width, height)
    }

    override fun create() {
        splashWorker?.closeSplashScreen()
        DI.initialize()
        Game.create()
    }

    override fun render() {
        Game.render()
    }

    override fun resume() {
        Game.resume()
    }

    override fun dispose() {
        DI.disposeSafely()
    }

    override fun pause() {
        Game.pause()
    }
}
