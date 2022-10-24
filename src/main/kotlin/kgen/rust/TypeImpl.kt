package kgen.rust

import kgen.Id
import kgen.indent

/** Represents an implementation of provided type.
 *
 * @property type The type being implemented.
 * @property functions List of functions in the implementation.
 * @property genericParamSet Generic parameters of the impl.
 * @property genericArgSet Generic arguments for the type.
 *
 */
data class TypeImpl(
    val type: Type,
    val functions: List<Fn>,
    val genericParamSet: GenericParamSet = GenericParamSet(),
    val genericArgSet: GenericArgSet = GenericArgSet(),
    val doc: String? = null,
    val unitTestImplFunctions: Boolean = true,
    val unitTests: List<Id> = emptyList(),
    val uses: Set<Use> = emptySet()
) : AsRust {

    val allUses get() = uses + functions.map { it.uses }.flatten()
    
    override val asRust: String
        get() = listOf(
            withWhereClause(
                "impl${genericParamSet.asRust} ${type.asRustName}",
                genericParamSet
            ) + " {",
            indent(
                functions.joinToString("\n\n") {
                    it.asRust
                }
            ),
            "}"
        ).joinToString("\n")
}
