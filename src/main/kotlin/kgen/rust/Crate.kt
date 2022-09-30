package kgen.rust

data class Crate(
    val nameId: String,
    val doc: String = missingDoc(nameId, "Crate"),
    val rootModule: Module = Module("lib"),
    val cargoToml: CargoToml = CargoToml(nameId)
) : Identifiable(nameId) {
}

enum class CrateType {
    Library,
    Binary
}