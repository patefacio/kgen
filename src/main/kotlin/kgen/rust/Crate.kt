package kgen.rust

import kgen.Identifier
import kgen.missingDoc

/** Represents a rust crate.
 *
 * @property nameId The name of the crate.
 * @property doc The documentation string for the crate.
 * @property rootModule The top level module (either lib.rs or main.rs).
 * @property buildModule An optional `build.rs` module used for crates requiring
 *           extra build step, like generating rust from protobuf files.
 * @property cargoToml Modeled toml for the crate.
 */
data class Crate(
    val nameId: String,
    val doc: String = missingDoc(nameId, "Crate"),
    val rootModule: Module = Module("lib"),
    val buildModule: Module? = null,
    val cargoToml: CargoToml = CargoToml(nameId)
) : Identifier(nameId) {
}

enum class CrateType {
    Library,
    Binary
}