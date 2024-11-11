package kgen.rust

import kgen.*
import kgen.rust.decl_macro.DeclMacro
import kgen.utility.panicTest
import kgen.utility.unitTest

data class Module(
    val nameId: String,
    val doc: String? = missingDoc(nameId, "Module"),
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
    val declMacros: List<DeclMacro> = emptyList(),
    val lazies: List<Lazy> = emptyList(),
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
    val isBinary: Boolean = false,
    val disabled: Boolean = false
) : Identifier(nameId), AsRust {

    val rustFileName = "${nameId}.rs"
    val isInline get() = moduleType == ModuleType.Inline
    val isPrivate get() = visibility == Visibility.None
    private val sizesIncluded get() = includeTypeSizes && !isBinary

    private val structConstsImpls = structs.mapNotNull { it.constsTypeImpl }

    private val structAccessorImpls = structs.mapNotNull { struct ->
        val accessors = struct.accessors
        if (accessors.isNotEmpty()) {
            val implGeneric = struct.genericParamSet.withoutDefaults
            TypeImpl(
                implGeneric.genericTypeOf(struct.asRustName.asType),
                functions = accessors,
                doc = "Accessors for [${struct.structName}] fields",
                genericParamSet = implGeneric
            )
        } else {
            null
        }
    }

    private val allTraitImpls = traitImpls +
            structs.map { it.allTraitImpls }.flatten() +
            enums.map { it.allTraitImpls }.flatten()

    private val allTypeImpls = typeImpls +
            structs.mapNotNull { it.augmentedTypeImpl } +
            enums.mapNotNull { it.typeImpl }

    private val allStaticInits = staticInits + structs
        .fold(mutableListOf()) { acc, struct ->
            acc.addAll(struct.staticInits)
            acc
        }

    private val allLazies = lazies + structs
        .fold(mutableListOf()) { acc, struct ->
            acc.addAll(struct.lazies)
            acc
        }


    private val typeSizesImpl
        get(): String {

            val typedItems = structs.filter { struct ->
                // If a type is generic and there are no default types for a generic parameter
                // we are unable to get type (it is not fully specified)
                struct.genericParamSet.typeParams.isEmpty() ||
                        struct.genericParamSet.typeParams.all { typeParam ->
                            typeParam.default != null
                        }
            }.map { it.asRustName } + enums.filter { enum ->
                enum.genericParamSet.typeParams.isEmpty() ||
                        enum.genericParamSet.typeParams.all { typeParam ->
                            typeParam.default != null
                        }
            }.map { it.asRustName }


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
            if (doc != null) {
                commentTriple(doc)
            } else {
                ""
            },
            attrs.asOuterAttr,
            "$asModDecl {",
            indent(content) ?: "",
            "}"
        )

    } else {
        content
    }

    private val requiresUnitTest
        get() = allTraitImpls.any { it.hasUnitTests } || allTypeImpls.any { it.hasTestModule } || functions.any {
            it.hasUnitTest ?: false
        }

    private val testModule
        get() = if (requiresUnitTest) {
            Module(
                "unit_tests",
                "Unit tests for `${nameId}`",
                moduleType = ModuleType.Inline,
                modules = allTraitImpls.mapNotNull { it.testModule } + allTypeImpls.mapNotNull { it.testModule },
                functions = functions.filter { it.hasUnitTest ?: it.hasTokioTest ?: false }.map {
                    Fn(
                        "test_${it.nameId}",
                        doc = null,
                        attrs = it.testFunctionAttrs,
                        isAsync = it.hasTokioTest == true,
                        emptyBlockContents = """todo!("Add test ${it.nameId}")""",
                        visibility = Visibility.None
                    )
                } + (functions.map { fn ->
                    fn.testNameIds.map { unitTest(it, fn.testFunctionAttrs) }
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
            functions.map { it.allUses }.flatten() +
            allTraitImpls.map { it.allUses }.flatten() +
            allTypeImpls.map { it.allUses }.flatten() +
            structs.map { it.allUses }.flatten() +
            enums.map { it.uses }.flatten() +
            if (allStaticInits.isNotEmpty()) {
                setOf(Use("static_init::dynamic"))
            } else {
                emptySet()
            } +
            if (allLazies.isNotEmpty()) {
                setOf(Use("once_cell::sync::Lazy"))
            } else {
                emptySet()
            }


    private val pubUses = allUses.filter { it.visibility == Visibility.Pub }

    private val exportedItemNames = structs
        .filter { it.visibility.isExport }
        .map { Pair(it.structName, it.visibility) } +
            enums.filter { it.visibility.isExport }.map { Pair(it.asRustName, it.visibility) } +
            traits.filter { it.visibility.isExport }.map { Pair(it.asRustName, it.visibility) } +
            functions.filter { it.visibility.isExport }.map { Pair(it.rustFunctionName, it.visibility) } +
            typeAliases.filter { it.visibility.isExport }.map { Pair(it.asRustName, it.visibility) } +
            consts.filter { it.visibility.isExport }.map { Pair(it.asRustName, it.visibility) } +
            lazies.filter { it.visibility.isExport }.map { Pair(it.asRustName, it.visibility) } +
            declMacros.filter { it.visibility.isExport }.map { Pair(it.asRustName, it.visibility) } +
            functions.filter { it.visibility.isExport }.map { Pair(it.nameId, it.visibility) }


    private val allExportedItemNames: Set<Pair<String, Visibility>>
        get() = modules.fold(exportedItemNames.toMutableSet()) { acc, module ->
            acc.addAll(module.allExportedItemNames.map { (itemName, visibility) ->
                Pair("${module.nameId}::$itemName", visibility)
            })
            acc
        }

    private val exportUses
        get() = if (moduleRootType == ModuleRootType.LibraryRoot) {
            allExportedItemNames.map { (itemName, visibility) ->
                visibility.makeUse(itemName)
            }.toSet()
        } else {
            emptySet()
        }

    private val usingsSection =
        listOfNotNull(
            announceSection("pub module uses",
                (pubUses + exportUses).joinToString("\n") { it.asRust }),
            announceSection("module uses",
                allUses.filter { it.visibility != Visibility.Pub }.joinToString("\n") { it.asRust }),
        ).joinNonEmpty()

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
                usingsSection,
                announceSection("mod decls",
                    (modules
                        .filter { (it.moduleType != ModuleType.Inline) && !it.disabled }
                        .map { "${it.asModDecl};" }).joinToString("\n")
                ),
                announceSection("declarative macros",
                    declMacros.joinToString("\n") { it.asRust }),
                announceSection("type aliases",
                    typeAliases.joinToString("\n") { it.asRust }),
                announceSection("static inits",
                    allStaticInits.joinToString("\n\n") { it.asRust }),
                announceSection("lazy inits",
                    allLazies.joinToString("\n\n") { it.asRust }),
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
                    (allTypeImpls + structAccessorImpls + structConstsImpls).joinToString("\n\n") { it.asRust }
                ),
                announceSection("trait impls",
                    allTraitImpls.joinToString("\n\n") { it.asRust }
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

