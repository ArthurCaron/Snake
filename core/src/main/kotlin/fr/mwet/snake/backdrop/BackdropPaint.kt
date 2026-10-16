package fr.mwet.snake.backdrop

import com.badlogic.gdx.math.MathUtils

// Modulo that stays in [0, max) for negative values too: Kotlin % keeps the sign of the dividend,
// so -15 % 10 = -5. Adding max once and taking % again brings it back into range.
// (22, 10) -> 2
// (-15, 10) -> 5
internal fun wrap(value: Float, max: Float): Float = ((value % max) + max) % max

// A sine moved from [-1, 1] to [0, 1]: +1 gives [0, 2], * 0.5 gives [0, 1].
// The result can drive an alpha or a blend factor directly, with no negative values.
// unitSin(0) = 0.5, unitSin(PI / 2) = 1, unitSin(PI) = 0.5, unitSin(3 * PI / 2) = 0.
internal fun unitSin(radians: Float): Float = (MathUtils.sin(radians) + 1f) * 0.5f
