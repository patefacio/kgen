package kgen.rust

import kgen.*

/** The value of a constant, transformed literally */
data class StaticValue(val value: String)

/** The string as a literal value for assignment to a const variable */
val String.asStaticValue get() = StaticValue(this)

/** A [rust compile-time const](https://doc.rust-lang.org/std/keyword.static.html) statement.
 *
 * @property nameId The snake case name of the static. Will be converted to all caps in
 *                  definition.
 * @property doc Comment for the `static`
 * @property type Rust type of the `static`
 * @property value Value assigned, double-quoted if a string, literal if numeric or [ConstValue]
 * @property attrs Rust attributes for the static variable
 * @property visibility Rust visibility
 */
data class Static(
    val nameId: String,
    val doc: String? = missingDoc(nameId, "Static"),
    val type: Type,
    val value: Any,
    val attrs: AttrList = AttrList(),
    val visibility: Visibility = Visibility.Pub
) : Identifier(nameId), AsRust {

    val asRustName get() = id.shout

    /** The [Const] as a rust string */
    override val asRust: String
        get() {
            val rustValue = when (value) {
                is String -> doubleQuote(value)
                is StaticValue -> value.value
                else -> value.toString()
            }
            return listOf(
                if (doc != null) {
                    commentTriple(doc)
                } else {
                    ""
                },
                attrs.asOuterAttr,
                "${trailingText(visibility.asRust)}static ${id.shout}: ${type.asRust} = $rustValue;"
            ).joinNonEmpty()
        }
}