package kgen.rust

data class Binary(
    val nameId: String,
    val doc: String = missingDoc(nameId, "Binary"),
    val module: Module
) : Identifiable(nameId) {
}