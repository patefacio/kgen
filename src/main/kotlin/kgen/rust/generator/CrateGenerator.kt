package kgen.rust.generator

import kgen.rust.Crate
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
    val srcPathString = srcPath.pathString
    val srcPathExists = srcPath.exists()

    val targetPath = if(srcPathExists) {
        srcPath
    } else {
        createTempDirectory("crate_${crate.nameId}")
        File(srcPathString).mkdirs()
    }

    fun generate() {

    }

}