package kgen.rust

import kgen.*
import kgen.utility.panicTest
import kgen.utility.unitTest

data class Module(
    val nameId: String,
    val doc: String = missingDoc(nameId, "Module"),
    val moduleType: ModuleType = ModuleType.FileModule,
    val moduleRootType: ModuleRootType = ModuleRootType.NonRoot,
    val visibility: Visibility = Visibility.Pub,
    val enums: List<Enum> = emptyList(),
    val traits: List<Trait> = emptyList(),
    val functions: List<Fn> = emptyList(),
    val structs: List<Struct> = emptyList(),
    val modules: List<Module> = emptyList(),
    val uses: Set<Use> = emptySet(),
    val typeAliases: List<TypeAlias> = emptyList(),
    val staticInits: List<StaticInit> = emptyList(),
    val consts: List<Const> = emptyList(),
    val attrs: AttrList = AttrList(),
    val macroUses: List<String> = emptyList(),
    val testMacroUses: List<String> = emptyList(),
    val traitImpls: List<TraitImpl> = emptyList(),
    val typeImpls: List<TypeImpl> = emptyList(),
    val codeBlock: String? = emptyOpenDelimitedBlock("mod-def $nameId"),
    val moduleBody: FnBody? = null,
    val publishPublicTypes: Boolean = true,
    val classicModStructure: Boolean = true,
    val includeTypeSizes: Boolean = false,
    val isBinary: Boolean = false
) : Identifier(nameId), AsRust {

    val isInline get() = moduleType == ModuleType.Inline
    val isPrivate get() = visibility == Visibility.None
    val sizesIncluded get() = includeTypeSizes && !isBinary

    private val structAccessorImpls = structs.mapNotNull { struct ->
        val accessors = struct.accessors
        if (accessors.isNotEmpty()) {
            TypeImpl(
                if (struct.genericParamSet.isEmpty) {
                    struct.asRustName.asType
                } else {
                    "${struct.asRustName}${struct.genericParamSet.asRust}".asType
                },
                functions = accessors,
                doc = "Accessors for [${struct.structName}] fields",
                genericParamSet = struct.genericParamSet
            )
        } else {
            null
        }
    }

    private val typeSizesImpl
        get(): String {

            val typedItems = structs.map { it.asRustName } + enums.map { it.asRustName }

            val extensions = modules.map { submodule ->
                val statementAttr = if (submodule.attrs.attrs.contains(attrCfgTest)) {
                    attrCfgTest.asOuterAttr
                } else {
                    ""
                }
                """$statementAttr                    
result.extend(${submodule.nameId}::get_type_sizes().into_iter().map(|(k,v)| (format!("$nameId::{k}"), v)));
                """.trimIndent()
            }

            val typeBtreeMap = listOf(
                "std::collections::BTreeMap::from([",
                indent(
                    typedItems
                        .joinToString(",\n") { rustName ->
                            val itemName = doubleQuote("$nameId::$rustName")
                            "(String::from($itemName), " +
                                    "(std::mem::size_of::<$rustName>(), std::mem::align_of::<$rustName>())" +
                                    ")"
                        }
                ),
                "])"
            ).joinToString("\n")

            return if (extensions.isNotEmpty() || typedItems.isNotEmpty()) {
                if (extensions.isNotEmpty()) {
                    listOf(
                        "let mut result = $typeBtreeMap;",
                        extensions.joinToString("\n\n"),
                        "result"
                    ).joinToString("\n")
                } else {
                    typeBtreeMap
                }
            } else {
                "std::collections::BTreeMap::new()"
            }
        }

    private val allFunctions
        get() = if (!sizesIncluded) {
            functions
        } else {
            functions + Fn(
                "get_type_sizes",
                "Returns BTreeMap of type name to size.",
                returnDoc = "Map of type name to its size.",
                returnType = "std::collections::BTreeMap<String, (usize, usize)>".asType,
                body = FnBody(typeSizesImpl),
                attrs = attrDebugBuild.asAttrList,
                visibility = Visibility.Pub
            )
        }

    private fun wrapIfInline(content: String) = if (moduleType == ModuleType.Inline) {
        joinNonEmpty(
            commentTriple(doc),
            attrs.asOuterAttr,
            "$asModDecl {",
            indent(content) ?: "",
            "}"
        )

    } else {
        content
    }

    private val requiresUnitTest
        get() = traitImpls.any { it.hasUnitTests } || typeImpls.any { it.hasTestModule } || functions.any {
            it.hasUnitTest ?: false
        }

    private val testModule
        get() = if (requiresUnitTest) {
            Module(
                "unit_tests",
                "Unit tests for `${nameId}`",
                moduleType = ModuleType.Inline,
                modules = traitImpls.mapNotNull { it.testModule } + typeImpls.mapNotNull { it.testModule },
                functions = functions.filter { it.hasUnitTest ?: false }.map {
                    Fn(
                        "test_${it.nameId}",
                        doc = null,
                        attrs = AttrList(attrTestFn),
                        emptyBlockContents = """todo!("Add test ${it.nameId}")""",
                        visibility = Visibility.None
                    )
                } + (functions.map { fn ->
                    fn.testNameIds.map { unitTest(it) }
                } + functions.map { fn ->
                    fn.panicTestNameIds.map { panicTest(it) }
                }).flatten(),
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
            structs.map { it.uses }.flatten() +
            enums.map { it.uses }.flatten() +
            if(staticInits.isNotEmpty()) {
                setOf(Use("static_init::dynamic"))
            } else {
                emptySet()
            }


    override
    val asRust: String
        get() = wrapIfInline(
            listOf(
                if (!isInline) {
                    listOf(
                        innerDoc(doc) ?: "",
                        attrs.asInnerAttr,
                    ).joinNonEmpty()
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
                announceSection("static inits",
                    staticInits.joinToString("\n\n") { it.asRust }),
                announceSection("constants",
                    consts.joinToString("\n") { it.asRust }),
                announceSection("enums",
                    enums.joinToString("\n\n") { it.asRust }),
                announceSection("traits",
                    traits.joinToString("\n\n") { it.asRust }),
                announceSection("structs",
                    structs.joinToString("\n\n") { it.asRust }),
                announceSection("functions",
                    allFunctions.joinToString("\n\n") { it.asRust }),
                leadingText(
                    modules
                        .filter { it.moduleType == ModuleType.Inline }
                        .joinToString("\n\n") { it.asRust },
                    "\n"
                ),
                announceSection("type impls",
                    (typeImpls + structAccessorImpls).joinToString("\n\n") { it.asRust }
                ),
                announceSection("trait impls",
                    traitImpls.joinToString("\n\n") { it.asRust }
                ),
                testModule?.asRust ?: "",
                codeBlock ?: "",
                moduleBody?.asRust ?: "",
            ).joinNonEmpty("\n\n")
        )

    private val asModDecl: String
        get() = listOfNotNull(
            if (moduleType != ModuleType.Inline && attrs.attrs.isNotEmpty()) {
                attrs.asOuterAttr
            } else {
                null
            },
            "${trailingText(visibility.asRust)}mod $nameId"
        ).joinToString("\n")
}

enum class ModuleRootType {
    LibraryRoot,
    BinaryRoot,
    NonRoot
}

enum class ModuleInclusionType {
    All,
    StopAtPrivateInclude,
    StopAtPrivateExclude
}

fun allModules(
    module: Module,
    path: List<String>,
    moduleInclusionType: ModuleInclusionType = ModuleInclusionType.All
): Set<Pair<List<String>, Module>> =
    (module.modules.fold(
        mutableSetOf<Pair<List<String>, Module>>()
    ) { acc, submodule ->
        val includeSubmodules = when (moduleInclusionType) {
            ModuleInclusionType.All -> true
            ModuleInclusionType.StopAtPrivateInclude -> !module.isPrivate
            ModuleInclusionType.StopAtPrivateExclude -> !submodule.isPrivate
        }

        if (includeSubmodules) {
            acc.addAll(allModules(submodule, path + listOf(module.nameId), moduleInclusionType))
        }

        acc
    }) + if (when (moduleInclusionType) {
            ModuleInclusionType.All -> true
            ModuleInclusionType.StopAtPrivateInclude -> true
            ModuleInclusionType.StopAtPrivateExclude -> !module.isPrivate
        }
    ) {
        setOf(Pair(path + module.nameId, module))
    } else {
        emptySet()
    }

