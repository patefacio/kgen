package kgen.rust

import kgen.Identifier
import kgen.comment


/** A let bidning
 * @property nameId Snake case name of the binding
 * @property rhs The _right hand side_ of the binding
 * @property doc A comment for the binding
 * @property value Any rust attributes
 *
 * Code generation support tends to shy away from the inside of functions,
 * but some support exists for consistent initializers which use let bindings.
 */
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
