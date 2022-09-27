package kgen.rust

import kgen.commentTriple
import kgen.joinNonEmpty
import kgen.trailingSpace

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
    val attrs: AttrList = AttrList(),
    val macroUses: List<String> = emptyList(),
    val testMacroUses: List<String> = emptyList()
) : Identifiable(nameId), AsRust {

    override val asRust: String
        get() = listOf(
            commentTriple(doc),
            announceSection("macro-use imports",
                macroUses.map { "#[macro_use]\nextern crate $it;" }
            ),
            announceSection("test-macro-use imports",
                testMacroUses.map { "#[cfg(test)]\n#[macro_use]\nextern crate $it;" }
            ),
            announceSection("module uses",
                uses.joinToString("\n") { it.asRust }),
            announceSection(
                "mod decls",
                modules.map {
                    it.asModDecl
                }),
            announceSection("structs",
                structs.map { it.asRust })


        ).joinNonEmpty()

    val asModDecl: String get() = "${trailingSpace(visibility.asRust)}mod $nameId;"
}

