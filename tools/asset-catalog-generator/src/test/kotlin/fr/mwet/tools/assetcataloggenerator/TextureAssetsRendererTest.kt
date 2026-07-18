package fr.mwet.tools.assetcataloggenerator

import fr.mwet.tools.assetcataloggenerator.reader.AssetCatalogItem
import fr.mwet.tools.assetcataloggenerator.reader.AtlasRegionCatalogItem
import fr.mwet.tools.assetcataloggenerator.renderer.renderTextureAssets
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TextureAssetsRendererTest {
    @Test
    fun `merges groups that clean to the same Kotlin name`() {
        val output = renderTextureAssets(
            packageName = "test.assets",
            atlasPath = "textures/test.atlas",
            items = listOf(
                AtlasRegionCatalogItem(
                    item = AssetCatalogItem(
                        groups = listOf("player-one"),
                        name = "head",
                        fullPath = "player-one/head",
                    ),
                    frameCount = 1,
                ),
                AtlasRegionCatalogItem(
                    item = AssetCatalogItem(
                        groups = listOf("player_one"),
                        name = "tail",
                        fullPath = "player_one/tail",
                    ),
                    frameCount = 1,
                ),
            ),
        )

        assertEquals(1, Regex("val playerOne: PlayerOne").findAll(output).count())
        assertEquals(1, Regex("inner class PlayerOne").findAll(output).count())
        assertTrue("val head: AtlasRegion" in output)
        assertTrue("val tail: AtlasRegion" in output)
    }

    @Test
    fun `prefixes root aliases with parent group when leaf names collide`() {
        val output = renderTextureAssets(
            packageName = "test.assets",
            atlasPath = "textures/test.atlas",
            items = listOf(
                AtlasRegionCatalogItem(
                    item = AssetCatalogItem(
                        groups = listOf("game", "player"),
                        name = "head",
                        fullPath = "game/player/head",
                    ),
                    frameCount = 1,
                ),
                AtlasRegionCatalogItem(
                    item = AssetCatalogItem(
                        groups = listOf("game", "enemy"),
                        name = "head",
                        fullPath = "game/enemy/head",
                    ),
                    frameCount = 1,
                ),
            ),
        )

        assertTrue("val playerHead: AtlasRegion get() = game.player.head" in output)
        assertTrue("val enemyHead: AtlasRegion get() = game.enemy.head" in output)
    }

    @Test
    fun `keeps blank lines between generated texture asset sections`() {
        val output = renderTextureAssets(
            packageName = "test.assets",
            atlasPath = "textures/test.atlas",
            items = listOf(
                AtlasRegionCatalogItem(
                    item = AssetCatalogItem(
                        groups = listOf("game"),
                        name = "grid-cell",
                        fullPath = "game/grid-cell",
                    ),
                    frameCount = 1,
                ),
                AtlasRegionCatalogItem(
                    item = AssetCatalogItem(
                        groups = listOf("game", "player"),
                        name = "head",
                        fullPath = "game/player/head",
                    ),
                    frameCount = 1,
                ),
            ),
        )

        assertTrue(
            """
                |    val game: Game by lazy { Game() }
                |
                |    val gridCell: AtlasRegion get() = game.gridCell
                |    val head: AtlasRegion get() = game.player.head
                |
                |    inner class Game {
                |
                |        val player: Player by lazy { Player() }
                |
                |        val gridCell: AtlasRegion by lazy {
                |            textureAtlas.findRegion(TextureRegions.Game.GridCell)
                |        }
                |
                |        inner class Player {
                |            val head: AtlasRegion by lazy {
                |                textureAtlas.findRegion(TextureRegions.Game.Player.Head)
                |            }
                |        }
                |    }
            """.trimMargin() in output,
        )
    }
}
