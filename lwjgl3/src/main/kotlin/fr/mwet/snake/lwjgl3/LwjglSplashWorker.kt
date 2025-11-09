package fr.mwet.snake.lwjgl3

import fr.mwet.snake.utils.SplashWorker
import java.awt.SplashScreen

class LwjglSplashWorker : SplashWorker {
    override fun closeSplashScreen() {
        SplashScreen.getSplashScreen()?.close()
    }
}
