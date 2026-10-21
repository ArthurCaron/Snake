package fr.mwet.snake

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.ScreenViewport
import fr.mwet.snake.tuning.GameplayTuning.WORLD_HEIGHT
import fr.mwet.snake.tuning.GameplayTuning.WORLD_WIDTH

private const val UI_REFERENCE_SHORT_SIDE = 900f

class GameViewport(camera: OrthographicCamera) : FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera)

class StageViewport : ScreenViewport() {
    override fun update(screenWidth: Int, screenHeight: Int, centerCamera: Boolean) {
        unitsPerPixel = UI_REFERENCE_SHORT_SIDE / maxOf(1, minOf(screenWidth, screenHeight))
        super.update(screenWidth, screenHeight, centerCamera)
    }
}
