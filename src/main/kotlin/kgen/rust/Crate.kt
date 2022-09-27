package kgen.rust

data class Crate(
    val nameId: String,
    val doc: String = "TODO: Document $nameId",
    val rootModule: Module = Module(nameId),
    val cargoToml: CargoToml = CargoToml(nameId)
) : Identifiable(nameId) {
}