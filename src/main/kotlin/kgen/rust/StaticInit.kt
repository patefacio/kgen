package kgen.rust

import kgen.*

/**
 * Represents a Rust static initializer, allowing the definition of static variables with attributes,
 * visibility, and optional initialization values.
 *
 * @property nameId The snake case name for the static variable.
 * @property doc The documentation string associated with the static initializer.
 * @property type The type of the static variable in Rust.
 * @property value The optional value assigned to the static variable. If `null`, a default value block is generated.
 * @property attrs A list of attributes applied to the static variable. Defaults to an empty attribute list.
 * @property visibility The visibility level of the static variable (e.g., public or private). Defaults to `Pub`.
 */
data class StaticInit(
    val nameId: String,
    val doc: String,
    val type: Type,
    val value: String? = null,
    val attrs: AttrList = AttrList(),
    val visibility: Visibility = Visibility.Pub
) : Identifier(nameId), AsRust {

    /**
     * Generates the Rust representation of the static initializer, including visibility, attributes,
     * and an optional initialization value.
     *
     * If `value` is not provided, a placeholder block is used instead.
     * The resulting Rust declaration will look like:
     *
     * ```
     * /// Documentation string
     * #[attribute]
     * pub static NAME: Type = value_or_placeholder;
     * ```
     */
    override val asRust: String
        get() {
            val rustValue = when (value) {
                null -> "{\n${emptyCloseDelimitedBlock("static_init for $nameId")}\n}"
                else -> value
            }
            return listOf(
                commentTriple(doc),
                (attrs.attrs + attrDynamic).asOuterAttr,
                "${trailingText(visibility.asRust)}static ${id.shout}: ${type.asRust} = $rustValue;"
            ).joinNonEmpty()
        }
}
