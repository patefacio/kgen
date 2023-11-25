package kgen.rust

import kgen.*

data class Let(
    val nameId: String,
    val rhs: String,
    val doc: String? = null,
    val type: Type? = null,
) : Identifier(nameId), AsRust {

    private val typeName: String
        get() = if (type != null) {
            ": ${type.asRustName} "
        } else {
            ""
        }

    override val asRust: String get() = listOfNotNull(
        doc.comment,
        "let $nameId$typeName = $rhs;"
    ).joinToString("\n")

}
