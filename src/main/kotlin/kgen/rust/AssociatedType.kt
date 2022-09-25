package kgen.rust

import kgen.*

data class AssociatedType(
    val nameId: String,
    val doc: String = "TODO: DOCUMENT AssociatedType($nameId)",
) : AsRust {

    val id = id(nameId)

    override val asRust: String
        get() = listOf(
            commentTriple(doc),
            "type ${id.capCamel};"
        ).joinNonEmpty()
}