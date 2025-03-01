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
 * @property attrs Properties for the impl, eg `#[cfg(debug_assertions]` for debugOnly
 */
data class TraitImpl(
    val type: Type,
    val trait: Trait,
    val doc: String = missingDoc(trait.nameId, "TraitImpl"),
    val genericParamSet: GenericParamSet = GenericParamSet(),
    val genericArgSet: GenericArgSet = GenericArgSet(),
    val selfBounds: Bounds = Bounds(),
    val associatedTypeAssignments: List<String> = emptyList(),
    val fnBodies: Map<String, String> = emptyMap(),
    val unitTestTraitFunctions: Boolean = false,
    val functionUnitTests: List<Id> = emptyList(),
    val uses: Set<Use> = emptySet(),
    val noFunctionComments: Boolean = false,
    val attrs: AttrList = AttrList(),
    val debugOnly: Boolean = false,
) : AsRust {

    /** True if any/all functions have unit tests. */
    val hasUnitTests get() = unitTestTraitFunctions || functionUnitTests.isNotEmpty()

    val allUses = uses + trait.allUses

    val allAttrs
        get() = if (debugOnly) {
            attrs + attrDebugBuild
        } else {
            attrs
        }

    private val unitTestFunctionIds
        get() = if (unitTestTraitFunctions) {
            trait.functions.filter { it.hasUnitTest ?: true }.map { it.id }
        } else {
            emptyList()
        } + functionUnitTests

    private val requiresArgMatching
        get() = trait.genericParamSet.typeParams.isNotEmpty() &&
                genericArgSet.types.isNotEmpty()

    private val matchedParamToArg = if (requiresArgMatching) {
        genericArgSet.types.withIndex().associate { (i, arg) -> trait.genericParamSet.typeParams[i] to arg }
    } else {
        emptyMap()
    }

    /** When defining traits with generic parameters it is common to use those types in
     * function parameters. Then when implementing the trait those type parameters must
     * be provided type arguments. The function implementations will refer to the actual
     * type and not the original generic type parameter as the trait functions do. So
     * this is a way to replace any function param types referencing the generics in
     * the trait's definition with the corresponding type passed in as the type argument.
     * It does this replacement textually with a word-bounded regex.
     */
    private val patchedFunctions
        get(): List<Fn> {

            val replacements = matchedParamToArg.entries.associate { (key, value) ->
                """(?<!::)\b${key.asRust}\b""".toRegex() to value.asRust
            }

            return trait
                .functions
                .filter { it.body == null }
                .map { function ->
                    function.copy(
                        visibility = Visibility.None,
                        params = function.params.map { fnParam ->
                            val paramType = replacements.entries.fold(fnParam.type) { paramType, replacement ->
                                UnmodeledType(paramType.asRust.replace(replacement.key, replacement.value))
                            }
                            fnParam.copy(type = paramType)
                        },
                        returnType = function.returnType?.asRust?.let { rt ->
                            UnmodeledType(replacements.entries.fold(rt) { acc, replacement ->
                                acc.replace(replacement.key, replacement.value)
                            })
                        }
                    )
                }
        }

    val testModule
        get() = if (unitTestFunctionIds.isNotEmpty()) {
            val testModuleNameId = "test_${trait.nameId}_on_${type.sanitized.lowercase()}".replace("__", "_")
            Module(
                testModuleNameId,
                "Test trait ${trait.nameId} on ${type.asRust}",
                functions = unitTestFunctionIds.map {
                    Fn(
                        it.snakeCaseName,
                        blockName = "test ${trait.asRustName}::${it.snakeCaseName} on ${type.sanitizedSpecial}",
                        emptyBlockContents = """todo!("Test ${it.snakeCaseName}")""",
                        doc = null,
                        attrs = AttrList(attrTestFn),
                        visibility = Visibility.None
                    )
                },
                moduleType = ModuleType.Inline,
            )
        } else {
            null
        }

    override val asRust: String
        get() = listOf(
            withWhereClause(
                "${attrs.asOuterAttr}impl ${trailingText(genericParamSet.asRust)}${trait.asRustName}${genericArgSet.asRust} for ${type.asRust}",
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
                patchedFunctions.joinToString("\n\n") {
                    if (fnBodies.contains(it.nameId)) {
                        it.copy(body = FnBody(fnBodies.getValue(it.nameId))).asRust
                    } else {
                        it.asRust("fn ${trait.asRustName}::${it.nameId} for ${type.asRust}")
                    }
                }),
            "}"
        ).joinToString("\n")

}