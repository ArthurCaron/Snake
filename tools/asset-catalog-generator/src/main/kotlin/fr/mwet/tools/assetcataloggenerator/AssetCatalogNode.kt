package fr.mwet.tools.assetcataloggenerator

import fr.mwet.tools.assetcataloggenerator.reader.AssetCatalogItem

class AssetCatalogNode(val name: String = "") {
    val constants = mutableListOf<AssetCatalogConstant>()
    val children = linkedMapOf<String, AssetCatalogNode>()

    fun add(item: AssetCatalogItem) {
        var node = this
        item.groups.forEach { group ->
            val name = group.toCleanVariableName()
            node = node.children.getOrPut(name) { AssetCatalogNode(name) }
        }

        val baseName = item.name.toCleanVariableName()
        if (node.constants.any { it.name == baseName && it.fullPath == item.fullPath }) {
            return
        }

        node.constants += AssetCatalogConstant(
            name = node.uniqueConstantName(baseName),
            fullPath = item.fullPath,
        )
    }

    private fun uniqueConstantName(baseName: String): String {
        var candidate = baseName
        var index = 2
        while (children.containsKey(candidate) || constants.any { it.name == candidate }) {
            candidate = "$baseName$index"
            index++
        }
        return candidate
    }

    data class AssetCatalogConstant(
        val name: String,
        val fullPath: String,
    )
}
