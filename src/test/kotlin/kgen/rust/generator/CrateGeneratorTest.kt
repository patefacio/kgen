package kgen.rust.generator

import kgen.kgenLogger
import kgen.rust.Crate
import kgen.rust.Module
import kgen.rust.Struct
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import kotlin.io.path.createTempDirectory
import kotlin.io.path.pathString

val testCrate = Crate("test_crate",
    "Sample test crate",
    Module("root", "Root module", structs = listOf(Struct("s")))
)

internal class CrateGeneratorTest {

    @Test
    fun getSrcPath() {
        assertEquals(
            "/foo/bar/goo/src",
            CrateGenerator(Crate("foo"), "/foo/bar/goo").srcPathString
        )
    }

    @Test
    fun generateNew() {
        val targetDir = createTempDirectory("new_crate")
        val crateGenerator = CrateGenerator(testCrate, targetDir.pathString)
        kgenLogger.warn { "Generating new crate into $targetDir"}
        crateGenerator.generate()
    }
}