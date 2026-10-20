package fr.mwet.snake.lwjgl3

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.backends.lwjgl3.DefaultLwjgl3Input
import com.badlogic.gdx.utils.IntIntMap
import fr.mwet.snake.inputs.KeyNames
import org.lwjgl.glfw.GLFW.GLFW_KEY_LAST
import org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE
import org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN
import org.lwjgl.glfw.GLFW.glfwGetKeyName

class GlfwKeyNames : KeyNames {
    private val glfwKeys by lazy { glfwKeysByKeycode() }

    override fun nameOf(keycode: Int): String? {
        val glfwKey = glfwKeys.get(keycode, GLFW_KEY_UNKNOWN)
        val layoutName = if (glfwKey == GLFW_KEY_UNKNOWN) null else glfwGetKeyName(glfwKey, 0)
        return layoutName?.uppercase() ?: Input.Keys.toString(keycode)
    }
}

private fun glfwKeysByKeycode(): IntIntMap {
    val keys = IntIntMap()
    val input = Gdx.input as? DefaultLwjgl3Input ?: return keys
    for (glfwKey in GLFW_KEY_SPACE..GLFW_KEY_LAST) {
        val keycode = input.getGdxKeyCode(glfwKey)
        if (keycode != Input.Keys.UNKNOWN && !keys.containsKey(keycode)) keys.put(keycode, glfwKey)
    }
    return keys
}
