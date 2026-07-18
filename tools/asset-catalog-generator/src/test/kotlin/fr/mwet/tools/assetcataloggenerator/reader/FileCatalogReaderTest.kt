package fr.mwet.tools.assetcataloggenerator.reader

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FileCatalogReaderTest {
    private val tempDirs = mutableListOf<Path>()

    @AfterTest
    fun deleteTempDirs() {
        tempDirs.forEach { tempDir -> tempDir.toFile().deleteRecursively() }
        tempDirs.clear()
    }

    @Test
    fun `returns empty list when asset directory is missing`() {
        val inputDir = createTempDir()

        assertEquals(
            emptyList(),
            readFileCatalogItems(
                inputDir = inputDir,
                specificDir = "sounds",
                extensions = setOf("wav"),
                nested = false,
            ),
        )
    }

    @Test
    fun `reads flat assets sorted and filtered by extension`() {
        val inputDir = createTempDir()
        writeAsset(inputDir.resolve("sounds/zap.WAV"))
        writeAsset(inputDir.resolve("sounds/ui/click.ogg"))
        writeAsset(inputDir.resolve("sounds/readme.txt"))
        writeAsset(inputDir.resolve("music/theme.ogg"))

        assertEquals(
            listOf(
                AssetCatalogItem(
                    groups = emptyList(),
                    name = "click",
                    fullPath = "sounds/ui/click.ogg",
                ),
                AssetCatalogItem(
                    groups = emptyList(),
                    name = "zap",
                    fullPath = "sounds/zap.WAV",
                ),
            ),
            readFileCatalogItems(
                inputDir = inputDir,
                specificDir = "sounds",
                extensions = setOf("ogg", "wav"),
                nested = false,
            ),
        )
    }

    @Test
    fun `keeps nested groups after the top-level asset directory when requested`() {
        val inputDir = createTempDir()
        writeAsset(inputDir.resolve("fonts/menu/title/Chewy-Regular.ttf"))

        assertEquals(
            listOf(
                AssetCatalogItem(
                    groups = listOf("menu", "title"),
                    name = "Chewy-Regular",
                    fullPath = "fonts/menu/title/Chewy-Regular.ttf",
                ),
            ),
            readFileCatalogItems(
                inputDir = inputDir,
                specificDir = "fonts",
                extensions = setOf("ttf"),
                nested = true,
            ),
        )
    }

    private fun createTempDir(): Path = Files.createTempDirectory("file-catalog-reader-test").also { tempDir ->
        tempDirs.add(tempDir)
    }

    private fun writeAsset(path: Path) {
        Files.createDirectories(path.parent)
        Files.writeString(path, "")
    }
}
