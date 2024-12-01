package kgen.rust

import kgen.*
import kgen.utility.panicTest
import kgen.utility.unitTest

/** Represents an implementation of provided type.
 *
 * @property type The type being implemented.
 * @property functions List of functions in the implementation.
 * @property genericParamSet Generic parameters of the impl.
 * @property genericArgSet Generic arguments for the type.
 * @property skipTestsSet Functions that don't warrant a test
 *
 */
data class TypeImpl(
    val type: Type,
    val functions: List<Fn>,
    val genericParamSet: GenericParamSet = GenericParamSet(),
    val genericArgSet: GenericArgSet = GenericArgSet(),
    val doc: String? = null,
    val unitTestImplFunctions: Boolean = true,
    val skipTestsSet: Set<Id> = emptySet(),
    val functionUnitTests: List<Id> = emptyList(),
    val uses: Set<Use> = emptySet(),
    val consts: List<Const> = emptyList(),
) : AsRust {

    val allUses get() = uses + functions.map { it.allUses }.flatten()

    private val functionsById = functions.associateBy { it.id }

    constructor(
        type: Type,
        vararg functions: Fn,
        genericParamSet: GenericParamSet = GenericParamSet(),
        genericArgSet: GenericArgSet = GenericArgSet(),
        doc: String? = null,
        unitTestImplFunctions: Boolean = true,
        skipTestsSet: Set<Id> = emptySet(),
        functionUnitTests: List<Id> = emptyList(),
        uses: Set<Use> = emptySet(),
        consts: List<Const> = emptyList(),
    ) : this(
        type,
        functions.toList(),
        genericParamSet,
        genericArgSet,
        doc,
        unitTestImplFunctions,
        skipTestsSet,
        functionUnitTests,
        uses,
        consts
    )


    val hasTestModule get() = unitTestFunctionIds.isNotEmpty() || panicTestFunctionIds.isNotEmpty()

    private val unitTestFunctionIds
        get() = if (unitTestImplFunctions) {
            functions.filter {
                !skipTestsSet.contains(it.id) && it.hasUnitTest != false
            }.map { it.id }
        } else {
            emptyList()
        } + functionUnitTests + functions.map { it.testNameIds.map { nameId -> nameId.asId } }.flatten()

    private val panicTestFunctionIds
        get() = functions.map { it.panicTestNameIds.map { nameId -> nameId.asId } }.flatten()

    val testModule
        get() = if (hasTestModule) {
            val testModuleNameId = "test_${type.sanitized.asSnake}"
            Module(
                testModuleNameId,
                "Test type ${type.asRustName}",
                functions = unitTestFunctionIds.map {
                    unitTest(
                        it,
                        "test ${type.sanitizedSpecial}::${it.snakeCaseName}",
                        functionsById[it]?.testFunctionAttrs ?: attrTestFn.asAttrList
                    )
                } + panicTestFunctionIds.map { panicTest(it) },
                moduleType = ModuleType.Inline,
                visibility = Visibility.None
            )
        } else {
            null
        }

    override val asRust: String
        get() = listOfNotNull(
            doc.commentTriple,
            withWhereClause(
                "impl${genericParamSet.asRust} ${type.asRustName}${genericArgSet.asRust}",
                genericParamSet
            ) + " {",
            functions
                .takeIf { it.isNotEmpty() }
                ?.joinToString("\n\n") {
                    val fn = if (it.nameId == it.blockName) {
                        it.copy(
                            blockName = "${type.sanitizedSpecial}::${it.nameId}"
                        )
                    } else {
                        it
                    }
                    fn.asRust
                }.indented,
            consts.takeIf { it.isNotEmpty() }?.joinToString("\n\n") { it.asRust }.indented,
            "}"
        ).joinToString("\n")
}
