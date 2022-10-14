package kgen.rust

import kgen.*

/** Represents an implementation of a trait on specified type.
 *
 * From a code generation perspective, a benefit here is a TraitImpl
 * in a module will provide for all functions with the signature and
 * stubs (which do not provide function bodies).
 *
 * @property type Type the trait is implemented for.
 * @property trait Trait being implemented for the `type`.
 * @property doc Documentation for the trait impl.
 * @property genericParamSet Generic parameters of the impl.
 * @property genericArgSet Generic arguments for the type.
 * @property selfBounds Bounds in the where clause associated with `Self`.
 * @property associatedTypeAssignments The impl's definition for associated types.
 * @property bodies Map of function name to function body - for small functions which
 *           are easily captured in the model (as opposed to the source protect block).
 * @property unitTestTraitFunctions If true all functions have unit tests.
 * @property functionUnitTests List of function names to include empty unit test,
 *           in case not all need to be unit tested.
 * @property uses List of uses for the trait impl
 * @property noFunctionComments If set the comments will not be included - for
 *           simple type trait impls no need to duplicate large doc comments.
 */
data class TraitImpl(
    val type: Type,
    val trait: Trait,
    val doc: String = missingDoc(trait.nameId, "TraitImpl"),
    val genericParamSet: GenericParamSet = GenericParamSet(),
    val genericArgSet: GenericArgSet = GenericArgSet(),
    val selfBounds: Bounds = Bounds(),
    val associatedTypeAssignments: List<String> = emptyList(),
    val bodies: Map<String, String> = emptyMap(),
    val unitTestTraitFunctions: Boolean = false,
    val functionUnitTests: List<Id> = emptyList(),
    val uses: Set<Use> = emptySet(),
    val noFunctionComments: Boolean = false
) : AsRust {

    /** True if any/all functions have unit tests. */
    val hasUnitTests get() = unitTestTraitFunctions || functionUnitTests.isNotEmpty()

    private val unitTestFunctionIds
        get() = if (unitTestTraitFunctions) {
            trait.functions.map { it.id }
        } else {
            emptyList()
        } + functionUnitTests

    val testModule
        get() = if (unitTestFunctionIds.isNotEmpty()) {
            val testModuleNameId = "test_${trait.nameId}_on_${type.sanitized.lowercase()}"
            Module(
                testModuleNameId,
                "Test trait ${trait.nameId} on ${type.asRust}",
                functions = unitTestFunctionIds.map {
                    Fn(
                        it.snakeCaseName,
                        blockName = "${testModuleNameId}_${it.snakeCaseName}",
                        doc = null,
                        attrs = AttrList(attrTestFn)
                    )
                },
                moduleType = ModuleType.Inline
            )
        } else {
            null
        }

    override val asRust: String
        get() = listOf(
            withWhereClause(
                "impl ${trailingText(genericParamSet.asRust)}${trait.asRustName}${genericArgSet.asRust} for ${type.asRust}",
                if (selfBounds.isEmpty()) {
                    genericParamSet
                } else {
                    genericParamSet.copy(
                        typeParams = genericParamSet.typeParams +
                                TypeParam("self", bounds = selfBounds)
                    )
                }
            ) + " {",
            indent(
                trailingText(
                    associatedTypeAssignments.map { "type $it;" }.joinNonEmpty("\n\n")
                )
            ),
            indent(
                trait
                    .functions
                    .filter { it.body == null }
                    .map {
                        if (noFunctionComments) {
                            it.copy(doc = null)
                        } else {
                            it
                        }
                    }
                    .joinToString("\n\n") {
                        if (bodies.contains(it.nameId)) {
                            it.copy(body = FnBody(bodies.getValue(it.nameId))).asRust
                        } else {
                            it.asRust("fn ${trait.asRustName}::${it.nameId} for ${type.asRust}")
                        }
                    }),
            "}"
        ).joinToString("\n")

}