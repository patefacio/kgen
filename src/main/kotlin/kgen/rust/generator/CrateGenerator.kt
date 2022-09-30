package kgen.rust.generator

import kgen.*
import kgen.rust.Crate
import kgen.rust.Module
import kgen.rust.ModuleType
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.*


/**
 * The steps are:
 *
 * - Generate all code into temporary folder
 * - Rust format the code
 * - Merge the results back into original displaying the update status of each file
 *
 * This is a bit complicated of an approach, but it is important to announce what
 * has changed and if a file has not changed it should not be touched (i.e. timestamp
 * should not change). Any approach that directly writes to the original files without
 * a temp location will always have all files at the latest.
 */
data class CrateGenerator(
    val crate: Crate,
    val cratePath: String
) {
    val srcPath = Paths.get(cratePath, "src").toAbsolutePath()
    val tomlPath = Paths.get(cratePath, "Cargo.toml").toAbsolutePath()
    val srcPathString = srcPath.pathString

    /**
     * Returns a target src path to generate code into.
     * If srcPath does not exist it is created and used as result.
     * If srcPath does exist, a temp source folder is created and returned.
     * In this case all code is written first to a temp folder.
     */
    private fun getTargetPath(srcPathExists: Boolean): Path = if (srcPathExists) {
        val tempPath = createTempDirectory("crate_${crate.nameId}")
        val tempSrcPath = Paths.get(tempPath.pathString, "src")
        File(tempSrcPath.pathString).mkdirs()
        // Copy the cargo file into the newly created directory so `cargo fmt` will work
        tomlPath.copyTo(Paths.get(tempPath.pathString, "Cargo.toml"))
        tempSrcPath
    } else {
        File(srcPathString).mkdirs()
        srcPath
    }

    fun generate(announceUpdates: Boolean = true): List<MergeResult> {
        if (!File(cratePath).exists() && !File(cratePath).mkdirs()) {
            throw RuntimeException("Unable to create crate directory $cratePath")
        }
        // If source path exists, use a tmp path to put generated code, then
        // format that code and diff/replace to original
        val srcPathExists = srcPath.exists()
        val targetSrcPath: Path = getTargetPath(srcPathExists)
        val tomlMergeResult = mergeGeneratedWithFile(
            crate.cargoToml.toml,
            tomlPath.pathString,
            scriptDelimiter,
            announceUpdates = announceUpdates
        )
        val targetSrcPathString = targetSrcPath.pathString

        val moduleGenerationResults =
            generateTo(crate.rootModule, targetSrcPathString, announceUpdates = announceUpdates && !srcPathExists)
        "cd $targetSrcPath; cargo fmt".runShellCommand()

        return listOf(tomlMergeResult) +
                if (targetSrcPath == srcPath) {
                    moduleGenerationResults
                } else {
                    // Since code was generated to a temp folder, each must be merged back into
                    // original crate src
                    val tempToSrcMergeResults = moduleGenerationResults.map { tempFileMergeResult ->
                        val content = File(tempFileMergeResult.targetPath).readText()
                        val actualTargetPath =
                            tempFileMergeResult.targetPath.replace(targetSrcPathString, srcPathString)
                        mergeGeneratedWithFile(content, actualTargetPath, announceUpdates = announceUpdates)
                    }
                    val tempCrateDir = targetSrcPath.parent
                    kgenLogger.warn { "Deleting temp crate directory - $tempCrateDir" }
                    tempCrateDir.toFile().deleteRecursively()
                    tempToSrcMergeResults
                }
    }

    private fun generateTo(module: Module, targetPath: String, announceUpdates: Boolean): List<MergeResult> {
        val outPath = when (module.moduleType) {
            ModuleType.Directory -> {
                val dir = Paths.get(targetPath, module.nameId).pathString
                File(dir).mkdirs()
                Paths.get(dir, "mod.rs")
            }

            ModuleType.FileModule -> Paths.get(targetPath, "${module.nameId}.rs")
            ModuleType.Inline -> null
        }

        return listOf(
            listOfNotNull(
                if (outPath != null) {
                    checkWriteFile(outPath.pathString, module.asRust, announceUpdates = announceUpdates)
                } else {
                    null
                }
            ),
            module.modules.filter { !it.isInline }.map {
                generateTo(it, outPath!!.parent.pathString, announceUpdates)
            }.flatten()
        ).flatten()
    }
}