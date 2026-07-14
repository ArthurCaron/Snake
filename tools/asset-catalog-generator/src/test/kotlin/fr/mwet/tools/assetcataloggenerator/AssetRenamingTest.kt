package fr.mwet.tools.assetcataloggenerator

import kotlin.test.Test
import kotlin.test.assertEquals

class AssetRenamingTest {
    @Test
    fun `converts asset names to clean Kotlin variable names`() {
        val cases = mapOf(
            "gridCell" to "GridCell",
            "snake--head" to "SnakeHead",
            "Fredoka_Condensed-Regular" to "FredokaCondensedRegular",
            "snakeHeadAnimation_0" to "SnakeHeadAnimation0",
            "Fredoka-VariableFont_wdth,wght" to "FredokaVariableFontWdthWght",
            "---" to "Asset",
        )

        cases.forEach { (assetName, expectedVariableName) ->
            assertEquals(
                expected = expectedVariableName,
                actual = assetName.toCleanVariableName(),
                message = "Unexpected generated name for $assetName",
            )
        }
    }

    @Test
    fun `prefixes names that start with digits`() {
        assertEquals("Asset3HitSound", "3-hit-sound".toCleanVariableName())
    }

    @Test
    fun `renames Kotlin keywords`() {
        assertEquals("ClassAsset", "class".toCleanVariableName())
        assertEquals("ObjectAsset", "object".toCleanVariableName())
    }

    @Test
    fun `uses fallback name when no letters or digits exist`() {
        assertEquals("Asset", "---".toCleanVariableName())
        assertEquals("Asset", "___".toCleanVariableName())
    }
}
