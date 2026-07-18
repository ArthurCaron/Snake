package fr.mwet.tools.assetcataloggenerator.reader

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AtlasCatalogReaderTest {
    private val tempDirs = mutableListOf<Path>()

    @AfterTest
    fun deleteTempDirs() {
        tempDirs.forEach { tempDir -> tempDir.toFile().deleteRecursively() }
        tempDirs.clear()
    }

    @Test
    fun `returns empty list when atlas file is missing`() {
        val inputDir = createTempDir()

        assertEquals(emptyList(), readAtlasRegionCatalogItems(inputDir.resolve("textures/textures.atlas")))
    }

    @Test
    fun `reads sorted region items and counts duplicate frames`() {
        val atlasPath = createTempDir().resolve("textures/textures.atlas")
        writeText(
            atlasPath,
            """
                |textures.png
                |size: 64,64
                |format: RGBA8888
                |filter: Nearest,Nearest
                |repeat: none
                |game/grid-cell
                |  rotate: false
                |  xy: 0, 0
                |  size: 8, 8
                |game/chloe/snek/snakeHeadAnimation
                |  index: 0
                |game/chloe/snek/snakeHeadAnimation
                |  index: 1
                |textures2.PNG
                |game/chloe/fruit/strawberry
                |  rotate: false
            """.trimMargin(),
        )

        assertEquals(
            listOf(
                AtlasRegionCatalogItem(
                    item = AssetCatalogItem(
                        groups = listOf("game", "chloe", "fruit"),
                        name = "strawberry",
                        fullPath = "game/chloe/fruit/strawberry",
                    ),
                    frameCount = 1,
                ),
                AtlasRegionCatalogItem(
                    item = AssetCatalogItem(
                        groups = listOf("game", "chloe", "snek"),
                        name = "snakeHeadAnimation",
                        fullPath = "game/chloe/snek/snakeHeadAnimation",
                    ),
                    frameCount = 2,
                ),
                AtlasRegionCatalogItem(
                    item = AssetCatalogItem(
                        groups = listOf("game"),
                        name = "grid-cell",
                        fullPath = "game/grid-cell",
                    ),
                    frameCount = 1,
                ),
            ),
            readAtlasRegionCatalogItems(atlasPath),
        )
    }

    private fun createTempDir(): Path = Files.createTempDirectory("atlas-catalog-reader-test").also { tempDir ->
        tempDirs.add(tempDir)
    }

    private fun writeText(path: Path, text: String) {
        Files.createDirectories(path.parent)
        Files.writeString(path, text)
    }
}
