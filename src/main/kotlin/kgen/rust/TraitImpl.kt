package kgen.rust

import kgen.*

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
    val unitTests: List<Id> = emptyList()
) : AsRust {

    val hasUnitTests get() = unitTestTraitFunctions || unitTests.isNotEmpty()
    private val unitTestFunctionIds
        get() = if (unitTestTraitFunctions) {
            trait.functions.map { it.id }
        } else {
            emptyList()
        } + unitTests

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
                trait.functions.filter { it.body == null }.joinToString("\n\n") {
                    if (bodies.contains(it.nameId)) {
                        it.copy(body = FnBody(bodies.getValue(it.nameId))).asRust
                    } else {
                        it.asRust("fn ${trait.asRustName}::${it.nameId} for ${type.asRust}")
                    }
                }),
            "}"
        ).joinToString("\n")

}