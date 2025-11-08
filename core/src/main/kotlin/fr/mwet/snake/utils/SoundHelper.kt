package fr.mwet.snake.utils

import fr.mwet.snake.DI
import fr.mwet.snake.assets.AssetHandler

class SoundHelper {
    private val assetHandler = DI.inject<AssetHandler>()

    // Sound and music ids
    private var switchId: Long = 0
    private var moveId: Long = 0
    private var eatId: Long = 0

    fun playSwitch() {
        switchId = assetHandler.switchSound.play()
    }

    fun playMove() {
        assetHandler.moveSound.stop()
        moveId = assetHandler.moveSound.play()
    }

    fun playEat() {
        eatId = assetHandler.eatSound.play()
    }
}
