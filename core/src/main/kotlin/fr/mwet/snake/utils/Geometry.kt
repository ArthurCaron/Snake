package fr.mwet.snake.utils

import kotlin.math.sqrt

// Pythagoras: dx and dy are the two sides of a right triangle, and the arrow (dx, dy) is its hypotenuse.
fun hypotenuse(dx: Float, dy: Float): Float = sqrt(dx * dx + dy * dy)

// The point (x, y) turned around (0, 0) by the angle whose cosine and sine are cos and sin:
// x' = x * cos - y * sin
// y' = x * sin + y * cos
// One function per coordinate, so that the caller needs no Vector2 and allocates nothing.
fun rotatedX(x: Float, y: Float, cos: Float, sin: Float): Float = x * cos - y * sin

fun rotatedY(x: Float, y: Float, cos: Float, sin: Float): Float = x * sin + y * cos
