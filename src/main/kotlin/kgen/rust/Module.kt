package kgen.rust

import kgen.*

data class Module(
    val nameId: String,
    val doc: String = "TODO: Document Module($nameId)",
    val moduleType: ModuleType = ModuleType.File,
    val visibility: Visibility = Visibility.None,
    val enums: List<Enum> = emptyList(),
    val traits: List<Trait> = emptyList(),
    val functions: List<Fn> = emptyList(),
    val structs: List<Struct> = emptyList(),
    val modules: List<Module> = emptyList(),
    val uses: List<Use> = emptyList(),
    val typeAliases: List<TypeAlias> = emptyList(),
    val consts: List<Const> = emptyList(),
    val attrs: AttrList = AttrList(),
    val macroUses: List<String> = emptyList(),
    val testMacroUses: List<String> = emptyList()
) : Identifiable(nameId), AsRust {

    private fun wrapIfInline(content: String) = if (moduleType == ModuleType.Inline) {
        listOf(
            commentTriple(doc),
            "$asModDecl {",
            indent(content),
            "}"
        ).joinToString("\n")

    } else {
        content
    }

    override val asRust: String
        get() = wrapIfInline(
            listOf(
                if (moduleType != ModuleType.Inline) {
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
                    uses.joinToString("\n") { it.asRust }),
                announceSection("mod decls",
                    modules.filter { it.moduleType != ModuleType.Inline }.joinToString("\n") { "${it.asModDecl};" }),
                announceSection("type aliases",
                    typeAliases.joinToString("\n") { it.asRust }),
                announceSection("constants",
                    consts.joinToString("\n") { it.asRust }),
                announceSection("enums",
                    enums.joinToString("\n") { it.asRust }),
                announceSection("traits",
                    traits.joinToString("\n") { it.asRust }),
                announceSection("structs",
                    structs.joinToString("\n") { it.asRust }),
                announceSection("functions",
                    functions.joinToString("\n") { it.asRust }),
                leadingText(
                    modules.filter { it.moduleType == ModuleType.Inline }.joinToString("\n\n") { it.asRust },
                    "\n"
                )
            ).joinNonEmpty("\n\n")
        )

    val asModDecl: String get() = "${trailingText(visibility.asRust)}mod $nameId"
}

fun visitModules(rootModule: Module, function: (module: Module) -> Unit) {
    kgenLogger.warn { "Visiting ${rootModule.nameId}" }
    function(rootModule)
    rootModule.modules.forEach { visitModules(it, function) }
}