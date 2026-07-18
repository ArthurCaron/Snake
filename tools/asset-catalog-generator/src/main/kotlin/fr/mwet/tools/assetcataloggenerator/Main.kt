package fr.mwet.tools.assetcataloggenerator

import fr.mwet.tools.assetcataloggenerator.reader.AssetCatalogItem
import fr.mwet.tools.assetcataloggenerator.reader.AtlasRegionCatalogItem
import fr.mwet.tools.assetcataloggenerator.reader.readAtlasRegionCatalogItems
import fr.mwet.tools.assetcataloggenerator.reader.readFileCatalogItems
import fr.mwet.tools.assetcataloggenerator.renderer.AssetCatalogNode
import fr.mwet.tools.assetcataloggenerator.renderer.renderAssetCatalog
import fr.mwet.tools.assetcataloggenerator.renderer.renderTextureAssets
import java.nio.file.Files
import java.nio.file.Path


fun main(args: Array<String>) {
    val (inputDir, outputDir, packageName) = Args.from(args).apply { validate() }

    val textureAtlasPath = "textures/textures.atlas"
    val textureRegionItems = readAtlasRegionCatalogItems(inputDir.resolve(textureAtlasPath))

    writeAssetCatalog(
        outputDir = outputDir,
        packageName = packageName,
        objectName = "TextureRegions",
        items = textureRegionItems.map { it.item },
    )
    writeTextureAssets(
        outputDir = outputDir,
        packageName = packageName,
        atlasPath = textureAtlasPath,
        items = textureRegionItems,
    )
    writeAssetCatalog(
        outputDir = outputDir,
        packageName = packageName,
        objectName = "SoundAssets",
        items = readFileCatalogItems(inputDir, "sounds", setOf("mp3", "ogg", "wav"), nested = false),
    )
    writeAssetCatalog(
        outputDir = outputDir,
        packageName = packageName,
        objectName = "MusicAssets",
        items = readFileCatalogItems(inputDir, "music", setOf("mp3", "ogg", "wav"), nested = false),
    )
    writeAssetCatalog(
        outputDir = outputDir,
        packageName = packageName,
        objectName = "FontAssets",
        items = readFileCatalogItems(inputDir, "fonts", setOf("ttf"), nested = true),
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

fun writeTextureAssets(
    outputDir: Path,
    packageName: String,
    atlasPath: String,
    items: List<AtlasRegionCatalogItem>,
) {
    val file = outputDir.resolve("TextureAssets.kt")
    Files.writeString(file, renderTextureAssets(packageName, atlasPath, items))
}
