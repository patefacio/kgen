package kgen.rust

import kgen.*

data class Module(
    val nameId: String,
    val doc: String = missingDoc(nameId, "Module"),
    val moduleType: ModuleType = ModuleType.FileModule,
    val visibility: Visibility = Visibility.None,
    val enums: List<Enum> = emptyList(),
    val traits: List<Trait> = emptyList(),
    val functions: List<Fn> = emptyList(),
    val structs: List<Struct> = emptyList(),
    val modules: List<Module> = emptyList(),
    val uses: Set<Use> = emptySet(),
    val typeAliases: List<TypeAlias> = emptyList(),
    val consts: List<Const> = emptyList(),
    val attrs: AttrList = AttrList(),
    val macroUses: List<String> = emptyList(),
    val testMacroUses: List<String> = emptyList(),
    val traitImpls: List<TraitImpl> = emptyList(),
    val typeImpls: List<TypeImpl> = emptyList(),
    val codeBlock: String? = emptyBlock("mod-def $nameId")
) : Identifier(nameId), AsRust {

    val isInline get() = moduleType == ModuleType.Inline

    private fun wrapIfInline(content: String) = if (moduleType == ModuleType.Inline) {
        joinNonEmpty(
            commentTriple(doc),
            attrs.asRust,
            "$asModDecl {",
            indent(content) ?: "",
            "}"
        )

    } else {
        content
    }

    private val requiresUnitTest
        get() = traitImpls.any { it.hasUnitTests } || typeImpls.any { it.hasUnitTests } || functions.any { it.hasUnitTest }

    val testModule
        get() = if (requiresUnitTest) {
            Module(
                "unit_tests",
                "Unit tests for `${nameId}`",
                moduleType = ModuleType.Inline,
                modules = traitImpls.mapNotNull { it.testModule } + typeImpls.mapNotNull { it.testModule },
                functions = functions.filter { it.hasUnitTest }.map {
                    Fn(
                        "test_${it.nameId}",
                        doc = null,
                        attrs = AttrList(attrTestFn),
                        emptyBlockContents = """todo!("Add test ${it.nameId}")"""
                    )
                },
                attrs = AttrList(attrCfgTest),
                visibility = Visibility.Pub
            )
        } else {
            null
        }

    private var allUses = uses +
            traits.map { it.allUses }.flatten() +
            functions.map { it.uses }.flatten() +
            traitImpls.map { it.uses }.flatten() +
            typeImpls.map { it.allUses }.flatten() +
            structs.map { it.uses }.flatten()


    override
    val asRust: String
        get() = wrapIfInline(
            listOf(
                if (!isInline) {
                    innerDoc(doc) ?: ""
                } else {
                    ""
                },
                announceSection("macro-use imports",
                    macroUses.map { "#[macro_use]\nextern crate $it;" }
                ),
                announceSection("test-macro-use imports",
                    testMacroUses.map { "#[cfg(test)]\n#[macro_use]\nextern crate $it;" }
                ),
                announceSection("module uses",
                    allUses.joinToString("\n") { it.asRust }),
                announceSection("mod decls",
                    (modules
                        .filter { it.moduleType != ModuleType.Inline }
                        .map { "${it.asModDecl};" }).joinToString("\n")
                ),
                announceSection("type aliases",
                    typeAliases.joinToString("\n") { it.asRust }),
                announceSection("constants",
                    consts.joinToString("\n") { it.asRust }),
                announceSection("enums",
                    enums.joinToString("\n\n") { it.asRust }),
                announceSection("traits",
                    traits.joinToString("\n\n") { it.asRust }),
                announceSection("structs",
                    structs.joinToString("\n\n") { it.asRust }),
                announceSection("functions",
                    functions.joinToString("\n\n") { it.asRust }),
                leadingText(
                    modules
                        .filter { it.moduleType == ModuleType.Inline }
                        .joinToString("\n\n") { it.asRust },
                    "\n"
                ),
                announceSection("type impls",
                    typeImpls.joinToString("\n\n") { it.asRust }
                ),
                announceSection("trait impls",
                    traitImpls.joinToString("\n\n") { it.asRust }
                ),
                testModule?.asRust ?: "",
                codeBlock ?: ""
            ).joinNonEmpty("\n\n")
        )

    val asModDecl: String get() = "${trailingText(visibility.asRust)}mod $nameId"
}

fun visitModules(rootModule: Module, function: (module: Module) -> Unit) {
    kgenLogger.warn { "Visiting ${rootModule.nameId}" }
    function(rootModule)
    rootModule.modules.forEach { visitModules(it, function) }
}