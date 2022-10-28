package kgen.rust

import kgen.*

/**
 * Returns a struct field.
 * @property id - Identifies the field within a struct
 * @property doc - Documentation for the field
 * @property type - Type of the field
 * @property access - Resolves to visibility and accessor functions
 */
data class Field(
    val nameId: String,
    val doc: String = missingDoc(nameId, "Field"),
    val type: Type = RustString,
    val access: Access = Access.Pub,
    val attrs: AttrList = AttrList()
) : Identifier(nameId), AsRust {

    val decl get() = "${trailingText(access.asRust)}$nameId: ${type.type}"

    val tupleStructDecl get() = "${trailingText(access.asRust)}${type.type}"

    override val asRust: String
        get() = listOf(
            commentTriple(doc),
            attrs.asRust,
            decl
        ).joinNonEmpty("\n")

    val asTupleStructField get() = listOf(
        commentTriple(doc),
        attrs.asRust,
        tupleStructDecl
    ).joinNonEmpty("\n")
}
