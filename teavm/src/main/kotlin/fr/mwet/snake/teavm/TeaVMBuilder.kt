package fr.mwet.snake.teavm

import java.io.File
import com.github.xpenatan.gdx.backends.teavm.config.AssetFileHandle
import com.github.xpenatan.gdx.backends.teavm.config.TeaBuildConfiguration
import com.github.xpenatan.gdx.backends.teavm.config.TeaBuilder
import com.github.xpenatan.gdx.backends.teavm.config.plugins.TeaReflectionSupplier
import com.github.xpenatan.gdx.backends.teavm.gen.SkipClass
import org.teavm.vm.TeaVMOptimizationLevel

/** Builds the TeaVM/HTML application. */
@SkipClass
object TeaVMBuilder {
    @JvmStatic fun main(arguments: Array<String>) {
        val teaBuildConfiguration = TeaBuildConfiguration().apply {
            assetsPath.add(AssetFileHandle("../assets"))
            webappPath = File("build/dist").canonicalPath
            // Register any extra classpath assets here:
            // additionalAssetsClasspathFiles += "fr/mwet/snake/asset.extension"
        }

        // Register DTOs used by libGDX Json. TeaVM needs explicit reflection metadata for them.
        listOf(
            "fr.mwet.snake.save.settings.serializable",
            "fr.mwet.snake.save.metadata.serializable",
            "fr.mwet.snake.save.game.serializable",
        ).forEach(TeaReflectionSupplier::addReflectionClass)
        // This one does the whole folder
//        TeaReflectionSupplier.addReflectionClass("fr.mwet.snake.save")

        val tool = TeaBuilder.config(teaBuildConfiguration)
        tool.mainClass = "fr.mwet.snake.teavm.TeaVMLauncher"
        tool.optimizationLevel = TeaVMOptimizationLevel.FULL
        tool.setObfuscated(true)
        TeaBuilder.build(tool)
    }
}
