package kgen.rust

import kgen.Identifier
import kgen.missingDoc
import kgen.rust.clap_binary.ClapBinary
import java.nio.file.Path

/** Represents a rust crate.
 *
 * @property nameId The name of the crate.
 * @property doc The documentation string for the crate.
 * @property rootModule The top level module (either lib.rs or main.rs).
 * @property buildModule An optional `build.rs` module used for crates requiring
 *           extra build step, like generating rust from protobuf files.
 * @property cargoToml Modeled toml for the crate.
 * @property binaries List of binaries, models as clap binaries, in the crate
 * @property includeTypeSizes If set will include sizes for structs and enums
 * @property integrationTestModules Modules for integration testing
 * @property handCodedSet Set of paths that were hand coded and should not be considered
 *                        orphaned code and hence not cleaned up
 */
data class Crate(
    val nameId: String,
    val doc: String = missingDoc(nameId, "Crate"),
    val rootModule: Module = Module("lib", moduleRootType = ModuleRootType.LibraryRoot),
    val buildModule: Module? = null,
    val cargoToml: CargoToml = CargoToml(nameId, description = doc),
    val binaries: List<ClapBinary> = emptyList(),
    val includeTypeSizes: Boolean = false,
    val integrationTestModules: List<Module> = emptyList(),
    val handCodedSet: Set<Path> = emptySet(),
) : Identifier(nameId) {

    init {
        if (rootModule.moduleRootType == ModuleRootType.NonRoot) {
            throw Exception("Assign root module type to crate `$nameId's` root module!")
        }
    }

}

enum class CrateType {
    Library,
    Binary
}