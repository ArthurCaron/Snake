package fr.mwet.tools.assetcataloggenerator.reader

import java.nio.file.Files
import java.nio.file.Path

fun readAtlasCatalogReader(atlasPath: Path): List<AssetCatalogItem> {
    if (!Files.isRegularFile(atlasPath)) {
        return emptyList()
    }

    return parseAtlasRegions(atlasPath).map { regionName ->
        val segments = regionName.split('/').filter { it.isNotBlank() }
        AssetCatalogItem(
            groups = segments.dropLast(1),
            name = segments.last(),
            fullPath = regionName,
        )
    }
}

private fun parseAtlasRegions(atlasPath: Path): List<String> {
    val regionNames = linkedSetOf<String>()
    Files.readAllLines(atlasPath).forEach { line ->
        if (line.isBlank() || line.startsWith(" ")) return@forEach

        val trimmed = line.trim()
        if (trimmed.contains(':')) return@forEach
        if (trimmed.endsWith(".png", ignoreCase = true)) return@forEach

        regionNames += trimmed
    }
    return regionNames.sorted()
}
