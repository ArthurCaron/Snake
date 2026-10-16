package fr.mwet.snake.utils

import kotlin.math.sqrt

// Pythagoras: dx and dy are the two sides of a right triangle, and the arrow (dx, dy) is its hypotenuse.
fun hypotenuse(dx: Float, dy: Float): Float = sqrt(dx * dx + dy * dy)
