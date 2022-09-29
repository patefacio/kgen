package kgen.rust

data class Crate(
    val nameId: String,
    val doc: String = missingDoc(nameId, "Crate"),
    val rootModule: Module = Module(nameId),
    val cargoToml: CargoToml = CargoToml(nameId)
) : Identifiable(nameId) {
}