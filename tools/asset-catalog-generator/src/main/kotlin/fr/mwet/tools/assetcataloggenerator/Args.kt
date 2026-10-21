package fr.mwet.tools.assetcataloggenerator

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Path.of

data class Args(
    val inputDir: Path,
    val outputDir: Path,
    val packageName: String,
    val translationsFile: Path?,
) {
    fun validate() {
        require(Files.isDirectory(inputDir)) { "Input directory does not exist: $inputDir" }
        require(translationsFile == null || Files.isRegularFile(translationsFile)) { "Translations file does not exist: $translationsFile" }
        if (Files.exists(outputDir)) {
            outputDir.toFile().deleteRecursively()
        }
        Files.createDirectories(outputDir)
    }

    companion object {
        fun from(args: Array<String>): Args {
            require(args.size % 2 == 0) {
                "Arguments must be provided as --name value pairs."
            }

            val options = args.toList()
                .chunked(2)
                .associate { (name, value) ->
                    require(name.startsWith("--")) { "Invalid argument: $name" }
                    name.removePrefix("--") to value
                }

            val input = options["input"] ?: error("Missing required argument --${"input"}")
            val output = options["output"] ?: error("Missing required argument --${"output"}")
            val packageName = options["package"] ?: error("Missing required argument --${"package"}")

            val inputDir = of(input).toAbsolutePath().normalize()
            val outputDir = of(output).toAbsolutePath().normalize().resolve(packageName.replace('.', '/'))

            val translationsFile = options["translations"]?.let { of(it).toAbsolutePath().normalize() }

            return Args(inputDir, outputDir, packageName, translationsFile)
        }
    }
}
