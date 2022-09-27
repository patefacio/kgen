package kgen.rust.generator

import kgen.kgenLogger
import kgen.mergeGeneratedWithFile
import kgen.rust.Crate
import kgen.scriptDelimiter
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.createTempDirectory
import kotlin.io.path.exists
import kotlin.io.path.pathString

data class CrateGenerator(
    val crate: Crate,
    val cratePath: String
) {
    val srcPath = Paths.get(cratePath, "src").toAbsolutePath()
    val tomlPath = Paths.get(cratePath, "Cargo.toml").toAbsolutePath()
    val srcPathString = srcPath.pathString
    val srcPathExists = srcPath.exists()

    // If source path exists, use a tmp path to put generated code, then
    // format that code and diff/replace to original
    val targetSrcPath = if(srcPathExists) {
        val tempPath = createTempDirectory("crate_${crate.nameId}")
        val tempSrcPath = Paths.get(tempPath.pathString, "src")
        File(tempSrcPath.pathString).mkdirs()
        tempSrcPath
    } else {
        File(srcPathString).mkdirs()
        srcPath
    }

    fun generate() {
        mergeGeneratedWithFile(crate.cargoToml.toml, tomlPath.pathString, scriptDelimiter)
        crate.rootModule
    }

}