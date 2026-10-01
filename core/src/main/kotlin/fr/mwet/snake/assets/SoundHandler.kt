package fr.mwet.snake.assets

import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Sound
import fr.mwet.snake.generated.assets.SoundAssets
import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry
import ktx.assets.getValue
import ktx.assets.loadAsset

class SoundHandler(assetManager: AssetManager) : DisposableRegistry by DisposableContainer() {
    val buttonPressedSound: Sound by assetManager.loadAsset(AssetDescriptor(SoundAssets.ButtonPressed, Sound::class.java))
    val moveSound: Sound by assetManager.loadAsset(AssetDescriptor(SoundAssets.SnakeMove, Sound::class.java))
    val eatSound: Sound by assetManager.loadAsset(AssetDescriptor(SoundAssets.SnakeEat, Sound::class.java))
    val lostSound: Sound by assetManager.loadAsset(AssetDescriptor(SoundAssets.Lost, Sound::class.java))
    val wonSound: Sound by assetManager.loadAsset(AssetDescriptor(SoundAssets.Won, Sound::class.java))
    val pauseSound: Sound by assetManager.loadAsset(AssetDescriptor(SoundAssets.Pause, Sound::class.java))
}
