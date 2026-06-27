package fr.mwet.snake.utils

import com.badlogic.gdx.files.FileHandle

fun <K, V> Map<K, V>.doesNotContainKey(key: K): Boolean = containsKey(key).not()

fun <E> Set<E>.doesNotContain(element: E): Boolean = contains(element).not()

fun FileHandle.doesNotExists(): Boolean = exists().not()
