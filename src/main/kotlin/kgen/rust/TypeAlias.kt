package kgen.rust

import kgen.Identifier
import kgen.commentTriple
import kgen.leadingText
import kgen.trailingText

/**
 * Represents a Rust type alias, including its name, aliased type, visibility, generics, and documentation.
 *
 * This class generates the Rust representation of a type alias, allowing for optional generics
 * and an accompanying documentation comment.
 *
 * @property nameId The snake case name of the type alias.
 * @property aliased The type being aliased by this type alias.
 * @property visibility The visibility level of the type alias (e.g., public or private). Defaults to `None` (private).
 * @property genericParamSet A set of generic parameters associated with the type alias. Defaults to an empty parameter set.
 * @property doc An optional documentation string for the type alias. Defaults to `null` if no documentation is provided.
 */
data class TypeAlias(
    val nameId: String,
    val aliased: Type,
    val visibility: Visibility = Visibility.None,
    val genericParamSet: GenericParamSet = GenericParamSet(),
    val doc: String? = null
) : Identifier(nameId), AsRust {

    /**
     * Returns the Rust-style name of the type alias, formatted in camel case.
     */
    val asRustName get() = id.capCamel

    /**
     * Generates the documentation comment for the type alias, if one is provided.
     */
    private val docComment
        get() = if (doc != null) {
        "${commentTriple(doc)}\n"
    } else {
        ""
    }

    /**
     * Generates the Rust representation of the type alias.
     *
     * Example output:
     * ```
     * /// Documentation comment (if present)
     * pub type AliasName<GenericParams> = AliasedType;
     * ```
     */
    override val asRust: String
        get() = "$docComment${trailingText(visibility.asRust)}type $asRustName${leadingText(genericParamSet.asRust)} = ${aliased.asRust};"
}
