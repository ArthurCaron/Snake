package fr.mwet.tools.assetcataloggenerator

import fr.mwet.tools.assetcataloggenerator.reader.AssetCatalogItem
import fr.mwet.tools.assetcataloggenerator.reader.readAtlasCatalogReader
import fr.mwet.tools.assetcataloggenerator.reader.readFileCatalogReader
import java.nio.file.Files
import java.nio.file.Path


fun main(args: Array<String>) {
    val (inputDir, outputDir, packageName) = Args.from(args).apply { validate() }

    writeAssetCatalog(
        outputDir = outputDir,
        packageName = packageName,
        objectName = "TextureRegions",
        items = readAtlasCatalogReader(inputDir.resolve("textures/textures.atlas")),
    )
    writeAssetCatalog(
        outputDir = outputDir,
        packageName = packageName,
        objectName = "SoundAssets",
        items = readFileCatalogReader(inputDir, "sounds", setOf("mp3", "ogg", "wav"), nested = false),
    )
    writeAssetCatalog(
        outputDir = outputDir,
        packageName = packageName,
        objectName = "MusicAssets",
        items = readFileCatalogReader(inputDir, "music", setOf("mp3", "ogg", "wav"), nested = false),
    )
    writeAssetCatalog(
        outputDir = outputDir,
        packageName = packageName,
        objectName = "FontAssets",
        items = readFileCatalogReader(inputDir, "fonts", setOf("ttf"), nested = true),
    )
}

private fun writeAssetCatalog(
    outputDir: Path,
    packageName: String,
    objectName: String,
    items: List<AssetCatalogItem>,
) {
    val root = AssetCatalogNode()
    items.forEach { root.add(it) }

    val file = outputDir.resolve("$objectName.kt")
    Files.writeString(file, renderAssetCatalog(packageName, objectName, root))
}
