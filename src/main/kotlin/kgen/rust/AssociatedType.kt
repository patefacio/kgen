package kgen.rust

import kgen.*

data class AssociatedType(
    val nameId: String,
    val doc: String = missingDoc(nameId, "AssociatedType"),
    val bounds: Bounds = Bounds()
) : AsRust {

    val id = id(nameId)

    private val boundsDecl = if (bounds.isEmpty()) {
        ""
    } else {
        ": ${bounds.asRust}"
    }

    override val asRust: String
        get() = listOf(
            commentTriple(doc),
            "type ${id.capCamel}$boundsDecl;"
        ).joinNonEmpty()
}