package kgen.rust

import kgen.Id
import kgen.asSnake
import kgen.commentTriple
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
    val functionUnitTests: List<Id> = emptyList(),
    val uses: Set<Use> = emptySet()
) : AsRust {

    val allUses get() = uses + functions.map { it.uses }.flatten()

    val hasUnitTests get() = unitTestImplFunctions || functionUnitTests.isNotEmpty()
    private val unitTestFunctionIds
        get() = if (unitTestImplFunctions) {
            functions.map { it.id }
        } else {
            emptyList()
        } + functionUnitTests

    val testModule
        get() = if (unitTestFunctionIds.isNotEmpty()) {
            val testModuleNameId = "test_${type.sanitized.asSnake}"
            Module(
                testModuleNameId,
                "Test type ${type.asRustName}",
                functions = unitTestFunctionIds.map {
                    Fn(
                        it.snakeCaseName,
                        blockName = "test ${type.sanitizedSpecial}::${it.snakeCaseName}",
                        emptyBlockContents = """todo!("Test ${it.snakeCaseName}")""",
                        doc = null,
                        attrs = AttrList(attrTestFn),
                    )
                },
                moduleType = ModuleType.Inline,
                uses = setOf(Use("test_log::test")),
                visibility = Visibility.None
            )
        } else {
            null
        }

    override val asRust: String
        get() = listOfNotNull(
            doc.commentTriple,
            withWhereClause(
                "impl${genericParamSet.asRust} ${type.asRustName}",
                genericParamSet
            ) + " {",
            indent(
                functions.map {
                    if (it.nameId == it.blockName) {
                        it.copy(
                            blockName = "${type.asRustName}::${it.nameId}"
                        )
                    } else {
                        it
                    }
                }.joinToString("\n\n") {
                    it.asRust
                }
            ),
            "}"
        ).joinToString("\n")
}
