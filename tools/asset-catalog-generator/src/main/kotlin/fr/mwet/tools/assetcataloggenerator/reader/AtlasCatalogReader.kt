package fr.mwet.tools.assetcataloggenerator.reader

import java.nio.file.Files
import java.nio.file.Path

fun readAtlasRegionCatalogItems(atlasPath: Path): List<AtlasRegionCatalogItem> {
    if (!Files.isRegularFile(atlasPath)) {
        return emptyList()
    }

    return parseAtlasRegionsWithFrameCounts(atlasPath).entries
        .sortedBy { it.key }
        .map { (regionName, frameCount) ->
            val segments = regionName.split('/').filter { it.isNotBlank() }
            AtlasRegionCatalogItem(
                item = AssetCatalogItem(
                    groups = segments.dropLast(1),
                    name = segments.last(),
                    fullPath = regionName,
                ),
                frameCount = frameCount,
            )
        }
}

private fun parseAtlasRegionsWithFrameCounts(atlasPath: Path): Map<String, Int> {
    val regionsWithFrameCounts = linkedMapOf<String, Int>()
    Files.readAllLines(atlasPath).forEach { line ->
        if (line.isBlank() || line.startsWith(" ")) return@forEach

        val trimmed = line.trim()
        if (trimmed.contains(':')) return@forEach
        if (trimmed.endsWith(".png", ignoreCase = true)) return@forEach

        regionsWithFrameCounts[trimmed] = regionsWithFrameCounts.getOrDefault(trimmed, 0) + 1
    }
    return regionsWithFrameCounts
}
