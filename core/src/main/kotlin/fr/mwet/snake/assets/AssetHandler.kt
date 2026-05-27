package fr.mwet.snake.assets

import com.badlogic.gdx.assets.AssetManager
import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry

class AssetHandler(private val assetManager: AssetManager) : DisposableRegistry by DisposableContainer() {
    private val listeners = mutableListOf<AssetsLoadedListener>()

    fun listenToAssetsLoaded(listener: AssetsLoadedListener) {
        listeners.add(listener)
    }

    fun loadAssets(): Progress {
        if (assetManager.update(16)) {
            println("Done loading")
            listeners.forEach { it.onAssetsLoaded() }
            return Progress(1f)
        }

        return Progress(assetManager.progress)
    }
}

interface AssetsLoadedListener {
    fun onAssetsLoaded()
}

@JvmInline
value class Progress(val value: Float)
