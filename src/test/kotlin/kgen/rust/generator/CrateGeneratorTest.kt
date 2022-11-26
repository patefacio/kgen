package kgen.rust.generator

import kgen.MergeFileStatus
import kgen.kgenLogger
import kgen.rust.*
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import kotlin.io.path.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.name
import kotlin.io.path.pathString

val testCrate = Crate(
    "test_crate",
    "Sample test crate",
    Module(
        "lib",
        "Root module",
        moduleRootType = ModuleRootType.LibraryRoot,
        structs = listOf(Struct("s")),
        modules = listOf(
            Module("c1"),
            Module(
                "c2",
                moduleType = ModuleType.Directory,
                modules = listOf(Module("c2_c1"))
            ),
            Module("c3", moduleType = ModuleType.Inline),
            Module("c4")
        )
    )
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
        val targetDir = if (false) {
            Path("/Users/dbdavidson/tmp/some_crate")
        } else {
            createTempDirectory("new_crate")
        }


        var generationResults = CrateGenerator(testCrate, targetDir.pathString).generate(false)

        // 6 files created, Cargo.toml, lib.rs, c1, c2/mod.rs, c2_c1.rs, c4.4s
        assertEquals(6, generationResults.size)
        generationResults.forEach {
            assertEquals(MergeFileStatus.Created, it.mergeFileStatus)
        }

        // Generate again and nothing should change
        generationResults = CrateGenerator(testCrate, targetDir.pathString).generate(false)
        assertEquals(6, generationResults.size)
        generationResults.forEach {
            assertEquals(MergeFileStatus.NoChange, it.mergeFileStatus)
        }

        // Update the module definition, changing the doc comment for c1 and adding
        // a new module
        val updatedCrate = testCrate.copy(
            rootModule = testCrate.rootModule.copy(
                modules = listOf(
                    testCrate.rootModule.modules.first().copy(
                        doc = "Updated doc for `c1`"
                    ),
                    Module("added_module")
                ) + testCrate.rootModule.modules.drop(1)
            )
        )

        generationResults = CrateGenerator(updatedCrate, targetDir.pathString).generate(false)

        assertEquals(
            mapOf(
                "Cargo.toml" to MergeFileStatus.NoChange,
                "lib.rs" to MergeFileStatus.Updated,
                "c1.rs" to MergeFileStatus.Updated,
                "added_module.rs" to MergeFileStatus.Created,
                "mod.rs" to MergeFileStatus.NoChange,
                "c2_c1.rs" to MergeFileStatus.NoChange,
                "c4.rs" to MergeFileStatus.NoChange
            ),
            generationResults.associate {
                Path(it.targetPath).fileName.name to it.mergeFileStatus
            }
        )
    }
}