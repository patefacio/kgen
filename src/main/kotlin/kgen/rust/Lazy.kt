package kgen.rust

import kgen.*

/** A lazy initialized value.
 * @property nameId Snake case name of the binding
 * @property doc A comment for the binding
 * @property type The rust type of the binding
 * @property value An optional initialization value
 * @property value Any rust attributes
 * @property visibility The visibility of the binding
 */
data class Lazy(
    val nameId: String,
    val doc: String,
    val type: Type,
    val value: String? = null,
    val attrs: AttrList = AttrList(),
    val visibility: Visibility = Visibility.Pub
) : Identifier(nameId), AsRust {

    val asRustName = id.shout
    override val asRust: String
        get() {
            val rustValue = when(value) {
                null -> "{\n${emptyCloseDelimitedBlock("lazy init for $nameId", 
                    emptyContents = "todo!(\"Write $nameId initializer\")")}\n}"
                else -> value
            }
            return listOf(
                commentTriple(doc),
                attrs.attrs.asOuterAttr,
                "${trailingText(visibility.asRust)}static $asRustName: Lazy<${type.asRust}> = Lazy::new(|| { $rustValue });"
            ).joinNonEmpty()
        }

}
