package kgen.rust

import kgen.*

/** Modeled rust [associated type](https://doc.rust-lang.org/reference/items/associated-items.html).
 *
 * Associated Items are the items declared in traits or defined in implementations.
 * They are called this because they are defined on an associate type — the type in the
 * implementation. They are a subset of the kinds of items you can declare in a module.
 *
 * @property nameId Snake case name of the associated type
 * @property doc Comment associated with the associated type
 * @property bounds Bounds of the associated type
 *
 */
data class AssociatedType(
    val nameId: String,
    val doc: String = missingDoc(nameId, "AssociatedType"),
    val bounds: Bounds = Bounds()
) : AsRust {

    /** Name as [Id] */
    val id = id(nameId)

    /** The bounds as rust decl or empty string if none */
    private val boundsDecl = if (bounds.isEmpty()) {
        ""
    } else {
        ": ${bounds.asRust}"
    }

    /** The associated type as rust code */
    override val asRust: String
        get() = listOf(
            commentTriple(doc),
            "type ${id.capCamel}$boundsDecl;"
        ).joinNonEmpty()
}