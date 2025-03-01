package kgen.rust

import kgen.Identifier
import kgen.joinNonEmpty
import kgen.trailingText

/**
 * Represents a module declaration in Rust, including its visibility and associated attributes.
 *
 * This class provides utilities for generating the Rust representations of both module declarations
 * and definitions based on the provided name, visibility, and attributes.
 *
 * @property nameId The snake case name for the module.
 * @property visibility The visibility level of the module (e.g., public or private). Defaults to `Pub`.
 * @property attrs A list of attributes associated with the module. Defaults to an empty `AttrList`.
 */
data class ModDecl(
    val nameId: String,
    val visibility: Visibility = Visibility.Pub,
    val attrs: AttrList = AttrList(),
) : Identifier(nameId) {

    /**
     * Generates the Rust declaration for the module.
     *
     * Example output:
     * ```
     * #[attribute]
     * pub mod module_name;
     * ```
     */
    val asModuleDecl: String
        get() = listOf(
            attrs.asOuterAttr,
            "${trailingText(visibility.asRust)}mod ${id.snake};"
        ).joinNonEmpty()

    /**
     * Generates the Rust module definition, which excludes the trailing semicolon.
     *
     * Example output:
     * ```
     * #[attribute]
     * pub mod module_name
     * ```
     */
    val asModuleDef: String
        get() = listOf(
            attrs.asOuterAttr,
            "${trailingText(visibility.asRust)}mod ${id.snake}"
        ).joinNonEmpty()
}
