package fr.mwet.snake

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.ScreenViewport
import fr.mwet.snake.tuning.GameplayTuning.WORLD_HEIGHT
import fr.mwet.snake.tuning.GameplayTuning.WORLD_WIDTH
import fr.mwet.snake.ui.UiMetrics.REFERENCE_BOARD_WIDTH

class GameViewport(camera: OrthographicCamera) : FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera)

// The UI lives inside the board, so its scale follows the width of the board when it fits the whole screen. A phone
// in portrait gives the board its full width; a landscape screen gives it less, and the UI shrinks with it. The game
// screen uses the same scale, even though its bands make the board a little smaller.
class StageViewport : ScreenViewport() {
    override fun update(screenWidth: Int, screenHeight: Int, centerCamera: Boolean) {
        val boardWidth = minOf(screenWidth.toFloat(), screenHeight * WORLD_WIDTH / WORLD_HEIGHT)
        unitsPerPixel = REFERENCE_BOARD_WIDTH / maxOf(1f, boardWidth)
        super.update(screenWidth, screenHeight, centerCamera)
    }
}
