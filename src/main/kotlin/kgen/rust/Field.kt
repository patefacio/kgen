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
    val id: Id,
    val doc: String = "TODO: DOCUMENT Field(${id.snakeCaseName})",
    val type: Type = RustString,
    val access: Access = Access.Pub,
    override val attrs: List<Attr> = emptyList()
) : AsRust, AttrList {
    val idVar get() = id.snake

    constructor(
        id: String,
        doc: String,
        type: Type = RustString,
        access: Access = Access.Pub,
        attrs: List<Attr>
    ) : this(id(id), doc, type, access, attrs)

    val decl get() = "${trailingSpace(access.asRust)}$idVar: ${type.type}"

    override val asRust: String
        get() = listOf(
            commentTriple(doc),
            attrs.asRust,
            decl
        ).joinNonEmpty("\n")
}
