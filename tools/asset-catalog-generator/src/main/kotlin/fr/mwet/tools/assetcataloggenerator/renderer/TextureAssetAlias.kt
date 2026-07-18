package fr.mwet.tools.assetcataloggenerator.renderer

import fr.mwet.tools.assetcataloggenerator.reader.AssetCatalogItem
import fr.mwet.tools.assetcataloggenerator.reader.AtlasRegionCatalogItem
import java.util.*

data class TextureAssetAlias(
    val propertyName: String,
    val isAnimation: Boolean,
    val accessPath: String,
)

fun buildTextureAssetAliases(
    node: TextureAssetNode,
    items: List<AtlasRegionCatalogItem>,
): List<TextureAssetAlias> {
    val duplicateNames = items.groupBy { it.item.name.toTexturePropertyName() }
        .filterValues { it.size > 1 }
        .keys
    val usedNames = node.children.values.mapTo(mutableSetOf()) { it.propertyName }

    return items.map { item ->
        val baseName = item.item.name.toTexturePropertyName()
        TextureAssetAlias(
            propertyName = item.item.uniqueNodePropertyName(
                baseName = baseName,
                shouldPrefix = baseName in duplicateNames,
                usedNames = usedNames,
            ),
            isAnimation = item.isAnimation,
            accessPath = item.item.textureAssetAccessPath(),
        )
    }
}

private fun AssetCatalogItem.uniqueNodePropertyName(
    baseName: String,
    shouldPrefix: Boolean,
    usedNames: MutableSet<String>,
): String {
    if (!shouldPrefix && usedNames.add(baseName)) {
        return baseName
    }

    for (depth in 1..groups.size) {
        val candidate = groups.takeLast(depth).toPropertyPrefix() + baseName.uppercaseFirstLetter()
        if (usedNames.add(candidate)) {
            return candidate
        }
    }

    var index = 2
    while (!usedNames.add("$baseName$index")) {
        index++
    }
    return "$baseName$index"
}

private fun List<String>.toPropertyPrefix(): String = joinToString("") { it.toTexturePropertyName() }

private fun String.uppercaseFirstLetter(): String =
    replaceFirstChar { char ->
        if (char.isLowerCase()) char.titlecase(Locale.ROOT) else char.toString()
    }
