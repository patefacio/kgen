package kgen.rust

import kgen.*

/** The value of a constant, transformed literally */
data class ConstValue(val value: String)

/** The string as a literal value for assignment to a const variable */
val String.asConstValue get() = ConstValue(this)

/** A [rust compile-time const](https://doc.rust-lang.org/std/keyword.const.html) statement.
 *
 * @property nameId The snake case name of the constant. Will be converted to all caps in
 *                  definition.
 * @property doc Comment for the `const`
 * @property type Rust type of the `const`
 * @property value Value assigned, double-quoted if a string, literal if numeric or [ConstValue]
 * @property attrs Rust attributes for the const variable
 * @property visibility Rust visibility
 */
data class Const(
    val nameId: String,
    val doc: String? = missingDoc(nameId, "Constant"),
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
                is ConstValue -> value.value
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