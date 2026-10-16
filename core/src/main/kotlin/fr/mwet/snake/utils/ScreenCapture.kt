package fr.mwet.snake.utils

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.ScreenUtils

// A copy of what the screen shows now, in a new texture. The caller owns that texture and disposes it.
fun captureScreen(): TextureRegion = ScreenUtils.getFrameBufferTexture()
