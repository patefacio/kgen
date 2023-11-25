package kgen.rust

import kgen.*

data class Const(
    val nameId: String,
    val doc: String? = missingDoc(nameId, "Constant"),
    val type: Type,
    val value: Any,
    val attrs: AttrList = AttrList(),
    val visibility: Visibility = Visibility.Pub
) : Identifier(nameId), AsRust {

    val asRustName  get() = id.shout

    override val asRust: String
        get() {
            val rustValue = when (value) {
                is String -> doubleQuote(value)
                else -> value.toString()
            }
            return listOf(
                if (doc != null) {
                    commentTriple(doc)
                } else {
                    ""
                },
                attrs.asOuterAttr,
                "${trailingText(visibility.asRust)}const ${id.shout}: ${type.asRust} = $rustValue;"
            ).joinNonEmpty()
        }
}