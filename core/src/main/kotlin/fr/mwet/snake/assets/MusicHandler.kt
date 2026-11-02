package fr.mwet.snake.assets

import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Music
import fr.mwet.snake.generated.assets.MusicAssets
import ktx.assets.getValue
import ktx.assets.loadAsset

class MusicHandler(assetManager: AssetManager) {
    val music: Music by assetManager.loadAsset(AssetDescriptor(MusicAssets.Music, Music::class.java))
}
