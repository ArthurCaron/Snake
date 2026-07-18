package fr.mwet.tools.assetcataloggenerator.renderer

import fr.mwet.tools.assetcataloggenerator.reader.AssetCatalogItem
import fr.mwet.tools.assetcataloggenerator.toCleanVariableName
import java.util.*

fun indent(level: Int): String = "    ".repeat(level)

fun String.escapeCharsForKotlinString(): String =
    buildString {
        this@escapeCharsForKotlinString.forEach { char ->
            when (char) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(char)
            }
        }
    }

fun String.toTexturePropertyName(): String = toCleanVariableName().replaceFirstChar { it.lowercase(Locale.ROOT) }

fun AssetCatalogItem.textureAssetAccessPath(): String =
    (groups.map { it.toTexturePropertyName() } + name.toTexturePropertyName()).joinToString(".")

fun AssetCatalogItem.textureRegionConstantReference(): String =
    (groups + name).joinToString(separator = ".", prefix = "TextureRegions.") { it.toCleanVariableName() }
