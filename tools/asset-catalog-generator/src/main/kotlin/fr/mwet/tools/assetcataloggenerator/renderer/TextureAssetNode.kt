package fr.mwet.tools.assetcataloggenerator.renderer

import fr.mwet.tools.assetcataloggenerator.reader.AtlasRegionCatalogItem
import fr.mwet.tools.assetcataloggenerator.toCleanVariableName

class TextureAssetNode(
    val className: String = "",
    val propertyName: String = "",
) {
    val regions = mutableListOf<TextureAssetRegion>()
    val children = linkedMapOf<String, TextureAssetNode>()

    fun add(item: AtlasRegionCatalogItem) {
        var node = this
        item.item.groups.forEach { group ->
            val className = group.toCleanVariableName()
            val propertyName = group.toTexturePropertyName()
            node = node.children.getOrPut(className) {
                TextureAssetNode(
                    className = className,
                    propertyName = propertyName,
                )
            }
        }

        node.regions += item.toTextureAssetRegion()
    }
}

data class TextureAssetRegion(
    val propertyName: String,
    val constantReference: String,
    val isAnimation: Boolean,
)

private fun AtlasRegionCatalogItem.toTextureAssetRegion() = TextureAssetRegion(
    propertyName = item.name.toTexturePropertyName(),
    constantReference = item.textureRegionConstantReference(),
    isAnimation = isAnimation,
)
