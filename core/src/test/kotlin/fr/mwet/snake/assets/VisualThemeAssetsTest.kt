package fr.mwet.snake.assets

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import fr.mwet.snake.render.VisualTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VisualThemeAssetsTest {
    @Test
    fun `every theme includes the same complete set of atlas regions and button splits`() {
        val atlasFile = FileHandle("../assets/textures/textures.atlas")
        val atlas = TextureAtlas.TextureAtlasData(atlasFile, atlasFile.parent(), false)
        val desert = atlas.regions.filter { it.name.startsWith("game/skin/") }
        assertTrue(desert.isNotEmpty())
        val required = desert.map { it.name.removePrefix("game/skin/") to it.index }.toSet()
        for (theme in VisualTheme.entries) {
            val prefix = "game/${theme.assetFolder}/"
            val regions = atlas.regions.filter { it.name.startsWith(prefix) }
            assertEquals(required, regions.map { it.name.removePrefix(prefix) to it.index }.toSet(), theme.tag)
            for (region in regions.filter { it.name.substringAfterLast('/').startsWith("button") }) {
                assertTrue(region.names?.contains("split") == true, "Missing nine-patch splits: ${region.name}")
            }
        }
    }
}
