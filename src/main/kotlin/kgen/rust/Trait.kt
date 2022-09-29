package kgen.rust

import kgen.*

data class Trait(
    val nameId: String,
    val doc: String = missingDoc(nameId, "Trait"),
    val functions: List<Fn> = emptyList(),
    val genericParamSet: GenericParamSet = GenericParamSet(),
    val visibility: Visibility = Visibility.None,
    val associatedTypes: List<AssociatedType> = emptyList(),
    val superTraits: List<Trait> = emptyList(),
    val uses: List<Use> = emptyList()
) : Identifiable(nameId), AsRust {

    val traitScopedFunctions = functions.map {
        it.copy(blockName = "trait fn ${id.capCamel}::${it.blockName}")
    }

    private val traitContent = indent(
        (associatedTypes.map { it.asRust } +
                traitScopedFunctions.map { it.asTraitFn }).joinNonEmpty("\n\n")
    )!!

    override
    val asRust: String
        get() = listOf(
            commentTriple(doc),
            "trait ${id.capCamel}${genericParamSet.asRust} {",
            emptyIfOnlyWhitespace(traitContent),
            "}"
        ).joinNonEmpty()

}

