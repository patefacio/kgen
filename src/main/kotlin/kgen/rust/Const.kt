package kgen.rust

import kgen.*

data class Const(
    val nameId: String,
    val doc: String,
    val type: Type,
    val value: Any,
    val visibility: Visibility = Visibility.None
) : Identifiable(nameId), AsRust {

    override val asRust: String
        get() {
            val rustValue = when(value) {
                is String -> doubleQuote(value)
                else -> value.toString()
            }
            return listOf(
                commentTriple(doc),
                "${trailingText(visibility.asRust)}const ${id.shout}: ${type.asRust} = $rustValue;"
            ).joinNonEmpty()
        }
}