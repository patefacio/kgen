package kgen.rust.generator

import kgen.*
import kgen.rust.Crate
import kgen.rust.Module
import kgen.rust.ModuleRootType
import kgen.rust.ModuleType
import kgen.utility.runSimpleCommand
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
 *
 * @property crate - The rust crate to generate
 * @property cratePath - Where to generate
 * @property generatorDeletedPath Occasionally users may rename modules and not
 * remember to remove the old module. If this is set, any  non-generated rust modules,
 * outside the `bin` directory will be moved to this path. `bin` is an exception
 * because it is common to hand-write small example programs.
 * Otherwise, a warning message will show.
 * @property noWarnNonGenerated If generatorDeletedPath not provided checks for rust
 * files not generated will still warn unless this is set to true.
 */
data class CrateGenerator(
    val crate: Crate,
    val cratePath: String,
    val generatorDeletedPath: Path? = null,
    val noWarnNonGenerated: Boolean = false
) {
    val srcPath = Paths.get(cratePath, "src").toAbsolutePath()
    val binPath = srcPath.resolve("../bin").toAbsolutePath()
    val benchPath = srcPath.resolve("../bench").toAbsolutePath()
    val integrationTestsPath = srcPath.resolve("../tests").toAbsolutePath()
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
        val tempBinPath = tempSrcPath.resolve("bin")
        val tempBenchesPath = tempSrcPath.resolve("../benches")
        File(tempSrcPath.pathString).mkdirs()
        File(tempBinPath.pathString).mkdirs()
        File(tempBenchesPath.pathString).mkdirs()
        // Copy the cargo file into the newly created directory so `cargo fmt` will work
        tomlPath.copyTo(Paths.get(tempPath.pathString, "Cargo.toml"))
        tempSrcPath
    } else {
        File(srcPathString).mkdirs()
        srcPath
    }

    private fun checkDeleteUnwantedFile(path: Path) {
        if (generatorDeletedPath != null) {
            val checkProtectedFile = path.relativeTo(srcPath)
            if (crate.handCodedSet.contains(checkProtectedFile)) {
                println("Leaving `$checkProtectedFile` alone")
            } else {
                println("MOVING UNWANTED: $path to $generatorDeletedPath")
                generatorDeletedPath.createDirectories()
                path.moveTo(generatorDeletedPath.resolve(path.name), overwrite = true)
            }
        } else {
            println("WARNING: NON GENERATED FILE -> $path")
        }
    }

    private fun checkForUnwantedFiles(rustSrcPath: Path, moduleGenerationResults: List<MergeResult>) {
        // At this point cargo fmt has succeeded. Find all rust files in package on disk that should not
        // were not generated.
        val generatedFiles = moduleGenerationResults
            // Skip checks on files in bin
            .filter {
                val notInBinDirectory = Path(it.targetPath).parent.name != "bin"
                notInBinDirectory
            }
            .associateBy { mergeResult ->
                // Get mapping of all rust files keyed starting at src: e.g.
                val key = mergeResult.targetPath.asPath.relativeTo(rustSrcPath.parent)
                key
            }

        // Save all rust files on disk to remove any that were not generated
        val potentiallyMoribundRustFiles = srcPath.toFile().walk().filter {
            val isRustFile = it.path.endsWith(".rs")
            val parentPathName = it.parentFile.name
            isRustFile && (parentPathName != "bin")
        }.associateBy { file ->
            file.toPath().relativeTo(srcPath.parent)
        }

        // Go through potentially moribund and if was not generated, remove
        potentiallyMoribundRustFiles.forEach { (relativePath, _) ->
            val wasGenerated = generatedFiles[relativePath]
            if (wasGenerated == null) {
                println("$relativePath WAS NOT GENERATED?")
                checkDeleteUnwantedFile(srcPath.parent.resolve(relativePath))
            }
        }
    }

    fun generate(announceUpdates: Boolean = true): List<MergeResult> {
        val tomlMergeResult = mergeGeneratedWithFile(
            crate.cargoToml.toml,
            tomlPath.pathString,
            scriptDelimiter,
            announceUpdates = announceUpdates
        )

        val buildModuleResult = if (crate.buildModule != null) {
            generateTo(crate.buildModule, cratePath, announceUpdates = announceUpdates, includeTypeSizes = false)
        } else {
            null
        }

        if (!File(cratePath).exists() && !File(cratePath).mkdirs()) {
            throw RuntimeException("Unable to create crate directory $cratePath")
        }
        // If source path exists, use a tmp path to put generated code, then
        // format that code and diff/replace to original
        val srcPathExists = srcPath.exists()
        val shouldAnnounce = announceUpdates && !srcPathExists
        val targetSrcPath: Path = getTargetPath(srcPathExists)
        val targetBinPath = targetSrcPath.resolve("bin").toAbsolutePath()
        val targetBenchPath = targetSrcPath.resolve("../benches").toAbsolutePath()
        val targetIntegrationTestsPath = targetSrcPath.resolve("../tests").toAbsolutePath()
        val targetExamplePath = targetSrcPath.resolve("../examples").toAbsolutePath()
        val targetSrcPathString = targetSrcPath.pathString

        val integrationTestsResults = crate.integrationTestModules.map { module ->
            generateTo(module, targetIntegrationTestsPath.pathString, announceUpdates, includeTypeSizes = false)
        }.flatten()

        val binaryModuleResults = crate.binaries.map { clapBinary ->
            val targetDir = if (clapBinary.inSeparateDirectory) {
                targetBinPath.resolve(clapBinary.nameId)
            } else {
                targetBinPath
            }
            generateTo(
                clapBinary.module,
                targetDir.pathString,
                announceUpdates = shouldAnnounce,
                includeTypeSizes = false
            )
        }.flatten()

        val moduleGenerationResults =
            generateTo(
                crate.rootModule,
                targetSrcPathString,
                announceUpdates = shouldAnnounce,
                crate.includeTypeSizes
            ) +
                    binaryModuleResults + integrationTestsResults

        "cargo fmt".runSimpleCommand(targetSrcPath.toFile(), echoOutputs = true)

        if (!noWarnNonGenerated) {
            checkForUnwantedFiles(targetSrcPath, moduleGenerationResults)
        }

        val updatedMergeResults = listOfNotNull(tomlMergeResult, buildModuleResult?.first()) +
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

        return updatedMergeResults
    }

    private fun generateTo(
        moduleOriginal: Module,
        targetPath: String,
        announceUpdates: Boolean,
        includeTypeSizes: Boolean
    ): List<MergeResult> {

        val module = moduleOriginal.copy(includeTypeSizes = includeTypeSizes)
        val isPlaceholderModule = module.moduleType == ModuleType.PlaceholderModule
        val moduleFileName = when (module.moduleRootType) {
            ModuleRootType.LibraryRoot -> "lib.rs"
            ModuleRootType.BinaryRoot -> "main.rs"
            ModuleRootType.NonRoot -> module.rustFileName
        }

        val outPath = when (module.moduleType) {
            ModuleType.Directory -> {
                val dir = Paths.get(targetPath, module.nameId).pathString
                File(dir).mkdirs()
                if (module.classicModStructure) {
                    Paths.get(dir, "mod.rs")
                } else {
                    Paths.get(targetPath, module.rustFileName)
                }
            }

            ModuleType.FileModule,
            ModuleType.PlaceholderModule -> Paths.get(targetPath, moduleFileName)

            ModuleType.Inline -> null
        }

        if (isPlaceholderModule) {
            // For a placeholder module, if it exists, leave it alone. If not, just create an empty
            // version of it. The out of modeling approach (e.g. tonic/proto will generate it in build procedures).
            if (!outPath!!.exists()) {
                kgenLogger.info { "Touching: file `${outPath.pathString}`" }
                outPath.writeText("")
            } else {
                kgenLogger.info { "Leaving: file `${outPath.pathString}`" }
            }
        }

        return listOf(
            listOfNotNull(
                if (!isPlaceholderModule && outPath != null) {
                    checkWriteFile(outPath.pathString, module.asRust, announceUpdates = announceUpdates)
                } else {
                    null
                }
            ),
            module.modules.filter { !it.isInline }.map {
                generateTo(
                    it,
                    if (module.classicModStructure) {
                        outPath!!.parent.pathString
                    } else {
                        outPath!!.parent.resolve(module.nameId).pathString
                    },
                    announceUpdates,
                    includeTypeSizes
                )
            }.flatten()
        ).flatten()
    }
}