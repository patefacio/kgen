package kgen.rust

import kgen.*

interface AsSuperTrait {
    val asSuperTrait: String
}

data class Trait(
    val nameId: String,
    val doc: String = missingDoc(nameId, "Trait"),
    val functions: List<Fn> = emptyList(),
    val genericParamSet: GenericParamSet = GenericParamSet(),
    val visibility: Visibility = Visibility.Pub,
    val associatedTypes: List<AssociatedType> = emptyList(),
    val superTraits: List<AsSuperTrait> = emptyList(),
    val uses: Set<Use> = emptySet(),
    val attrs: AttrList = AttrList()
) : Identifier(nameId), AsRust, AsSuperTrait {

    val allUses get() = uses + functions.map { it.allUses }.flatten()

    constructor(
        nameId: String,
        doc: String = missingDoc(nameId, "Trait"),
        vararg functions: Fn,
        genericParamSet: GenericParamSet = GenericParamSet(),
        visibility: Visibility = Visibility.Pub,
        associatedTypes: List<AssociatedType> = emptyList(),
        superTraits: List<AsSuperTrait> = emptyList(),
        uses: Set<Use> = emptySet(),
        attrs: AttrList = AttrList()
    ) : this(
        nameId,
        doc,
        functions.toList(),
        genericParamSet = genericParamSet,
        visibility = visibility,
        associatedTypes = associatedTypes,
        superTraits = superTraits,
        uses = uses,
        attrs
    )

    val asRustName get() = id.capCamel

    override val asSuperTrait: String
        get() = asRustName

    private val traitScopedFunctions get() = functions.map {
        it.copy(blockName = "trait fn ${id.capCamel}::${it.blockName}").copy(visibility = Visibility.None)
    }

    private val traitContent get() = indent(
        (associatedTypes.map { it.asRust } +
                traitScopedFunctions.map { it.asTraitFn }).joinNonEmpty("\n\n")
    )!!

    private val superTraitDecl
        get() = if (superTraits.isNotEmpty()) {
            ": ${superTraits.joinToString(" + ") { it.asSuperTrait }}"
        } else {
            ""
        }

    override
    val asRust: String
        get() = listOf(
            commentTriple(doc),
            withWhereClause(
                "${trailingText(visibility.asRust)}trait ${id.capCamel}${genericParamSet.asRust}$superTraitDecl",
                genericParamSet
            ) + " {",
            emptyIfOnlyWhitespace(traitContent),
            "}"
        ).joinNonEmpty()

}

data class UnmodeledTrait(override val asSuperTrait: String) : AsSuperTrait

fun unmodeledSuperTraits(vararg superTraits: String) = superTraits.map { UnmodeledTrait(it) }

val String.asSuperTrait get() = UnmodeledTrait(this)

val List<String>.asSuperTraits get() = this.map { UnmodeledTrait(it) }