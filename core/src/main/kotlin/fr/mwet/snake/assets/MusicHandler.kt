package fr.mwet.snake.assets

import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Music
import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry
import ktx.assets.getValue
import ktx.assets.loadAsset

private const val MUSIC_ROOT = "music"

class MusicHandler(assetManager: AssetManager) : DisposableRegistry by DisposableContainer() {
    val music: Music by assetManager.loadAsset(AssetDescriptor("$MUSIC_ROOT/music.mp3", Music::class.java))
}
