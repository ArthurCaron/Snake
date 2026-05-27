package fr.mwet.snake.assets

import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Sound
import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry
import ktx.assets.getValue
import ktx.assets.loadAsset

private const val SOUNDS_ROOT = "sounds"

class SoundHandler(assetManager: AssetManager) : DisposableRegistry by DisposableContainer() {
    val switchSound: Sound by assetManager.loadAsset(AssetDescriptor("$SOUNDS_ROOT/switch.mp3", Sound::class.java))
    val moveSound: Sound by assetManager.loadAsset(AssetDescriptor("$SOUNDS_ROOT/snake_move.mp3", Sound::class.java))
    val eatSound: Sound by assetManager.loadAsset(AssetDescriptor("$SOUNDS_ROOT/snake_eat.mp3", Sound::class.java))

    // Sound and music ids
    private var switchId: Long = 0
    private var moveId: Long = 0
    private var eatId: Long = 0

    fun playSwitch() {
        switchId = switchSound.play()
    }

    fun playMove() {
        moveSound.stop()
        moveId = moveSound.play()
    }

    fun playEat() {
        eatId = eatSound.play()
    }
}
