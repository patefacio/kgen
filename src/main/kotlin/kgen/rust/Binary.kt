package kgen.rust

data class Binary(
    val nameId: String,
    val doc: String = "TODO: Document Binary($nameId)",
    val module: Module
) : Identifiable(nameId) {
}