package fr.mwet.tools.assetcataloggenerator.reader

data class AssetCatalogItem(
    val groups: List<String>,
    val name: String,
    val fullPath: String,
)

data class AtlasRegionCatalogItem(
    val item: AssetCatalogItem,
    val frameCount: Int,
) {
    val isAnimation: Boolean get() = frameCount > 1
}
