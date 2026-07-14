package fr.mwet.tools.assetcataloggenerator.reader

import fr.mwet.tools.assetcataloggenerator.extension
import fr.mwet.tools.assetcataloggenerator.toAssetPathString
import java.nio.file.Files
import java.nio.file.Path

fun readFileCatalogReader(
    inputDir: Path,
    specificDir: String,
    extensions: Set<String>,
    nested: Boolean,
): List<AssetCatalogItem> =
    scanAssets(inputDir, specificDir, extensions).map { assetPath ->
        val segments = assetPath.split('/').filter { it.isNotBlank() }
        val fileName = segments.last()
        AssetCatalogItem(
            groups = if (nested) segments.drop(1).dropLast(1) else emptyList(),
            name = fileName.substringBeforeLast('.', fileName),
            fullPath = assetPath,
        )
    }

private fun scanAssets(
    inputDir: Path,
    specificDir: String,
    extensions: Set<String>,
): List<String> {
    val root = inputDir.resolve(specificDir)
    if (!Files.isDirectory(root)) {
        return emptyList()
    }

    val assets = mutableListOf<String>()
    Files.walk(root).use { stream ->
        stream
            .filter { path -> Files.isRegularFile(path) }
            .forEach { path ->
                val assetPath = inputDir.relativize(path).toAssetPathString()
                if (assetPath.extension() in extensions) {
                    assets += assetPath
                }
            }
    }
    return assets.sorted()
}
