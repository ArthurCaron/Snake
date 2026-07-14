package fr.mwet.tools.assetcataloggenerator

import java.io.File
import java.nio.file.Path
import java.util.*

fun Path.toAssetPathString(): String =
    toString().replace(File.separatorChar, '/').replace('\\', '/')

fun String.extension(): String =
    substringAfterLast('.', "").lowercase(Locale.ROOT)
